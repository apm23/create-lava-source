package com.anjas.createlavasource.mixin;

import com.zurrtum.create.content.fluids.FluidPropagator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FluidPropagator.class, remap = false)
public abstract class FluidPropagatorMixin {

    private static final int MECHANICAL_PUMP_RANGE = 320;

    @Inject(method = "getPumpRange", at = @At("HEAD"), cancellable = true, remap = false)
    private static void createLavaSource$extendMechanicalPumpRange(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(MECHANICAL_PUMP_RANGE);
    }
}
