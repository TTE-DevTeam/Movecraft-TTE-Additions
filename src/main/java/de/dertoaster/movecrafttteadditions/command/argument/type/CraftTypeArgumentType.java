package de.dertoaster.movecrafttteadditions.command.argument.type;

import com.google.common.base.Predicates;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.type.CraftType;
import org.bukkit.permissions.Permissible;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class CraftTypeArgumentType implements CustomArgumentType.Converted<CraftType, String> {

    @Override
    public CraftType convert(String value) throws CommandSyntaxException {
        if (value != null) {
            CraftType type = CraftManager.getInstance().getCraftTypeFromString(value);
            return type;
        }
        return null;
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        S s = context.getSource();
        Predicate<CraftType> checkFunction;
        if (s instanceof Permissible permissible) {
            checkFunction = (craftType) -> permissible.hasPermission("movecraft." + craftType.getStringProperty(CraftType.NAME) + ".pilot");
        } else {
            checkFunction = Predicates.alwaysTrue();
        }
        CraftManager.getInstance().getCraftTypes().forEach(ct -> {
            if (checkFunction.test(ct)) {
                builder.suggest(ct.getStringProperty(CraftType.NAME));
            }
        });
        return builder.buildFuture();
    }

}
