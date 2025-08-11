package joshie.vbc.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import joshie.vbc.config.BlockPenaltyCache;
import joshie.vbc.config.FlatPenaltyCache;
import joshie.vbc.config.ProfessionCache;
import joshie.vbc.config.TickLocalCache;
import joshie.vbc.config.VillagerGlobalTickCache;
import joshie.vbc.utils.BlockPosPool;
import joshie.vbc.utils.PerformanceTracker;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class PerformanceCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("villager_brain_config")
                .then(Commands.literal("performance")
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> {
                            CommandSourceStack source = ctx.getSource();
                            
                            PerformanceTracker.PerformanceStats perfStats = PerformanceTracker.getStats();
                            BlockPenaltyCache.CacheStats penaltyStats = BlockPenaltyCache.getStats();
                            FlatPenaltyCache.CacheStats flatStats = FlatPenaltyCache.getStats();
                            ProfessionCache.CacheStats professionStats = ProfessionCache.getStats();
                            TickLocalCache.CacheStats tickStats = TickLocalCache.getStats();
                            VillagerGlobalTickCache.CacheStats globalStats = VillagerGlobalTickCache.getStats();
                            BlockPosPool.PoolStats poolStats = BlockPosPool.getStats();
                            
                            source.sendSuccess(() -> Component.literal("=== Villager Brain Config Performance Stats ==="), false);
                            
                            source.sendSuccess(() -> Component.literal(String.format(
                                "Malus calls: %d (avg: %.3fms)", 
                                perfStats.malusCallCount, perfStats.averageMalusTimeMs)), false);
                            source.sendSuccess(() -> Component.literal(String.format(
                                "Penalty lookups: %d (avg: %.3fms)", 
                                perfStats.penaltyLookupCount, perfStats.averagePenaltyTimeMs)), false);
                            
                            source.sendSuccess(() -> Component.literal(String.format(
                                "Block penalty cache: walkOn=%d, walkThrough=%d", 
                                penaltyStats.walkOnCacheSize, penaltyStats.walkThroughCacheSize)), false);
                            source.sendSuccess(() -> Component.literal(String.format(
                                "Flat penalty cache: professions=%d, entries=%d, global=%s", 
                                flatStats.professionsLoaded, flatStats.totalEntries, flatStats.globalPenaltiesLoaded)), false);
                            source.sendSuccess(() -> Component.literal(String.format(
                                "Profession cache: valid=%d, total=%d (%.1f%% hit rate)", 
                                professionStats.validEntries, professionStats.totalEntries,
                                professionStats.totalEntries > 0 ? 
                                    (professionStats.validEntries * 100.0 / professionStats.totalEntries) : 0.0)), false);
                            source.sendSuccess(() -> Component.literal(String.format(
                                "Tick-local cache: size=%d, tick=%d", 
                                tickStats.cacheSize, tickStats.lastTick)), false);
                            source.sendSuccess(() -> Component.literal(String.format(
                                "🚀 GLOBAL tick cache: malus=%d(%.1f%%), nodeType=%d(%.1f%%), tick=%d", 
                                globalStats.malusCacheSize, globalStats.getMalusHitRate(),
                                globalStats.nodeTypeCacheSize, globalStats.getNodeTypeHitRate(),
                                globalStats.currentTick)), false);
                            source.sendSuccess(() -> Component.literal(String.format(
                                "ThreadLocal BlockPos pool: ~%d instances", 
                                poolStats.estimatedInstances)), false);
                            
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(Commands.literal("clear-cache")
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> {
                            BlockPenaltyCache.clear();
                            FlatPenaltyCache.clear();
                            ProfessionCache.clear();
                            TickLocalCache.clear();
                            VillagerGlobalTickCache.reset();
                            BlockPosPool.clear();
                            
                            ctx.getSource().sendSuccess(() -> Component.literal("All caches cleared including GLOBAL tick cache"), true);
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(Commands.literal("reset-stats")
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> {
                            PerformanceTracker.reset();
                            
                            ctx.getSource().sendSuccess(() -> Component.literal("Performance statistics reset"), true);
                            return Command.SINGLE_SUCCESS;
                        })));
    }
} 