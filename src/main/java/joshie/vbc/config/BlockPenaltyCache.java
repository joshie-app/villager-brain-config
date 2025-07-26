package joshie.vbc.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class BlockPenaltyCache {
    private static final Map<String, Double> walkOnCache = new ConcurrentHashMap<>();
    private static final Map<String, Double> walkThroughCache = new ConcurrentHashMap<>();
    
    private static final Map<String, Map<String, Double>> precomputedWalkOnTags = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Double>> precomputedWalkThroughTags = new ConcurrentHashMap<>();
    
    public static void clear() {
        walkOnCache.clear();
        walkThroughCache.clear();
        precomputedWalkOnTags.clear();
        precomputedWalkThroughTags.clear();
    }
    
    public static double getWalkOnBlockPenalty(String blockId, String profession) {
        String key = blockId + ":" + profession;
        return walkOnCache.computeIfAbsent(key, k -> 
            ConfigManager.computeWalkOnBlockPenalty(blockId, profession));
    }
    
    public static double getWalkThroughBlockPenalty(String blockId, String profession) {
        String key = blockId + ":" + profession;
        return walkThroughCache.computeIfAbsent(key, k -> 
            ConfigManager.computeWalkThroughBlockPenalty(blockId, profession));  
    }

    public static void precomputeTagPenalties() {
        precomputedWalkOnTags.clear();
        precomputedWalkThroughTags.clear();
        
        for (String profession : ConfigManager.CONFIG.professions.keySet()) {
            precomputeTagPenaltiesForProfession(profession);
        }

        precomputeGlobalTagPenalties();
    }
    
    private static void precomputeTagPenaltiesForProfession(String profession) {
        ModConfig professionConfig = ConfigManager.CONFIG.professions.get(profession);
        if (professionConfig == null) return;
        
        Map<String, Double> walkOnTags = new ConcurrentHashMap<>();
        for (Map.Entry<String, Double> entry : professionConfig.walkOnBlockPenalties.entrySet()) {
            if (entry.getKey().startsWith("#")) {
                walkOnTags.put(entry.getKey(), entry.getValue());
            }
        }
        if (!walkOnTags.isEmpty()) {
            precomputedWalkOnTags.put(profession, walkOnTags);
        }
        
        Map<String, Double> walkThroughTags = new ConcurrentHashMap<>();
        for (Map.Entry<String, Double> entry : professionConfig.walkThroughBlockPenalties.entrySet()) {
            if (entry.getKey().startsWith("#")) {
                walkThroughTags.put(entry.getKey(), entry.getValue());
            }
        }
        if (!walkThroughTags.isEmpty()) {
            precomputedWalkThroughTags.put(profession, walkThroughTags);
        }
    }
    
    private static void precomputeGlobalTagPenalties() {
        Map<String, Double> globalWalkOnTags = new ConcurrentHashMap<>();
        for (Map.Entry<String, Double> entry : ConfigManager.CONFIG.walkOnBlockPenalties.entrySet()) {
            if (entry.getKey().startsWith("#")) {
                globalWalkOnTags.put(entry.getKey(), entry.getValue());
            }
        }
        if (!globalWalkOnTags.isEmpty()) {
            precomputedWalkOnTags.put("global", globalWalkOnTags);
        }
        
        Map<String, Double> globalWalkThroughTags = new ConcurrentHashMap<>();
        for (Map.Entry<String, Double> entry : ConfigManager.CONFIG.walkThroughBlockPenalties.entrySet()) {
            if (entry.getKey().startsWith("#")) {
                globalWalkThroughTags.put(entry.getKey(), entry.getValue());
            }
        }
        if (!globalWalkThroughTags.isEmpty()) {
            precomputedWalkThroughTags.put("global", globalWalkThroughTags);
        }
    }
    
    public static Map<String, Double> getPrecomputedWalkOnTags(String profession) {
        return precomputedWalkOnTags.get(profession);
    }
    
    public static Map<String, Double> getPrecomputedWalkThroughTags(String profession) {
        return precomputedWalkThroughTags.get(profession);
    }
    
    public static Map<String, Double> getGlobalWalkOnTags() {
        return precomputedWalkOnTags.get("global");
    }
    
    public static Map<String, Double> getGlobalWalkThroughTags() {
        return precomputedWalkThroughTags.get("global");
    }


    public static CacheStats getStats() {
        return new CacheStats(walkOnCache.size(), walkThroughCache.size());
    }
    
    public static class CacheStats {
        public final int walkOnCacheSize;
        public final int walkThroughCacheSize;
        
        CacheStats(int walkOnCacheSize, int walkThroughCacheSize) {
            this.walkOnCacheSize = walkOnCacheSize;
            this.walkThroughCacheSize = walkThroughCacheSize;
        }
        
        @Override
        public String toString() {
            return String.format("BlockPenaltyCache[walkOn=%d, walkThrough=%d]", 
                walkOnCacheSize, walkThroughCacheSize);
        }
    }
} 