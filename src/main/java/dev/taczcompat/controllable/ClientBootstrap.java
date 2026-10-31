package dev.taczcompat.controllable;

import com.mrcrayfish.controllable.client.input.Buttons;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.event.ControllerEvents;
import com.mrcrayfish.controllable.event.Value;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;


/** Only loaded when both TACZ and Controllable are present. */
final class ClientBootstrap {

    static void init() {
        // Registered in the mod constructor, i.e. before TACZ registers its own INPUT listener during client
        // setup. Controllable stops at the first listener that returns true, so we see (and may swallow) first.
        ControllerEvents.INPUT.register(ClientBootstrap::onInput);
        ControllerEvents.UPDATE_CAMERA.register(AimAssist::onCamera);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientBootstrap::onClientSetup);
        MinecraftForge.EVENT_BUS.addListener(ClientBootstrap::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(ClientBootstrap::onRenderTick);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(CompatBindings::registerAll);
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            VehicleSupport.tick();
            NativeBindingMirror.sync();
            AimAssist.tick();
        }
    }

    private static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            GunHints.frame();
            AimAssist.frame(event.renderTickTime);
        }
    }

    /** ControllerEvents.Input#handle(Controller, Value<Integer>, int originalButton, boolean pressed) */
    private static boolean onInput(Controller controller, Value<Integer> button, int originalButton, boolean pressed) {
        NativeBindingMirror.sync();

        int id = button.get();
        if (CompatBindings.isRegistered() && VehicleSupport.isRidingVehicle()) {
            if (id >= 0 && id == CompatBindings.VEHICLE_HORN.getButton()) {
                VehicleSupport.setHorn(pressed);
                return true;
            }
            if (id >= 0 && id == CompatBindings.VEHICLE_EXIT.getButton()) {
                if (pressed) {
                    VehicleSupport.requestExit();
                }
                return true;
            }
            // The vehicle mod polls the analog triggers itself; swallow the click events so nothing else reacts.
            if (id == Buttons.LEFT_TRIGGER || id == Buttons.RIGHT_TRIGGER) {
                return true;
            }
        }
        if (TriggerConditioner.apply(controller, id, pressed)) {
            return true;
        }

        if (pressed && CompatBindings.isRegistered()
                && id == CompatBindings.ATTACHMENTS.getButton()
                && GunContext.INSTANCE.isActive()) {
            AttachmentsAction.tryOpen();
            return true; // swallow so the vanilla binding on that button doesn't also fire
        }
        return false;
    }

    private ClientBootstrap() {}
}
