package dev.taczcompat.controllable;

import com.mrcrayfish.controllable.client.binding.IBindingContext;
import com.tacz.guns.api.item.IGun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/** Active while in-game (no screen open), not spectating, and holding a TACZ gun in the main hand. */
public final class GunContext implements IBindingContext {
    public static final GunContext INSTANCE = new GunContext();

    private GunContext() {}

    @Override
    public boolean isActive() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        return mc.screen == null && player != null && !player.isSpectator()
                && !VehicleSupport.isRidingVehicle() && IGun.mainHandHoldGun(player);
    }

    /** Only conflicts with itself, so it never shows up as a clash against vanilla or TACZ's own bindings. */
    @Override
    public boolean conflicts(IBindingContext other) {
        return other == this;
    }
}
