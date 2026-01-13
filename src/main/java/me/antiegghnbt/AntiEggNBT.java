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
        getLogger().info("AntiEggNBT enabled (full vanilla enforcement)");
    }

    /* =========================================================
       1️⃣ SPAWN EGGS → ТОЛЬКО ВАНИЛЬНЫЕ МОБЫ
       ========================================================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEggSpawn(CreatureSpawnEvent event) {

        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)
            return;

        Entity entity = event.getEntity();
        Location loc = entity.getLocation();
        EntityType type = entity.getType();

        // ❌ Giant → Zombie
        if (type == EntityType.GIANT) {
            event.setCancelled(true);
            loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
            return;
        }

        // ❌ Slime / Magma → size 1
        if (entity instanceof Slime slime) {
            event.setCancelled(true);
            Slime clean = (Slime) loc.getWorld().spawnEntity(loc, type);
            clean.setSize(1);
            return;
        }

        // 🔁 ВСЁ ОСТАЛЬНОЕ — пересоздаём БЕЗ NBT
        event.setCancelled(true);
        loc.getWorld().spawnEntity(loc, type);
    }

    /* =========================================================
       2️⃣ SPawner — ЧИСТИМ ПРИ ВСТАВКЕ ЯЙЦА
       ========================================================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerEggUse(PlayerInteractEvent event) {

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

        // ✅ ЖЁСТКО ставим ТОЛЬКО ТИП
        spawner.setSpawnedType(type);
        spawner.update(true);

        event.setCancelled(true);
    }

    /* =========================================================
       3️⃣ НЕ-МОБЫ (minecart, armorstand и т.п.)
       БЛОКИРУЕМ НА ИСПОЛЬЗОВАНИИ ПРЕДМЕТА
       ========================================================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onIllegalItemUse(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
            event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        ItemStack item = event.getItem();
        if (item == null)
            return;

        switch (item.getType()) {
            case ARMOR_STAND:
            case MINECART:
            case CHEST_MINECART:
            case FURNACE_MINECART:
            case TNT_MINECART:
            case HOPPER_MINECART:
            case COMMAND_BLOCK_MINECART:
                event.setCancelled(true);
                item.setAmount(0); // 💥 сжигаем предмет
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
