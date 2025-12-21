package pl.tenfajnybartek.dropplugin.utils;

import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import pl.tenfajnybartek.dropplugin.managers.ConfigManager;

public final class ItemUtils {
    private ItemUtils() {}

    public static boolean giveItem(Inventory inventory, ItemStack itemStack) {
        if (itemStack == null) return false;
        if (canPickup(inventory, itemStack)) {
            inventory.addItem(itemStack);
            return true;
        }
        return false;
    }

    public static boolean canPickup(Inventory inventory, ItemStack itemStack) {
        if (inventory == null || itemStack == null) return false;
        if (inventory.firstEmpty() == -1) {
            for (ItemStack existingItem : inventory.getStorageContents()) {
                if (existingItem != null && existingItem.isSimilar(itemStack)) {
                    if (existingItem.getAmount() + itemStack.getAmount() <= existingItem.getMaxStackSize()) {
                        return true;
                    }
                }
            }
            return false;
        }
        return true;
    }

    public static void recalculateDurability(Player player, ItemStack item) {
        if (player == null || item == null) return;
        if (!(item.getItemMeta() instanceof Damageable)) return;

        Damageable damageableMeta = (Damageable) item.getItemMeta();
        int currentDamage = damageableMeta.getDamage();

        int maxDurability = item.getType().getMaxDurability();
        if (maxDurability <= 0) return;

        int enchantLevel = item.getEnchantmentLevel(Enchantment.UNBREAKING);
        boolean shouldDamage = true;
        
        if (enchantLevel > 0) {
            int damageChance = 100 / (enchantLevel + 1);
            int roll = RandomUtils.getRandInt(0, 100);
            if (roll >= damageChance) {
                shouldDamage = false;
            }
        }
        
        if (!shouldDamage) {
            return;
        }

        int newDamage = currentDamage + 1;

        if (newDamage >= maxDurability) {
            player.getInventory().clear(player.getInventory().getHeldItemSlot());
            if (player.getLocation() != null) {
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            }
        } else {
            damageableMeta.setDamage(newDamage);
            item.setItemMeta(damageableMeta);
        }
    }

    public static void giveItem(Player player, ItemStack itemStack) {
        if (itemStack == null || player == null) return;

        if (!ItemUtils.giveItem(player.getInventory(), itemStack)) {
            ConfigManager cfg = ConfigManager.getConfigManager();
            if (cfg != null && cfg.isToInv()) {
                if (cfg.isMessageInv()) {
                    ChatUtils.sendMessage(player, cfg.getMessageInvFull());
                }
                return;
            }
            player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
        }
    }
}