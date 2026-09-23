package dev.kwlew;

import dev.kwlew.kernel.Bootstrap;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class BetterRTP extends JavaPlugin {

    private Bootstrap bootstrap;

    @Override
    public void onEnable() {
        long start = System.nanoTime();

        bootstrap = new Bootstrap(this);
        try {
            bootstrap.init();
        } catch (RuntimeException e) {
            // Bootstrap has already rolled back whatever it brought up; just stop here instead of
            // leaving a half-enabled plugin registered with the server.
            StartupMessage.printFailure(this, elapsedMillis(start));
            getLogger().log(Level.SEVERE, "Startup error", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        StartupMessage.print(this, elapsedMillis(start));
    }

    @Override
    public void onDisable() {
        if (bootstrap != null) {
            bootstrap.shutdown();
        }
    }

    public Bootstrap bootstrap() {
        return bootstrap;
    }

    private static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
