package io.yukkuric.bindable_mana_pool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.api.mana.ManaCollector;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

@Mixin(ManaPoolBlockEntity.class)
public abstract class MixinManaPool implements ManaCollector {
    @Shadow(remap = false)
    private int manaCap;
    @Override
    public void onClientDisplayTick() {
    }
    @Override
    public float getManaYieldMultiplier(ManaBurst manaBurst) {
        return 1;
    }
    @Override
    public int getMaxMana() {
        return manaCap;
    }
}
