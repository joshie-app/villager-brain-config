package joshie.vbc.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

public class MoveVillagerCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("movevillager")
                .then(Commands.argument("target", EntityArgument.entity())
                        .then(Commands.argument("destination", Vec3Argument.vec3())
                                .executes(ctx -> {
                                    try {
                                        Entity entity = EntityArgument.getEntity(ctx, "target");
                                        Vec3 destination = Vec3Argument.getVec3(ctx, "destination");

                                        if (entity instanceof Villager villager) {
                                            if (moveVillagerTo(villager, destination)) {
                                                ctx.getSource().sendSuccess(() -> Component.literal("Villager is moving!"), true);
                                                return Command.SINGLE_SUCCESS;
                                            } else {
                                                ctx.getSource().sendFailure(Component.literal("Villager cannot find a path!"));
                                                return 0;
                                            }
                                        } else {
                                            ctx.getSource().sendFailure(Component.literal("Target is not a villager!"));
                                            return 0;
                                        }
                                    } catch (CommandSyntaxException e) {
                                        ctx.getSource().sendFailure(Component.literal("Invalid command usage!"));
                                        return 0;
                                    }
                                }))));
    }

    private static boolean moveVillagerTo(Villager villager, Vec3 destination) {
        PathNavigation navigation = villager.getNavigation();
        if (navigation.createPath(destination.x(), destination.y(), destination.z(), 1) != null) {
            navigation.moveTo(destination.x(), destination.y(), destination.z(), 0.5);
            return true;
        }
        return false;
    }
}