package me.zombie_striker.qg.miscitems;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.zombie_striker.customitemmanager.ArmoryBaseObject;
import me.zombie_striker.customitemmanager.CustomBaseObject;
import me.zombie_striker.customitemmanager.CustomItemManager;
import me.zombie_striker.customitemmanager.MaterialStorage;
import me.zombie_striker.qg.QAMain;
import me.zombie_striker.qg.api.QualityArmory;
import me.zombie_striker.qg.handlers.BulletWoundHandler;
import me.zombie_striker.qg.handlers.HotbarMessager;

public class MedKit extends CustomBaseObject implements ArmoryBaseObject {

    List<UUID> medkitHeartUsage = new ArrayList<>();
    HashMap<UUID, Long> lastTimeHealed = new HashMap<>();
    HashMap<UUID, Double> PercentTimeHealed = new HashMap<>();

    public MedKit(final MaterialStorage ms, final String name, final String displayname, final ItemStack[] ings, final int cost) {
        super(name, ms, displayname, null, false);
        super.setIngredients(ings);
        this.setPrice(cost);
    }

    @Override
    public int getCraftingReturn() { return 1; }

    @Override
    public boolean is18Support() { return false; }

    @Override
    public void set18Supported(final boolean b) {}

    @SuppressWarnings("deprecation")
    @Override
    public boolean onRMB(final Player e, final ItemStack usedItem) {
        final Player healer = e.getPlayer();
        if (!BulletWoundHandler.bloodLevel.containsKey(healer.getUniqueId())) {

            if (healer.getHealth() < healer.getMaxHealth()) {

                if (!this.lastTimeHealed.containsKey(healer.getUniqueId())
                        || System.currentTimeMillis() - this.lastTimeHealed.get(healer.getUniqueId()) > 1500) {
                    this.PercentTimeHealed.put(healer.getUniqueId(), 0.0);
                }
                this.lastTimeHealed.put(healer.getUniqueId(), System.currentTimeMillis());

                final double percent = (100.0 / 3) / QAMain.S_MEDKIT_HEALDELAY;

                final double p2 = this.PercentTimeHealed.get(healer.getUniqueId());

                if (p2 + percent < 100) {
                    this.PercentTimeHealed.put(healer.getUniqueId(), p2 + percent);
                } else {
                    healer.playSound(healer.getLocation(), this.getSoundOnEquip(), 1, 1);
                    healer.setHealth(Math.min(healer.getMaxHealth(), healer.getHealth() + QAMain.S_MEDKIT_HEAL_AMOUNT));
                    this.PercentTimeHealed.remove(healer.getUniqueId());
                    this.lastTimeHealed.remove(healer.getUniqueId());
                    /*
                     * try { HotbarMessager.sendHotBarMessage(healer, Main.S_MEDKIT_HEALINGHEARTS);
                     * } catch (Error | Exception e5) { }
                     */

                    /*
                     * medkitHeartUsage.add(healer.getUniqueId()); new BukkitRunnable() {
                     * @Override public void run() { medkitHeartUsage.remove(healer.getUniqueId());
                     * } }.runTaskLater(Main.getInstance(), (long) (20 * Main.S_MEDKIT_HEARTDELAY));
                     */
                }

                final int totalBars = 25;
                final double percentLoss = (p2 + percent) / 100;
                final int healthBars = Math.min((int) (percentLoss * totalBars), totalBars);

                final StringBuilder levelbar = new StringBuilder();
                levelbar.append(ChatColor.WHITE);
                levelbar.append(QualityArmory.repeat(":", healthBars));
                levelbar.append(ChatColor.BLACK);
                levelbar.append(QualityArmory.repeat(":", totalBars - healthBars));
                try {
                    HotbarMessager.sendHotBarMessage(healer, ChatColor.RED + "[" + levelbar.toString() + ChatColor.RED + "] "
                            + new DecimalFormat("##0.#").format((p2 + percent)) + " percent!");
                } catch (final Exception e2) {
                }

            } else {
                try {
                    HotbarMessager.sendHotBarMessage(healer, QAMain.S_FULLYHEALED);
                } catch (Error | Exception e5) {
                }
            }
            return true;
        }
        final double bloodlevel = BulletWoundHandler.bloodLevel.get(healer.getUniqueId());
        final double percentBlood = Math.max(0, bloodlevel / QAMain.bulletWound_initialbloodamount);

        final ChatColor severity = percentBlood > 75 ? ChatColor.WHITE
                : percentBlood > 50 ? ChatColor.GRAY : percentBlood > 25 ? ChatColor.RED : ChatColor.DARK_RED;
        if (BulletWoundHandler.bleedoutMultiplier.containsKey(healer.getUniqueId())
                && BulletWoundHandler.bleedoutMultiplier.get(healer.getUniqueId()) < 0)
            BulletWoundHandler.bleedoutMultiplier.put(healer.getUniqueId(), Math.min(0,
                    BulletWoundHandler.bleedoutMultiplier.get(healer.getUniqueId()) + QAMain.bulletWound_MedkitBloodlossHealRate));

        final double newRate = BulletWoundHandler.bleedoutMultiplier.containsKey(healer.getUniqueId())
                ? BulletWoundHandler.bleedoutMultiplier.get(healer.getUniqueId())
                : 0;

        try {
            final int totalBars = 25;
            final int healthBars = (int) (percentBlood * totalBars);

            final StringBuilder levelbar = new StringBuilder();
            levelbar.append(severity);
            levelbar.append(QualityArmory.repeat(":", healthBars));
            levelbar.append(ChatColor.BLACK);
            levelbar.append(QualityArmory.repeat(":", totalBars - healthBars));

            HotbarMessager.sendHotBarMessage(healer,
                    ChatColor.RED + QAMain.S_MEDKIT_HEALING + "[" + levelbar.toString() + ChatColor.RED + "] " + QAMain.S_MEDKIT_BLEEDING
                            + " " + (newRate < 0 ? ChatColor.DARK_RED : ChatColor.GRAY) + new DecimalFormat("##0.##").format(newRate)
                            + ChatColor.GRAY + "+" + QAMain.bulletWound_BloodIncreasePerSecond);
        } catch (Error | Exception e5) {
        }
        return true;
    }

    @Override
    public boolean onShift(final Player shooter, final ItemStack usedItem, final boolean toggle) { return false; }

    @Override
    public boolean onLMB(final Player e, final ItemStack usedItem) {
        return false;
        // TODO Auto-generated method stub

    }

    @Override
    public ItemStack getItemStack() {
        return CustomItemManager.getItemType("gun").getItem(this.getItemData().getMat(), this.getItemData().getData(),
                this.getItemData().getVariant());
    }

    @Override
    public boolean onSwapTo(final Player shooter, final ItemStack usedItem) {
        if (this.getSoundOnEquip() != null)
            shooter.getWorld().playSound(shooter.getLocation(), this.getSoundOnEquip(), 1, 1);
        return false;
    }

    @Override
    public boolean onSwapAway(final Player shooter, final ItemStack usedItem) { return false; }
}
