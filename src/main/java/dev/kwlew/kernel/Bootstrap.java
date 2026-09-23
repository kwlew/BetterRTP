package dev.kwlew.kernel;

import dev.kwlew.BetterRTP;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;

/**
 * Wires every component together and drives the plugin's lifecycle.
 * <p>
 * Registration is grouped by feature area purely for readability - {@link Registry#resolve} is
 * depth-first, so a component's dependencies are always constructed before it regardless of which
 * group pulled it in. Group order only decides the relative position of components with no
 * dependency edge between them.
 */
public class Bootstrap {

    private final Registry registry = new Registry();
    private final BetterRTP plugin;

    private boolean tornDown;

    public Bootstrap(BetterRTP plugin) {
        this.plugin = plugin;

        // Registered under both keys so components can ask for either; the Registry would
        // otherwise try to construct a second plugin instance for BetterRTP.
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

    /**
     * Config, storage and other services everything else depends on. Must come first when it
     * binds interfaces, since the Registry can't construct an interface on its own.
     */
    private void initCore() {
    }

    private void initGameplay() {
    }

    private void initCommands() {
    }

    private void initListeners() {
    }

    /**
     * Optional third-party hooks. Last on purpose: they report on core services, so those must
     * already exist. Being last also means they shut down first, before the data they read.
     */
    private void initAPI() {
    }

    /**
     * Runs {@code init()} across every component, then {@code start()}. A failure in either phase
     * tears down whatever was already brought up rather than leaving the plugin half-enabled.
     */
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

    /**
     * Shuts down every component the {@link Registry} managed to construct, in reverse creation
     * order, so a component is always torn down before the dependencies it was built from.
     * <p>
     * Deliberately keyed off what was <em>constructed</em> rather than what was successfully
     * initialised. A constructor can already own a resource (a thread, a file handle), so if a
     * later component fails to construct, that resource still has to be released or it can keep
     * the JVM alive and the server never finishes stopping. This is why every {@code shutdown()}
     * must tolerate never having been {@code init()}ed.
     * <p>
     * Safe to call twice: the rollback path and {@code onDisable} can both reach it.
     */
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
