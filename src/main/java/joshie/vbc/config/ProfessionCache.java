package joshie.vbc.config;

import net.minecraft.world.entity.npc.Villager;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.lang.ref.WeakReference;


public class ProfessionCache {
    private static final Map<Villager, WeakReference<String>> professionCache = new ConcurrentHashMap<>();
    
    public static void clear() {
        professionCache.clear();
    }
    

    public static String getProfessionId(Villager villager) {
        WeakReference<String> ref = professionCache.get(villager);
        String professionId = ref != null ? ref.get() : null;
        
        if (professionId == null) {
            #if mc <=211
            professionId = villager.level().registryAccess()
                    .registryOrThrow(net.minecraft.core.registries.Registries.VILLAGER_PROFESSION)
                    .getKey(villager.getVillagerData().getProfession())
                    .toString();
            #else
            professionId = villager.level().registryAccess()
                    .lookupOrThrow(net.minecraft.core.registries.Registries.VILLAGER_PROFESSION)
                    .getKey(villager.getVillagerData().getProfession())
                    .toString();
            #endif
            professionCache.put(villager, new WeakReference<>(professionId));
        }
        
        return professionId;
    }
    

    public static void cleanupDeadReferences() {
        professionCache.entrySet().removeIf(entry -> entry.getValue().get() == null);
    }

    public static CacheStats getStats() {
        long validEntries = professionCache.values().stream()
                .mapToLong(ref -> ref.get() != null ? 1 : 0)
                .sum();
        return new CacheStats((int) validEntries, professionCache.size());
    }
    
    public static class CacheStats {
        public final int validEntries;
        public final int totalEntries;
        
        CacheStats(int validEntries, int totalEntries) {
            this.validEntries = validEntries;
            this.totalEntries = totalEntries;
        }
        
        @Override
        public String toString() {
            return String.format("ProfessionCache[valid=%d, total=%d, hitRate=%.2f%%]", 
                validEntries, totalEntries, 
                totalEntries > 0 ? (validEntries * 100.0 / totalEntries) : 0.0);
        }
    }
} 