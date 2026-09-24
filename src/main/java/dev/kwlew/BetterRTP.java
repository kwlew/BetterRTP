package dev.kwlew;

import dev.kwlew.kernel.Bootstrap;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class BetterRTP extends JavaPlugin {

    private Bootstrap bootstrap;

    @Override
    public void onEnable() {
        long start = System.nanoTime();

        if (!VersionSupport.supports(getServer().getBukkitVersion())) {
            getLogger().severe("Haven requires Paper 1.18.2 or newer; found "
                    + getServer().getBukkitVersion());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();

        try {
            bootstrap = new Bootstrap(this);
            bootstrap.init();
        } catch (Throwable t) {
            getLogger().log(Level.SEVERE, "Haven failed to start and will be disabled.", t);

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
