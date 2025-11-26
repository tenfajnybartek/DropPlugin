package pl.tenfajnybartek.dropplugin.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import pl.tenfajnybartek.dropplugin.base.DropPlugin;

import java.util.LinkedList;
import java.util.List;

public final class ParserUtils {
    private ParserUtils() {}

    public static ItemStack parseItemStack(final String string) {
        if (string == null || string.isBlank()) return null;

        // Domyślny item, będzie nadpisany przez material jeśli podane
        final ItemStack is = new ItemStack(Material.DIAMOND);
        final ItemMeta im = is.getItemMeta();

        final String[] args = string.split(" ");
        for (final String arg : args) {
            if (arg == null || arg.isBlank()) continue;
            final String[] splitArg = arg.split(":", 2);
            if (splitArg.length < 2) continue;
            final String key = splitArg[0].trim();
            final String value = splitArg[1].trim();
            if (key.equalsIgnoreCase("material")) {
                final Material mat = Material.matchMaterial(value);
                if (mat != null) {
                    is.setType(mat);
                }
            } else if (key.equalsIgnoreCase("amount")) {
                try {
                    int amt = Integer.parseInt(value);
                    is.setAmount(Math.max(1, amt));
                } catch (NumberFormatException ignored) {}
            } else if (key.equalsIgnoreCase("name")) {
                if (im != null) {
                    Component comp = ChatUtils.toComponent(value.replace("_", " "));
                    im.displayName(comp);
                }
            } else if (key.equalsIgnoreCase("lore")) {
                if (im != null) {
                    List<Component> loreComp = new LinkedList<>();
                    String[] splitLore = value.split("@nl");
                    for (String s : splitLore) {
                        loreComp.add(ChatUtils.toComponent(s.replace("_", " ")));
                    }
                    im.lore(loreComp);
                }
            } else if (key.equalsIgnoreCase("data") || key.equalsIgnoreCase("durability")) {
                try {
                    int data = Integer.parseInt(value);
                    if (im instanceof Damageable) {
                        ((Damageable) im).setDamage(data);
                    }
                } catch (NumberFormatException ignored) {}
            } else if (key.equalsIgnoreCase("enchants")) {
                String[] enchantmentArray = value.split("@nl");
                for (String s : enchantmentArray) {
                    if (s == null || s.isBlank()) continue;
                    String[] enchantmentSplit = s.split(";", 2);
                    if (enchantmentSplit.length < 2) continue;
                    String enchKey = enchantmentSplit[0].trim();
                    String levelStr = enchantmentSplit[1].trim();
                    Enchantment ench = null;
                    try {
                        NamespacedKey nsKey = new NamespacedKey(DropPlugin.getPlugin(DropPlugin.class), enchKey);
                        ench = Enchantment.getByKey(nsKey);
                    } catch (Throwable t) {
                        // ignore - spróbujemy jeszcze getByName
                        ench = Enchantment.getByName(enchKey.toUpperCase());
                    }
                    try {
                        int level = Integer.parseInt(levelStr);
                        if (ench != null && im != null) {
                            im.addEnchant(ench, level, true);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        if (im != null) is.setItemMeta(im);
        return is;
    }
}