package dev.kwlew.kernel;

import dev.kwlew.BetterRTP;
import dev.kwlew.managers.ConfigManager;
import dev.kwlew.managers.MessageManager;
import dev.kwlew.managers.TeleportManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;

public class Bootstrap {

    private final Registry registry = new Registry();
    private final BetterRTP plugin;

    private boolean tornDown;

    public Bootstrap(BetterRTP plugin) {
        this.plugin = plugin;

        registry.register(BetterRTP.class, plugin);
        registry.register(JavaPlugin.class, plugin);
        registry.register(Registry.class, registry);
    }

    public void init() {
        initCore();
        initGameplay();
        initCommands();
        initListeners();
        initAPI();

        registry.seal();

        runLifecycle();
    }

    private void initCore() {
        registry.resolve(ConfigManager.class);
        registry.resolve(MessageManager.class);
    }

    private void initGameplay() {
        registry.resolve(TeleportManager.class);
    }

    private void initCommands() {

    }

    private void initListeners() {

    }

    private void initAPI() {

    }

    private void runLifecycle() {
        List<LifecycleComponent> components = lifecycleComponents(registry.getAll());

        try {
            for (LifecycleComponent component : components) {
                component.init();
            }

            for (LifecycleComponent component : components) {
                component.start();
            }
        } catch (RuntimeException e) {
            plugin.getLogger().severe("Startup failed; rolling back components that were brought up.");
            shutdown();
            throw e;
        }
    }

    public void shutdown() {
        if (tornDown) {
            return;
        }

        tornDown = true;

        for (LifecycleComponent component : lifecycleComponents(registry.getAllReversed())) {
            try {
                component.shutdown();
            } catch (RuntimeException e) {
                plugin.getLogger().log(Level.SEVERE,
                        "Error shutting down " + component.getClass().getSimpleName(), e);
            }
        }
    }

    private List<LifecycleComponent> lifecycleComponents(List<Object> instances) {
        return instances.stream()
                .filter(LifecycleComponent.class::isInstance)
                .map(LifecycleComponent.class::cast)
                .toList();
    }

    public Registry registry() {
        return registry;
    }
}
