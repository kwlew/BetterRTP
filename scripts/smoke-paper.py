#!/usr/bin/env python3
"""Boot a real Paper server with the built BetterRTP jar and check that the plugin enables.

Usage: smoke-paper.py <paper-version> <java-major>

The server runs on $PAPER_JAVA_HOME (falls back to $JAVA_HOME). The script exits non-zero if
the plugin fails to enable, the server errors while loading it, or startup times out.
"""
import glob
import json
import os
import shutil
import subprocess
import sys
import tempfile
import threading
import time
import urllib.request

PROJECT = "paper"
USER_AGENT = "BetterRTP-CI (https://github.com/kwlew/BetterRTP)"
PLUGIN = "BetterRTP"
TIMEOUT_SECONDS = 300

# Printed by StartupMessage once Bootstrap has finished.
SUCCESS_MARKER = "Enabled in "
FAILURE_MARKERS = (
    f"Error occurred while enabling {PLUGIN}",
    f"Could not load plugin",
    f"{PLUGIN} failed to start",
    "Unsupported API version",
    "UnsupportedClassVersionError",
)


def fetch_json(url):
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request) as response:
        return json.load(response)


def download_paper(version, target):
    build = fetch_json(f"https://fill.papermc.io/v3/projects/{PROJECT}/versions/{version}/builds/latest")
    download = build["downloads"]["server:default"]
    print(f"Downloading {download['name']} ({build['channel']})", flush=True)
    request = urllib.request.Request(download["url"], headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request) as response, open(target, "wb") as out:
        shutil.copyfileobj(response, out)


def find_plugin_jar():
    # The plain jar task is disabled, so the shadow jar is the only non-sources/javadoc jar.
    jars = [jar for jar in glob.glob("build/libs/*.jar")
            if not jar.endswith(("-sources.jar", "-javadoc.jar"))]
    if len(jars) != 1:
        sys.exit(f"Expected exactly one plugin jar in build/libs, found: {jars}")
    return os.path.abspath(jars[0])


def main():
    if len(sys.argv) != 3:
        sys.exit(__doc__)

    version, java = sys.argv[1], sys.argv[2]
    java_home = os.environ.get("PAPER_JAVA_HOME") or os.environ.get("JAVA_HOME")
    java_bin = os.path.join(java_home, "bin", "java") if java_home else "java"
    plugin_jar = find_plugin_jar()

    server_dir = tempfile.mkdtemp(prefix=f"paper-{version}-")
    os.makedirs(os.path.join(server_dir, "plugins"))
    shutil.copy(plugin_jar, os.path.join(server_dir, "plugins"))
    with open(os.path.join(server_dir, "eula.txt"), "w") as f:
        f.write("eula=true\n")
    with open(os.path.join(server_dir, "server.properties"), "w") as f:
        f.write("online-mode=false\nserver-port=0\n"
                "generate-structures=false\nspawn-protection=0\n")

    server_jar = os.path.join(server_dir, "paper.jar")
    download_paper(version, server_jar)

    print(f"Starting Paper {version} on Java {java} ({java_bin})", flush=True)
    process = subprocess.Popen(
        [java_bin, "-Xmx2G", "-Dcom.mojang.eula.agree=true", "-jar", server_jar, "--nogui"],
        cwd=server_dir,
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
    )

    # Kill the server if it hangs, so the loop below always sees EOF.
    timer = threading.Timer(TIMEOUT_SECONDS, process.kill)
    timer.start()

    enabled = False
    failure = None
    stopping = False
    try:
        for line in process.stdout:
            print(line, end="", flush=True)

            if failure is None and any(marker in line for marker in FAILURE_MARKERS):
                failure = line.strip()

            if SUCCESS_MARKER in line and PLUGIN in line:
                enabled = True

            # "Done (" means the server finished starting; every plugin has had its turn.
            if "Done (" in line and not stopping:
                stopping = True
                process.stdin.write("stop\n")
                process.stdin.flush()
    finally:
        timer.cancel()
        process.wait()
        shutil.rmtree(server_dir, ignore_errors=True)

    if not stopping:
        sys.exit(f"Paper {version} did not finish starting within {TIMEOUT_SECONDS}s (exit {process.returncode})")
    if failure:
        sys.exit(f"{PLUGIN} failed on Paper {version}: {failure}")
    if not enabled:
        sys.exit(f"{PLUGIN} never printed its startup message on Paper {version}")

    print(f"{PLUGIN} enabled successfully on Paper {version} (Java {java})")


if __name__ == "__main__":
    main()
