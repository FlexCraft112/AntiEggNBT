package me.antiegghnbt;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack as NMSItem;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;

public class AntiEggNBT extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("AntiEggNBT enabled");
    }

    @EventHandler
    public void onUseEgg(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK &&
            event.getAction() != Action.RIGHT_CLICK_AIR) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        if (!item.getType().name().endsWith("_SPAWN_EGG")) return;

        NMSItem nms = CraftItemStack.asNMSCopy(item);
        if (!nms.hasTag()) return;

        CompoundTag tag = nms.getTag();
        if (!tag.contains("EntityTag")) return;

        CompoundTag entityTag = tag.getCompound("EntityTag");

        if (entityTag.contains("Size") && entityTag.getInt("Size") >= 2) {
            event.setCancelled(true);
            player.getInventory().remove(item);
            player.sendMessage("§cЗапрещено использовать NBT-яйца больших слизней.");
        }
    }
}
