package me.antiegghnbt;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiEggNBT extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiEggNBT enabled (NBT eggs hard blocked)");
    }

    /* ===============================
       УНИВЕРСАЛЬНАЯ ПРОВЕРКА ЯЙЦА
       =============================== */
    private boolean isNBTSpawnEgg(ItemStack item) {
        if (item == null) return false;
        if (!item.getType().name().endsWith("_SPAWN_EGG")) return false;

        if (!item.hasItemMeta()) return false;

        // ВАЖНО: ванильное яйцо = почти пустой meta
        return item.getItemMeta().getAsString().length() > 2;
    }

    /* ===============================
       КЛИК ПКМ ПО МИРУ
       =============================== */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onUseEgg(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (!isNBTSpawnEgg(item)) return;

        event.setCancelled(true);
        event.getPlayer().getInventory().remove(item);
        event.getPlayer().sendMessage("§cNBT-яйца запрещены");
    }

    /* ===============================
       КРЕАТИВ-ИНВЕНТАРЬ
       =============================== */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreative(InventoryCreativeEvent event) {
        ItemStack item = event.getCursor();
        if (!isNBTSpawnEgg(item)) return;

        event.setCancelled(true);
        event.setCursor(null);
    }

    /* ===============================
       ПЕРЕТАСКИВАНИЕ В ИНВЕНТАРЯХ
       =============================== */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (!isNBTSpawnEgg(item)) return;

        event.setCancelled(true);
        event.setCurrentItem(null);
    }
}
