package me.antiegghnbt;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiEggNBT extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiEggNBT enabled (ABSOLUTE EGG OVERRIDE)");
    }

    @EventHandler
    public void onEggUse(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
            event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        ItemStack item = event.getItem();
        if (item == null)
            return;

        Material mat = item.getType();
        if (!mat.name().endsWith("_SPAWN_EGG"))
            return;

        event.setCancelled(true); // ❌ УБИВАЕМ ВАНИЛЬНЫЙ СПАВН

        EntityType type = getEntityTypeFromEgg(mat);
        if (type == null)
            return;

        Location loc = event.getPlayer().getLocation()
                .add(event.getPlayer().getLocation().getDirection());

        // === SLIME / MAGMA ===
        if (type == EntityType.SLIME || type == EntityType.MAGMA_CUBE) {
            Slime slime = (Slime) loc.getWorld().spawnEntity(loc, type);
            slime.setSize(1);
            log("Forced vanilla slime");
            return;
        }

        // === GIANT ===
        if (type == EntityType.GIANT) {
            loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
            log("Blocked giant, spawned zombie");
            return;
        }

        // === ВСЕ ОСТАЛЬНЫЕ ===
        if (!type.isAlive()) {
            log("Blocked non-living egg attempt: " + type);
            return;
        }

        loc.getWorld().spawnEntity(loc, type);
        log("Spawned clean vanilla: " + type);
    }

    private EntityType getEntityTypeFromEgg(Material egg) {
        try {
            return EntityType.valueOf(
                    egg.name().replace("_SPAWN_EGG", "")
            );
        } catch (Exception e) {
            return null;
        }
    }

    private void log(String msg) {
        getLogger().warning(msg);
    }
}
