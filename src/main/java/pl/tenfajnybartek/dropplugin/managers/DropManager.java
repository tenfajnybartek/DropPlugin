package pl.tenfajnybartek.dropplugin.managers;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import pl.tenfajnybartek.dropplugin.base.DropPlugin;
import pl.tenfajnybartek.dropplugin.objects.Chance;
import pl.tenfajnybartek.dropplugin.objects.Count;
import pl.tenfajnybartek.dropplugin.objects.Drop;
import pl.tenfajnybartek.dropplugin.objects.User;
import pl.tenfajnybartek.dropplugin.utils.ChatUtils;
import pl.tenfajnybartek.dropplugin.utils.ItemUtils;
import pl.tenfajnybartek.dropplugin.utils.ParserUtils;
import pl.tenfajnybartek.dropplugin.utils.RandomUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Zaktualizowana wersja DropManager z poprawką nazwy zmiennej (uniknięcie konfliktu "give").
 */
public class DropManager {
    private final UserManager userManager;
    private final List<Drop> dropList;
    private final ConfigManager config;
    private final int stoneExp;
    private final int obsidianExp;

    public DropManager(DropPlugin plugin) {
        this.userManager = plugin.getUserManager();
        this.dropList = new ArrayList<>();
        this.config = plugin.getConfigManager();
        DropConfigManager dropConfig = plugin.getDropConfig();

        FileConfiguration dropCfg = dropConfig != null ? dropConfig.getDropConfig() : null;
        this.stoneExp = dropCfg != null ? dropCfg.getInt("exps.stone", 0) : 0;
        this.obsidianExp = dropCfg != null ? dropCfg.getInt("exps.obsidian", 0) : 0;

        if (dropCfg != null) {
            ConfigurationSection dropsSection = dropCfg.getConfigurationSection("drops");
            if (dropsSection != null) {
                for (String id : dropsSection.getKeys(false)) {
                    ConfigurationSection section = dropsSection.getConfigurationSection(id);
                    if (section == null) continue;
                    Count amount = Count.parse(section.getString("amount", "1-2"));
                    Count height = Count.parse(section.getString("height", "0-90"));
                    Count points = Count.parse(section.getString("points", "3-7"));
                    double chance = section.getDouble("chance", 5.0);
                    boolean fortune = section.getBoolean("fortune", true);
                    int exp = section.getInt("exp", 3);
                    ItemStack itemStack = ParserUtils.parseItemStack(section.getString("item"));
                    if (itemStack == null) {
                        // fallback na podstawowy materiał
                        itemStack = new ItemStack(Material.STONE);
                    }
                    String name = section.getString("name", "Drop");
                    Drop drop = new Drop(name, fortune, itemStack, chance, height, amount, points, exp);
                    this.addDrop(drop);
                }
            }
        }
    }

    public void addDrop(Drop drop) {
        if (drop == null) return;
        this.dropList.add(drop);
    }

    public void breakBlock(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) return;

        User user = this.userManager.getUser(event.getPlayer());
        if (user == null) return;

        Block block = event.getBlock();
        Material originalMaterial = block.getType(); // zachowaj oryginalny typ zanim zamienimy blok na AIR
        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();

        // EXP dla konkretnych bloków
        if (originalMaterial == Material.OBSIDIAN) {
            int give = this.obsidianExp;
            if (this.config.isTurboExp() || user.isTurboExp()) give *= 2;
            event.getPlayer().giveExp(give);
            return; // koniec obsługi dla obsidianu
        }

        boolean isStoneLike = originalMaterial == Material.STONE ||
                originalMaterial == Material.GRANITE ||
                originalMaterial == Material.DIORITE ||
                originalMaterial == Material.ANDESITE ||
                originalMaterial == Material.DEEPSLATE;

        if (isStoneLike) {
            int give = this.stoneExp;
            if (this.config.isTurboExp() || user.isTurboExp()) give *= 2;
            event.getPlayer().giveExp(give);

            // Usuń blok i anuluj event
            event.setCancelled(true);
            block.setType(Material.AIR);

            // Przywrócenie cobble logiki (silk touch)
            if (user.isCobble()) {
                ItemStack item;
                boolean hasSilk = tool != null && tool.containsEnchantment(Enchantment.SILK_TOUCH);
                if (hasSilk) {
                    // jeśli był silk touch, daj odpowiedni niewłamany blok
                    item = new ItemStack(originalMaterial);
                } else {
                    item = new ItemStack(Material.COBBLESTONE);
                }
                ItemUtils.giveItem(user.getPlayer(), item);
            }

            // Filtrujemy dropy i losujemy
            double playerY = event.getPlayer().getLocation().getY();
            for (Drop drop : this.dropList) {
                // pomin brakujące warunki
                if (user.isDisabled(drop)) continue;
                Count h = drop.getHeight();
                if (h != null) {
                    if (playerY > h.getMax() || playerY < h.getMin()) continue;
                }

                // oblicz bazową szansę - zakładamy, że drop.getChance() zwraca wartość 0.0-1.0
                double chance = drop.getChance();

                // Fortune zwiększa szansę (jeżeli drop ma fortune)
                if (drop.isFortune() && tool != null) {
                    int fortuneLevel = tool.getEnchantments().getOrDefault(Enchantment.FORTUNE, 0);
                    if (fortuneLevel > 0) {
                        chance += (fortuneLevel / 100.0); // traktujemy poziom jako procent (np. 1 -> 0.01)
                    }
                }

                // Turbo drop podwaja szansę
                if (this.config.isTurboDrop() || user.isTurboDrop()) {
                    chance *= 2.0;
                }

                // Szanse z permów z konfiguracji (zakładamy, że wartości w ConfigManager są w formacie "5" -> 5%)
                for (Chance permChance : this.config.getChances().values()) {
                    if (user.getPlayer().hasPermission(permChance.getPerm())) {
                        // dodajemy wartość jako procent -> /100.0
                        Double c = permChance.getChance();
                        if (c != null) chance += c.doubleValue() / 100.0;
                    }
                }

                // finalne losowanie
                if (!RandomUtils.getChance(chance)) continue;

                // jeśli narzędzie się zużywa — przeliczamy
                if (tool != null) {
                    ItemUtils.recalculateDurability(event.getPlayer(), tool);
                }

                int amount = drop.getAmount() != null ? drop.getAmount().random() : 1;
                int exp = drop.getExp() * amount;
                if (this.config.isTurboExp() || user.isTurboExp()) exp *= 2;
                event.getPlayer().giveExp(exp);

                int points = drop.getPoints() != null ? drop.getPoints().random() : 0;
                points *= amount;

                if (user.isMessages()) {
                    String msg = this.config.getDropMessage()
                            .replace("{PKT}", String.valueOf(points))
                            .replace("{EXP}", String.valueOf(exp))
                            .replace("{AMOUNT}", String.valueOf(amount))
                            .replace("{DROP}", drop.getName());
                    ChatUtils.sendMessage(event.getPlayer(), msg);
                }

                user.addPoints(points);
                user.addDrop(drop, amount);

                // dajemy itemy (bez używania przestarzałej durabilities)
                ItemStack toGive = drop.getItemStack().clone(); // renamed variable to avoid shadowing/conflict
                toGive.setAmount(amount);
                ItemUtils.giveItem(event.getPlayer(), toGive);
            }
        }
    }

    public List<Drop> getDropList() {
        return this.dropList;
    }
}