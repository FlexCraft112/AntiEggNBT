package me.antiegghnbt;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
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
import org.bukkit.plugin.java.JavaPlugin;

public class AntiEggNBT extends JavaPlugin implements Listener {

    private RegionQuery regionQuery;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        RegionContainer container = WorldGuard.getInstance()
                .getPlatform().getRegionContainer();
        regionQuery = container.createQuery();

        getLogger().info("AntiEggNBT enabled");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEggUse(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        ItemStack item = event.getItem();
        if (item == null || !item.getType().name().endsWith("_SPAWN_EGG"))
            return;

        if (event.getClickedBlock() == null)
            return;

        Location spawnLoc = event.getClickedBlock()
                .getLocation().add(0.5, 1, 0.5);

        // ❌ ЗАПРЕТ В РЕГИОНЕ zona
        ApplicableRegionSet regions = regionQuery.getApplicableRegions(
                BukkitAdapter.adapt(spawnLoc)
        );

        boolean inZona = regions.getRegions().stream()
                .anyMatch(r -> r.getId().equalsIgnoreCase("zona"));

        if (inZona) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cЗдесь нельзя использовать яйца спавна.");
            return;
        }

        // ❗ ВСЕГДА отменяем ваниль (NBT режется тут)
        event.setCancelled(true);

        EntityType type = eggToEntity(item.getType());
        if (type == null || !type.isAlive())
            return;

        // ❌ БЛОК НЕ-МОБОВ (minecart, armorstand и т.п.)
        if (!type.isSpawnable() || !type.isAlive())
            return;

        Entity entity = spawnLoc.getWorld().spawnEntity(spawnLoc, type);

        // SLIME / MAGMA → size = 1
        if (entity instanceof Slime slime) {
            slime.setSize(1);
        }

        // GIANT → ZOMBIE
        if (entity.getType() == EntityType.GIANT) {
            entity.remove();
            spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
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
