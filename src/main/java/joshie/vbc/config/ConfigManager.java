package joshie.vbc.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import joshie.vbc.utils.PerformanceTracker;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    public static final Path CONFIG_PATH = Path.of("config/villager_brain_config.json");
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static ModConfig CONFIG = new ModConfig();
    public static Map<String, List<ProcessedAvoidEntry>> processedAvoids = new HashMap<>();


    public static void loadConfig() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                CONFIG = GSON.fromJson(reader, ModConfig.class);

                if (CONFIG.professions == null) {
                    CONFIG.professions = new HashMap<>();
                }

            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
            }
        } else {
            generateDefaultConfig();
            saveConfig();
        }
        
        BlockPenaltyCache.clear();
        BlockPenaltyCache.precomputeTagPenalties();
        ProfessionCache.clear();
        
        FlatPenaltyCache.initialize();
    }

    public static void saveConfig() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(CONFIG, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void processAvoidEntries(Level level) {
        #if mc <= 211
        Registry<Block> blockRegistry = level.registryAccess().registryOrThrow(Registries.BLOCK);
        #else
        Registry<Block> blockRegistry = level.registryAccess().lookupOrThrow(Registries.BLOCK);
        #endif
        for (Map.Entry<String, ModConfig> entry : CONFIG.professions.entrySet()) {
            String profession = entry.getKey();
            List<AvoidEntry> rawList = entry.getValue().avoids;
            if (rawList == null) continue;

            List<ProcessedAvoidEntry> processed = new ArrayList<>();

            for (AvoidEntry avoid : rawList) {
                ProcessedAvoidEntry p = new ProcessedAvoidEntry();
                p.radius = avoid.radius;
                p.penalty = avoid.penalty;

                if (avoid.block.startsWith("#")) {
                    p.isTag = true;
                    ResourceLocation tagId = ResourceLocation.tryParse(avoid.block.substring(1));
                    if (tagId != null) {
                        p.tag = TagKey.create(Registries.BLOCK, tagId);
                        processed.add(p);
                    }
                } else {
                    p.isTag = false;
                    ResourceLocation blockId = ResourceLocation.tryParse(avoid.block);
                    if (blockId != null && blockRegistry.containsKey(blockId)) {
                        #if mc <= 211
                        p.block = blockRegistry.get(blockId);
                        #else
                        if (blockRegistry.get(blockId).isPresent()) {
                            p.block = blockRegistry.get(blockId).get().value();
                        }
                        #endif
                        processed.add(p);
                    }
                }
            }

            processedAvoids.put(profession, processed);
        }
    }



    private static void generateDefaultConfig() {
        CONFIG.walkOnBlockPenalties.put("minecraft:lava", -1.0);
        CONFIG.walkOnBlockPenalties.put("minecraft:fire", -1.0);
        CONFIG.walkOnBlockPenalties.put("minecraft:soul_fire", -1.0);
        CONFIG.walkOnBlockPenalties.put("minecraft:magma_block", -1.0);
        CONFIG.walkOnBlockPenalties.put("minecraft:sweet_berry_bush", -1.0);
        CONFIG.walkOnBlockPenalties.put("minecraft:cactus", -1.0);
        CONFIG.walkOnBlockPenalties.put("minecraft:wither_rose", -1.0);
        CONFIG.walkOnBlockPenalties.put("minecraft:powder_snow", -1.0);
        CONFIG.walkOnBlockPenalties.put("minecraft:water", 32.0);
        CONFIG.walkOnBlockPenalties.put("minecraft:gravel", 2.0);
        CONFIG.walkOnBlockPenalties.put("minecraft:stone", 4.0);
        CONFIG.walkOnBlockPenalties.put("minecraft:cobblestone", 4.0);
        CONFIG.walkOnBlockPenalties.put("minecraft:farmland", 16.0);
        CONFIG.walkOnBlockPenalties.put("minecraft:dirt_path", 0.0);
        CONFIG.walkOnBlockPenalties.put("#minecraft:planks", 0.0);
        CONFIG.walkOnBlockPenalties.put("#minecraft:wool", 0.0);
        CONFIG.walkOnBlockPenalties.put("default", 4.0);

        CONFIG.walkThroughBlockPenalties.put("minecraft:lava", -1.0);
        CONFIG.walkThroughBlockPenalties.put("minecraft:fire", -1.0);
        CONFIG.walkThroughBlockPenalties.put("minecraft:soul_fire", -1.0);
        CONFIG.walkThroughBlockPenalties.put("minecraft:sweet_berry_bush", -1.0);
        CONFIG.walkThroughBlockPenalties.put("minecraft:cactus", -1.0);
        CONFIG.walkThroughBlockPenalties.put("minecraft:wither_rose", -1.0);
        CONFIG.walkThroughBlockPenalties.put("minecraft:powder_snow", -1.0);
        CONFIG.walkThroughBlockPenalties.put("minecraft:cobweb", 20.0);
        CONFIG.walkThroughBlockPenalties.put("minecraft:water", 32.0);
        CONFIG.walkThroughBlockPenalties.put("minecraft:air", 0.0);
        CONFIG.walkThroughBlockPenalties.put("minecraft:short_grass", 4.0);
        CONFIG.walkThroughBlockPenalties.put("minecraft:tall_grass", 8.0);
        CONFIG.walkThroughBlockPenalties.put("minecraft:fern", 4.0);
        CONFIG.walkThroughBlockPenalties.put("minecraft:large_fern", 8.0);
        CONFIG.walkThroughBlockPenalties.put("default", 0.0);

        CONFIG.actionsPenalties.put("jump", 10.0);

        CONFIG.avoids.add(new AvoidEntry("minecraft:lava", 5, -1.0));
        CONFIG.avoids.add(new AvoidEntry("minecraft:fire", 3, -1.0));
        CONFIG.avoids.add(new AvoidEntry("minecraft:soul_fire", 3, -1.0));
        CONFIG.avoids.add(new AvoidEntry("minecraft:magma_block", 2, -1.0));
        CONFIG.avoids.add(new AvoidEntry("minecraft:cactus", 2, -1.0));
        CONFIG.avoids.add(new AvoidEntry("minecraft:sweet_berry_bush", 2, -1.0));
        CONFIG.avoids.add(new AvoidEntry("minecraft:wither_rose", 3, -1.0));
        CONFIG.avoids.add(new AvoidEntry("minecraft:powder_snow", 2, -1.0));

        ModConfig armorerConfig = new ModConfig();
        armorerConfig.walkOnBlockPenalties.put("minecraft:stone", 0.0);
        armorerConfig.walkOnBlockPenalties.put("minecraft:cobblestone", 0.0);
        armorerConfig.walkOnBlockPenalties.put("minecraft:deepslate", 0.0);
        armorerConfig.walkOnBlockPenalties.put("minecraft:andesite", 0.0);
        armorerConfig.walkOnBlockPenalties.put("minecraft:diorite", 0.0);
        armorerConfig.walkOnBlockPenalties.put("minecraft:granite", 0.0);
        armorerConfig.walkOnBlockPenalties.put("minecraft:iron_block", 0.0);
        armorerConfig.walkOnBlockPenalties.put("minecraft:anvil", 0.0);
        armorerConfig.walkOnBlockPenalties.put("minecraft:chipped_anvil", 0.0);
        armorerConfig.walkOnBlockPenalties.put("minecraft:damaged_anvil", 0.0);
        armorerConfig.walkOnBlockPenalties.put("minecraft:blast_furnace", 0.0);
        armorerConfig.walkOnBlockPenalties.put("minecraft:smoker", 2.0);
        armorerConfig.walkOnBlockPenalties.put("minecraft:furnace", 2.0);
        armorerConfig.walkOnBlockPenalties.put("minecraft:water", -1.0);
        armorerConfig.walkOnBlockPenalties.put("minecraft:farmland", 20.0);
        armorerConfig.walkOnBlockPenalties.put("minecraft:grass_block", 8.0);
        armorerConfig.walkOnBlockPenalties.put("minecraft:dirt_path", 2.0);
        armorerConfig.walkOnBlockPenalties.put("default", 8.0);
        armorerConfig.walkThroughBlockPenalties.put("minecraft:tall_grass", 12.0);
        armorerConfig.walkThroughBlockPenalties.put("minecraft:large_fern", 12.0);
        armorerConfig.avoids.add(new AvoidEntry("minecraft:water", 3, 16.0));
        CONFIG.professions.put("minecraft:armorer", armorerConfig);

        ModConfig butcherConfig = new ModConfig();
        butcherConfig.walkOnBlockPenalties.put("minecraft:grass_block", 2.0);
        butcherConfig.walkOnBlockPenalties.put("minecraft:dirt", 2.0);
        butcherConfig.walkOnBlockPenalties.put("minecraft:coarse_dirt", 2.0);
        butcherConfig.walkOnBlockPenalties.put("minecraft:hay_block", 0.0);
        butcherConfig.walkOnBlockPenalties.put("minecraft:smoker", 0.0);
        butcherConfig.walkOnBlockPenalties.put("minecraft:furnace", 2.0);
        butcherConfig.walkOnBlockPenalties.put("minecraft:water", -1.0);
        butcherConfig.walkOnBlockPenalties.put("minecraft:farmland", 12.0);
        butcherConfig.walkOnBlockPenalties.put("minecraft:dirt_path", 0.0);
        butcherConfig.walkOnBlockPenalties.put("default", 8.0);
        butcherConfig.walkThroughBlockPenalties.put("minecraft:short_grass", 1.0);
        butcherConfig.walkThroughBlockPenalties.put("minecraft:tall_grass", 2.0);
        butcherConfig.avoids.add(new AvoidEntry("minecraft:composter", 2, 8.0));
        CONFIG.professions.put("minecraft:butcher", butcherConfig);

        ModConfig cartographerConfig = new ModConfig();
        cartographerConfig.walkOnBlockPenalties.put("minecraft:cartography_table", 0.0);
        cartographerConfig.walkOnBlockPenalties.put("minecraft:lectern", 0.0);
        cartographerConfig.walkOnBlockPenalties.put("minecraft:bookshelf", 0.0);
        cartographerConfig.walkOnBlockPenalties.put("minecraft:sand", 2.0);
        cartographerConfig.walkOnBlockPenalties.put("minecraft:red_sand", 2.0);
        cartographerConfig.walkOnBlockPenalties.put("minecraft:gravel", 2.0);
        cartographerConfig.walkOnBlockPenalties.put("minecraft:water", -1.0);
        cartographerConfig.walkOnBlockPenalties.put("minecraft:farmland", 16.0);
        cartographerConfig.walkOnBlockPenalties.put("minecraft:dirt_path", 0.0);
        cartographerConfig.walkOnBlockPenalties.put("minecraft:grass_block", 6.0);
        cartographerConfig.walkOnBlockPenalties.put("default", 8.0);
        cartographerConfig.walkThroughBlockPenalties.put("minecraft:tall_grass", 10.0);
        cartographerConfig.walkThroughBlockPenalties.put("minecraft:large_fern", 10.0);
        cartographerConfig.avoids.add(new AvoidEntry("minecraft:lava", 5, -1.0));
        CONFIG.professions.put("minecraft:cartographer", cartographerConfig);

        ModConfig clericConfig = new ModConfig();
        clericConfig.walkOnBlockPenalties.put("minecraft:stone", 0.0);
        clericConfig.walkOnBlockPenalties.put("minecraft:cobblestone", 0.0);
        clericConfig.walkOnBlockPenalties.put("minecraft:stone_bricks", 0.0);
        clericConfig.walkOnBlockPenalties.put("minecraft:brewing_stand", 0.0);
        clericConfig.walkOnBlockPenalties.put("minecraft:altar", 0.0);
        clericConfig.walkOnBlockPenalties.put("minecraft:enchanting_table", 0.0);
        clericConfig.walkOnBlockPenalties.put("minecraft:bookshelf", 0.0);
        clericConfig.walkOnBlockPenalties.put("minecraft:water", -1.0);
        clericConfig.walkOnBlockPenalties.put("minecraft:gravel", 4.0);
        clericConfig.walkOnBlockPenalties.put("minecraft:farmland", 12.0);
        clericConfig.walkOnBlockPenalties.put("minecraft:dirt_path", 2.0);
        clericConfig.walkOnBlockPenalties.put("minecraft:grass_block", 8.0);
        clericConfig.walkOnBlockPenalties.put("default", 8.0);
        clericConfig.walkThroughBlockPenalties.put("minecraft:tall_grass", 10.0);
        clericConfig.walkThroughBlockPenalties.put("minecraft:large_fern", 10.0);
        clericConfig.avoids.add(new AvoidEntry("minecraft:wither_rose", 5, -1.0));
        clericConfig.avoids.add(new AvoidEntry("minecraft:soul_fire", 4, -1.0));
        CONFIG.professions.put("minecraft:cleric", clericConfig);

        ModConfig farmerConfig = new ModConfig();
        farmerConfig.walkOnBlockPenalties.put("minecraft:farmland", 0.0);
        farmerConfig.walkOnBlockPenalties.put("minecraft:grass_block", 1.0);
        farmerConfig.walkOnBlockPenalties.put("minecraft:dirt", 1.0);
        farmerConfig.walkOnBlockPenalties.put("minecraft:coarse_dirt", 2.0);
        farmerConfig.walkOnBlockPenalties.put("minecraft:hay_block", 0.0);
        farmerConfig.walkOnBlockPenalties.put("minecraft:composter", 0.0);
        farmerConfig.walkOnBlockPenalties.put("minecraft:water", 24.0);
        farmerConfig.walkOnBlockPenalties.put("minecraft:dirt_path", 0.0);
        farmerConfig.walkOnBlockPenalties.put("minecraft:stone", 8.0);
        farmerConfig.walkOnBlockPenalties.put("minecraft:cobblestone", 8.0);
        farmerConfig.walkOnBlockPenalties.put("default", 8.0);
        farmerConfig.walkThroughBlockPenalties.put("minecraft:short_grass", 0.0);
        farmerConfig.walkThroughBlockPenalties.put("minecraft:tall_grass", 1.0);
        farmerConfig.walkThroughBlockPenalties.put("minecraft:wheat", 2.0);
        farmerConfig.walkThroughBlockPenalties.put("minecraft:carrots", 2.0);
        farmerConfig.walkThroughBlockPenalties.put("minecraft:potatoes", 2.0);
        farmerConfig.walkThroughBlockPenalties.put("minecraft:beetroots", 2.0);
        farmerConfig.avoids.add(new AvoidEntry("minecraft:farmland", 1, 32.0));
        CONFIG.professions.put("minecraft:farmer", farmerConfig);

        ModConfig fishermanConfig = new ModConfig();
        fishermanConfig.walkOnBlockPenalties.put("minecraft:water", 0.0);
        fishermanConfig.walkOnBlockPenalties.put("minecraft:sand", 2.0);
        fishermanConfig.walkOnBlockPenalties.put("minecraft:gravel", 1.0);
        fishermanConfig.walkOnBlockPenalties.put("minecraft:clay", 1.0);
        fishermanConfig.walkOnBlockPenalties.put("minecraft:barrel", 0.0);
        fishermanConfig.walkOnBlockPenalties.put("minecraft:prismarine", 0.0);
        fishermanConfig.walkOnBlockPenalties.put("minecraft:prismarine_bricks", 0.0);
        fishermanConfig.walkOnBlockPenalties.put("minecraft:dark_prismarine", 0.0);
        fishermanConfig.walkOnBlockPenalties.put("minecraft:sea_lantern", 0.0);
        fishermanConfig.walkOnBlockPenalties.put("minecraft:dirt_path", 2.0);
        fishermanConfig.walkOnBlockPenalties.put("minecraft:grass_block", 6.0);
        fishermanConfig.walkOnBlockPenalties.put("minecraft:farmland", 16.0);
        fishermanConfig.walkOnBlockPenalties.put("default", 8.0);
        fishermanConfig.walkThroughBlockPenalties.put("minecraft:water", 0.0);
        fishermanConfig.walkThroughBlockPenalties.put("minecraft:kelp", 2.0);
        fishermanConfig.walkThroughBlockPenalties.put("minecraft:seagrass", 2.0);
        fishermanConfig.walkThroughBlockPenalties.put("minecraft:tall_seagrass", 4.0);
        CONFIG.professions.put("minecraft:fisherman", fishermanConfig);

        ModConfig fletcherConfig = new ModConfig();
        fletcherConfig.walkOnBlockPenalties.put("minecraft:fletching_table", 0.0);
        fletcherConfig.walkOnBlockPenalties.put("minecraft:grass_block", 2.0);
        fletcherConfig.walkOnBlockPenalties.put("minecraft:dirt", 2.0);
        fletcherConfig.walkOnBlockPenalties.put("minecraft:sand", 4.0);
        fletcherConfig.walkOnBlockPenalties.put("minecraft:gravel", 2.0);
        fletcherConfig.walkOnBlockPenalties.put("minecraft:water", -1.0);
        fletcherConfig.walkOnBlockPenalties.put("minecraft:farmland", 16.0);
        fletcherConfig.walkOnBlockPenalties.put("minecraft:dirt_path", 0.0);
        fletcherConfig.walkOnBlockPenalties.put("default", 8.0);
        fletcherConfig.walkThroughBlockPenalties.put("minecraft:short_grass", 1.0);
        fletcherConfig.walkThroughBlockPenalties.put("minecraft:tall_grass", 2.0);
        fletcherConfig.walkThroughBlockPenalties.put("minecraft:fern", 1.0);
        fletcherConfig.walkThroughBlockPenalties.put("minecraft:large_fern", 2.0);
        fletcherConfig.avoids.add(new AvoidEntry("minecraft:lava", 4, -1.0));
        CONFIG.professions.put("minecraft:fletcher", fletcherConfig);

        ModConfig leatherworkerConfig = new ModConfig();
        leatherworkerConfig.walkOnBlockPenalties.put("minecraft:cauldron", 0.0);
        leatherworkerConfig.walkOnBlockPenalties.put("minecraft:water_cauldron", 0.0);
        leatherworkerConfig.walkOnBlockPenalties.put("minecraft:grass_block", 2.0);
        leatherworkerConfig.walkOnBlockPenalties.put("minecraft:dirt", 2.0);
        leatherworkerConfig.walkOnBlockPenalties.put("minecraft:hay_block", 0.0);
        leatherworkerConfig.walkOnBlockPenalties.put("minecraft:water", -1.0);
        leatherworkerConfig.walkOnBlockPenalties.put("minecraft:farmland", 12.0);
        leatherworkerConfig.walkOnBlockPenalties.put("minecraft:dirt_path", 0.0);
        leatherworkerConfig.walkOnBlockPenalties.put("default", 8.0);
        leatherworkerConfig.walkThroughBlockPenalties.put("minecraft:short_grass", 1.0);
        leatherworkerConfig.walkThroughBlockPenalties.put("minecraft:tall_grass", 2.0);
        leatherworkerConfig.avoids.add(new AvoidEntry("minecraft:lava", 3, -1.0));
        CONFIG.professions.put("minecraft:leatherworker", leatherworkerConfig);

        ModConfig librarianConfig = new ModConfig();
        librarianConfig.walkOnBlockPenalties.put("minecraft:bookshelf", 0.0);
        librarianConfig.walkOnBlockPenalties.put("minecraft:lectern", 0.0);
        librarianConfig.walkOnBlockPenalties.put("minecraft:enchanting_table", 0.0);
        librarianConfig.walkOnBlockPenalties.put("minecraft:cartography_table", 2.0);
        librarianConfig.walkOnBlockPenalties.put("minecraft:stone", 2.0);
        librarianConfig.walkOnBlockPenalties.put("minecraft:cobblestone", 2.0);
        librarianConfig.walkOnBlockPenalties.put("minecraft:stone_bricks", 0.0);
        librarianConfig.walkOnBlockPenalties.put("minecraft:water", -1.0);
        librarianConfig.walkOnBlockPenalties.put("minecraft:farmland", 20.0);
        librarianConfig.walkOnBlockPenalties.put("minecraft:dirt_path", 2.0);
        librarianConfig.walkOnBlockPenalties.put("minecraft:grass_block", 10.0);
        librarianConfig.walkOnBlockPenalties.put("default", 8.0);
        librarianConfig.walkThroughBlockPenalties.put("minecraft:tall_grass", 12.0);
        librarianConfig.walkThroughBlockPenalties.put("minecraft:large_fern", 12.0);
        librarianConfig.avoids.add(new AvoidEntry("minecraft:water", 3, 20.0));
        librarianConfig.avoids.add(new AvoidEntry("minecraft:lava", 5, -1.0));
        CONFIG.professions.put("minecraft:librarian", librarianConfig);

        ModConfig masonConfig = new ModConfig();
        masonConfig.walkOnBlockPenalties.put("minecraft:stonecutter", 0.0);
        masonConfig.walkOnBlockPenalties.put("minecraft:stone", 0.0);
        masonConfig.walkOnBlockPenalties.put("minecraft:cobblestone", 0.0);
        masonConfig.walkOnBlockPenalties.put("minecraft:stone_bricks", 0.0);
        masonConfig.walkOnBlockPenalties.put("minecraft:smooth_stone", 0.0);
        masonConfig.walkOnBlockPenalties.put("minecraft:andesite", 0.0);
        masonConfig.walkOnBlockPenalties.put("minecraft:diorite", 0.0);
        masonConfig.walkOnBlockPenalties.put("minecraft:granite", 0.0);
        masonConfig.walkOnBlockPenalties.put("minecraft:deepslate", 0.0);
        masonConfig.walkOnBlockPenalties.put("minecraft:blackstone", 0.0);
        masonConfig.walkOnBlockPenalties.put("minecraft:gravel", 2.0);
        masonConfig.walkOnBlockPenalties.put("minecraft:water", -1.0);
        masonConfig.walkOnBlockPenalties.put("minecraft:farmland", 20.0);
        masonConfig.walkOnBlockPenalties.put("minecraft:dirt_path", 4.0);
        masonConfig.walkOnBlockPenalties.put("minecraft:grass_block", 10.0);
        masonConfig.walkOnBlockPenalties.put("default", 8.0);
        masonConfig.walkThroughBlockPenalties.put("minecraft:tall_grass", 12.0);
        masonConfig.walkThroughBlockPenalties.put("minecraft:large_fern", 12.0);
        masonConfig.avoids.add(new AvoidEntry("minecraft:water", 3, 16.0));
        CONFIG.professions.put("minecraft:mason", masonConfig);

        ModConfig shepherdConfig = new ModConfig();
        shepherdConfig.walkOnBlockPenalties.put("minecraft:loom", 0.0);
        shepherdConfig.walkOnBlockPenalties.put("minecraft:grass_block", 0.0);
        shepherdConfig.walkOnBlockPenalties.put("minecraft:dirt", 1.0);
        shepherdConfig.walkOnBlockPenalties.put("minecraft:coarse_dirt", 2.0);
        shepherdConfig.walkOnBlockPenalties.put("#minecraft:wool", 0.0);
        shepherdConfig.walkOnBlockPenalties.put("minecraft:hay_block", 0.0);
        shepherdConfig.walkOnBlockPenalties.put("minecraft:water", -1.0);
        shepherdConfig.walkOnBlockPenalties.put("minecraft:farmland", 12.0);
        shepherdConfig.walkOnBlockPenalties.put("minecraft:dirt_path", 0.0);
        shepherdConfig.walkOnBlockPenalties.put("minecraft:stone", 8.0);
        shepherdConfig.walkOnBlockPenalties.put("minecraft:cobblestone", 8.0);
        shepherdConfig.walkOnBlockPenalties.put("default", 8.0);
        shepherdConfig.walkThroughBlockPenalties.put("minecraft:short_grass", 0.0);
        shepherdConfig.walkThroughBlockPenalties.put("minecraft:tall_grass", 1.0);
        shepherdConfig.walkThroughBlockPenalties.put("minecraft:fern", 0.0);
        shepherdConfig.walkThroughBlockPenalties.put("minecraft:large_fern", 1.0);
        shepherdConfig.avoids.add(new AvoidEntry("minecraft:lava", 4, -1.0));
        CONFIG.professions.put("minecraft:shepherd", shepherdConfig);

        ModConfig toolsmithConfig = new ModConfig();
        toolsmithConfig.walkOnBlockPenalties.put("minecraft:smithing_table", 0.0);
        toolsmithConfig.walkOnBlockPenalties.put("minecraft:stone", 0.0);
        toolsmithConfig.walkOnBlockPenalties.put("minecraft:cobblestone", 0.0);
        toolsmithConfig.walkOnBlockPenalties.put("minecraft:anvil", 0.0);
        toolsmithConfig.walkOnBlockPenalties.put("minecraft:chipped_anvil", 0.0);
        toolsmithConfig.walkOnBlockPenalties.put("minecraft:damaged_anvil", 0.0);
        toolsmithConfig.walkOnBlockPenalties.put("minecraft:iron_block", 0.0);
        toolsmithConfig.walkOnBlockPenalties.put("minecraft:furnace", 2.0);
        toolsmithConfig.walkOnBlockPenalties.put("minecraft:blast_furnace", 2.0);
        toolsmithConfig.walkOnBlockPenalties.put("minecraft:water", -1.0);
        toolsmithConfig.walkOnBlockPenalties.put("minecraft:farmland", 20.0);
        toolsmithConfig.walkOnBlockPenalties.put("minecraft:dirt_path", 2.0);
        toolsmithConfig.walkOnBlockPenalties.put("minecraft:grass_block", 8.0);
        toolsmithConfig.walkOnBlockPenalties.put("default", 8.0);
        toolsmithConfig.walkThroughBlockPenalties.put("minecraft:tall_grass", 10.0);
        toolsmithConfig.walkThroughBlockPenalties.put("minecraft:large_fern", 10.0);
        toolsmithConfig.avoids.add(new AvoidEntry("minecraft:water", 3, 16.0));
        CONFIG.professions.put("minecraft:toolsmith", toolsmithConfig);

        ModConfig weaponsmithConfig = new ModConfig();
        weaponsmithConfig.walkOnBlockPenalties.put("minecraft:grindstone", 0.0);
        weaponsmithConfig.walkOnBlockPenalties.put("minecraft:stone", 1.0);
        weaponsmithConfig.walkOnBlockPenalties.put("minecraft:cobblestone", 1.0);
        weaponsmithConfig.walkOnBlockPenalties.put("minecraft:anvil", 0.0);
        weaponsmithConfig.walkOnBlockPenalties.put("minecraft:chipped_anvil", 0.0);
        weaponsmithConfig.walkOnBlockPenalties.put("minecraft:damaged_anvil", 0.0);
        weaponsmithConfig.walkOnBlockPenalties.put("minecraft:iron_block", 0.0);
        weaponsmithConfig.walkOnBlockPenalties.put("minecraft:furnace", 2.0);
        weaponsmithConfig.walkOnBlockPenalties.put("minecraft:blast_furnace", 2.0);
        weaponsmithConfig.walkOnBlockPenalties.put("minecraft:lava", -1.0);
        weaponsmithConfig.walkOnBlockPenalties.put("minecraft:water", -1.0);
        weaponsmithConfig.walkOnBlockPenalties.put("minecraft:farmland", 20.0);
        weaponsmithConfig.walkOnBlockPenalties.put("minecraft:dirt_path", 2.0);
        weaponsmithConfig.walkOnBlockPenalties.put("minecraft:grass_block", 8.0);
        weaponsmithConfig.walkOnBlockPenalties.put("default", 8.0);
        weaponsmithConfig.walkThroughBlockPenalties.put("minecraft:air", 2.0);
        weaponsmithConfig.walkThroughBlockPenalties.put("minecraft:tall_grass", 10.0);
        weaponsmithConfig.walkThroughBlockPenalties.put("minecraft:large_fern", 10.0);
        weaponsmithConfig.avoids.add(new AvoidEntry("minecraft:water", 3, 16.0));
        CONFIG.professions.put("minecraft:weaponsmith", weaponsmithConfig);

        ModConfig nitwitConfig = new ModConfig();
        nitwitConfig.walkOnBlockPenalties.put("minecraft:gravel", 4.0);
        nitwitConfig.walkOnBlockPenalties.put("minecraft:sand", 4.0);
        nitwitConfig.walkOnBlockPenalties.put("minecraft:dirt", 2.0);
        nitwitConfig.walkOnBlockPenalties.put("minecraft:grass_block", 2.0);
        nitwitConfig.walkOnBlockPenalties.put("minecraft:water", 8.0);
        nitwitConfig.walkOnBlockPenalties.put("minecraft:farmland", 8.0);
        nitwitConfig.walkOnBlockPenalties.put("minecraft:dirt_path", 2.0);
        nitwitConfig.walkOnBlockPenalties.put("default", 4.0);
        nitwitConfig.walkThroughBlockPenalties.put("minecraft:water", 0.0);
        nitwitConfig.walkThroughBlockPenalties.put("minecraft:short_grass", 1.0);
        nitwitConfig.walkThroughBlockPenalties.put("minecraft:tall_grass", 1.0);
        nitwitConfig.walkThroughBlockPenalties.put("minecraft:fern", 1.0);
        nitwitConfig.walkThroughBlockPenalties.put("minecraft:large_fern", 1.0);
        nitwitConfig.avoids.add(new AvoidEntry("minecraft:bookshelf", 4, 12.0));
        nitwitConfig.avoids.add(new AvoidEntry("minecraft:lectern", 3, 8.0));
        nitwitConfig.avoids.add(new AvoidEntry("minecraft:enchanting_table", 3, 8.0));
        CONFIG.professions.put("minecraft:nitwit", nitwitConfig);
    }

    public static double getWalkOnBlockPenalty(String blockId, String profession) {
        return BlockPenaltyCache.getWalkOnBlockPenalty(blockId, profession);
    }

    public static double getWalkThroughBlockPenalty(String blockId, String profession) {
        return BlockPenaltyCache.getWalkThroughBlockPenalty(blockId, profession);
    }
    
    public static double computeWalkOnBlockPenalty(String blockId, String profession) {
        long startTime = PerformanceTracker.startTimer();
        try {
            return getPenalty(blockId, profession, CONFIG.walkOnBlockPenalties, "walkOnBlockPenalties");
        } finally {
            if (startTime > 0) {
                PerformanceTracker.recordPenaltyLookup(System.nanoTime() - startTime);
            }
        }
    }

    public static double computeWalkThroughBlockPenalty(String blockId, String profession) {
        long startTime = PerformanceTracker.startTimer();
        try {
            return getPenalty(blockId, profession, CONFIG.walkThroughBlockPenalties, "walkThroughBlockPenalties");
        } finally {
            if (startTime > 0) {
                PerformanceTracker.recordPenaltyLookup(System.nanoTime() - startTime);
            }
        }
    }


    private static double getPenalty(String blockId, String profession, Map<String, Double> globalPenalties, String penaltyType) {
        if (CONFIG.professions.containsKey(profession)) {
            Map<String, Double> professionPenalties;
            if (penaltyType.equals("walkOnBlockPenalties")) {
                professionPenalties = CONFIG.professions.get(profession).walkOnBlockPenalties;
            } else {
                professionPenalties = CONFIG.professions.get(profession).walkThroughBlockPenalties;
            }

            if (professionPenalties.containsKey(blockId)) return professionPenalties.get(blockId);

            double tagPenalty = getTagPenalty(blockId, professionPenalties);
            if (tagPenalty != -1.0) return tagPenalty;
        }

        if (globalPenalties.containsKey(blockId)) return globalPenalties.get(blockId);

        double tagPenalty = getTagPenalty(blockId, globalPenalties);
        if (tagPenalty != -1.0) return tagPenalty;

        return globalPenalties.getOrDefault("default", 4.0);
    }

    private static double getTagPenalty(String blockId, Map<String, Double> penalties) {
        String[] parts = blockId.split(":");
        if (parts.length != 2) return -1.0;

        ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(parts[0].substring(1), parts[1]);
        Block block = null;
        #if mc <= 211
        block = BuiltInRegistries.BLOCK.get(resourceLocation);
        #else
        if (BuiltInRegistries.BLOCK.get(resourceLocation).isPresent()) {
            block = BuiltInRegistries.BLOCK.get(resourceLocation).get().value();
        }
        #endif
        if (block != null) {
            for (String key : penalties.keySet()) {
                if (key.startsWith("#")) {
                    TagKey<Block> blockTag = TagKey.create(BuiltInRegistries.BLOCK.key(),
                            ResourceLocation.fromNamespaceAndPath(key.substring(1).split(":")[0], key.substring(1).split(":")[1]));
                    if (block.builtInRegistryHolder().is(blockTag)) {
                        return penalties.get(key);
                    }
                }
            }

            Item item = block.asItem();
            if (item != null) {
                for (String key : penalties.keySet()) {
                    if (key.startsWith("#")) {
                        TagKey<Item> itemTag = TagKey.create(BuiltInRegistries.ITEM.key(),
                                ResourceLocation.fromNamespaceAndPath(key.substring(1).split(":")[0], key.substring(1).split(":")[1]));
                        if (item.builtInRegistryHolder().is(itemTag)) {
                            return penalties.get(key);
                        }
                    }
                }
            }
        }

        return -1.0;
    }
    public static Float getActionPenalty(String professionId, String actionKey) {
        ModConfig p = CONFIG.professions.get(professionId);
        if (p != null && p.actionsPenalties.containsKey(actionKey)) {
            return p.actionsPenalties.get(actionKey).floatValue() + CONFIG.actionsPenalties.getOrDefault(actionKey, 0.0).floatValue();
        }
        return CONFIG.actionsPenalties.getOrDefault(actionKey, 0.0).floatValue();
    }

}
