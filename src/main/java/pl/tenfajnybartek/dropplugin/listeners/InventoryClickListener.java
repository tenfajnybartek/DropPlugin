package pl.tenfajnybartek.dropplugin.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.tenfajnybartek.dropplugin.base.DropPlugin;
import pl.tenfajnybartek.dropplugin.managers.ConfigManager;
import pl.tenfajnybartek.dropplugin.objects.Drop;
import pl.tenfajnybartek.dropplugin.objects.User;
import pl.tenfajnybartek.dropplugin.utils.ChatUtils;

public class InventoryClickListener implements Listener {
    private final DropPlugin plugin;
    private final ConfigManager config;

    public InventoryClickListener(DropPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getPluginConfig();
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Component expectedTitle = LegacyComponentSerializer.legacyAmpersand().deserialize(config.getGuiName());
        Component actualTitle = event.getView().title();

        if (actualTitle == null || !actualTitle.equals(expectedTitle)) return;

        User user = plugin.getUserManager().getUser(player);
        if (user == null) return;

        ItemStack clickedItem = event.getCurrentItem();
        event.setCancelled(true);

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        ItemMeta meta = clickedItem.getItemMeta();
        Component itemDisplayName = meta.displayName();

        if (itemDisplayName == null) return;

        String itemName = LegacyComponentSerializer.legacyAmpersand().serialize(itemDisplayName);

        handleItemClick(player, user, itemName);
    }

    private void handleItemClick(Player player, User user, String itemName) {
        for (Drop drop : plugin.getDropManager().getDropList()) {
            String dropName = ChatUtils.colour(config.getGuiItemName().replace("{NAME}", drop.getName()));
            if (itemName.equalsIgnoreCase(dropName)) {
                handleIndividualDrop(player, user, drop);
                return;
            }
        }
        if (itemName.equalsIgnoreCase(ChatUtils.colour(config.getEnableAllName()))) {
            handleEnableAllDrops(player, user);
            return;
        }
        if (itemName.equalsIgnoreCase(ChatUtils.colour(config.getDisableAllName()))) {
            handleDisableAllDrops(player, user);
            return;
        }
        if (itemName.equalsIgnoreCase(ChatUtils.colour(config.getGuiMessagesName()))) {
            user.setMessages(!user.isMessages());
            player.openInventory(plugin.getDropMenu().createInventory(player));
            return;
        }
        if (itemName.equalsIgnoreCase(ChatUtils.colour(config.getGuiCobbleName()))) {
            user.setCobble(!user.isCobble());
            player.openInventory(plugin.getDropMenu().createInventory(player));
        }
    }

    private void handleIndividualDrop(Player player, User user, Drop drop) {
        if (user.isDisabled(drop)) {
            user.removeDisabledDrop(drop);
        } else {
            user.addDisabledDrop(drop);
        }
        player.openInventory(plugin.getDropMenu().createInventory(player));
    }

    private void handleEnableAllDrops(Player player, User user) {
        for (Drop drop : plugin.getDropManager().getDropList()) {
            if (user.isDisabled(drop)) {
                user.removeDisabledDrop(drop);
            }
        }
        player.openInventory(plugin.getDropMenu().createInventory(player));
    }

    private void handleDisableAllDrops(Player player, User user) {
        for (Drop drop : plugin.getDropManager().getDropList()) {
            if (!user.isDisabled(drop)) {
                user.addDisabledDrop(drop);
            }
        }
        player.openInventory(plugin.getDropMenu().createInventory(player));
    }
}