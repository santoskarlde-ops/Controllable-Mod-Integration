package dev.taczcompat.controllable;

import com.mrcrayfish.controllable.client.binding.BindingRegistry;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.binding.IBindingContext;
import com.mrcrayfish.controllable.client.input.Buttons;

/** All bindings live in one category and are rebindable from Controllable's bindings screen. */
public final class CompatBindings {
    public static final String CATEGORY = "key.categories.tacz_controllable_compat";
    private static final String PREFIX = TaczControllableCompat.MOD_ID + ".binding.";

    public static ButtonBinding SHOOT;
    public static ButtonBinding AIM;
    public static ButtonBinding RELOAD;
    public static ButtonBinding MELEE;
    public static ButtonBinding FIRE_MODE;
    public static ButtonBinding INSPECT;
    public static ButtonBinding ATTACHMENTS;
    public static ButtonBinding VEHICLE_HORN;
    public static ButtonBinding VEHICLE_EXIT;

    private static boolean registered;

    public static boolean isRegistered() {
        return registered;
    }

    /** Must run before Controllable loads saved bindings (it does that once Minecraft finished loading). */
    static void registerAll() {
        if (registered) {
            return;
        }
        SHOOT       = create(Buttons.RIGHT_TRIGGER, "shoot");
        AIM         = create(Buttons.LEFT_TRIGGER, "aim");
        RELOAD      = create(Buttons.X, "reload");
        MELEE       = create(Buttons.B, "melee");
        FIRE_MODE   = create(Buttons.DPAD_LEFT, "fire_mode");
        INSPECT     = create(-1, "inspect");           // unbound by default
        ATTACHMENTS = create(Buttons.DPAD_UP, "attachments");
        VEHICLE_HORN = create(Buttons.LEFT_THUMB_STICK, "vehicle_horn", VehicleContext.INSTANCE);
        VEHICLE_EXIT = create(Buttons.Y, "vehicle_exit", VehicleContext.INSTANCE);
        registered = true;
    }

    private static ButtonBinding create(int defaultButton, String id) {
        return create(defaultButton, id, GunContext.INSTANCE);
    }

    private static ButtonBinding create(int defaultButton, String id, IBindingContext context) {
        ButtonBinding binding = new ButtonBinding(defaultButton, PREFIX + id, CATEGORY, context);
        BindingRegistry.getInstance().register(binding);
        return binding;
    }

    private CompatBindings() {}
}
