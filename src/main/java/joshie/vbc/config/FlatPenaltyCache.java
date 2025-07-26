package joshie.vbc.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.npc.Villager;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class FlatPenaltyCache {
    private static final int WALK_ON_INDEX = 0;
    private static final int WALK_THROUGH_INDEX = 1;
    private static final int AVOID_INDEX = 2;
    private static final int PENALTY_TYPES = 3;
    
    private static final Map<String, float[][]> professionPenalties = new ConcurrentHashMap<>();
    private static volatile float[][] globalPenalties;
    
    private static final int MAX_BLOCK_ID = 8192;
    
    public static void initialize() {
        clear();
        precomputeAllPenalties();
    }
    
    public static void clear() {
        professionPenalties.clear();
        globalPenalties = null;
    }
    

    private static void precomputeAllPenalties() {
        globalPenalties = new float[MAX_BLOCK_ID][PENALTY_TYPES];
        
        for (String profession : ConfigManager.CONFIG.professions.keySet()) {
            float[][] penalties = new float[MAX_BLOCK_ID][PENALTY_TYPES];
            professionPenalties.put(profession, penalties);
            
            for (Block block : BuiltInRegistries.BLOCK) {
                int blockId = BuiltInRegistries.BLOCK.getId(block);
                if (blockId >= 0 && blockId < MAX_BLOCK_ID) {
                    String blockName = BuiltInRegistries.BLOCK.getKey(block).toString();
                    
                    penalties[blockId][WALK_ON_INDEX] = (float) ConfigManager.computeWalkOnBlockPenalty(blockName, profession);
                    penalties[blockId][WALK_THROUGH_INDEX] = (float) ConfigManager.computeWalkThroughBlockPenalty(blockName, profession);
                    penalties[blockId][AVOID_INDEX] = 0.0f;
                    
                    if (globalPenalties[blockId][WALK_ON_INDEX] == 0.0f) {
                        globalPenalties[blockId][WALK_ON_INDEX] = (float) ConfigManager.computeWalkOnBlockPenalty(blockName, "default");
                        globalPenalties[blockId][WALK_THROUGH_INDEX] = (float) ConfigManager.computeWalkThroughBlockPenalty(blockName, "default");
                    }
                }
            }
        }
    }

    public static float getCombinedMalus(Block blockBelow, Block blockAt, String profession, 
                                        boolean needWalkOn, boolean needWalkThrough) {
        float[][] penalties = professionPenalties.get(profession);
        if (penalties == null) {
            penalties = globalPenalties;
        }
        
        if (penalties == null) {
            return 0.0f;
        }
        
        float totalMalus = 0.0f;
        
        if (needWalkOn && blockBelow != null) {
            int blockId = BuiltInRegistries.BLOCK.getId(blockBelow);
            if (blockId >= 0 && blockId < penalties.length) {
                float penalty = penalties[blockId][WALK_ON_INDEX];
                if (penalty < 0) return -1.0f;
                totalMalus += penalty;
            }
        }
        
        if (needWalkThrough && blockAt != null) {
            int blockId = BuiltInRegistries.BLOCK.getId(blockAt);
            if (blockId >= 0 && blockId < penalties.length) {
                float penalty = penalties[blockId][WALK_THROUGH_INDEX];
                if (penalty < 0) return -1.0f;
                totalMalus += penalty;
            }
        }
        
        return totalMalus;
    }
    

    public static float getWalkOnPenalty(Block block, String profession) {
        float[][] penalties = professionPenalties.get(profession);
        if (penalties == null) {
            penalties = globalPenalties;
        }
        
        if (penalties != null && block != null) {
            int blockId = BuiltInRegistries.BLOCK.getId(block);
            if (blockId >= 0 && blockId < penalties.length) {
                return penalties[blockId][WALK_ON_INDEX];
            }
        }
        
        return 0.0f;
    }
    

    public static float getWalkThroughPenalty(Block block, String profession) {
        float[][] penalties = professionPenalties.get(profession);
        if (penalties == null) {
            penalties = globalPenalties;
        }
        
        if (penalties != null && block != null) {
            int blockId = BuiltInRegistries.BLOCK.getId(block);
            if (blockId >= 0 && blockId < penalties.length) {
                return penalties[blockId][WALK_THROUGH_INDEX];
            }
        }
        
        return 0.0f;
    }
    

    public static CacheStats getStats() {
        int professionsLoaded = professionPenalties.size();
        int totalEntries = professionsLoaded * MAX_BLOCK_ID * PENALTY_TYPES;
        boolean globalLoaded = globalPenalties != null;
        
        return new CacheStats(professionsLoaded, totalEntries, globalLoaded);
    }
    
    public static class CacheStats {
        public final int professionsLoaded;
        public final int totalEntries;
        public final boolean globalPenaltiesLoaded;
        
        CacheStats(int professionsLoaded, int totalEntries, boolean globalPenaltiesLoaded) {
            this.professionsLoaded = professionsLoaded;
            this.totalEntries = totalEntries;
            this.globalPenaltiesLoaded = globalPenaltiesLoaded;
        }
        
        @Override
        public String toString() {
            return String.format("FlatPenaltyCache[professions=%d, entries=%d, global=%s]", 
                professionsLoaded, totalEntries, globalPenaltiesLoaded);
        }
    }
} 