package me.antiegghnbt;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiEggNBT extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiEggNBT enabled (Egg reset + WG protection)");
    }

    /* =========================================================
       ЯЙЦА → ВСЕГДА ДЕФОЛТНЫЙ МОБ (NBT УБИВАЕМ)
       ========================================================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEggSpawn(CreatureSpawnEvent event) {

        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)
            return;

        Entity original = event.getEntity();
        Location loc = original.getLocation();

        // ❌ убиваем оригинал с NBT
        event.setCancelled(true);

        EntityType type = original.getType();

        // GIANT → ZOMBIE
        if (type == EntityType.GIANT) {
            loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
            return;
        }

        // SLIME / MAGMA → size 1
        if (original instanceof Slime) {
            Slime s = (Slime) loc.getWorld().spawnEntity(loc, type);
            s.setSize(1);
            return;
        }

        // ❌ НЕМOБЫ (вагонетки, стойки и т.д.)
        if (!type.isAlive()) {
            return;
        }

        // ✅ обычный моб, БЕЗ NBT
        loc.getWorld().spawnEntity(loc, type);
    }

    /* =========================================================
       ПРОВЕРКА РЕГИОНОВ WG ПЕРЕД ИСПОЛЬЗОВАНИЕМ ЯЙЦА
       ========================================================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEggUse(PlayerInteractEvent event) {

        if (event.getItem() == null)
            return;

        ItemStack item = event.getItem();
        if (!item.getType().name().endsWith("_SPAWN_EGG"))
            return;

        if (event.getClickedBlock() == null)
            return;

        Player player = event.getPlayer();
        Location loc = event.getClickedBlock().getLocation();

        if (!canUseEggHere(player, loc)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "❌ Здесь нельзя использовать яйца спавна");
        }
    }

    /* =========================================================
       ЛОГИКА WORLDGUARD
       ========================================================= */
    private boolean canUseEggHere(Player player, Location loc) {

        RegionManager rm = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(loc.getWorld()));

        if (rm == null)
            return true;

        ApplicableRegionSet regions = rm.getApplicableRegions(
                BukkitAdapter.asBlockVector(loc));

        // ❌ зона спавна
        for (ProtectedRegion region : regions) {
            if (region.getId().equalsIgnoreCase("zona")) {
                return player.hasPermission("antiegghnbt.bypass.zona");
            }
        }

        // ❌ чужие регионы
        for (ProtectedRegion region : regions) {
            if (!region.isOwner(WorldGuardPlugin.inst().wrapPlayer(player))) {
                return player.hasPermission("antiegghnbt.bypass.region");
            }
        }

        return true;
    }
}
