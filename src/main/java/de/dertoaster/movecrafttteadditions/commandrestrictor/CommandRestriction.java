package de.dertoaster.movecrafttteadditions.commandrestrictor;

import de.dertoaster.movecrafttteadditions.TTEAdditionsPlugin;
import de.dertoaster.movecrafttteadditions.init.TTEAdditionsCraftDataTags;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.sign.AbstractMovecraftSign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record CommandRestriction(
    String command,
    Pattern shouldBLockRegEx,
    Set<Class<? extends AbstractMovecraftSign>> possibleSigns
) implements ConfigurationSerializable {

    public static final Map<String, List<CommandRestriction>> COMMAND_TO_RESTRICTION = new ConcurrentHashMap<>();

    public CommandRestriction(String command, Pattern shouldBLockRegEx,  Set<Class<? extends AbstractMovecraftSign>> possibleSigns) {
        this.command = command;
        this.shouldBLockRegEx = shouldBLockRegEx;
        this.possibleSigns = possibleSigns;

        COMMAND_TO_RESTRICTION.computeIfAbsent(command, k -> new ArrayList<>()).add(this);
    }

    public static CommandRestriction deserialize(Map<String, Object> rawData) {
        try {
            String command = (String) rawData.get("Command");
            Pattern shouldBLockRegEx = Pattern.compile((String) rawData.get("ValidationRegEx"), Pattern.CASE_INSENSITIVE);
            Set<Class<? extends AbstractMovecraftSign>> possibleSigns = new HashSet<>();
            List<String> list = (List<String>) rawData.get("PossibleSigns");
            for(String cls : list) {
                try {
                    Class clazz = Class.forName(cls);
                    if (AbstractMovecraftSign.class.isAssignableFrom(clazz)) {
                        possibleSigns.add(clazz);
                    } else {
                        TTEAdditionsPlugin.getInstance().getLogger().warning("Specified sign <" + cls + "> is not a subclass of AbstractMovecraftSign!");
                    }
                } catch (ClassNotFoundException e) {
                    TTEAdditionsPlugin.getInstance().getLogger().warning("Specified sign <" + cls + "> cant be found!");
                }
            }

            return new CommandRestriction(command, shouldBLockRegEx, possibleSigns);
        } catch(ClassCastException | NullPointerException exception) {
            throw new RuntimeException("Invalid configuration data!");
        }
    }

    public boolean restricts(final String commandString, final @NotNull Craft craft) {
        Matcher matcher = this.shouldBLockRegEx().matcher(commandString);
        if (!matcher.find()) {
            return false;
        }
        // Matched the command string, check signs!
        Set<Class<? extends AbstractMovecraftSign>> signsAboardCraft = craft.getDataTag(TTEAdditionsCraftDataTags.SIGNS_ON_CRAFT);
        if (signsAboardCraft == null || signsAboardCraft.isEmpty()) {
            return true;
        } else {
            for (Class<? extends AbstractMovecraftSign> signClass : signsAboardCraft) {
                for (Class<? extends AbstractMovecraftSign> checkSignClass : this.possibleSigns()) {
                    if (checkSignClass.isAssignableFrom(signClass)) {
                        // We found a sign! The command is not restricted
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> serialized = Map.of(
                "Command", this.command(),
                "ValidationRegEx", this.shouldBLockRegEx().pattern()
        );

        List<String> classNames = new ArrayList<>();
        this.possibleSigns().forEach(className -> {
            // TODO: Check if this is actually correct
            classNames.add(className.toString());
        });

        serialized.put("PossibleSigns", classNames);

        return serialized;
    }

}
