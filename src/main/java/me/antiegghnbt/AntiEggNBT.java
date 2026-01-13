package me.antiegghnbt;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiEggNBT extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiEggNBT enabled (NBT eggs removal mode)");
    }

    /* =========================================
       1️⃣ ЧИСТИМ ИНВЕНТАРЬ ПРИ ВХОДЕ
       ========================================= */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        cleanInventory(event.getPlayer().getInventory());
    }

    /* =========================================
       2️⃣ БЛОК ИСПОЛЬЗОВАНИЯ ЯИЦ
       ========================================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onUse(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (isIllegalEgg(item)) {
            event.setCancelled(true);
            item.setAmount(0);
        }
    }

    /* =========================================
       3️⃣ БЛОК ПЕРЕМЕЩЕНИЯ (СПАВНЕРЫ / КЛИКИ)
       ========================================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (isIllegalEgg(item)) {
            event.setCancelled(true);
            item.setAmount(0);
        }
    }

    /* ========================================= */

    private void cleanInventory(PlayerInventory inv) {
        for (ItemStack item : inv.getContents()) {
            if (isIllegalEgg(item)) {
                item.setAmount(0);
            }
        }
    }

    private boolean isIllegalEgg(ItemStack item) {
        if (item == null) return false;
        if (!item.getType().name().endsWith("_SPAWN_EGG")) return false;

        ItemMeta meta = item.getItemMeta();
        return meta != null; // любое meta = НЕ ваниль
    }
}
