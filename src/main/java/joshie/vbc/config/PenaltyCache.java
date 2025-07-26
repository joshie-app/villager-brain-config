package joshie.vbc.config;

import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PenaltyCache {
    private static final Map<BlockPos, Double> cache = new HashMap<>();

    public static void clear() {
        cache.clear();
    }

    public static double getOrCompute(BlockPos pos, Supplier<Double> compute) {
        return cache.computeIfAbsent(pos.immutable(), k -> compute.get());
    }
}