package joshie.vbc.config;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ProcessedAvoidEntry {
    public int radius;
    public double penalty;
    public boolean isTag;
    public Block block;
    public TagKey<Block> tag;

    public boolean matches(Block target) {
        if (isTag) return target.builtInRegistryHolder().is(tag);
        return target == block;
    }
}