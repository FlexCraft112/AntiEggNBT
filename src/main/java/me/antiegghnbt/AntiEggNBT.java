package me.antiegghnbt;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.java.JavaPlugin;

public final class AntiEggNBT extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiEggNBT enabled (NBT eggs removed, vanilla allowed)");
    }

    /* =========================================================
       ОБЩАЯ ПРОВЕРКА: ЯЙЦО С NBT ИЛИ НЕТ
       ========================================================= */
    private boolean isModifiedSpawnEgg(ItemStack item) {
        if (item == null) return false;
        if (!item.getType().name().endsWith("_SPAWN_EGG")) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        // Любые данные = вмешательство
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.isEmpty()) return true;

        // display / lore / custom name — тоже признак
        if (meta.hasDisplayName()) return true;
        if (meta.hasLore()) return true;
        if (meta.hasCustomModelData()) return true;

        return false;
    }

    /* =========================================================
       1️⃣ ИСПОЛЬЗОВАНИЕ ЯЙЦА (ПКМ)
       ========================================================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEggUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK &&
            event.getAction() != Action.RIGHT_CLICK_AIR) return;

        ItemStack item = event.getItem();
        if (!isModifiedSpawnEgg(item)) return;

        Player player = event.getPlayer();
        event.setCancelled(true);

        // удаляем яйцо
        item.setAmount(0);

        player.sendMessage("§cЭто яйцо содержит запрещённые NBT-данные.");
    }

    /* =========================================================
       2️⃣ СПАВНЕРЫ (ПКМ ПО СПАВНЕРУ)
       ========================================================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerInsert(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.SPAWNER) return;

        ItemStack item = event.getItem();
        if (!isModifiedSpawnEgg(item)) return;

        event.setCancelled(true);
        item.setAmount(0);

        event.getPlayer().sendMessage("§cNBT-яйца запрещены для спавнеров.");
    }

    /* =========================================================
       3️⃣ ИНВЕНТАРИ (перетаскивание, креатив, хотбар)
       ========================================================= */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (!isModifiedSpawnEgg(item)) return;

        event.setCancelled(true);
        event.setCurrentItem(null);

        if (event.getWhoClicked() instanceof Player player) {
            player.sendMessage("§cNBT-яйца удалены.");
        }
    }
}
