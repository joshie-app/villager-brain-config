package joshie.vbc.utils;

import joshie.vbc.config.ConfigManager;
import joshie.vbc.config.PenaltyCache;
import joshie.vbc.config.ProcessedAvoidEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import static joshie.vbc.VillagerBrainConfig.LOGGER;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static joshie.vbc.config.ConfigManager.getWalkOnBlockPenalty;
import static joshie.vbc.config.ConfigManager.getWalkThroughBlockPenalty;

public class MalusUtils {
    public static boolean hasMalus(PathfinderMob mob, BlockPos pos) {
/**                getWalkOnBlockPenalty(mob.level().getBlockState(pos).getBlock().builtInRegistryHolder().getRegisteredName(), getProfessionId((Villager) mob)) > 0
 || getWalkThroughBlockPenalty(mob.level().getBlockState(pos).getBlock().builtInRegistryHolder().getRegisteredName(), getProfessionId((Villager) mob)) > 0
 || getAvoidPenalty(mob, pos) > 0;
 **/
        return false;
    }

    public static String getProfessionId(Villager villager) {
        return joshie.vbc.config.ProfessionCache.getProfessionId(villager);
    }



    public static float maybeVillagerMalus(NodeEvaluator evaluator, PathType pathType, Mob mob, int x, int y, int z) {
        if (pathType == PathType.BLOCKED || pathType == PathType.LEAVES) {
            return -1.0F;
        }
        
        long startTime = PerformanceTracker.startTimer();
        
        try {
            if (mob instanceof Villager villager) {
                BlockPos.MutableBlockPos pos = BlockPosPool.acquire1();
                pos.set(x, y, z);
                
                String professionId = getProfessionId(villager);
                
                return joshie.vbc.config.VillagerGlobalTickCache.getMalus(pos, () -> {
                    BlockPos.MutableBlockPos posBelow = BlockPosPool.acquire2();
                    BlockPos.MutableBlockPos posAt = BlockPosPool.acquire3();
                    
                    posBelow.set(x, y - 1, z);
                    posAt.set(x, y, z);
                    
                    Block blockBelow = mob.level().getBlockState(posBelow).getBlock();
                    Block blockAtPos = mob.level().getBlockState(posAt).getBlock();
                    
                    float combinedMalus = joshie.vbc.config.FlatPenaltyCache.getCombinedMalus(
                        blockBelow, blockAtPos, professionId, true, true);
                    
                    if (combinedMalus < 0) {
                        return -1.0f;
                    }
                    
                    float avoidPenalty = (float) getAvoidPenalty(mob, pos);
                    if (avoidPenalty < 0) {
                        return -1.0f;
                    }
                    
                    return combinedMalus + avoidPenalty;
                });
            }
            
            return mob.getPathfindingMalus(pathType);
        } finally {
            if (startTime > 0) {
                PerformanceTracker.recordMalusCall(System.nanoTime() - startTime);
            }
        }
    }

    public static double getAvoidPenalty(Mob mob, BlockPos nodePos) {
        if (!(mob instanceof Villager villager)) return 0;

        String professionId = getProfessionId((Villager) mob);
        List<ProcessedAvoidEntry> avoids = ConfigManager.processedAvoids.get(professionId);
        if (avoids == null || avoids.isEmpty()) return 0;

        return PenaltyCache.getOrCompute(nodePos, () -> {
            Level level = mob.level();
            if (!(level instanceof ServerLevel serverLevel)) return 0.0;

            double total = 0;
            Set<ProcessedAvoidEntry> matched = new HashSet<>();

            int maxRadius = avoids.stream().mapToInt(a -> a.radius).max().orElse(0);
            int y0 = nodePos.getY();

            for (int y = y0; y <= y0 + 2; y++) {
                for (int dx = -maxRadius; dx <= maxRadius; dx++) {
                    for (int dz = -maxRadius; dz <= maxRadius; dz++) {
                        double distSq = dx * dx + dz * dz;
                        BlockPos checkPos = nodePos.offset(dx, y - y0, dz);
                        Block block = serverLevel.getBlockState(checkPos).getBlock();

                        for (ProcessedAvoidEntry avoid : avoids) {
                            if (matched.contains(avoid)) continue;
                            if (distSq > avoid.radius * avoid.radius) continue;

                            if (avoid.matches(block)) {
                                total += avoid.penalty;
                                matched.add(avoid);
                            }
                        }
                    }
                }
            }
            return total;
        });
    }

    public static float getJumpPenalty(Mob mob) {
        if (!(mob instanceof Villager villager)) {
            return 0.0f;
        }
        String professionId = getProfessionId(villager);
        return ConfigManager.getActionPenalty(professionId, "jump");
    }
}

