package dev.taczcompat.controllable;

import com.mrcrayfish.controllable.client.Action;
import com.mrcrayfish.controllable.client.OverlayHandler;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import com.mrcrayfish.controllable.client.input.Buttons;
import com.mrcrayfish.controllable.client.overlay.ActionHintOverlay;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Controllable 0.21.9 declares ControllerEvents.GATHER_ACTIONS but never posts it, so the hint list cannot be
 * edited through the API. ActionHintOverlay rebuilds a private Map<Integer, Action> (button -> hint) once per
 * tick; this edits that map by reflection every frame, before the overlay is drawn: while a gun is held the
 * vanilla "Attack" / "Use Item" hints are swapped for Shoot / Aim / Reload (and optionally more), and while
 * riding a vehicle-mod vehicle for Accelerate / Brake / Handbrake / Horn / Exit.
 * If reflection fails it logs once and gives up; nothing else is affected.
 */
final class GunHints {
    private static Map<Integer, Action> hints;
    private static boolean failed;

    static void frame() {
        if (failed || !CompatBindings.isRegistered()) {
            return;
        }
        boolean vehicle = VehicleSupport.isRidingVehicle();
        if (!vehicle && !GunContext.INSTANCE.isActive()) {
            return;
        }
        Map<Integer, Action> map = hints();
        if (map == null) {
            return;
        }
        int attack = ButtonBindings.ATTACK.getButton();
        int use = ButtonBindings.USE_ITEM.getButton();
        if (!map.containsKey(attack) && !map.containsKey(use)) {
            return; // hints hidden, or nothing is being shown for the click buttons
        }
        map.remove(attack);
        map.remove(use);

        if (vehicle) {
            putRaw(map, Buttons.RIGHT_TRIGGER, "hint.accelerate");
            putRaw(map, Buttons.LEFT_TRIGGER, "hint.brake");
            if (VehicleSupport.isLandVehicle()) {
                putRaw(map, Buttons.RIGHT_BUMPER, "hint.handbrake");
            }
            put(map, CompatBindings.VEHICLE_HORN, "vehicle_horn");
            put(map, CompatBindings.VEHICLE_EXIT, "vehicle_exit");
            return;
        }
        put(map, CompatBindings.SHOOT, "shoot");
        put(map, CompatBindings.AIM, "aim");
        put(map, CompatBindings.RELOAD, "reload");
        if (!vehicle && CompatConfig.EXTRA_HINTS.get()) {
            put(map, CompatBindings.FIRE_MODE, "fire_mode");
            put(map, CompatBindings.ATTACHMENTS, "attachments");
            put(map, CompatBindings.MELEE, "melee");
            put(map, CompatBindings.INSPECT, "inspect");
        }
    }

    private static void putRaw(Map<Integer, Action> map, int button, String langKey) {
        map.put(button, new Action(Component.translatable(TaczControllableCompat.MOD_ID + "." + langKey), Action.Side.RIGHT));
    }

    private static void put(Map<Integer, Action> map, ButtonBinding binding, String id) {
        if (binding.getButton() >= 0) {
            map.put(binding.getButton(), new Action(
                    Component.translatable(TaczControllableCompat.MOD_ID + ".binding." + id), Action.Side.RIGHT));
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Integer, Action> hints() {
        if (hints != null) {
            return hints;
        }
        try {
            Field overlays = OverlayHandler.class.getDeclaredField("OVERLAYS");
            overlays.setAccessible(true);
            for (Object overlay : (List<?>) overlays.get(null)) {
                if (overlay instanceof ActionHintOverlay) {
                    Field actions = ActionHintOverlay.class.getDeclaredField("actions");
                    actions.setAccessible(true);
                    hints = (Map<Integer, Action>) actions.get(overlay);
                    return hints;
                }
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            failed = true;
            TaczControllableCompat.LOGGER.error("[{}] Could not access Controllable's hint overlay; gun hints disabled.",
                    TaczControllableCompat.MOD_ID, e);
        }
        return null;
    }

    private GunHints() {}
}
