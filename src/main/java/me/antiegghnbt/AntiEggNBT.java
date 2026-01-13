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
        getLogger().info("AntiEggNBT enabled (hard vanilla reset)");
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {

        // Работаем ТОЛЬКО с яйцами
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)
            return;

        Entity entity = event.getEntity();
        Location loc = entity.getLocation();

        // === SLIME / MAGMA ===
        if (entity instanceof Slime slime) {
            if (slime.getSize() > 1) {
                event.setCancelled(true);

                Slime clean = (Slime) loc.getWorld().spawnEntity(loc, entity.getType());
                clean.setSize(1);

                log(entity, "Slime size reset");
            }
            return;
        }

        // === GIANT ZOMBIE ===
        if (entity.getType() == EntityType.GIANT) {
            event.setCancelled(true);
            loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
            log(entity, "Giant replaced with Zombie");
            return;
        }

        // === SAFETY: если тип не совпадает с яйцом ===
        // (на случай подмены EntityTag.id)
        EntityType safeType = entity.getType();
        if (!safeType.isAlive()) {
            event.setCancelled(true);
            log(entity, "Illegal entity blocked");
        }
    }

    private void log(Entity e, String reason) {
        getLogger().warning(
                reason + " | Player egg spawn blocked: " + e.getType()
        );
    }
}
