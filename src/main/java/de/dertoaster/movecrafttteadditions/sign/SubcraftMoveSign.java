package de.dertoaster.movecrafttteadditions.sign;

import de.dertoaster.movecrafttteadditions.craft.SubcraftMoveCraft;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.SubCraft;
import net.countercraft.movecraft.craft.SubCraftImpl;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.CraftPilotEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.processing.functions.Result;
import net.countercraft.movecraft.sign.AbstractSubcraftSign;
import net.countercraft.movecraft.sign.SignListener;
import net.countercraft.movecraft.util.Pair;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public class SubcraftMoveSign extends AbstractSubcraftSign {

    public static final String OPERATION = "MOVE";

    public static final Component DEFAULT_LINE_3 = Component.text("-->");
    public static final Component DEFAULT_LINE_4 = Component.text("<--");

    public SubcraftMoveSign(Function<String, @Nullable CraftType> craftTypeRetrievalFunction, Supplier<Plugin> plugin) {
        super(craftTypeRetrievalFunction, plugin);
    }

    protected SignListener.SignWrapper signWrapperCur = null;

    // TODO: Pass the signWrapper to the detect task later!
    @Override
    protected boolean internalProcessSignWithCraft(Action clickType, SignListener.SignWrapper sign, @Nullable Craft craft, Player player) {
        this.signWrapperCur = sign;
        boolean result = super.internalProcessSignWithCraft(clickType, sign, craft, player);
        this.signWrapperCur = null;
        return result;
    }

    protected Vector getMovementVector(SignListener.SignWrapper wrapper, CraftType craftType) {
        final int MAX_MOVEMENT = craftType.getIntProperty(CraftType.MAX_STATIC_MOVE);

        int offsetFrontBack = 0;
        int offsetVertical = 0;
        int offsetLeftRight = 0;

        String shiftData = this.signWrapperCur.getRaw(2);
        if (!shiftData.isBlank()) {
            String[] strArr = shiftData.split(",");
            if (strArr.length == 3) {
                try {
                    offsetFrontBack = Integer.parseInt(strArr[0]);
                    offsetVertical = Integer.parseInt(strArr[1]);
                    offsetLeftRight = Integer.parseInt(strArr[2]);
                } catch(NumberFormatException nfe) {
                    offsetFrontBack = 0;
                    offsetVertical = 0;
                    offsetLeftRight = 0;
                }
            }
        }

        final Vector signDirection = this.signWrapperCur.facing().getDirection().normalize();
        Vector movement = signDirection.clone();
        if (offsetLeftRight != 0 || offsetFrontBack != 0 || offsetVertical != 0) {
            // Limit the values
            offsetFrontBack = Math.clamp(offsetFrontBack, -MAX_MOVEMENT, MAX_MOVEMENT);
            offsetVertical = Math.clamp(offsetVertical, -MAX_MOVEMENT, MAX_MOVEMENT);
            offsetLeftRight = Math.clamp(offsetLeftRight, -MAX_MOVEMENT, MAX_MOVEMENT);

            movement.setY(movement.getBlockY() + offsetVertical);

            if (offsetFrontBack != 0) {
                Vector shiftFrontBack = signDirection.clone().normalize().multiply(offsetFrontBack);
                movement.add(shiftFrontBack);
            }
            if (offsetLeftRight != 0) {
                Vector shiftLeftRight = signDirection.clone().normalize().rotateAroundY(Math.PI / 2).multiply(offsetLeftRight);
                movement.add(shiftLeftRight);
            }
        }

        return movement;
    }

    @Override
    protected void runDetectTask(Action action, CraftType subcraftType, Craft craft, World world, Player player, MovecraftLocation startPoint) {
        if (this.signWrapperCur == null) {
            return;
        }
        Vector movement = this.getMovementVector(this.signWrapperCur, subcraftType);
        if (action.isRightClick()) {
            movement.multiply(-1);
        }

        if (movement.length() != 0) {
            final Vector finalMovement = movement;
            CraftManager.getInstance().detect(startPoint, subcraftType, (type, w, p, parents) -> {
                if (parents.size() > 1) {
                    return new Pair(Result.failWithMessage(I18nSupport.getInternationalisedString("Detection - Failed - Already commanding a craft")), (Object)null);
                } else if (parents.size() < 1) {
                    return new Pair(Result.succeed(), new SubcraftMoveCraft(type, w, p));
                } else {
                    Craft parent = (Craft)parents.iterator().next();
                    return new Pair(Result.succeed(), new SubCraftImpl(type, w, parent));
                }
            }, world, player, player, (subcraft) -> {
                return () -> {
                    Bukkit.getServer().getPluginManager().callEvent(new CraftPilotEvent(subcraft, CraftPilotEvent.Reason.SUB_CRAFT));
                    if (subcraft instanceof SubCraft) {
                        Craft parent = ((SubCraft)subcraft).getParent();
                        HitBox newHitbox = parent.getHitBox().difference(subcraft.getHitBox());
                        parent.setHitBox(newHitbox);
                    }

                    (new BukkitRunnable() {
                        public void run() {
                            subcraft.translate(world, finalMovement.getBlockX(), finalMovement.getBlockY(), finalMovement.getBlockZ());
                            if (subcraft instanceof SubCraft) {
                                Craft parent = ((SubCraft)subcraft).getParent();
                                HitBox newHitbox = parent.getHitBox().union(subcraft.getHitBox());
                                parent.setHitBox(newHitbox);
                            }

                            CraftManager.getInstance().release(subcraft, net.countercraft.movecraft.events.CraftReleaseEvent.Reason.SUB_CRAFT, false);
                        }
                    }).runTaskLater(Movecraft.getInstance(), 3L);
                };
            });
        }

    }

    @Override
    protected boolean isActionAllowed(String s) {
        return s.equalsIgnoreCase(OPERATION);
    }

    @Override
    protected void onActionAlreadyInProgress(Player player) {
        player.sendMessage(I18nSupport.getInternationalisedString("Movement - Already Moving"));
    }

    @Override
    protected boolean isSignValid(Action clickType, SignListener.SignWrapper sign, Player player) {
        if (super.isSignValid(clickType, sign, player)) {
            CraftType craftType = this.getCraftType(sign);

            final Vector movement = this.getMovementVector(sign, craftType);
            if (clickType.isRightClick()) {
                movement.multiply(-1);
            }

            if (!craftType.getBoolProperty(CraftType.ALLOW_HORIZONTAL_MOVEMENT) && (movement.getX() != 0 || movement.getZ() != 0)) {
                player.sendMessage(I18nSupport.getInternationalisedString("Crafttype does not support horizontal movement!"));
                return false;
            }

            if (!craftType.getBoolProperty(CraftType.ALLOW_VERTICAL_MOVEMENT) && (movement.getY() != 0)) {
                player.sendMessage(I18nSupport.getInternationalisedString("Crafttype does not support vertical movement!"));
                return false;
            }

            return true;
        }
        return false;
    }

    @Override
    protected Component getDefaultTextFor(int line) {
        switch (line) {
            case 2: return DEFAULT_LINE_3;
            case 3: return DEFAULT_LINE_4;
            default: return null;
        }
    }

    @Override
    protected boolean canPlayerUseSignForCraftType(Action action, SignListener.SignWrapper signWrapper, Player player, CraftType craftType) {
        String craftTypeStr = craftType.getStringProperty(CraftType.NAME).toLowerCase();
        if (!player.hasPermission("movecraft." + craftTypeStr + ".move")) {
            player.sendMessage(I18nSupport.getInternationalisedString("Insufficient Permissions"));
            return false;
        } else if (!craftType.getBoolProperty(CraftType.CAN_STATIC_MOVE)) {
            player.sendMessage(I18nSupport.getInternationalisedString("Specified craft type can not static move!"));
            return false;
        } else {
            final Vector movement = this.getMovementVector(signWrapper, craftType);
            if (action.isRightClick()) {
                movement.multiply(-1);
            }

            if (!craftType.getBoolProperty(CraftType.ALLOW_HORIZONTAL_MOVEMENT) && (movement.getX() != 0 || movement.getZ() != 0)) {
                player.sendMessage(I18nSupport.getInternationalisedString("Crafttype does not support horizontal movement!"));
                return false;
            }

            if (!craftType.getBoolProperty(CraftType.ALLOW_VERTICAL_MOVEMENT) && (movement.getY() != 0)) {
                player.sendMessage(I18nSupport.getInternationalisedString("Crafttype does not support vertical movement!"));
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onCraftIsBusy(Player player, Craft craft) {
        player.sendMessage(I18nSupport.getInternationalisedString("Detection - Parent Craft is busy"));
    }

    @Override
    protected void onCraftNotFound(Player player, SignListener.SignWrapper signWrapper) {

    }
}
