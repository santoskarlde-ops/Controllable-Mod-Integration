package dev.taczcompat.controllable;

import com.mrcrayfish.controllable.client.input.Buttons;
import com.mrcrayfish.controllable.client.input.Controller;

/**
 * Controllable turns each analog trigger into a button with a fixed "value >= 0.5" test and no hysteresis,
 * so a trigger resting near 0.5 produces press/release/press events. TACZ's aim toggles on those events.
 *
 * This runs first in Controllable's INPUT event. For LT/RT edges it checks the live analog value:
 *  - press edge but value < pressThreshold   -> swallow it and keep the stored state "released"
 *  - release edge but value > releaseThreshold -> swallow it and keep the stored state "pressed"
 * Controllable re-checks the raw state every tick, so the swallowed edge re-fires until the value really
 * crosses the threshold. Everything downstream (TACZ, vanilla bindings) only sees clean transitions.
 */
final class TriggerConditioner {

    /** @return true if the event was swallowed. */
    static boolean apply(Controller controller, int button, boolean pressed) {
        if (button != Buttons.LEFT_TRIGGER && button != Buttons.RIGHT_TRIGGER) {
            return false;
        }
        if (!CompatConfig.CONDITION_TRIGGERS.get() || !GunContext.INSTANCE.isActive()) {
            return false;
        }
        float value = button == Buttons.LEFT_TRIGGER ? controller.getLTriggerValue() : controller.getRTriggerValue();

        if (pressed) {
            if (value >= CompatConfig.press()) {
                return false;
            }
            controller.getButtonsStates().setState(button, false);
            return true;
        }
        if (value <= CompatConfig.release()) {
            return false;
        }
        controller.getButtonsStates().setState(button, true);
        return true;
    }

    private TriggerConditioner() {}
}
