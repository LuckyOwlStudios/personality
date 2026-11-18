package net.luckyowlstudios.personality.trait;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * {@code /personality traits <targets> [add|remove <trait> | reroll]}: inspect and edit animal traits (ops only).
 */
public final class TraitCommands {

    private static final DynamicCommandExceptionType UNKNOWN_TRAIT =
            new DynamicCommandExceptionType(id -> Component.translatable("commands.personality.traits.unknown", String.valueOf(id)));

    private static final SuggestionProvider<CommandSourceStack> TRAIT_SUGGESTIONS =
            (context, builder) -> SharedSuggestionProvider.suggestResource(Traits.all().stream().map(Trait::id), builder);

    private TraitCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("personality")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("traits")
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .executes(TraitCommands::list)
                                .then(Commands.literal("add")
                                        .then(Commands.argument("trait", ResourceLocationArgument.id())
                                                .suggests(TRAIT_SUGGESTIONS)
                                                .executes(context -> edit(context, (current, trait) -> {
                                                    Set<Trait> next = new LinkedHashSet<>();
                                                    for (Trait other : current) {
                                                        if (other.isCompatibleWith(trait)) {
                                                            next.add(other);
                                                        }
                                                    }
                                                    next.add(trait);
                                                    return next;
                                                }))))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("trait", ResourceLocationArgument.id())
                                                .suggests(TRAIT_SUGGESTIONS)
                                                .executes(context -> edit(context, (current, trait) -> {
                                                    Set<Trait> next = new LinkedHashSet<>(current);
                                                    next.remove(trait);
                                                    return next;
                                                }))))
                                .then(Commands.literal("reroll")
                                        .executes(TraitCommands::reroll)))));
    }

    private static int list(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        List<Mob> mobs = holders(EntityArgument.getEntities(context, "targets"));
        for (Mob mob : mobs) {
            report(context.getSource(), mob);
        }
        if (mobs.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("commands.personality.traits.no_animals"));
        }
        return mobs.size();
    }

    private static int edit(CommandContext<CommandSourceStack> context, BiFunction<Set<Trait>, Trait, Set<Trait>> change) throws CommandSyntaxException {
        ResourceLocation id = ResourceLocationArgument.getId(context, "trait");
        Trait trait = Traits.get(id);
        if (trait == null) {
            throw UNKNOWN_TRAIT.create(id);
        }
        List<Mob> mobs = holders(EntityArgument.getEntities(context, "targets"));
        for (Mob mob : mobs) {
            TraitHolder holder = (TraitHolder) mob;
            holder.personality$setTraits(change.apply(holder.personality$getTraits(), trait));
            report(context.getSource(), mob);
        }
        return mobs.size();
    }

    private static int reroll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        List<Mob> mobs = holders(EntityArgument.getEntities(context, "targets"));
        for (Mob mob : mobs) {
            ((TraitHolder) mob).personality$setTraits(Traits.roll(mob.getRandom()));
            report(context.getSource(), mob);
        }
        return mobs.size();
    }

    private static List<Mob> holders(Collection<? extends Entity> entities) {
        List<Mob> mobs = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity instanceof Mob mob && mob instanceof TraitHolder) {
                mobs.add(mob);
            }
        }
        return mobs;
    }

    private static void report(CommandSourceStack source, Mob mob) {
        Set<Trait> traits = ((TraitHolder) mob).personality$getTraits();
        Component names = traits.isEmpty()
                ? Component.translatable("commands.personality.traits.none").withStyle(ChatFormatting.GRAY)
                : ComponentUtils.formatList(traits, trait -> {
                    MutableComponent name = trait.displayName().copy().withStyle(ChatFormatting.GOLD);
                    return name.withStyle(style -> style.withHoverEvent(new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT, trait.description())));
                });
        source.sendSuccess(() -> Component.translatable("commands.personality.traits.list", mob.getDisplayName(), names), false);
    }
}
