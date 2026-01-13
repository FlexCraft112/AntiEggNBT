package me.antiegghnbt;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiEggNBT extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiEggNBT enabled (stable Bukkit version)");
    }

    /* =========================================================
       1️⃣ ЯЙЦА — ТОЛЬКО ВАНИЛЬ
       ========================================================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEggSpawn(CreatureSpawnEvent event) {

        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)
            return;

        Entity entity = event.getEntity();
        Location loc = entity.getLocation();
        EntityType type = entity.getType();

        event.setCancelled(true); // ❗ ВСЕГДА

        // SLIME / MAGMA
        if (entity instanceof Slime) {
            Slime slime = (Slime) loc.getWorld().spawnEntity(loc, type);
            slime.setSize(1);
            return;
        }

        // GIANT → ZOMBIE
        if (type == EntityType.GIANT) {
            loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
            return;
        }

        // НЕ живые — не спавним
        if (!type.isAlive())
            return;

        // ЧИСТЫЙ ванильный моб
        loc.getWorld().spawnEntity(loc, type);
    }

    /* =========================================================
       2️⃣ СПАВНЕРЫ — ТОЛЬКО ЧИСТЫЙ ТИП
       ========================================================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerUse(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.SPAWNER)
            return;

        ItemStack item = event.getItem();
        if (item == null || !item.getType().name().endsWith("_SPAWN_EGG"))
            return;

        EntityType type = eggToEntity(item.getType());
        if (type == null || !type.isAlive())
            return;

        CreatureSpawner spawner = (CreatureSpawner) block.getState();
        spawner.setSpawnedType(type);
        spawner.update(true);

        event.setCancelled(true);
    }

    /* =========================================================
       3️⃣ УДАЛЯЕМ НЕ-МОБОВ ПОСЛЕ СПАВНА
       ========================================================= */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onCreatureSpawnMonitor(CreatureSpawnEvent event) {

        EntityType type = event.getEntityType();

        if (type == EntityType.ARMOR_STAND
                || type.name().contains("MINECART")
                || type == EntityType.FALLING_BLOCK) {

            Bukkit.getScheduler().runTask(this, () -> {
                event.getEntity().remove();
            });
        }
    }

    /* ========================================================= */

    private EntityType eggToEntity(Material egg) {
        try {
            return EntityType.valueOf(
                    egg.name().replace("_SPAWN_EGG", "")
            );
        } catch (Exception e) {
            return null;
        }
    }
}
