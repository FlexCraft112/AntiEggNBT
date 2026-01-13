package me.antiegghnbt;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiEggNBT extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiEggNBT enabled (FORCE VANILLA EGGS)");
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {

        // ✅ ТОЛЬКО яйца
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)
            return;

        Entity original = event.getEntity();
        Location loc = original.getLocation();
        EntityType type = original.getType();

        // ❌ отменяем ВСЕГДА
        event.setCancelled(true);

        // ❌ защита от мусора
        if (!type.isAlive()) {
            log("Blocked non-living entity: " + type);
            return;
        }

        // === SLIME / MAGMA ===
        if (type == EntityType.SLIME || type == EntityType.MAGMA_CUBE) {
            Slime clean = (Slime) loc.getWorld().spawnEntity(loc, type);
            clean.setSize(1);
            log("Reset slime size");
            return;
        }

        // === GIANT ===
        if (type == EntityType.GIANT) {
            loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
            log("Giant replaced with Zombie");
            return;
        }

        // === ВСЕ ОСТАЛЬНЫЕ МОБЫ ===
        loc.getWorld().spawnEntity(loc, type);
        log("Spawned clean vanilla: " + type);
    }

    private void log(String msg) {
        getLogger().warning(msg);
    }
}
