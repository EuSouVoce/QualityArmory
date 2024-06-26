package me.zombie_striker.qg.guns.chargers;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.zombie_striker.qg.QAMain;
import me.zombie_striker.qg.api.QualityArmory;
import me.zombie_striker.qg.guns.Gun;
import me.zombie_striker.qg.guns.utils.GunUtil;
import me.zombie_striker.qg.guns.utils.WeaponSounds;

public class BurstFireCharger implements ChargingHandler {

    public static HashMap<UUID, BukkitTask> shooters = new HashMap<>();

    public BurstFireCharger() { ChargingManager.add(this); }

    @Override
    public boolean isCharging(final Player player) { return BurstFireCharger.shooters.containsKey(player.getUniqueId()); }

    @Override
    public boolean shoot(final Gun g, final Player player, final ItemStack stack) {
        GunUtil.shootHandler(g, player, 1);
        // final AttachmentBase attach = QualityArmory.getGunWithAttchments(stack);
        GunUtil.playShoot(g, player);
        BurstFireCharger.shooters.put(player.getUniqueId(), new BukkitRunnable() {
            int slotUsed = player.getInventory().getHeldItemSlot();
            boolean offhand = QualityArmory.isIronSights(player.getInventory().getItemInMainHand());
            int shotCurrently = 1;

            @Override
            public void run() {
                int slot;
                if (this.offhand) {
                    slot = -1;
                } else {
                    slot = player.getInventory().getHeldItemSlot();
                }

                int amount = Gun.getAmount(player);
                if (this.shotCurrently >= g.getBulletsPerShot() || this.slotUsed != player.getInventory().getHeldItemSlot()
                        || amount <= 0) {
                    if (BurstFireCharger.shooters.containsKey(player.getUniqueId()))
                        BurstFireCharger.shooters.remove(player.getUniqueId()).cancel();
                    return;
                }

                GunUtil.shootHandler(g, player, 1);
                GunUtil.playShoot(g, player);
                if (QAMain.enableRecoil && g.getRecoil() > 0) {
                    GunUtil.addRecoil(player, g);
                }
                this.shotCurrently++;
                amount--;

                if (amount < 0)
                    amount = 0;

                // if (QAMain.enableVisibleAmounts) {
                // stack.setAmount(amount > 64 ? 64 : amount == 0 ? 1 : amount);
                // }
                final ItemMeta im = stack.getItemMeta();
                Gun.updateAmmo(g, player, amount);
                stack.setItemMeta(im);
                if (slot == -1) {
                    try {
                        if (QualityArmory.isIronSights(player.getInventory().getItemInMainHand())) {
                            if (QualityArmory.isGun(player.getInventory().getItemInOffHand()))
                                player.getInventory().setItemInOffHand(stack);
                        }
                    } catch (final Error e) {
                    }
                } else {
                    if (QualityArmory.isGun(player.getInventory().getItem(slot)))
                        player.getInventory().setItem(slot, stack);
                }
                QualityArmory.sendHotbarGunAmmoCount(player, g, stack, false);
            }
        }.runTaskTimer(QAMain.getInstance(), 10 / g.getFireRate(), 10 / g.getFireRate()));
        return false;
    }

    @Override
    public String getName() {

        return ChargingManager.BURSTFIRE;
    }

    @Override
    public String getDefaultChargingSound() {
        return WeaponSounds.RELOAD_BULLET.getSoundName();
        // g.getChargingSound()
    }

}
