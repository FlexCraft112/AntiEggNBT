package me.antiegghnbt;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiEggNBT extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiEggNBT enabled (HARD egg protection)");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEggUse(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK
                && event.getAction() != Action.RIGHT_CLICK_AIR)
            return;

        ItemStack item = event.getItem();
        if (item == null)
            return;

        Material mat = item.getType();
        if (!mat.name().endsWith("_SPAWN_EGG"))
            return;

        event.setCancelled(true); // ❗ БЛОКИРУЕМ ИГРУ ПОЛНОСТЬЮ

        EntityType type = eggToEntity(mat);
        if (type == null)
            return;

        // ❌ НЕ-ЖИВЫЕ СУЩНОСТИ = В БЛОК
        if (!type.isAlive()) {
            getLogger().warning("Blocked non-living egg spawn: " + type);
            return;
        }

        Location loc = event.getPlayer().getLocation().add(0, 1, 0);
        Entity spawned = loc.getWorld().spawnEntity(loc, type);

        // SLIME / MAGMA → size = 1
        if (spawned instanceof Slime slime) {
            slime.setSize(1);
        }

        // GIANT → ZOMBIE
        if (spawned.getType() == EntityType.GIANT) {
            spawned.remove();
            loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
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
