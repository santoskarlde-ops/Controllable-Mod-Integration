package dev.taczcompat.controllable;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.event.Value;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Aim assist for any item that implements TACZ's IGun (IGun.mainHandHoldGun is a plain instanceof check, so
 * every gun-pack gun counts). Two parts:
 *  - magnetism: each tick, nudge the view a fraction of the way toward the best target in the cone,
 *    only while a controller is the active device;
 *  - friction: slow the right stick down while a target is in the cone (via Controllable's UPDATE_CAMERA event).
 */
final class AimAssist {
    private static volatile boolean hasTarget;
    private static boolean failed;
    private static int debugTicks;
    // Chosen once per tick, followed smoothly every rendered frame (see frame()).
    private static LivingEntity target;
    private static double strengthPerTick;
    private static long lastFrameNanos;

    /** Client tick START. */
    static void tick() {
        hasTarget = false;
        target = null;
        if (failed || !CompatConfig.AIM_ASSIST.get()) {
            return;
        }
        try {
            run();
        } catch (RuntimeException e) {
            failed = true;
            TaczControllableCompat.LOGGER.error("[{}] Aim assist disabled after an error.", TaczControllableCompat.MOD_ID, e);
        }
    }

    private static void run() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null || !GunContext.INSTANCE.isActive()) {
            debug("skip: not holding a gun (or a screen is open)");
            return;
        }
        Controller controller = Controllable.getController();
        if (controller == null) {
            debug("skip: no active controller");
            return;
        }
        boolean aiming = IClientPlayerGunOperator.fromLocalPlayer(player).isAim();
        // While the aim button is held the controller is obviously in use, even if no stick has moved for a while.
        boolean inUse = Controllable.getInput().isControllerInUse()
                || (aiming && CompatBindings.isRegistered() && controller.isButtonPressed(CompatBindings.AIM.getButton()));
        if (!inUse) {
            debug("skip: controller not in use (move a stick or press a button)");
            return;
        }

        double range = CompatConfig.AIM_RANGE.get();
        double bestAngle = CompatConfig.AIM_MAX_ANGLE.get();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        Vec3 bestPoint = null;
        LivingEntity bestTarget = null;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(range), t -> isValidTarget(player, t))) {
            Vec3 point = aimPoint(e);
            Vec3 to = point.subtract(eye);
            double dist = to.length();
            if (dist < 1.0E-3D || dist > range) {
                continue;
            }
            double angle = Math.toDegrees(Math.acos(Mth.clamp(look.dot(to) / dist, -1.0D, 1.0D)));
            if (angle >= bestAngle || !player.hasLineOfSight(e)) {
                continue;
            }
            bestAngle = angle;
            bestPoint = point;
            bestTarget = e;
        }
        if (bestPoint == null) {
            debug("skip: no valid target within " + CompatConfig.AIM_MAX_ANGLE.get() + " degrees with line of sight");
            return;
        }
        hasTarget = true;

        boolean stickMoving = Math.hypot(controller.getRThumbStickXValue(), controller.getRThumbStickYValue()) > 0.1D;
        if (CompatConfig.AIM_REQUIRE_STICK.get() && !stickMoving) {
            debug("skip: target found but right stick idle and onlyWhileLooking is on");
            return;
        }

        target = bestTarget;
        strengthPerTick = (aiming ? CompatConfig.AIM_STRENGTH_ADS : CompatConfig.AIM_STRENGTH_HIP).get();
        debug("assisting (" + (aiming ? "ADS" : "hip") + ")");
    }

    /**
     * Every rendered frame (not just 20 times a second): turn toward the target chosen by tick().
     * Exponential smoothing with the per-tick strength converted to a per-second rate, so the pull is
     * identical at any frame rate and has no per-tick steps.
     */
    static void frame(float partialTick) {
        long now = System.nanoTime();
        double dt = lastFrameNanos == 0L ? 0.0D : Mth.clamp((now - lastFrameNanos) / 1.0E9D, 0.0D, 0.1D);
        lastFrameNanos = now;

        LivingEntity t = target;
        if (t == null || dt <= 0.0D || strengthPerTick <= 0.0D || failed) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.isPaused() || mc.screen != null || !t.isAlive()) {
            return;
        }

        Vec3 eye = player.getEyePosition(partialTick);
        Vec3 pos = t.getPosition(partialTick);
        Vec3 d = new Vec3(pos.x, pos.y + t.getBbHeight() * 0.65D, pos.z).subtract(eye);
        double horizontal = Math.sqrt(d.x * d.x + d.z * d.z);
        float wantYaw = (float) (Math.atan2(d.z, d.x) * (180.0D / Math.PI)) - 90.0F;
        float wantPitch = (float) (-(Math.atan2(d.y, horizontal) * (180.0D / Math.PI)));

        float deltaYaw = Mth.wrapDegrees(wantYaw - player.getYRot());
        float deltaPitch = wantPitch - player.getXRot();

        double s = Math.min(strengthPerTick, 0.95D);
        double ratePerSecond = -Math.log(1.0D - s) * 20.0D;
        float fraction = (float) (1.0D - Math.exp(-ratePerSecond * dt));
        float cap = (float) (CompatConfig.AIM_MAX_STEP.get() * 20.0D * dt);
        float stepYaw = Mth.clamp(deltaYaw * fraction, -cap, cap);
        float stepPitch = Mth.clamp(deltaPitch * fraction, -cap, cap);

        // Set the rotation directly (not Entity#turn) so other mods scaling turn() for ADS can't swallow the pull.
        float oldPitch = player.getXRot();
        float newPitch = Mth.clamp(oldPitch + stepPitch, -90.0F, 90.0F);
        player.setYRot(player.getYRot() + stepYaw);
        player.setXRot(newPitch);
        player.yRotO += stepYaw;
        player.xRotO += newPitch - oldPitch;
    }

    private static void debug(String message) {
        if (!CompatConfig.AIM_DEBUG.get() || ++debugTicks % 20 != 0) {
            return;
        }
        TaczControllableCompat.LOGGER.info("[{}] aim assist {}", TaczControllableCompat.MOD_ID, message);
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.displayClientMessage(Component.literal("[aim assist] " + message), true);
        }
    }

    /** ControllerEvents.UpdateCamera: first Value is yaw, second is pitch (both stick deltas). */
    static boolean onCamera(Value<Float> yaw, Value<Float> pitch) {
        double friction = CompatConfig.AIM_FRICTION.get();
        if (hasTarget && friction > 0.0D && CompatConfig.AIM_ASSIST.get()) {
            float factor = (float) (1.0D - friction);
            yaw.set(yaw.get() * factor);
            pitch.set(pitch.get() * factor);
        }
        return false;
    }

    private static boolean isValidTarget(LocalPlayer self, LivingEntity e) {
        if (e == self || !e.isAlive() || e.isSpectator() || e.isInvisible()) {
            return false;
        }
        if (e instanceof Player) {
            return CompatConfig.TARGET_PLAYERS.get();
        }
        if (e instanceof TamableAnimal pet && pet.isOwnedBy(self)) {
            return false;
        }
        if (CompatConfig.TARGET_HOSTILES.get()
                && (e instanceof Enemy || (e instanceof Mob mob && mob.getTarget() == self))) {
            return true;
        }
        return CompatConfig.TARGET_OTHER_MOBS.get();
    }

    private static Vec3 aimPoint(LivingEntity e) {
        return new Vec3(e.getX(), e.getY() + e.getBbHeight() * 0.65D, e.getZ());
    }

    private AimAssist() {}
}
