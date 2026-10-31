package dev.taczcompat.controllable;

import com.mrcrayfish.controllable.client.binding.IBindingContext;

/** Active while riding a vehicle from MrCrayfish's Vehicle Mod. */
public final class VehicleContext implements IBindingContext {
    public static final VehicleContext INSTANCE = new VehicleContext();

    private VehicleContext() {}

    @Override
    public boolean isActive() {
        return VehicleSupport.isRidingVehicle();
    }

    @Override
    public boolean conflicts(IBindingContext other) {
        return other == this;
    }
}
