package joshie.vbc.utils;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

public class PerformanceTracker {
    private static final AtomicLong totalMalusCallTime = new AtomicLong(0);
    private static final AtomicInteger malusCallCount = new AtomicInteger(0);
    private static final AtomicLong totalPenaltyLookupTime = new AtomicLong(0);
    private static final AtomicInteger penaltyLookupCount = new AtomicInteger(0);
    
    private static final boolean TRACKING_ENABLED =
        Boolean.parseBoolean(System.getProperty("villager_brain_config.performance.tracking", "false"));
    
    public static void recordMalusCall(long nanoTime) {
        if (TRACKING_ENABLED) {
            totalMalusCallTime.addAndGet(nanoTime);
            malusCallCount.incrementAndGet();
        }
    }
    
    public static void recordPenaltyLookup(long nanoTime) {
        if (TRACKING_ENABLED) {
            totalPenaltyLookupTime.addAndGet(nanoTime);
            penaltyLookupCount.incrementAndGet();
        }
    }
    
    public static long startTimer() {
        return TRACKING_ENABLED ? System.nanoTime() : 0;
    }
    
    public static PerformanceStats getStats() {
        if (!TRACKING_ENABLED) {
            return new PerformanceStats(0, 0, 0.0, 0, 0, 0.0);
        }
        
        int malusCalls = malusCallCount.get();
        long totalMalus = totalMalusCallTime.get();
        double avgMalus = malusCalls > 0 ? totalMalus / (double) malusCalls / 1_000_000.0 : 0.0;
        
        int penaltyLookups = penaltyLookupCount.get();
        long totalPenalty = totalPenaltyLookupTime.get();
        double avgPenalty = penaltyLookups > 0 ? totalPenalty / (double) penaltyLookups / 1_000_000.0 : 0.0;
        
        return new PerformanceStats(malusCalls, totalMalus, avgMalus, penaltyLookups, totalPenalty, avgPenalty);
    }
    
    public static void reset() {
        totalMalusCallTime.set(0);
        malusCallCount.set(0);
        totalPenaltyLookupTime.set(0);
        penaltyLookupCount.set(0);
    }
    
    public static class PerformanceStats {
        public final int malusCallCount;
        public final long totalMalusTimeNanos;
        public final double averageMalusTimeMs;
        public final int penaltyLookupCount;
        public final long totalPenaltyTimeNanos;
        public final double averagePenaltyTimeMs;
        
        PerformanceStats(int malusCallCount, long totalMalusTimeNanos, double averageMalusTimeMs,
                        int penaltyLookupCount, long totalPenaltyTimeNanos, double averagePenaltyTimeMs) {
            this.malusCallCount = malusCallCount;
            this.totalMalusTimeNanos = totalMalusTimeNanos;
            this.averageMalusTimeMs = averageMalusTimeMs;
            this.penaltyLookupCount = penaltyLookupCount;
            this.totalPenaltyTimeNanos = totalPenaltyTimeNanos;
            this.averagePenaltyTimeMs = averagePenaltyTimeMs;
        }
        
        @Override
        public String toString() {
            return String.format(
                "PerformanceStats[malusCalls=%d, avgMalus=%.3fms, penaltyLookups=%d, avgPenalty=%.3fms]",
                malusCallCount, averageMalusTimeMs, penaltyLookupCount, averagePenaltyTimeMs
            );
        }
    }
} 