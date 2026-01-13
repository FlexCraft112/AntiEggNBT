package me.antiegghnbt;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiEggNBT extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiEggNBT enabled (FULL VANILLA RESET)");
    }

    @EventHandler
    public void onEggUse(PlayerInteractEvent event) {

        if (!event.hasItem())
            return;

        ItemStack item = event.getItem();
        if (item == null)
            return;

        if (!item.getType().name().endsWith("_SPAWN_EGG"))
            return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;

        // ❗ ЛЮБОЙ кастом = подозрительно
        boolean dirty =
                meta.hasLore()
             || meta.hasDisplayName()
             || meta.hasCustomModelData()
             || meta.getPersistentDataContainer().getKeys().size() > 0;

        if (!dirty)
            return;

        // ===== RESET TO PURE VANILLA =====
        Material cleanEgg = item.getType();
        ItemStack clean = new ItemStack(cleanEgg, item.getAmount());

        event.getPlayer().getInventory()
                .setItem(event.getHand(), clean);

        getLogger().warning(
                "SpawnEgg NBT wiped: " + cleanEgg.name()
                + " by " + event.getPlayer().getName()
        );
    }
}
