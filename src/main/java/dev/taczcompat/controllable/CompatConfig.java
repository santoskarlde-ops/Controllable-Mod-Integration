package dev.taczcompat.controllable;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public final class CompatConfig {
    // Defaults (also used by the in-game "Reset aim assist" button)
    static final double D_RANGE = 48.0D;
    static final double D_ANGLE = 12.0D;
    static final double D_HIP = 0.15D;
    static final double D_ADS = 0.30D;
    static final double D_STEP = 3.0D;
    static final double D_FRICTION = 0.35D;
    static final boolean D_STICK = false;
    static final boolean D_HOSTILES = true;
    static final boolean D_PLAYERS = false;
    static final boolean D_OTHER = true;
    /** Bump when aim assist defaults change; old config files are reset to the new defaults once. */
    static final int DEFAULTS_REVISION_NOW = 2;

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue CONDITION_TRIGGERS;
    public static final ForgeConfigSpec.DoubleValue PRESS_THRESHOLD;
    public static final ForgeConfigSpec.DoubleValue RELEASE_THRESHOLD;
    public static final ForgeConfigSpec.BooleanValue MIRROR_NATIVE_BINDINGS;

    public static final ForgeConfigSpec.BooleanValue AIM_ASSIST;
    public static final ForgeConfigSpec.DoubleValue AIM_RANGE;
    public static final ForgeConfigSpec.DoubleValue AIM_MAX_ANGLE;
    public static final ForgeConfigSpec.DoubleValue AIM_STRENGTH_HIP;
    public static final ForgeConfigSpec.DoubleValue AIM_STRENGTH_ADS;
    public static final ForgeConfigSpec.DoubleValue AIM_MAX_STEP;
    public static final ForgeConfigSpec.DoubleValue AIM_FRICTION;
    public static final ForgeConfigSpec.BooleanValue AIM_REQUIRE_STICK;
    public static final ForgeConfigSpec.BooleanValue TARGET_HOSTILES;
    public static final ForgeConfigSpec.BooleanValue TARGET_PLAYERS;
    public static final ForgeConfigSpec.BooleanValue TARGET_OTHER_MOBS;
    public static final ForgeConfigSpec.BooleanValue AIM_DEBUG;
    public static final ForgeConfigSpec.IntValue DEFAULTS_REVISION;
    public static final ForgeConfigSpec.BooleanValue EXTRA_HINTS;
    public static final ForgeConfigSpec.BooleanValue GTA_VEHICLE_CONTROLS;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("triggers");
        CONDITION_TRIGGERS = b
                .comment("Treat LT/RT as clean digital buttons (press/release hysteresis) while holding a TACZ gun.")
                .define("conditionTriggers", true);
        PRESS_THRESHOLD = b
                .comment("Trigger value (0..1, after Controllable's trigger dead zone) needed to count as pressed.",
                        "Controllable itself only reports a press from 0.5 upwards, so values below 0.5 behave like 0.5.")
                .defineInRange("pressThreshold", 0.60D, 0.50D, 1.0D);
        RELEASE_THRESHOLD = b
                .comment("Trigger value the axis must fall to (or below) before it counts as released.",
                        "Keep this clearly lower than pressThreshold; the gap is the hysteresis band.")
                .defineInRange("releaseThreshold", 0.40D, 0.0D, 0.95D);
        b.pop();

        b.push("bindings");
        MIRROR_NATIVE_BINDINGS = b
                .comment("Keep TACZ's built-in Controllable bindings (category 'TACZ') in sync with this mod's bindings,",
                        "so each action is handled exactly once. Turn off only if you want to use TACZ's own bindings instead.")
                .define("mirrorNativeBindings", true);
        b.pop();

        b.push("hints");
        EXTRA_HINTS = b.comment("Also show Fire Mode, Attachments, Melee and Inspect in Controllable's button hints (Shoot, Aim and Reload are always shown).")
                .define("extraHints", false);
        b.pop();

        b.push("vehicles");
        GTA_VEHICLE_CONTROLS = b.comment("MrCrayfish's Vehicle Mod: GTA V style driving (RT gas, LT brake/reverse, left stick steer,",
                        "RB handbrake). Switches on the vehicle mod's own 'useTriggers' option while you ride.")
                .define("gtaVehicleControls", true);
        b.pop();

        b.push("aimAssist");
        AIM_ASSIST = b
                .comment("Aim assist while a controller is in use and any TACZ gun (including gun-pack guns) is in the main hand.")
                .define("enabled", true);
        AIM_RANGE = b.comment("Maximum target distance in blocks.")
                .defineInRange("range", D_RANGE, 4.0D, 128.0D);
        AIM_MAX_ANGLE = b.comment("Target must be within this many degrees of the crosshair.")
                .defineInRange("maxAngleDegrees", D_ANGLE, 1.0D, 30.0D);
        AIM_STRENGTH_HIP = b.comment("Fraction of the remaining angle corrected per tick when firing from the hip.")
                .defineInRange("strengthHip", D_HIP, 0.0D, 0.6D);
        AIM_STRENGTH_ADS = b.comment("Same, while aiming down sights.")
                .defineInRange("strengthAds", D_ADS, 0.0D, 0.6D);
        AIM_MAX_STEP = b.comment("Hard cap on the correction per tick, in degrees.")
                .defineInRange("maxStepDegrees", D_STEP, 0.1D, 10.0D);
        AIM_FRICTION = b.comment("Right-stick slowdown while a target is inside the cone (0 = off, 0.35 = 35% slower).")
                .defineInRange("friction", D_FRICTION, 0.0D, 0.9D);
        AIM_REQUIRE_STICK = b.comment("Only pull toward the target while the right stick is being moved.")
                .define("onlyWhileLooking", D_STICK);
        TARGET_HOSTILES = b.comment("Assist toward hostile mobs (and mobs currently targeting you).")
                .define("targetHostiles", D_HOSTILES);
        TARGET_PLAYERS = b.comment("Assist toward other players.")
                .define("targetPlayers", D_PLAYERS);
        TARGET_OTHER_MOBS = b.comment("Assist toward every other mob (animals, villagers, golems...). Your own pets are skipped.")
                .define("targetOtherMobs", D_OTHER);
        AIM_DEBUG = b.comment("Show (action bar) and log why aim assist is or isn't acting, once per second.")
                .define("debug", false);
        DEFAULTS_REVISION = b.comment("Internal: used to reset old aim assist settings when defaults change. Don't edit.")
                .defineInRange("defaultsRevision", 0, 0, 1000);
        b.pop();

        SPEC = b.build();
    }

    /** Resets every aim assist option to its default. */
    public static void resetAimAssist() {
        AIM_ASSIST.set(true);
        AIM_RANGE.set(D_RANGE);
        AIM_MAX_ANGLE.set(D_ANGLE);
        AIM_STRENGTH_HIP.set(D_HIP);
        AIM_STRENGTH_ADS.set(D_ADS);
        AIM_MAX_STEP.set(D_STEP);
        AIM_FRICTION.set(D_FRICTION);
        AIM_REQUIRE_STICK.set(D_STICK);
        TARGET_HOSTILES.set(D_HOSTILES);
        TARGET_PLAYERS.set(D_PLAYERS);
        TARGET_OTHER_MOBS.set(D_OTHER);
    }

    /** Old config files keep their old (weak) aim assist values; reset them once when the defaults revision changes. */
    static void onConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }
        try {
            if (DEFAULTS_REVISION.get() < DEFAULTS_REVISION_NOW) {
                resetAimAssist();
                DEFAULTS_REVISION.set(DEFAULTS_REVISION_NOW);
                SPEC.save();
            }
        } catch (RuntimeException e) {
            TaczControllableCompat.LOGGER.warn("[{}] Could not migrate old config.", TaczControllableCompat.MOD_ID, e);
        }
    }

    /** Effective press threshold. */
    public static float press() {
        return PRESS_THRESHOLD.get().floatValue();
    }

    /** Effective release threshold, always at least 0.05 below the press threshold. */
    public static float release() {
        return (float) Math.min(RELEASE_THRESHOLD.get(), PRESS_THRESHOLD.get() - 0.05D);
    }

    private CompatConfig() {}
}
