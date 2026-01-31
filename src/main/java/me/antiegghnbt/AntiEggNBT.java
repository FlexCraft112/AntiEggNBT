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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class AntiEggNBT extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiEggNBT включён — удаляет все NBT-яйца кроме тех, что в меню ChestCommands");
    }

    /**
     * Проверка: яйцо с любым NBT (даже минимальным)
     * Ты хотел удалять ВСЕ яйца с NBT → поэтому > 2 символов
     */
    private boolean isNBTSpawnEgg(ItemStack item) {
        if (item == null) return false;
        if (!item.getType().name().endsWith("_SPAWN_EGG")) return false;
        if (!item.hasItemMeta()) return false;
        // Любое яйцо с NBT (даже минимальным) → удаляем
        return item.getItemMeta().getAsString().length() > 2;
    }

    /**
     * Проверяем, открыто ли сейчас меню ChestCommands
     * (по заголовку инвентаря)
     */
    private boolean isChestCommandsMenu(InventoryClickEvent event) {
        if (event.getView() == null) return false;

        // Получаем заголовок как Component → конвертируем в строку с §
        var titleComponent = event.getView().getTitle();
        String title = LegacyComponentSerializer.legacySection().serialize(titleComponent);

        // Приводим к нижнему регистру для поиска
        title = title.toLowerCase();

        // Список условий — добавляй свои заголовки меню сюда
        return 
            title.contains("магазин") ||
            title.contains("яйца") ||
            title.contains("купить") ||
            title.contains("монеты") ||
            title.contains("shop") ||
            title.contains("eggs") ||
            title.contains("мобы") ||
            title.contains("mob") ||
            // Точные заголовки (если используешь цвета):
            title.contains("§8магазин яиц") ||
            title.contains("§6купить мобов") ||
            title.contains("§eяйца за монеты") ||
            title.contains("§cмагазин спавнеров");
    }

    // ────────────────────────────────────────────────
    //                СОБЫТИЯ
    // ────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onUseEgg(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (!isNBTSpawnEgg(item)) return;

        event.setCancelled(true);
        event.getPlayer().getInventory().remove(item);
        event.getPlayer().sendMessage("§cNBT-яйца запрещены");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreative(InventoryCreativeEvent event) {
        ItemStack item = event.getCursor();
        if (!isNBTSpawnEgg(item)) return;

        event.setCancelled(true);
        event.setCursor(null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        // Если это меню ChestCommands — НЕ трогаем ничего
        if (isChestCommandsMenu(event)) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (!isNBTSpawnEgg(item)) return;

        event.setCancelled(true);
        event.setCurrentItem(null);

        // Дополнительно удаляем из курсора, если там тоже яйцо
        if (isNBTSpawnEgg(event.getCursor())) {
            event.setCursor(null);
        }

        Player player = (Player) event.getWhoClicked();
        player.sendMessage("§cNBT-яйцо удалено из инвентаря");
    }
}
