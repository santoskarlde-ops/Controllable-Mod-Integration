package dev.taczcompat.controllable;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.gui.GunRefitScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

/** TACZ has no controller entry point for the refit screen, so this repeats what RefitKey does on key press. */
final class AttachmentsAction {

    static boolean tryOpen() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || player.isSpectator() || mc.screen != null || !IGun.mainHandHoldGun(player)) {
            return false;
        }
        ItemStack stack = player.getMainHandItem();
        IGun gun = IGun.getIGunOrNull(stack);
        if (gun != null && gun.hasAttachmentLock(stack)) {
            return false;
        }
        mc.setScreen(new GunRefitScreen());
        return true;
    }

    private AttachmentsAction() {}
}
