package joshie.vbc.mixins;

import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WalkNodeEvaluator.class)
public interface WalkNodeEvaluatorAccessor {
    @Invoker("isDiagonalValid")
    boolean callIsDiagonalValid(Node center, Node neighborA, Node neighborB);
}
