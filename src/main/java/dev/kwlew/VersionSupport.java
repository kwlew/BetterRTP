package dev.kwlew;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VersionSupport {

    private static final Pattern VERSION = Pattern.compile("^(\\d+)\\.(\\d+)(?:\\.(\\d+))?.*");

    private VersionSupport() {}

    public static boolean supports(String bukkitVersion) {
        return isAtLeast(bukkitVersion, 1, 18, 2);
    }

    public static boolean supportsBrigadier(String bukkitVersion) {
        return isAtLeast(bukkitVersion, 1, 20, 6);
    }

    private static boolean isAtLeast(String bukkitVersion, int requiredMajor,
                                     int requiredMinor, int requiredPatch) {
        Matcher match = VERSION.matcher(bukkitVersion);
        if (!match.matches()) {
            return false;
        }
        int major = Integer.parseInt(match.group(1));
        int minor = Integer.parseInt(match.group(2));
        int patch = match.group(3) == null ? 0 : Integer.parseInt(match.group(3));

        return major > requiredMajor
                || (major == requiredMajor && (minor > requiredMinor
                || (minor == requiredMinor && patch >= requiredPatch)));
    }
}
