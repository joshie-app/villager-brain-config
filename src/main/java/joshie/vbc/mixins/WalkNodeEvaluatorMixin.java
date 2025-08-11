package joshie.vbc.mixins;

import joshie.vbc.utils.MalusUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import static joshie.vbc.VillagerBrainConfig.LOGGER;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

#if mc >= 205
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
#endif

#if mc < 205
import net.minecraft.world.level.pathfinder.BlockPathTypes;
#endif



@Mixin(WalkNodeEvaluator.class)
public abstract class WalkNodeEvaluatorMixin {

    @Inject(method = "getStartNode", at = @At("RETURN"), cancellable = true)
    private void modifyGetStartNode(BlockPos pos, CallbackInfoReturnable<Node> cir) {
        WalkNodeEvaluator evaluator = (WalkNodeEvaluator) (Object) this;
        Node node = cir.getReturnValue();
        PathType pathType = evaluator.getCachedPathType(node.x, node.y, node.z);

        float f = MalusUtils.maybeVillagerMalus(evaluator, pathType, evaluator.mob, node.x, node.y, node.z);

        if (f >= 0.0F) {
            node.costMalus = Math.max(node.costMalus, f);
        }

        cir.setReturnValue(node);
        return;
    }

    @Inject(method = "canStartAt", at = @At("HEAD"), cancellable = true)
    private void modifyCanStartAt(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        WalkNodeEvaluator evaluator = (WalkNodeEvaluator) (Object) this;
        PathType pathType = evaluator.getCachedPathType(pos.getX(), pos.getY(), pos.getZ());

        cir.setReturnValue(pathType != PathType.OPEN && MalusUtils.maybeVillagerMalus(evaluator, pathType, evaluator.mob, pos.getX(), pos.getY(), pos.getZ()) >= 0.0F);
    }

    @Inject(method = "getNeighbors", at = @At("HEAD"), cancellable = true)
    private void modifyGetNeighbors(Node[] outputArray, Node node, CallbackInfoReturnable<Integer> cir) {

        WalkNodeEvaluator evaluator = (WalkNodeEvaluator) (Object) this;

        int i = 0;
        int j = 0;

        PathType pathType = evaluator.getCachedPathType(node.x, node.y + 1, node.z);
        PathType pathType2 = evaluator.getCachedPathType(node.x, node.y, node.z);

        float malus = MalusUtils.maybeVillagerMalus(evaluator, pathType, evaluator.mob, node.x, node.y + 1, node.z);

        if (malus >= 0.0F && pathType2 != PathType.STICKY_HONEY) {
            j = Mth.floor(Math.max(1.0F, evaluator.mob.maxUpStep()));
        }

        double d = evaluator.getFloorLevel(new BlockPos(node.x, node.y, node.z));
        Iterator<Direction> var9 = Direction.Plane.HORIZONTAL.iterator();

        Direction direction;
        while (var9.hasNext()) {
            direction = var9.next();
            Node node2 = evaluator.findAcceptedNode(node.x + direction.getStepX(), node.y, node.z + direction.getStepZ(), j, d, direction, pathType2);
            evaluator.reusableNeighbors[direction.get2DDataValue()] = node2;
            if (evaluator.isNeighborValid(node2, node)) {
                outputArray[i++] = node2;
            }
        }

        var9 = Direction.Plane.HORIZONTAL.iterator();

        while (var9.hasNext()) {
            direction = var9.next();
            Direction direction2 = direction.getClockWise();

            Node neighborA = evaluator.reusableNeighbors[direction.get2DDataValue()];
            Node neighborB = evaluator.reusableNeighbors[direction2.get2DDataValue()];

            if (((WalkNodeEvaluatorAccessor) evaluator).callIsDiagonalValid(node, neighborA, neighborB)) {
                Node node3 = evaluator.findAcceptedNode(
                        node.x + direction.getStepX() + direction2.getStepX(),
                        node.y,
                        node.z + direction.getStepZ() + direction2.getStepZ(),
                        j, d, direction, pathType2
                );

                if (evaluator.isDiagonalValid(node3)) {
                    outputArray[i++] = node3;
                }
            }
        }


        cir.setReturnValue(i);
    }

    private void applyPenaltyAndLogJumpNode(WalkNodeEvaluator evaluator, Node node, int y) {
        float penalty = MalusUtils.getJumpPenalty(evaluator.mob);
        if (node.costMalus >= 0.0F) {
            node.costMalus += penalty;
        }
    }

    @Inject(method = "tryJumpOn", at = @At("HEAD"), cancellable = true)
    private void modifyTryJumpOn(int x, int y, int z, int verticalDeltaLimit, double nodeFloorLevel, Direction direction, PathType pathType, BlockPos.MutableBlockPos pos, CallbackInfoReturnable<Node> cir) {
        WalkNodeEvaluator evaluator = (WalkNodeEvaluator) (Object) this;
        Node node = evaluator.findAcceptedNode(x, y + 1, z, verticalDeltaLimit - 1, nodeFloorLevel, direction, pathType);
        if (node == null) {
            cir.setReturnValue(null);
            return;
        } else if (evaluator.mob.getBbWidth() >= 1.0F) {
            applyPenaltyAndLogJumpNode(evaluator, node, y);
            cir.setReturnValue(node);
            return;
        } else if (node.type != PathType.OPEN && node.type != PathType.WALKABLE) {
            applyPenaltyAndLogJumpNode(evaluator, node, y);
            cir.setReturnValue(node);
            return;
        } else {
            applyPenaltyAndLogJumpNode(evaluator, node, y);
            double d0 = (double)(x - direction.getStepX()) + 0.5;
            double d1 = (double)(z - direction.getStepZ()) + 0.5;
            double d2 = (double)evaluator.mob.getBbWidth() / 2.0;
            AABB aabb = new AABB(d0 - d2, evaluator.getFloorLevel(pos.set(d0, (double)(y + 1), d1)) + 0.001, d1 - d2, d0 + d2, (double)evaluator.mob.getBbHeight() + evaluator.getFloorLevel(pos.set((double)node.x, (double)node.y, (double)node.z)) - 0.002, d1 + d2);
            cir.setReturnValue(evaluator.hasCollisions(aabb) ? null : node);
            return;
        }
    }

    @Inject(method = "findAcceptedNode", at = @At("HEAD"), cancellable = true)
    private void modifyFindAcceptedNode(int x, int y, int z, int verticalDeltaLimit, double nodeFloorLevel,
                                        Direction direction, PathType pathType, CallbackInfoReturnable<Node> cir) {
        WalkNodeEvaluator evaluator = (WalkNodeEvaluator) (Object) this;
        Node node = null;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        double d = evaluator.getFloorLevel(mutableBlockPos.set(x, y, z));


        if (d - nodeFloorLevel > evaluator.getMobJumpHeight()) {
            cir.setReturnValue(null);
            return;
        }

        PathType pathType2 = evaluator.getCachedPathType(x, y, z);
        float f = MalusUtils.maybeVillagerMalus(evaluator, pathType2, evaluator.mob, x, y, z);

        if (f < 0) {
            pathType2 = PathType.BLOCKED;
//            verticalDeltaLimit = 0;
        }

        if (f >= 0.0F) {
            node = evaluator.getNodeAndUpdateCostToMax(x, y, z, pathType2, f);
        }

        if (WalkNodeEvaluator.doesBlockHavePartialCollision(pathType) && node != null &&
                node.costMalus >= 0.0F && !evaluator.canReachWithoutCollision(node)) {
            node = null;
        }

        if (pathType2 != PathType.WALKABLE && (!evaluator.isAmphibious() || pathType2 != PathType.WATER)) {
            if ((node == null || node.costMalus < 0.0F) && verticalDeltaLimit > 0 &&
                    (pathType2 != PathType.FENCE || evaluator.canWalkOverFences()) &&
                    pathType2 != PathType.UNPASSABLE_RAIL && pathType2 != PathType.TRAPDOOR &&
                    pathType2 != PathType.POWDER_SNOW) {
                node = evaluator.tryJumpOn(x, y, z, verticalDeltaLimit, nodeFloorLevel, direction, pathType, mutableBlockPos);
            } else if (!evaluator.isAmphibious() && pathType2 == PathType.WATER && !evaluator.canFloat()) {
                node = evaluator.tryFindFirstNonWaterBelow(x, y, z, node);
            } else if (pathType2 == PathType.OPEN) {
                node = evaluator.tryFindFirstGroundNodeBelow(x, y, z);
            } else if (WalkNodeEvaluator.doesBlockHavePartialCollision(pathType2) && node == null) {
                node = evaluator.getClosedNode(x, y, z, pathType2);
            }
        }
        cir.setReturnValue(node);
    }


    @Inject(method = "tryFindFirstNonWaterBelow", at = @At("HEAD"), cancellable = true)
    private void modifyTryFindFirstNonWaterBelow(int x, int y, int z, Node node, CallbackInfoReturnable<Node> cir) {
        WalkNodeEvaluator evaluator = (WalkNodeEvaluator) (Object) this;
        y--;

        #if mc > 211
        while(y > evaluator.mob.level().getMinY()) {
        #else
        while(y > evaluator.mob.level().getMinBuildHeight()) {
        #endif
            PathType pathType = evaluator.getCachedPathType(x, y, z);
            if (pathType != PathType.WATER) {
                cir.setReturnValue(node);
                return;
            }

            float malus = MalusUtils.maybeVillagerMalus(evaluator, pathType, evaluator.mob, x, y, z);
            node = evaluator.getNodeAndUpdateCostToMax(x, y, z, pathType, malus);
            y--;
        }

        cir.setReturnValue(node);
        return;
    }

    @Inject(method = "tryFindFirstGroundNodeBelow", at = @At("HEAD"), cancellable = true)
    private void modifyTryFindFirstGroundNodeBelow(int x, int y, int z, CallbackInfoReturnable<Node> cir) {
        WalkNodeEvaluator evaluator = (WalkNodeEvaluator) (Object) this;
        #if mc > 211
        for(int i = y - 1; i >= evaluator.mob.level().getMinY(); --i) {
        #else
        for(int i = y - 1; i >= evaluator.mob.level().getMinBuildHeight(); --i) {
        #endif
            if (y - i > evaluator.mob.getMaxFallDistance()) {
                cir.setReturnValue(evaluator.getBlockedNode(x, i, z));
                return;
            }

            PathType pathType = evaluator.getCachedPathType(x, i, z);
            float f = MalusUtils.maybeVillagerMalus(evaluator, pathType, evaluator.mob, x, i, z);

            if (pathType != PathType.OPEN) {
                if (f >= 0.0F) {
                    cir.setReturnValue(evaluator.getNodeAndUpdateCostToMax(x, i, z, pathType, f));
                    return;
                }
                cir.setReturnValue(evaluator.getBlockedNode(x, i, z));
                return;
            }
        }

        cir.setReturnValue(evaluator.getBlockedNode(x, y, z));
        return;
    }

    @Inject(method = "getPathTypeOfMob", at = @At("HEAD"), cancellable = true)
    private void modifyGetPathTypeOfMob(PathfindingContext context, int x, int y, int z, Mob mob, CallbackInfoReturnable<PathType> cir) {
        WalkNodeEvaluator evaluator = (WalkNodeEvaluator) (Object) this;
        Set<PathType> set = evaluator.getPathTypeWithinMobBB(context, x, y, z);

        if (set.contains(PathType.FENCE)) {
            cir.setReturnValue(PathType.FENCE);
            return;
        } else if (set.contains(PathType.UNPASSABLE_RAIL)) {
            cir.setReturnValue(PathType.UNPASSABLE_RAIL);
            return;
        }

        PathType pathType = PathType.BLOCKED;
        Iterator<PathType> var8 = set.iterator();

        while(var8.hasNext()) {
            PathType pathType2 = var8.next();
            float malus = MalusUtils.maybeVillagerMalus(evaluator, pathType2, mob, x, y, z);

            if (malus < 0.0F) {
                cir.setReturnValue(pathType2);
                return;
            }

            float currentMalus = MalusUtils.maybeVillagerMalus(evaluator, pathType, mob, x, y, z);
            if (malus >= currentMalus) {
                pathType = pathType2;
            }
        }

        if (evaluator.entityWidth <= 1 && pathType != PathType.OPEN &&
                MalusUtils.maybeVillagerMalus(evaluator, pathType, mob, x, y, z) == 0.0F &&
                evaluator.getPathType(context, x, y, z) == PathType.OPEN) {
            cir.setReturnValue(PathType.OPEN);
            return;
        } else {
            cir.setReturnValue(pathType);
            return;
        }
    }

    @Inject(method = "isDiagonalValid(Lnet/minecraft/world/level/pathfinder/Node;Lnet/minecraft/world/level/pathfinder/Node;Lnet/minecraft/world/level/pathfinder/Node;)Z", at = @At("RETURN"), cancellable = true)
    private void modifyIsDiagonalValid(Node root, Node xNode, Node zNode, CallbackInfoReturnable<Boolean> cir) {
        WalkNodeEvaluator evaluator = (WalkNodeEvaluator) (Object) this;
        if (evaluator.mob instanceof Villager) {
            if (zNode != null && xNode != null) {
                zNode = evaluator.getNode(zNode.x, root.y, zNode.z);
                xNode = evaluator.getNode(xNode.x, root.y, xNode.z);
                if (xNode.type != PathType.WALKABLE_DOOR && zNode.type != PathType.WALKABLE_DOOR) {
                    boolean flag = zNode.type == PathType.FENCE && xNode.type == PathType.FENCE && (double) (evaluator.mob.getBbWidth()) < 0.5;

                    float malusZ = MalusUtils.maybeVillagerMalus(evaluator, zNode.type, evaluator.mob, zNode.x, zNode.y, zNode.z); // B
                    float malusX = MalusUtils.maybeVillagerMalus(evaluator, xNode.type, evaluator.mob, xNode.x, xNode.y, xNode.z); // A

                    boolean zCondition = (malusZ >= 0.0F) && zNode.y == root.y ;
                    boolean xCondition = (malusX >= 0.0F) && xNode.y == root.y;
                    boolean result = zCondition && xCondition;
                    cir.setReturnValue(result);
                    return;
                } else {
                    cir.setReturnValue(false);
                    return;
                }
            } else {
                cir.setReturnValue(false);
                return;
            }
        }
    }


}
