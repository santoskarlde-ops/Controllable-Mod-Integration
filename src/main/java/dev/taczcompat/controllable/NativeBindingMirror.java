package dev.taczcompat.controllable;

import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.tacz.guns.compat.controllable.ControllableInner;

/**
 * TACZ 1.1.8 ships its own Controllable bridge (ControllableInner) that already calls its controller
 * entry points (ShootKey.shootControllerTick, AimKey.onAimControllerPress, ReloadKey.onReloadControllerPress,
 * MeleeKey.onMeleeControllerPress, FireSelectKey.onFireSelectControllerPress, InspectKey.onInspectControllerPress).
 * Rather than calling them a second time, this copies our bindings' buttons onto TACZ's, so there is exactly
 * one handler per action and shoot (which TACZ polls every tick) reads the same button as our binding.
 * If TACZ's class is missing or changed, mirroring switches itself off after logging once.
 */
final class NativeBindingMirror {
    private static boolean failed;
    /** While riding a vehicle, TACZ's gun bindings are unbound so RT/LT only drive the vehicle. */
    private static boolean parked;

    static void sync() {
        if (failed || !CompatBindings.isRegistered() || !CompatConfig.MIRROR_NATIVE_BINDINGS.get()) {
            return;
        }
        try {
            parked = VehicleSupport.isRidingVehicle();
            push(CompatBindings.SHOOT, ControllableInner.SHOOT);
            push(CompatBindings.AIM, ControllableInner.AIM);
            push(CompatBindings.RELOAD, ControllableInner.RELOAD);
            push(CompatBindings.MELEE, ControllableInner.MELEE);
            push(CompatBindings.FIRE_MODE, ControllableInner.FIRE_SELECT);
            push(CompatBindings.INSPECT, ControllableInner.INSPECT);
        } catch (Throwable t) {
            failed = true;
            TaczControllableCompat.LOGGER.error(
                    "[{}] TACZ's built-in Controllable bindings not found; mirroring disabled. "
                            + "Shoot/aim/reload will only work through TACZ's own bindings.",
                    TaczControllableCompat.MOD_ID, t);
        }
    }

    private static void push(ButtonBinding ours, ButtonBinding tacz) {
        int wanted = parked ? -1 : ours.getButton();
        if (tacz.getButton() != wanted) {
            tacz.setButton(wanted);
        }
    }

    private NativeBindingMirror() {}
}
