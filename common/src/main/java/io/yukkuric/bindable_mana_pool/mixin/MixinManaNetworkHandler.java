package io.yukkuric.bindable_mana_pool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.botania.api.internal.ManaNetwork;
import vazkii.botania.api.mana.*;
import vazkii.botania.common.handler.ManaNetworkHandler;

@Mixin(ManaNetworkHandler.class)
public abstract class MixinManaNetworkHandler implements ManaNetwork {
    @Shadow(remap = false)
    public abstract void fireManaNetworkEvent(ManaReceiver thing, ManaBlockType type, ManaNetworkAction action);
    @Inject(method = "onNetworkEvent", at = @At("HEAD"), remap = false)
    void poolsAreCollectors(ManaReceiver thing, ManaBlockType type, ManaNetworkAction action, CallbackInfo ci) {
        if (type == ManaBlockType.POOL && (thing instanceof ManaCollector))
            fireManaNetworkEvent(thing, ManaBlockType.COLLECTOR, action);
    }
}
