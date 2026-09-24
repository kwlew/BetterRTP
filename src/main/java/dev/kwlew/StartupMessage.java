package dev.kwlew;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

import static dev.kwlew.COLORS.*;

/**
 * Prints the coloured console banner shown once the plugin has finished enabling.
 */
final class StartupMessage {

    private static final String MODRINTH = "https://modrinth.com/project/kbetterrtp";
    private static final String GITHUB = "https://github.com/kwlew/BetterRTP";

    private static final String LINE = ANSI_PURPLE + "-".repeat(46) + ANSI_RESET;

    private StartupMessage() {}

    static void print(JavaPlugin plugin, long startupMillis) {
        Logger logger = plugin.getLogger();
        String version = plugin.getDescription().getVersion();

        logger.info(LINE);
        logger.info(ANSI_CYAN + "  BetterRTP " + ANSI_WHITE + "v" + version + ANSI_RESET);
        logger.info(ANSI_WHITE + "  by " + ANSI_YELLOW + "kwlew" + ANSI_RESET);
        logger.info("");
        logger.info(ANSI_GREEN + "  Enabled in " + ANSI_YELLOW + startupMillis + "ms" + ANSI_RESET);
        logger.info("");
        logger.info(ANSI_WHITE + "  Modrinth: " + ANSI_BLUE + MODRINTH + ANSI_RESET);
        logger.info(ANSI_WHITE + "  GitHub:   " + ANSI_BLUE + GITHUB + ANSI_RESET);
        logger.info(LINE);
    }

    static void printFailure(JavaPlugin plugin, long elapsedMillis) {
        plugin.getLogger().severe(ANSI_RED + "BetterRTP failed to start after " + elapsedMillis
                + "ms; the plugin has been disabled. Report issues at " + GITHUB + "/issues" + ANSI_RESET);
    }
}
