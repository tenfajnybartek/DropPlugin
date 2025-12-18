package pl.tenfajnybartek.dropplugin.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import pl.tenfajnybartek.dropplugin.base.DropPlugin;
import pl.tenfajnybartek.dropplugin.managers.ConfigManager;
import pl.tenfajnybartek.dropplugin.managers.DropManager;
import pl.tenfajnybartek.dropplugin.managers.UserManager;
import pl.tenfajnybartek.dropplugin.objects.Chance;
import pl.tenfajnybartek.dropplugin.objects.Drop;
import pl.tenfajnybartek.dropplugin.objects.User;

import java.util.Objects;
import java.util.stream.Collectors;

public class DropMenu {
    private final DropManager dropManager;
    private final UserManager userManager;
    private final ConfigManager config;

    public DropMenu(DropPlugin plugin) {
        this.dropManager = plugin.getDropManager();
        this.userManager = plugin.getUserManager();
        this.config = plugin.getPluginConfig();
    }

    public Inventory createInventory(Player player) {
        int size = this.config.getGuiSize();
        if (size < 9) size = 9;
        if (size % 9 != 0) size = (size / 9) * 9;

        Component title = LegacyComponentSerializer.legacyAmpersand().deserialize(this.config.getGuiName());
        Inventory inventory = Bukkit.createInventory(null, size, title);

        User user = this.userManager.getUser(player);

        ItemStack cobble = this.config.getCobbleItem()
                .setName(this.config.getGuiCobbleName())
                .setLore(this.config.getGuiCobbleLores().stream()
                        .map(entry -> this.replaced(user, entry, 1))
                        .collect(Collectors.toList()))
                .build();

        ItemStack messages = this.config.getMessagesItem()
                .setName(this.config.getGuiMessagesName())
                .setLore(this.config.getGuiMessagesLores().stream()
                        .map(entry -> this.replaced(user, entry, 2))
                        .collect(Collectors.toList()))
                .build();

        ItemStack turbos = this.config.getTurboItem()
                .setName(this.config.getGuiTurboName())
                .setLore(this.config.getGuiTurboLores().stream()
                        .map(entry -> this.replaced(user, entry, 3))
                        .collect(Collectors.toList()))
                .build();

        ItemStack level = this.config.getLevelItem()
                .setName(this.config.getGuiLevelName())
                .setLore(this.config.getGuiLevelLores().stream()
                        .map(entry -> this.replaced(user, entry, 4))
                        .collect(Collectors.toList()))
                .build();

        ItemStack enableAll = this.config.getEnableItemAll()
                .setName(this.config.getEnableAllName())
                .setLore(this.config.getEnableAllLores())
                .build();

        ItemStack disableAll = this.config.getDisableItemAll()
                .setName(this.config.getDisableAllName())
                .setLore(this.config.getDisableAllLores())
                .build();

        ItemStack filler = this.config.getFillerItem()
                .setName(this.config.getGuiFillerName())
                .build();

        for (Drop drop : this.dropManager.getDropList()) {
            int playerLevel = (user != null) ? user.getLvl() : 1;
            boolean isLocked = !drop.isUnlocked(playerLevel);
            
            ItemBuilder item;
            if (isLocked) {
                // Zablokowany drop - konfigurowalny wygląd z config.yml
                item = this.config.getLockedItem()
                        .setName(this.config.getGuiLockedName())
                        .setLore(this.config.getGuiLockedLores().stream()
                                .map(entry -> entry
                                        .replace("{DROP-NAME}", drop.getName())
                                        .replace("{NEEDED-LEVEL}", String.valueOf(drop.getNeededLevel()))
                                        .replace("{PLAYER-LEVEL}", String.valueOf(playerLevel)))
                                .collect(Collectors.toList()));
            } else {
                // Odblokowany drop - normalne wyświetlanie
                item = new ItemBuilder(drop.getItemStack().getType())
                        .setName(StringUtils.replace(this.config.getGuiItemName(), "{NAME}", drop.getName()))
                        .setLore(this.config.getGuiItemLores().stream()
                                .map(entry -> this.replaced(user, drop, entry))
                                .collect(Collectors.toList()));

                if (this.config.isEnchanted()) {
                    boolean enabledForUser = !(user != null && user.isDisabled(drop));
                    if (enabledForUser) {
                        item.addEnchant(Enchantment.UNBREAKING, 1);
                        item.setFlag(ItemFlag.HIDE_ENCHANTS);
                    }
                }
            }

            inventory.addItem(item.build());
        }

        if (this.config.isFillerStatus()) {
            for (int x = 0; x < size; ++x) {
                if (inventory.getItem(x) != null) continue;
                inventory.setItem(x, filler);
            }
        }

        if (this.config.isEnableAllStatus()) {
            int slot = this.config.getEnableAllSlot();
            if (slot >= 0 && slot < size) inventory.setItem(slot, enableAll);
        }

        if (this.config.isDisableAllStatus()) {
            int slot = this.config.getDisableAllSlot();
            if (slot >= 0 && slot < size) inventory.setItem(slot, disableAll);
        }

        if (this.config.isTurboStatus()) {
            int slot = this.config.getTurboSlot();
            if (slot >= 0 && slot < size) inventory.setItem(slot, turbos);
        }

        if (this.config.isMessagesStatus()) {
            int slot = this.config.getMessagesSlot();
            if (slot >= 0 && slot < size) inventory.setItem(slot, messages);
        }

        if (this.config.isCobbleStatus()) {
            int slot = this.config.getCobbleSlot();
            if (slot >= 0 && slot < size) inventory.setItem(slot, cobble);
        }

        if (this.config.isLevelStatus()) {
            int slot = this.config.getLevelSlot();
            if (slot >= 0 && slot < size) inventory.setItem(slot, level);
        }

        return inventory;
    }

    private String replaced(User user, Drop drop, String entry) {
        double bonus = 0.0;
        Player viewer = null;
        if (user != null) {
            viewer = user.getPlayer();
        }

        for (Chance chancee : this.config.getChances().values()) {
            if (viewer != null && viewer.hasPermission(chancee.getPerm())) {
                // W nowym formacie wartość jest w procentach, więc dzielimy przez 100
                bonus += Objects.requireNonNullElse(chancee.getChance(), 0.0) / 100.0;
            }
        }

        double sumChance = drop.getChance() + bonus;
        if (this.config.isTurboDrop() || (user != null && user.isTurboDrop())) {
            sumChance *= 2.0;
        }

        entry = StringUtils.replace(entry, "{CHANCE}", String.valueOf(drop.getChance()));
        entry = StringUtils.replace(entry, "{CHANCE-BONUS}", String.valueOf(bonus));
        entry = StringUtils.replace(entry, "{CHANCE-SUM}", String.valueOf(sumChance));
        entry = StringUtils.replace(entry, "{HEIGHT-MIN}", String.valueOf(drop.getHeight().getMin()));
        entry = StringUtils.replace(entry, "{HEIGHT-MAX}", String.valueOf(drop.getHeight().getMax()));
        entry = StringUtils.replace(entry, "{EXP}", String.valueOf(drop.getExp()));
        entry = StringUtils.replace(entry, "{AMOUNT-MIN}", String.valueOf(drop.getAmount().getMin()));
        entry = StringUtils.replace(entry, "{AMOUNT-MAX}", String.valueOf(drop.getAmount().getMax()));
        entry = StringUtils.replace(entry, "{POINTS-MIN}", String.valueOf(drop.getPoints().getMin()));
        entry = StringUtils.replace(entry, "{POINTS-MAX}", String.valueOf(drop.getPoints().getMax()));
        entry = StringUtils.replace(entry, "{STATUS}", (user != null && user.getDisabledDrops().contains(drop.getName())) ? this.config.getGuiStatusOff() : this.config.getGuiStatusOn());
        entry = StringUtils.replace(entry, "{FORTUNE}", drop.isFortune() ? this.config.getGuiStatusOn() : this.config.getGuiStatusOff());
        entry = StringUtils.replace(entry, "{MINED}", Integer.toString(user != null ? user.getDrop(drop.getName()) : 0));
        return entry;
    }

    public String replaced(User user, String entry, int type) {
        switch (type) {
            case 1: {
                entry = StringUtils.replace(entry, "{STATUS}", (user != null && user.isCobble()) ? this.config.getGuiStatusOn() : this.config.getGuiStatusOff());
                return entry;
            }
            case 2: {
                entry = StringUtils.replace(entry, "{STATUS}", (user != null && user.isMessages()) ? this.config.getGuiStatusOn() : this.config.getGuiStatusOff());
                return entry;
            }
            case 3: {
                entry = StringUtils.replace(entry, "{DROP-STATUS}", this.config.isTurboDrop() ? this.config.getGuiStatusOn() : this.config.getGuiStatusOff());
                entry = StringUtils.replace(entry, "{DROP-EXPIRE}", this.config.isTurboDrop() ? DataUtils.durationToString(this.config.getTurboDrop()) : "0s.");
                entry = StringUtils.replace(entry, "{EXP-STATUS}", this.config.isTurboExp() ? this.config.getGuiStatusOn() : this.config.getGuiStatusOff());
                entry = StringUtils.replace(entry, "{EXP-EXPIRE}", this.config.isTurboExp() ? DataUtils.durationToString(this.config.getTurboExp()) : "0s.");
                entry = StringUtils.replace(entry, "{ME-DROP-STATUS}", (user != null && user.isTurboDrop()) ? this.config.getGuiStatusOn() : this.config.getGuiStatusOff());
                entry = StringUtils.replace(entry, "{ME-DROP-EXPIRE}", (user != null && user.isTurboDrop()) ? DataUtils.durationToString(user.getTurboDrop()) : "0s.");
                entry = StringUtils.replace(entry, "{ME-EXP-STATUS}", (user != null && user.isTurboExp()) ? this.config.getGuiStatusOn() : this.config.getGuiStatusOff());
                entry = StringUtils.replace(entry, "{ME-EXP-EXPIRE}", (user != null && user.isTurboExp()) ? DataUtils.durationToString(user.getTurboExp()) : "0s.");
                return entry;
            }
            case 4: {
                entry = StringUtils.replace(entry, "{LVL}", String.valueOf(user != null ? user.getLvl() : 0));
                entry = StringUtils.replace(entry, "{PKT}", String.valueOf(user != null ? user.getPoints() : 0));
                entry = StringUtils.replace(entry, "{POINTS-TO-LEVEL}", String.valueOf(user != null ? user.getPointsToNextLevel() : 0));
                return entry;
            }
        }
        return entry;
    }
}