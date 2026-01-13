package me.antiegghnbt;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiEggNBT extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiEggNBT enabled (full protection)");
    }

    /* =========================================================
       1️⃣ ЯЙЦА — ТОЛЬКО ДЕФОЛТНЫЕ МОБЫ
       ========================================================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEggSpawn(CreatureSpawnEvent event) {

        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)
            return;

        Entity entity = event.getEntity();
        Location loc = entity.getLocation();

        // SLIME / MAGMA
        if (entity instanceof Slime slime) {
            event.setCancelled(true);
            Slime clean = (Slime) loc.getWorld().spawnEntity(loc, entity.getType());
            clean.setSize(1);
            return;
        }

        // GIANT → ZOMBIE
        if (entity.getType() == EntityType.GIANT) {
            event.setCancelled(true);
            loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
            return;
        }

        // ВСЁ ОСТАЛЬНОЕ — пересоздаём
        EntityType type = entity.getType();
        event.setCancelled(true);
        loc.getWorld().spawnEntity(loc, type);
    }

    /* =========================================================
       2️⃣ СПАВНЕРЫ — ЧИСТИМ ПРИ ВСТАВКЕ ЯЙЦА
       ========================================================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerEggUse(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (event.getClickedBlock() == null)
            return;

        if (event.getClickedBlock().getType() != Material.SPAWNER)
            return;

        ItemStack item = event.getItem();
        if (item == null || !item.getType().name().endsWith("_SPAWN_EGG"))
            return;

        Block block = event.getClickedBlock();
        CreatureSpawner spawner = (CreatureSpawner) block.getState();

        EntityType type = eggToEntity(item.getType());
        if (type == null)
            return;

        // 💥 ЖЁСТКО ставим ТОЛЬКО тип, БЕЗ NBT
        spawner.setSpawnedType(type);
        spawner.update(true);

        event.setCancelled(true);
    }

    /* =========================================================
       3️⃣ НЕ-МОБЫ (minecart, armorstand, falling_block)
       ========================================================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAnySpawn(EntitySpawnEvent event) {

        EntityType type = event.getEntityType();

        // ❌ Запрещаем NBT-спавн через яйца / команды
        switch (type) {
            case ARMOR_STAND:
            case MINECART:
            case CHEST_MINECART:
            case FURNACE_MINECART:
            case TNT_MINECART:
            case HOPPER_MINECART:
            case COMMAND_BLOCK_MINECART:
            case FALLING_BLOCK:
                event.setCancelled(true);
                break;
            default:
                break;
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
