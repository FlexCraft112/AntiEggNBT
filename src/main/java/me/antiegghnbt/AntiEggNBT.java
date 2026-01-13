package me.antiegghnbt;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
        getLogger().info("AntiEggNBT enabled (FULL egg sanitization)");
    }

    @EventHandler
    public void onEggUse(PlayerInteractEvent event) {
        if (!event.hasItem()) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        // только яйца
        if (!item.getType().name().endsWith("_SPAWN_EGG"))
            return;

        ItemMeta meta = item.getItemMeta();

        // если есть ЛЮБОЙ NBT — сбрасываем
        if (meta != null && (
                meta.hasDisplayName()
                || meta.hasLore()
                || meta.hasCustomModelData()
        )) {

            Player p = event.getPlayer();
            Material cleanEgg = item.getType();

            // отменяем использование
            event.setCancelled(true);

            // забираем грязное яйцо
            item.setAmount(item.getAmount() - 1);

            // выдаём чистое ванильное
            ItemStack vanilla = new ItemStack(cleanEgg, 1);
            p.getInventory().addItem(vanilla);

            p.sendMessage("§cNBT в яйце удалён. Используй ванильное яйцо.");
            getLogger().warning("Sanitized spawn egg from " + p.getName());
        }
    }
}
