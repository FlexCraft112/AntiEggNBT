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

import java.lang.reflect.Method;

public class AntiEggNBT extends JavaPlugin implements Listener {

    private Method getTitleMethod;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiEggNBT включён — удаляет все NBT-яйца (кроме меню ChestCommands)");

        // Рефлексия для getTitle() — работает в 1.20.1
        try {
            getTitleMethod = Class.forName("org.bukkit.inventory.InventoryView").getMethod("getTitle");
        } catch (Exception e) {
            getLogger().severe("Не удалось получить метод getTitle через рефлексию: " + e.getMessage());
        }
    }

    /* ===============================
       УНИВЕРСАЛЬНАЯ ПРОВЕРКА ЯЙЦА
       =============================== */
    private boolean isNBTSpawnEgg(ItemStack item) {
        if (item == null) return false;
        if (!item.getType().name().endsWith("_SPAWN_EGG")) return false;
        if (!item.hasItemMeta()) return false;
        // Как в твоём оригинале — любое яйцо с NBT > 2 символов
        return item.getItemMeta().getAsString().length() > 2;
    }

    /* ===============================
       ПРОВЕРКА — ЭТО МЕНЮ CHESTCOMMANDS?
       =============================== */
    private boolean isChestCommandsMenu(InventoryClickEvent event) {
        if (getTitleMethod == null) return false; // если рефлексия не удалась — не рискуем

        try {
            Object titleObj = getTitleMethod.invoke(event.getView());
            if (titleObj instanceof String) {
                String title = ((String) titleObj).toLowerCase();

                // Добавь свои заголовки меню (можно без цветов, contains гибкий)
                return title.contains("магазин") ||
                       title.contains("яйца") ||
                       title.contains("купить") ||
                       title.contains("монеты") ||
                       title.contains("shop") ||
                       title.contains("eggs") ||
                       title.contains("мобы") ||
                       title.contains("mob") ||
                       title.contains("магазин яиц") ||
                       title.contains("купить мобов") ||
                       title.contains("яйца за монеты") ||
                       title.contains("магазин спавнеров");
            }
        } catch (Exception ignored) {
            // Если рефлексия сломалась — считаем, что не магазин
        }
        return false;
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
       ПЕРЕТАСКИВАНИЕ / КЛИКИ В ИНВЕНТАРЯХ
       =============================== */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        // Если это меню магазина — пропускаем
        if (isChestCommandsMenu(event)) {
            return;
        }

        boolean changed = false;

        ItemStack current = event.getCurrentItem();
        if (isNBTSpawnEgg(current)) {
            event.setCurrentItem(null);
            changed = true;
        }

        ItemStack cursor = event.getCursor();
        if (isNBTSpawnEgg(cursor)) {
            event.setCursor(null);
            changed = true;
        }

        if (changed) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.sendMessage("§cNBT-яйцо удалено из инвентаря");
            player.updateInventory();
        }
    }
}
