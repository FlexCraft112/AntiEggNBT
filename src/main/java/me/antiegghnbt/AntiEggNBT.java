package me.antiegghnbt;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiEggNBT extends JavaPlugin implements Listener {

    private static final String SPAWN_REGION = "zona";

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiEggNBT ENABLED (hard NBT reset)");
    }

    /* =========================================================
       1) СПАВН ИЗ ЯИЦ — ТОЛЬКО ВАНИЛЛА
       ========================================================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEggSpawn(CreatureSpawnEvent event) {

        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)
            return;

        Location loc = event.getLocation();
        Player player = event.getEntity().getSpawner() instanceof Player
                ? (Player) event.getEntity().getSpawner()
                : null;

        if (player != null) {
            if (isInSpawnRegion(loc) || !canBuildHere(player, loc)) {
                event.setCancelled(true);
                return;
            }
        }

        Entity entity = event.getEntity();
        EntityType type = entity.getType();

        // ❌ ВСЕГДА пересоздаём
        event.setCancelled(true);

        // SLIME → size 1
        if (entity instanceof Slime) {
            Slime s = (Slime) loc.getWorld().spawnEntity(loc, EntityType.SLIME);
            s.setSize(1);
            return;
        }

        // GIANT → ZOMBIE
        if (type == EntityType.GIANT) {
            loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
            return;
        }

        // ВСЁ ОСТАЛЬНОЕ — обычный ванильный моб
        loc.getWorld().spawnEntity(loc, type);
    }

    /* =========================================================
       2) ЗАПРЕТ ЯИЦ В СПАВНЕРАХ (НАВСЕГДА)
       ========================================================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerUse(PlayerInteractEvent event) {

        if (event.getClickedBlock() == null)
            return;

        if (event.getClickedBlock().getType() != Material.SPAWNER)
            return;

        ItemStack item = event.getItem();
        if (item == null || !item.getType().name().endsWith("_SPAWN_EGG"))
            return;

        // ❌ ВСЕГДА
        event.setCancelled(true);
        event.getPlayer().sendMessage("§cИспользование яиц в спавнерах запрещено.");
    }

    /* =========================================================
       WorldGuard helpers
       ========================================================= */

    private boolean isInSpawnRegion(Location loc) {
        RegionManager rm = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(loc.getWorld()));

        if (rm == null) return false;

        ApplicableRegionSet set = rm.getApplicableRegions(BukkitAdapter.asBlockVector(loc));
        for (ProtectedRegion r : set) {
            if (r.getId().equalsIgnoreCase(SPAWN_REGION)) {
                return true;
            }
        }
        return false;
    }

    private boolean canBuildHere(Player player, Location loc) {
        RegionManager rm = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(loc.getWorld()));

        if (rm == null) return true;

        ApplicableRegionSet set = rm.getApplicableRegions(BukkitAdapter.asBlockVector(loc));
        for (ProtectedRegion r : set) {
            if (!r.isOwner(WorldGuardPlugin.inst().wrapPlayer(player))
                    && !r.isMember(WorldGuardPlugin.inst().wrapPlayer(player))) {
                return false;
            }
        }
        return true;
    }
}
