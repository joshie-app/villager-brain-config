package joshie.vbc.mixins;

import joshie.vbc.config.VillagerGlobalTickCache;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WalkNodeEvaluator.class)
public class NodeTypeCacheMixin {

    @Inject(method = "getCachedPathType", at = @At("HEAD"), cancellable = true)
    private void globalCachePathType(int x, int y, int z, CallbackInfoReturnable<PathType> cir) {
        long posKey = net.minecraft.core.BlockPos.asLong(x, y, z);
        
        if (VillagerGlobalTickCache.hasNodeTypeCached(posKey)) {
            PathType cached = VillagerGlobalTickCache.getCachedNodeType(posKey);
            if (cached != null) {
                cir.setReturnValue(cached);
                return;
            }
        }
    }

    @Inject(method = "getCachedPathType", at = @At("RETURN"))
    private void cacheComputedResult(int x, int y, int z, CallbackInfoReturnable<PathType> cir) {
        PathType result = cir.getReturnValue();
        if (result != null) {
            long posKey = net.minecraft.core.BlockPos.asLong(x, y, z);
            VillagerGlobalTickCache.cacheNodeType(posKey, result);
        }
    }
} 