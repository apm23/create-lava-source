package com.anjas.createlavasource.mixin.client;

import com.zurrtum.create.client.foundation.entity.behaviour.CarriageAudioBehaviour;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.entity.Train;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Makes every locomotive/whistle carriage in a single Create train participate in
 * manual honking instead of only allowing carriage index 0 to reach the honk code.
 *
 * The vanilla/Create wheel-audio sharing path intentionally returns early on every
 * carriage except the first one. Unfortunately the manual honk logic lives after
 * that return, so whistles on a rear locomotive never get a chance to play.
 */
@Mixin(value = CarriageAudioBehaviour.class, remap = false)
public abstract class CarriageAudioBehaviourMixin {

    /**
     * The affected loop only needs to identify an audio leader before continuing.
     * Present the currently ticking carriage as that leader so it does not return
     * before reaching the honk section.
     */
    @Redirect(
        method = "tick",
        at = @At(
            value = "FIELD",
            target = "Lcom/zurrtum/create/content/trains/entity/Train;carriages:Ljava/util/List;",
            opcode = Opcodes.GETFIELD
        ),
        remap = false
    )
    private List<Carriage> createLavaSource$allowEveryCarriageToReachHonk(Train train) {
        CarriageContraptionEntity entity = ((CarriageAudioBehaviour) (Object) this).entity;
        Carriage current = entity.getCarriage();
        return current == null ? train.carriages : List.of(current);
    }

    /**
     * Once multiple carriages reach the honk section, do not let every carriage
     * decrement the train-global timer. Keep the original once-per-tick behaviour
     * by letting carriage index 0 own the timer decrement.
     */
    @Redirect(
        method = "tick",
        at = @At(
            value = "FIELD",
            target = "Lcom/zurrtum/create/content/trains/entity/Train;honkTicks:I",
            opcode = Opcodes.PUTFIELD
        ),
        remap = false
    )
    private void createLavaSource$decrementHonkTimerOnce(Train train, int value) {
        CarriageContraptionEntity entity = ((CarriageAudioBehaviour) (Object) this).entity;
        if (entity.carriageIndex == 0) {
            train.honkTicks = value;
        }
    }

    /**
     * Ordinary passenger/cargo carriages should not become sound sources. They are
     * allowed to maintain the shared timer above, then stop before the whistle code.
     */
    @Inject(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lcom/zurrtum/create/content/trains/entity/Train;determineHonk(Lnet/minecraft/world/level/Level;)V"
        ),
        cancellable = true,
        remap = false
    )
    private void createLavaSource$onlyWhistleCarriagesHonk(CallbackInfo ci) {
        CarriageContraptionEntity entity = ((CarriageAudioBehaviour) (Object) this).entity;
        if (!(entity.getContraption() instanceof CarriageContraption contraption)
            || contraption.soundQueue.getFirstWhistle(entity) == null) {
            ci.cancel();
        }
    }
}
