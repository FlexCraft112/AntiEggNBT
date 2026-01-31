package me.antiegghnbt;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiEggNBT extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiEggNBT включён — блокировка NBT-яиц (кроме меню ChestCommands)");
    }

    // Универсальная проверка на подозрительное NBT-яйцо
    private boolean isIllegalNBTSpawnEgg(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        String typeName = item.getType().name();
        if (!typeName.endsWith("_SPAWN_EGG")) return false;
        if (!item.hasItemMeta()) return false;

        // Ванильные яйца имеют короткий NBT (~20–70 символов)
        // Если сильно больше — это модифицированное/читерское
        String metaStr = item.getItemMeta().getAsString();
        return metaStr.length() > 80;  // ← можно поднять до 100–120, если ложные срабатывания
    }

    // Проверка — это меню магазина ChestCommands?
    private boolean isChestCommandsShopMenu(Inventory inv) {
        if (inv == null) return false;
        String title = inv.getTitle();  // или inv.getName() в старых версиях
        if (title == null) return false;

        title = title.toLowerCase();

        // Добавь сюда ВСЕ возможные заголовки твоих магазинов яиц
        // Можно использовать contains() для гибкости
        return title.contains("магазин") ||
               title.contains("яйца") ||
               title.contains("купить") ||
               title.contains("магаз") ||
               title.contains("shop") ||
               title.contains("eggs") ||
               title.contains("мобы") ||
               title.contains("mob") ||
               // Добавь точные названия своих меню, например:
               title.equals("§8§lМагазин Яиц") ||
               title.equals("§6Купить Мобов");
    }

    // ────────────────────────────────────────────────
    //                СОБЫТИЯ
    // ────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onUseEgg(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (!isIllegalNBTSpawnEgg(item)) return;

        event.setCancelled(true);
        event.getPlayer().getInventory().remove(item);
        event.getPlayer().sendMessage("§cЗапрещено использовать NBT-яйца!");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreative(InventoryCreativeEvent event) {
        ItemStack item = event.getCursor();
        if (!isIllegalNBTSpawnEgg(item)) return;

        event.setCancelled(true);
        event.setCursor(null);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getClickedInventory();

        // Если это меню ChestCommands — НЕ трогаем яйца!
        if (isChestCommandsShopMenu(inv)) {
            return;  // ← главное изменение: пропускаем магазин
        }

        // Проверяем курсор (предмет, который держит игрок)
        ItemStack cursor = event.getCursor();
        if (isIllegalNBTSpawnEgg(cursor)) {
            event.setCursor(null);
            event.setCancelled(true);
            player.sendMessage("§cЗапрещённый NBT-яйцо удалён!");
            return;
        }

        // Проверяем предмет в слоте
        ItemStack current = event.getCurrentItem();
        if (isIllegalNBTSpawnEgg(current)) {
            event.setCurrentItem(null);
            event.setCancelled(true);
            player.sendMessage("§cЗапрещённый NBT-яйцо удалён!");
        }
    }
}
