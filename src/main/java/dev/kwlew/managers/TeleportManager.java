package dev.kwlew.managers;

import dev.kwlew.kernel.LifecycleComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class TeleportManager implements LifecycleComponent {

    private final JavaPlugin plugin;

    public TeleportManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void teleport(Player player, Location location) {
        UUID id = player.getUniqueId();

        player.teleport(location);
    }

}
