package me.antiegghnbt;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiEggNBT extends JavaPlugin implements Listener {

    private WorldGuardPlugin wg;

    @Override
    public void onEnable() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (plugin instanceof WorldGuardPlugin) {
            wg = (WorldGuardPlugin) plugin;
        } else {
            getLogger().severe("WorldGuard not found!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEggUse(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || !item.getType().name().endsWith("_SPAWN_EGG")) return;

        if (event.getClickedBlock() == null) return;

        Location loc = event.getClickedBlock().getLocation().add(0.5, 1, 0.5);

        // ❌ REGION zona
        RegionManager rm = wg.getRegionManager(loc.getWorld());
        if (rm != null) {
            ApplicableRegionSet set = rm.getApplicableRegions(loc);
            if (set.getRegions().stream()
                    .anyMatch(r -> r.getId().equalsIgnoreCase("zona"))) {
                event.setCancelled(true);
                return;
            }
        }

        // ❌ отменяем ваниль полностью (NBT режется тут)
        event.setCancelled(true);

        EntityType type = eggToEntity(item.getType());
        if (type == null) return;

        // ❌ блок НЕ-мобов
        if (!type.isAlive()) return;

        Entity entity = loc.getWorld().spawnEntity(loc, type);

        // Slime fix
        if (entity instanceof Slime slime) {
            slime.setSize(1);
        }

        // Giant fix
        if (entity.getType() == EntityType.GIANT) {
            entity.remove();
            loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
        }
    }

    private EntityType eggToEntity(Material egg) {
        try {
            return EntityType.valueOf(egg.name().replace("_SPAWN_EGG", ""));
        } catch (Exception e) {
            return null;
        }
    }
}
