package joshie.vbc;

import joshie.vbc.config.ConfigManager;
import joshie.vbc.config.PenaltyCache;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import joshie.vbc.commands.MoveVillagerCommand;
import static joshie.vbc.config.ConfigManager.loadConfig;

#if FABRIC
    import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
    import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
    import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
    import net.fabricmc.api.ClientModInitializer;
    import net.fabricmc.api.ModInitializer;
    #if mc >= 215
    #elif after_21_1
    #endif

    #if current_20_1
    #endif
#endif

#if FORGE
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
#endif


#if NEO
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
#endif


#if FORGELIKE
@Mod("villager_brain_config")
#endif
public class VillagerBrainConfig #if FABRIC implements ModInitializer, ClientModInitializer #endif
{
    public static final String MODNAME = "Villager Brain Config";
    public static final String ID = "villager_brain_config";
    public static final Logger LOGGER = LogManager.getLogger(MODNAME);

    public VillagerBrainConfig(#if NEO IEventBus modEventBus, ModContainer modContainer #endif) {
        #if FORGE
        var context = FMLJavaModLoadingContext.get();
        var modEventBus = context.getModEventBus();
        #endif

        #if FORGELIKE
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        NeoForge.EVENT_BUS.register(this);
        #endif
    }


    #if FABRIC @Override #endif
    public void onInitialize() {
        #if FABRIC
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            MoveVillagerCommand.register(dispatcher);
            joshie.vbc.commands.PerformanceCommand.register(dispatcher);
        });
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            PenaltyCache.clear();
            if (server.getTickCount() % 20 == 0) {
                joshie.vbc.config.ProfessionCache.cleanupDeadReferences();
            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerLevel level = server.overworld();
            ConfigManager.processAvoidEntries(level);
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            PenaltyCache.clear();
            joshie.vbc.config.BlockPenaltyCache.clear();
            joshie.vbc.config.FlatPenaltyCache.clear();
            joshie.vbc.config.ProfessionCache.clear();
            joshie.vbc.config.TickLocalCache.clear();
            joshie.vbc.config.VillagerGlobalTickCache.reset();
            joshie.vbc.utils.BlockPosPool.clear();
        });
        #endif
        loadConfig();
    }

    #if FABRIC @Override #endif
    public void onInitializeClient() {
        #if AFTER_21_1
            #if FABRIC
            #endif
        #endif
    }


    #if FORGELIKE
    public void commonSetup(FMLCommonSetupEvent event) { onInitialize(); }
    public void clientSetup(FMLClientSetupEvent event) { onInitializeClient(); }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Pre event) {
        PenaltyCache.clear();
        if (event.getServer().getTickCount() % 20 == 0) {
            joshie.vbc.config.ProfessionCache.cleanupDeadReferences();
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        ServerLevel level = server.overworld();
        ConfigManager.processAvoidEntries(level);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        MoveVillagerCommand.register(event.getDispatcher());
        joshie.vbc.commands.PerformanceCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        PenaltyCache.clear();
        joshie.vbc.config.BlockPenaltyCache.clear();
        joshie.vbc.config.FlatPenaltyCache.clear();
        joshie.vbc.config.ProfessionCache.clear();
        joshie.vbc.config.TickLocalCache.clear();
        joshie.vbc.config.VillagerGlobalTickCache.reset();
        joshie.vbc.utils.BlockPosPool.clear();
    }

    #endif
}
