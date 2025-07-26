package joshie.vbc.config;

import net.minecraft.core.BlockPos;
import java.util.HashMap;
import java.util.Map;

public class TickLocalCache {
    private static final ThreadLocal<TickCache> CACHE = ThreadLocal.withInitial(TickCache::new);

    public static float getMalus(BlockPos pos, String profession, java.util.function.Supplier<Float> computer) {
        TickCache cache = CACHE.get();
        
        long currentTick = getCurrentTick();
        if (cache.lastTick != currentTick) {
            cache.clear();
            cache.lastTick = currentTick;
        }

        String key = pos.getX() + "," + pos.getY() + "," + pos.getZ() + ":" + profession;
        
        return cache.malusCache.computeIfAbsent(key, k -> computer.get());
    }

    public static void clear() {
        CACHE.remove();
    }

    private static long getCurrentTick() {
        return System.currentTimeMillis() / 50;
    }

    public static CacheStats getStats() {
        TickCache cache = CACHE.get();
        return new CacheStats(cache.malusCache.size(), cache.lastTick);
    }
    
    private static class TickCache {
        final Map<String, Float> malusCache = new HashMap<>();
        long lastTick = -1;
        
        void clear() {
            malusCache.clear();
        }
    }
    
    public static class CacheStats {
        public final int cacheSize;
        public final long lastTick;
        
        CacheStats(int cacheSize, long lastTick) {
            this.cacheSize = cacheSize;
            this.lastTick = lastTick;
        }
        
        @Override
        public String toString() {
            return String.format("TickLocalCache[size=%d, tick=%d]", cacheSize, lastTick);
        }
    }
} 