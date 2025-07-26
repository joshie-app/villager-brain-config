package joshie.vbc.mixins;

import joshie.vbc.utils.MalusUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SwimNodeEvaluator.class)
public abstract class SwimNodeEvaluatorMixin {

    @Inject(method = "findAcceptedNode", at = @At("HEAD"), cancellable = true)
    private void modifyFindAcceptedNode(int x, int y, int z, CallbackInfoReturnable<Node> cir) {
        SwimNodeEvaluator evaluator = (SwimNodeEvaluator) (Object) this;

        Mob mob = evaluator.mob;
        PathType pathType = evaluator.getCachedBlockType(x, y, z);

        if ((evaluator.allowBreaching && pathType == PathType.BREACH) || pathType == PathType.WATER) {
            float f = MalusUtils.maybeVillagerMalus(evaluator, pathType, mob, x, y, z);
            if (f >= 0.0F) {
                Node node = evaluator.getNode(x, y, z);
                node.type = pathType;
                node.costMalus = Math.max(node.costMalus, f);
                if (evaluator.currentContext.level().getFluidState(new BlockPos(x, y, z)).isEmpty()) {
                    node.costMalus += 8.0F;
                }
                cir.setReturnValue(node);
            }
        }
    }
}
