package me.antiegghnbt;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Slime;
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
        getLogger().info("AntiEggNBT enabled (stable vanilla mode)");
    }

    /* =========================================================
       1️⃣ ЯЙЦА — ВСЕГДА ВАНИЛЬ
       ========================================================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEggSpawn(CreatureSpawnEvent event) {

        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)
            return;

        Entity entity = event.getEntity();
        Location loc = entity.getLocation();
        EntityType type = entity.getType();

        // ❌ запрещённые типы через яйца
        if (!type.isAlive() || type == EntityType.GIANT) {
            event.setCancelled(true);
            loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
            return;
        }

        // SLIME / MAGMA → всегда size 1
        if (entity instanceof Slime slime) {
            if (slime.getSize() != 1) {
                event.setCancelled(true);
                Slime clean = (Slime) loc.getWorld().spawnEntity(loc, type);
                clean.setSize(1);
            }
        }
        // остальные мобы — ваниль, NBT Bukkit сам не применяет
    }

    /* =========================================================
       2️⃣ СПАВНЕРЫ — ЧИСТЫЙ ТИП, БЕЗ NBT
       ========================================================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerEggUse(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.SPAWNER)
            return;

        ItemStack egg = event.getItem();
        if (egg == null || !egg.getType().name().endsWith("_SPAWN_EGG"))
            return;

        EntityType type = eggToEntity(egg.getType());
        if (type == null || !type.isAlive())
            return;

        CreatureSpawner spawner = (CreatureSpawner) block.getState();
        spawner.setSpawnedType(type);
        spawner.update(true);

        event.setCancelled(true);
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
