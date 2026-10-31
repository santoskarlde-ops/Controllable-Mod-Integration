package dev.taczcompat.controllable;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.ForgeConfigSpec;

import java.lang.reflect.Field;

/**
 * Support for MrCrayfish's Vehicle Mod (checked against vehicle 0.45.3-1.20.1, unofficial 1.20.1 port).
 * That mod already reads the controller itself, with hard-coded buttons (RT/LT analog for gas and brake when its
 * "useTriggers" client option is on, left stick to steer, RB handbrake, R3 horn). Nothing here needs the vehicle
 * jar at compile time: vehicles are recognised by class name and the few hooks are reached by reflection.
 *  - useTriggers is switched on while riding (it defaults to off, which makes A/B the gas/reverse buttons);
 *  - the horn is driven through the vehicle mod's own KEY_HORN key mapping, so it can sit on any button;
 *  - "exit vehicle" taps the sneak key, which is how vanilla dismounts.
 */
final class VehicleSupport {
    private static final String ENTITY_PACKAGE = "com.mrcrayfish.vehicle.entity.";

    private static KeyMapping horn;
    private static boolean hornLookedUp;
    private static boolean hornDown;
    private static boolean triggersForced;
    private static boolean triggersFailed;
    private static int exitTicks;

    private static Entity vehicle() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? null : player.getVehicle();
    }

    static boolean isRidingVehicle() {
        Entity vehicle = vehicle();
        return vehicle != null && vehicle.getClass().getName().startsWith(ENTITY_PACKAGE);
    }

    /** Cars, bikes, boats... (the vehicles that use the handbrake), as opposed to planes and helicopters. */
    static boolean isLandVehicle() {
        Entity vehicle = vehicle();
        if (vehicle == null) {
            return false;
        }
        for (Class<?> c = vehicle.getClass(); c != null; c = c.getSuperclass()) {
            if (c.getName().equals(ENTITY_PACKAGE + "LandVehicleEntity")) {
                return true;
            }
        }
        return false;
    }

    /** Client tick START. */
    static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !isRidingVehicle()) {
            if (hornDown) {
                setHorn(false);
            }
            triggersForced = false;
            exitTicks = 0;
            return;
        }
        if (CompatConfig.GTA_VEHICLE_CONTROLS.get() && !triggersForced && !triggersFailed) {
            forceTriggers();
        }
        if (exitTicks > 0) {
            mc.options.keyShift.setDown(true);
            if (--exitTicks == 0) {
                mc.options.keyShift.setDown(false);
            }
        }
    }

    static void requestExit() {
        exitTicks = 3;
    }

    static void setHorn(boolean down) {
        KeyMapping key = horn();
        if (key != null) {
            key.setDown(down);
            hornDown = down;
        }
    }

    private static KeyMapping horn() {
        if (!hornLookedUp) {
            hornLookedUp = true;
            try {
                Class<?> keyBinds = Class.forName("com.mrcrayfish.vehicle.client.init.KeyBinds");
                horn = (KeyMapping) keyBinds.getField("KEY_HORN").get(null);
            } catch (ReflectiveOperationException | RuntimeException e) {
                TaczControllableCompat.LOGGER.warn("[{}] Vehicle horn key not found; vehicle horn binding disabled.",
                        TaczControllableCompat.MOD_ID, e);
            }
        }
        return horn;
    }

    private static void forceTriggers() {
        try {
            Class<?> config = Class.forName("com.mrcrayfish.vehicle.Config");
            Field clientField = config.getDeclaredField("CLIENT");
            clientField.setAccessible(true);
            Object client = clientField.get(null);
            Field useTriggers = client.getClass().getDeclaredField("useTriggers");
            useTriggers.setAccessible(true);
            ((ForgeConfigSpec.BooleanValue) useTriggers.get(client)).set(true);
            triggersForced = true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            triggersFailed = true;
            TaczControllableCompat.LOGGER.warn("[{}] Could not enable the vehicle mod's trigger controls; "
                    + "set useTriggers=true in its client config instead.", TaczControllableCompat.MOD_ID, e);
        }
    }

    private VehicleSupport() {}
}
