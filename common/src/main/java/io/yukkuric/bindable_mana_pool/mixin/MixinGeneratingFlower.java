package io.yukkuric.bindable_mana_pool.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import vazkii.botania.api.block_entity.*;
import vazkii.botania.api.mana.ManaCollector;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

@Mixin(GeneratingFlowerBlockEntity.class)
public abstract class MixinGeneratingFlower extends BindableSpecialFlowerBlockEntity<ManaCollector> {
    public MixinGeneratingFlower(BlockEntityType<?> type, BlockPos pos, BlockState state, Class<ManaCollector> bindClass) {
        super(type, pos, state, bindClass);
    }

    @Inject(method = "emptyManaIntoCollector", at = @At(value = "INVOKE", target = "Lvazkii/botania/api/block_entity/GeneratingFlowerBlockEntity;getMana()I", shift = At.Shift.AFTER), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    void forceEmpty(CallbackInfo ci, ManaCollector collector) {
        if (collector instanceof ManaPoolBlockEntity pool) {
            var maybeVoid = pool.getLevel().getBlockState(pool.getBlockPos().below());
            var hasVoid = maybeVoid.is(BotaniaBlocks.manaVoid);
            // drain all
            if (hasVoid) {
                int allMana = getMana();
                this.addMana(-allMana);
                collector.receiveMana(allMana);
                sync();
                ci.cancel();
            }
        }
    }
}
