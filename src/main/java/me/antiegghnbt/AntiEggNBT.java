package me.antiegghnbt;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldedit.math.BlockVector3;
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

    private WorldGuardPlugin wg;

    @Override
    public void onEnable() {
        wg = WorldGuardPlugin.inst();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiEggNBT enabled (stable WG build)");
    }

    /* =========================================================
       ЯЙЦА → ВСЕГДА ДЕФОЛТНЫЙ МОБ
       ========================================================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEggSpawn(CreatureSpawnEvent event) {

        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)
            return;

        Entity original = event.getEntity();
        Location loc = original.getLocation();
        EntityType type = original.getType();

        event.setCancelled(true); // 💥 убиваем NBT

        // ❌ НЕ МОБЫ
        if (!type.isAlive())
            return;

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

        // ✅ обычный моб без NBT
        loc.getWorld().spawnEntity(loc, type);
    }

    /* =========================================================
       ЗАПРЕТ ИСПОЛЬЗОВАНИЯ ЯИЦ В РЕГИОНАХ
       ========================================================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEggUse(PlayerInteractEvent event) {

        ItemStack item = event.getItem();
        if (item == null)
            return;

        if (!item.getType().name().endsWith("_SPAWN_EGG"))
            return;

        if (event.getClickedBlock() == null)
            return;

        Player player = event.getPlayer();
        Location loc = event.getClickedBlock().getLocation();

        if (!canUseHere(player, loc)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "❌ Здесь запрещено использовать яйца спавна");
        }
    }

    /* =========================================================
       WORLDGUARD ЛОГИКА
       ========================================================= */
    private boolean canUseHere(Player player, Location loc) {

        RegionManager rm = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(loc.getWorld()));

        if (rm == null)
            return true;

        BlockVector3 vec = BlockVector3.at(
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ()
        );

        ApplicableRegionSet regions = rm.getApplicableRegions(vec);

        // ❌ регион zona
        for (ProtectedRegion r : regions) {
            if (r.getId().equalsIgnoreCase("zona")) {
                return player.hasPermission("antiegghnbt.bypass.zona");
            }
        }

        // ❌ чужие регионы
        for (ProtectedRegion r : regions) {
            if (!r.isOwner(wg.wrapPlayer(player))) {
                return player.hasPermission("antiegghnbt.bypass.region");
            }
        }

        return true;
    }
}
