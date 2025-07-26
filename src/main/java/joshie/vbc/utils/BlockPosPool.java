package joshie.vbc.utils;

import net.minecraft.core.BlockPos;

public class BlockPosPool {
    private static final ThreadLocal<BlockPos.MutableBlockPos> POS_1 = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);
    private static final ThreadLocal<BlockPos.MutableBlockPos> POS_2 = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);
    private static final ThreadLocal<BlockPos.MutableBlockPos> POS_3 = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);
    

    public static BlockPos.MutableBlockPos acquire1() {
        return POS_1.get();
    }

    public static BlockPos.MutableBlockPos acquire2() {
        return POS_2.get();
    }

    public static BlockPos.MutableBlockPos acquire3() {
        return POS_3.get();
    }

    public static void clear() {
        POS_1.remove();
        POS_2.remove();
        POS_3.remove();
    }

    public static PoolStats getStats() {
        int activeThreads = Thread.activeCount();
        return new PoolStats(activeThreads * 3);
    }
    
    public static class PoolStats {
        public final int estimatedInstances;
        
        PoolStats(int estimatedInstances) {
            this.estimatedInstances = estimatedInstances;
        }
        
        @Override
        public String toString() {
            return String.format("ThreadLocalBlockPosPool[estimated=%d]", estimatedInstances);
        }
    }
} 