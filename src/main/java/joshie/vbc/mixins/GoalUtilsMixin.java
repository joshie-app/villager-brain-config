package joshie.vbc.mixins;

import joshie.vbc.utils.MalusUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GoalUtils.class)
public class GoalUtilsMixin {
    @Inject(method = "hasMalus", at = @At("HEAD"), cancellable = true)
    private static void injectHasMalus(PathfinderMob mob, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (mob instanceof Villager) {
            cir.setReturnValue(MalusUtils.hasMalus(mob, pos));
        }
    }
}