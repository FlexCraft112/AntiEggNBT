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
        getLogger().info("AntiEggNBT enabled (vanilla egg enforcement)");
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {

        // ✅ СПАВНЕРЫ НЕ ТРОГАЕМ
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER)
            return;

        // работаем ТОЛЬКО с яйцами
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)
            return;

        Entity entity = event.getEntity();
        Location loc = entity.getLocation();

        /* ===== SLIME / MAGMA SLIME ===== */
        if (entity instanceof Slime slime) {
            if (slime.getSize() != 1) {
                event.setCancelled(true);
                Slime clean = (Slime) loc.getWorld().spawnEntity(loc, entity.getType());
                clean.setSize(1);
                log(entity, "Slime size reset");
            }
            return;
        }

        /* ===== ЗАПРЕТ НЕ-ЖИВЫХ СУЩНОСТЕЙ ===== */
        if (!entity.getType().isAlive()) {
            event.setCancelled(true);
            log(entity, "Illegal non-living entity blocked");
            return;
        }

        /* ===== GIANT / EXPLOIT ===== */
        if (entity.getType() == EntityType.GIANT) {
            event.setCancelled(true);
            loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
            log(entity, "Giant replaced with Zombie");
            return;
        }

        /* ===== ЖЁСТКИЙ VANILLA RESET ===== */
        // удаляем ВСЁ кастомное, пересоздаём обычного моба
        event.setCancelled(true);
        loc.getWorld().spawnEntity(loc, entity.getType());
        log(entity, "NBT reset to vanilla");
    }

    private void log(Entity e, String reason) {
        getLogger().warning(
                "[AntiEggNBT] " + reason + " | Entity: " + e.getType()
        );
    }
}
