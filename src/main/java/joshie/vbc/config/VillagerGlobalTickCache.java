package joshie.vbc.config;

import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.PathType;


public class VillagerGlobalTickCache {
    public static final Long2FloatMap malusCache = new Long2FloatOpenHashMap(4096);
    public static final Long2ByteMap nodeTypeCache = new Long2ByteOpenHashMap(4096);
    
    private static volatile long currentTick = -1;
    
    public static int malusHits = 0;
    public static int malusMisses = 0;
    public static int nodeTypeHits = 0;
    public static int nodeTypeMisses = 0;
    

    public static float getMalus(BlockPos pos, java.util.function.Supplier<Float> computer) {
        long posKey = pos.asLong();
        
        ensureTickValid();
        
        if (malusCache.containsKey(posKey)) {
            malusHits++;
            return malusCache.get(posKey);
        }
        
        float result = computer.get();
        if (!Float.isNaN(result)) {
            malusCache.put(posKey, result);
            malusMisses++;
        }
        
        return result;
    }

    public static PathType getNodeType(int x, int y, int z, java.util.function.Supplier<PathType> computer) {
        long posKey = BlockPos.asLong(x, y, z);
        
        ensureTickValid();
        
        if (nodeTypeCache.containsKey(posKey)) {
            nodeTypeHits++;
            byte encoded = nodeTypeCache.get(posKey);
            return PathType.values()[encoded];
        }
        
        PathType result = computer.get();
        if (result != null) {
            nodeTypeCache.put(posKey, (byte) result.ordinal());
            nodeTypeMisses++;
        }
        
        return result;
    }
    

    public static void ensureTickValid() {
        long tick = getCurrentTick();
        if (currentTick != tick) {
            reset();
            currentTick = tick;
        }
    }
    

    public static void reset() {
        malusCache.clear();
        nodeTypeCache.clear();
        
        malusHits = 0;
        malusMisses = 0;
        nodeTypeHits = 0;
        nodeTypeMisses = 0;
    }
    

    private static long getCurrentTick() {
        return System.currentTimeMillis() / 50;
    }

    public static boolean hasNodeTypeCached(long posKey) {
        ensureTickValid();
        return nodeTypeCache.containsKey(posKey);
    }

    public static PathType getCachedNodeType(long posKey) {
        if (nodeTypeCache.containsKey(posKey)) {
            nodeTypeHits++;
            byte encoded = nodeTypeCache.get(posKey);
            return PathType.values()[encoded];
        }
        return null;
    }

    public static void cacheNodeType(long posKey, PathType pathType) {
        if (pathType != null) {
            ensureTickValid();
            nodeTypeCache.put(posKey, (byte) pathType.ordinal());
            nodeTypeMisses++;
        }
    }

    public static CacheStats getStats() {
        return new CacheStats(
            malusCache.size(), nodeTypeCache.size(),
            malusHits, malusMisses, nodeTypeHits, nodeTypeMisses,
            currentTick
        );
    }
    
    public static class CacheStats {
        public final int malusCacheSize;
        public final int nodeTypeCacheSize;
        public final int malusHits;
        public final int malusMisses;
        public final int nodeTypeHits;
        public final int nodeTypeMisses;
        public final long currentTick;
        
        CacheStats(int malusCacheSize, int nodeTypeCacheSize, 
                  int malusHits, int malusMisses, int nodeTypeHits, int nodeTypeMisses, 
                  long currentTick) {
            this.malusCacheSize = malusCacheSize;
            this.nodeTypeCacheSize = nodeTypeCacheSize;
            this.malusHits = malusHits;
            this.malusMisses = malusMisses;
            this.nodeTypeHits = nodeTypeHits;
            this.nodeTypeMisses = nodeTypeMisses;
            this.currentTick = currentTick;
        }
        
        public double getMalusHitRate() {
            int total = malusHits + malusMisses;
            return total > 0 ? (malusHits * 100.0 / total) : 0.0;
        }
        
        public double getNodeTypeHitRate() {
            int total = nodeTypeHits + nodeTypeMisses;
            return total > 0 ? (nodeTypeHits * 100.0 / total) : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "GlobalTickCache[malus=%d(%.1f%%), nodeType=%d(%.1f%%), tick=%d]",
                malusCacheSize, getMalusHitRate(), 
                nodeTypeCacheSize, getNodeTypeHitRate(), 
                currentTick
            );
        }
    }
} 