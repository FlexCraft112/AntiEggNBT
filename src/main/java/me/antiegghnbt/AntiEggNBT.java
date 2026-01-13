package me.antiegghnbt;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.bukkit.BukkitAdapter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

public class AntiEggNBT extends JavaPlugin implements Listener {

    private static final String PROTECTED_REGION = "zona";

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiEggNBT enabled (WorldGuard + permissions)");
    }

    /* =========================================================
       1️⃣ ЯЙЦА — ЛОГИКА СПАВНА
       ========================================================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEggSpawn(CreatureSpawnEvent event) {

        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)
            return;

        Location loc = event.getLocation();

        // ❗ ВНЕ региона zona — ваниль
        if (!isInProtectedRegion(loc))
            return;

        Entity original = event.getEntity();
        EntityType type = original.getType();

        event.setCancelled(true);

        // ❌ Запрещённые сущности
        switch (type) {
            case GIANT:
                loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
                return;
            case ARMOR_STAND:
            case MINECART:
            case CHEST_MINECART:
            case FURNACE_MINECART:
            case TNT_MINECART:
            case HOPPER_MINECART:
            case COMMAND_BLOCK_MINECART:
            case FALLING_BLOCK:
                return;
            default:
                break;
        }

        // 🧪 Slime / Magma — всегда маленькие
        if (type == EntityType.SLIME || type == EntityType.MAGMA_CUBE) {
            Slime slime = (Slime) loc.getWorld().spawnEntity(loc, type);
            slime.setSize(1);
            return;
        }

        // ✅ Всё остальное — дефолтный моб
        loc.getWorld().spawnEntity(loc, type);
    }

    /* =========================================================
       2️⃣ БЛОКИРУЕМ ЯЙЦА В ЧУЖИХ ПРИВАТАХ
       ========================================================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEggUse(PlayerInteractEvent event) {

        if (event.getClickedBlock() == null)
            return;

        ItemStack item = event.getItem();
        if (item == null)
            return;

        if (!item.getType().name().endsWith("_SPAWN_EGG"))
            return;

        Player player = event.getPlayer();
        Location loc = event.getClickedBlock().getLocation();

        // ❗ Проверяем только если есть регионы
        ApplicableRegionSet regions = getRegions(loc);
        if (regions == null)
            return;

        for (ProtectedRegion region : regions) {

            // zona — отдельная логика
            if (region.getId().equalsIgnoreCase(PROTECTED_REGION))
                return;

            // если игрок НЕ владелец и НЕ участник
            if (!region.isOwner(BukkitAdapter.adapt(player))
                    && !region.isMember(BukkitAdapter.adapt(player))) {

                event.setCancelled(true);
                player.sendMessage("§cВы не можете использовать яйца в чужом привате.");
                return;
            }
        }
    }

    /* =========================================================
       🔍 ПРОВЕРКА: ВНУТРИ zona?
       ========================================================= */
    private boolean isInProtectedRegion(Location loc) {

        ApplicableRegionSet regions = getRegions(loc);
        if (regions == null)
            return false;

        for (ProtectedRegion region : regions) {
            if (region.getId().equalsIgnoreCase(PROTECTED_REGION)) {
                return true;
            }
        }
        return false;
    }

    /* =========================================================
       🔍 ПОЛУЧЕНИЕ РЕГИОНОВ
       ========================================================= */
    private ApplicableRegionSet getRegions(Location loc) {

        if (loc.getWorld() == null)
            return null;

        RegionManager manager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(loc.getWorld()));

        if (manager == null)
            return null;

        return manager.getApplicableRegions(
                BukkitAdapter.asBlockVector(loc)
        );
    }
}
