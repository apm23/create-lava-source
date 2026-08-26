package com.anjas.createlavasource.mixin;

import com.zurrtum.create.content.fluids.transfer.FluidManipulationBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FluidManipulationBehaviour.class, remap = false)
public abstract class FluidManipulationBehaviourMixin {

    private static final int LAVA_BOTTOMLESS_THRESHOLD = 27;

    @Shadow
    @Nullable
    BlockPos rootPos;

    @Shadow
    protected abstract Level getLevel();

    @Inject(method = "maxBlocks", at = @At("HEAD"), cancellable = true, remap = false)
    private void createLavaSource$useSmallLavaThreshold(CallbackInfoReturnable<Integer> cir) {
        if (rootPos == null) {
            return;
        }

        FluidState state = getLevel().getFluidState(rootPos);
        if (state.getType() == Fluids.LAVA || state.getType() == Fluids.FLOWING_LAVA) {
            cir.setReturnValue(LAVA_BOTTOMLESS_THRESHOLD);
        }
    }
}
