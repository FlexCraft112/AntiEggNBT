package me.antiegghnbt;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiEggNBT extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiEggNBT enabled (ABSOLUTE MODE)");
    }

    @EventHandler
    public void onAnySpawn(EntitySpawnEvent event) {

        if (event.getEntity().getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)
            return;

        Entity entity = event.getEntity();
        Location loc = entity.getLocation();

        // ===== SLIME / MAGMA =====
        if (entity instanceof Slime slime) {
            if (slime.getSize() != 1) {
                event.setCancelled(true);
                Slime clean = (Slime) loc.getWorld().spawnEntity(loc, entity.getType());
                clean.setSize(1);
                log(entity, "Slime size hard reset");
            }
            return;
        }

        // ===== GIANT =====
        if (entity.getType() == EntityType.GIANT) {
            event.setCancelled(true);
            loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
            log(entity, "Giant replaced");
            return;
        }

        // ===== NON-CREATURE ENTITIES (minecart, armorstand, etc) =====
        if (!(entity instanceof LivingEntity)) {
            event.setCancelled(true);
            log(entity, "Illegal non-living entity from egg blocked");
        }
    }

    private void log(Entity e, String reason) {
        getLogger().warning(reason + " | " + e.getType());
    }
}
