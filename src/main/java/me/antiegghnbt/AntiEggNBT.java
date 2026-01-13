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
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiEggNBT extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiEggNBT enabled (FULL HARD RESET)");
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

        event.setCancelled(true);

        // SLIME / MAGMA → size 1
        if (entity instanceof Slime) {
            Slime s = (Slime) loc.getWorld().spawnEntity(loc, type);
            s.setSize(1);
            return;
        }

        // GIANT → ZOMBIE
        if (type == EntityType.GIANT) {
            loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
            return;
        }

        // ВСЁ ОСТАЛЬНОЕ → ЧИСТЫЙ ВАНИЛЬНЫЙ МОБ
        loc.getWorld().spawnEntity(loc, type);
    }

    /* =========================================================
       2️⃣ СПАВНЕРЫ — ЖЁСТКАЯ ЧИСТКА
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
        if (type == null)
            return;

        CreatureSpawner spawner = (CreatureSpawner) block.getState();
        spawner.setSpawnedType(type);
        spawner.update(true);

        event.setCancelled(true);
    }

    /* =========================================================
       3️⃣ НЕ-МОБЫ — БЛОК ТОЛЬКО ИЗ ЯИЦА / КОМАНД
       ========================================================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onIllegalSpawn(EntitySpawnEvent event) {

        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG
                && event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.COMMAND)
            return;

        switch (event.getEntityType()) {
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
