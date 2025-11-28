package pl.tenfajnybartek.dropplugin.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public final class ItemBuilder {
    private final ItemStack itemStack;
    private final ItemMeta itemMeta;
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public ItemBuilder(Material material) {
        if (material == null) throw new IllegalArgumentException("Material cannot be null");
        this.itemStack = new ItemStack(material);
        this.itemMeta = this.itemStack.getItemMeta();
    }

    public ItemBuilder(ItemStack itemStack) {
        if (itemStack == null) throw new IllegalArgumentException("ItemStack cannot be null");
        this.itemStack = itemStack;
        this.itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder(Material material, int stack) {
        if (material == null) throw new IllegalArgumentException("Material cannot be null");
        this.itemStack = new ItemStack(material, Math.max(1, stack));
        this.itemMeta = this.itemStack.getItemMeta();
    }

    public ItemBuilder(Material material, int stack, int data) {
        if (material == null) throw new IllegalArgumentException("Material cannot be null");
        this.itemStack = new ItemStack(material, Math.max(1, stack));
        this.itemMeta = this.itemStack.getItemMeta();

        if (this.itemMeta instanceof Damageable) {
            Damageable damageable = (Damageable) this.itemMeta;
            damageable.setDamage(data);
            this.itemStack.setItemMeta(this.itemMeta);
        }
    }

    private void refreshMeta() {
        this.itemStack.setItemMeta(this.itemMeta);
    }

    public ItemBuilder setName(String name) {
        if (name == null) return this;
        Component comp = LEGACY_SERIALIZER.deserialize(ChatUtils.colour(name));
        // Paper API exposes displayName(Component)
        this.itemMeta.displayName(comp);
        this.refreshMeta();
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        if (lore == null) return this;
        List<Component> components = lore.stream()
                .map(ChatUtils::colour)
                .map(LEGACY_SERIALIZER::deserialize)
                .collect(Collectors.toList());
        this.itemMeta.lore(components);
        this.refreshMeta();
        return this;
    }

    public ItemBuilder addEnchant(Enchantment enchant, int level) {
        if (enchant == null) return this;
        this.itemMeta.addEnchant(enchant, level, true);
        this.refreshMeta();
        return this;
    }

    public ItemBuilder setFlag(ItemFlag flag) {
        if (flag == null) return this;
        this.itemMeta.addItemFlags(flag);
        this.refreshMeta();
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        this.itemStack.setAmount(Math.max(1, amount));
        return this;
    }

    public ItemStack build() {
        this.itemStack.setItemMeta(this.itemMeta);
        return this.itemStack;
    }

    public ItemMeta getMeta() {
        return this.itemMeta;
    }
}
