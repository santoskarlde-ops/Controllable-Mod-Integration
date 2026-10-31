package dev.taczcompat.controllable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Locale;

/** Opened from Mods -> TACZ Controllable Compat -> Config. Two pages: toggles, then sliders. */
public final class CompatConfigScreen extends Screen {
    private final Screen parent;
    private int page;

    public CompatConfigScreen(Screen parent) {
        super(Component.literal("TACZ Controllable Compat"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 155;
        int right = this.width / 2 + 5;
        int y = 40;
        int step = 26;

        if (page == 0) {
            toggle(left, y, "Aim assist", CompatConfig.AIM_ASSIST);
            toggle(right, y, "Only while looking", CompatConfig.AIM_REQUIRE_STICK);
            toggle(left, y + step, "Hostile mobs", CompatConfig.TARGET_HOSTILES);
            toggle(right, y + step, "All other mobs", CompatConfig.TARGET_OTHER_MOBS);
            toggle(left, y + 2 * step, "Players", CompatConfig.TARGET_PLAYERS);
            toggle(right, y + 2 * step, "Debug (action bar)", CompatConfig.AIM_DEBUG);
            toggle(left, y + 3 * step, "Digital triggers", CompatConfig.CONDITION_TRIGGERS);
            toggle(right, y + 3 * step, "Mirror TACZ bindings", CompatConfig.MIRROR_NATIVE_BINDINGS);
            toggle(left, y + 4 * step, "Extra gun hints", CompatConfig.EXTRA_HINTS);
            toggle(right, y + 4 * step, "GTA V vehicle controls", CompatConfig.GTA_VEHICLE_CONTROLS);
        } else {
            slider(left, y, "Trigger press", CompatConfig.PRESS_THRESHOLD, 0.5D, 1.0D);
            slider(right, y, "Trigger release", CompatConfig.RELEASE_THRESHOLD, 0.0D, 0.95D);
            slider(left, y + step, "Range", CompatConfig.AIM_RANGE, 4.0D, 128.0D);
            slider(right, y + step, "Cone (deg)", CompatConfig.AIM_MAX_ANGLE, 1.0D, 30.0D);
            slider(left, y + 2 * step, "Pull hip", CompatConfig.AIM_STRENGTH_HIP, 0.0D, 0.6D);
            slider(right, y + 2 * step, "Pull ADS", CompatConfig.AIM_STRENGTH_ADS, 0.0D, 0.6D);
            slider(left, y + 3 * step, "Max step (deg)", CompatConfig.AIM_MAX_STEP, 0.1D, 10.0D);
            slider(right, y + 3 * step, "Stick friction", CompatConfig.AIM_FRICTION, 0.0D, 0.9D);
        }

        int bottom = this.height - 28;
        addRenderableWidget(Button.builder(Component.literal(page == 0 ? "Next page >" : "< Prev page"), b -> {
            page ^= 1;
            this.rebuildWidgets();
        }).bounds(this.width / 2 - 155, bottom, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Reset aim assist"), b -> {
            CompatConfig.resetAimAssist();
            this.rebuildWidgets();
        }).bounds(this.width / 2 - 50, bottom, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose())
                .bounds(this.width / 2 + 55, bottom, 100, 20).build());
    }

    private void toggle(int x, int y, String label, ForgeConfigSpec.BooleanValue value) {
        addRenderableWidget(Button.builder(toggleText(label, value.get()), b -> {
            boolean next = !value.get();
            value.set(next);
            b.setMessage(toggleText(label, next));
        }).bounds(x, y, 150, 20).build());
    }

    private static Component toggleText(String label, boolean on) {
        return Component.literal(label + ": " + (on ? "ON" : "OFF"));
    }

    private void slider(int x, int y, String label, ForgeConfigSpec.DoubleValue value, double min, double max) {
        addRenderableWidget(new DoubleSlider(x, y, 150, 20, label, value, min, max));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        try {
            CompatConfig.SPEC.save();
        } catch (RuntimeException e) {
            TaczControllableCompat.LOGGER.warn("[{}] Could not save config.", TaczControllableCompat.MOD_ID, e);
        }
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    private static final class DoubleSlider extends AbstractSliderButton {
        private final String label;
        private final ForgeConfigSpec.DoubleValue config;
        private final double min;
        private final double max;

        DoubleSlider(int x, int y, int width, int height, String label,
                     ForgeConfigSpec.DoubleValue config, double min, double max) {
            super(x, y, width, height, Component.empty(), Mth.clamp((config.get() - min) / (max - min), 0.0D, 1.0D));
            this.label = label;
            this.config = config;
            this.min = min;
            this.max = max;
            updateMessage();
        }

        private double current() {
            return min + this.value * (max - min);
        }

        @Override
        protected void updateMessage() {
            if (label == null) {
                return;
            }
            setMessage(Component.literal(label + ": " + String.format(Locale.ROOT, "%.2f", current())));
        }

        @Override
        protected void applyValue() {
            config.set(Mth.clamp(Math.round(current() * 100.0D) / 100.0D, min, max));
        }
    }
}
