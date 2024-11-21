package de.dertoaster.movecrafttteadditions.sign;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.sign.AbstractInformationSign;
import net.countercraft.movecraft.sign.SignListener;
import net.countercraft.movecraft.util.Counter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/*
 * Displays the orignal block count, current block count and how close the craft is to sinking
 */
public class IntegritySign extends AbstractInformationSign {

    List<Component> displayComponents = new ObjectArrayList();

    protected static final int TOTAL_PERCENTAGE_INDEX = 1;
    // Resembles how much of the blocks the craft can lose before sinking are still present
    protected static final int INTEGRITY_INDEX = 2;
    protected static final int CURRENT_SIZE_INDEX = 3;

    static final char[] NUMBER_SIZE_MARKERS = new char[]{'K', 'M', 'B', 'T'};

    @Override
    protected @Nullable Component getUpdateString(int lineIndex, Component oldData, Craft craft) {
        lineIndex--;
        if (this.displayComponents.isEmpty() || lineIndex > this.displayComponents.size() || this.displayComponents.get(lineIndex) == null) {
            return oldData;
        }
        return this.displayComponents.get(lineIndex);
    }

    @Override
    protected @Nullable Component getDefaultString(int i, Component component) {
        return EMPTY;
    }

    @Override
    protected boolean refreshSign(@Nullable Craft craft, SignListener.SignWrapper sign, boolean fillDefault, REFRESH_CAUSE refreshCause) {
        this.calcDisplayComponents(craft);

        return super.refreshSign(craft, sign, fillDefault, refreshCause);
    }

    static final double TWO_THIRDS = 2/3;
    static final double ONE_THIRD = 1/3;

    protected void calcDisplayComponents(Craft craft) {
        if (craft == null) {
            return;
        }
        this.displayComponents.clear();

        int nonNegligibleBlocks = craft.getDataTag(Craft.NON_NEGLIGIBLE_BLOCKS);
        int nonNegligibleSolidBlocks = craft.getDataTag(Craft.NON_NEGLIGIBLE_SOLID_BLOCKS);

        final int originalBlockCount = craft.getOrigBlockCount();
        // TODO: Round
        final int criticalBlockCount = (int)((double)originalBlockCount * (craft.getType().getDoubleProperty(CraftType.OVERALL_SINK_PERCENT) / 100));
        final double maxBlockLoss = originalBlockCount - criticalBlockCount;
        final int currentBlockCount = (craft.getType().getBoolProperty(CraftType.BLOCKED_BY_WATER) ? nonNegligibleBlocks : nonNegligibleSolidBlocks);
        final double blockDifference = originalBlockCount - currentBlockCount;

        double percentOfOriginalSize = ((double)currentBlockCount) / ((double)originalBlockCount);
        final double percentOfOriginalSizeReal = percentOfOriginalSize;
        percentOfOriginalSize *= 100.0;

        double percentOfMaxLoss = 1.0 - (blockDifference / maxBlockLoss);
        final double percentOfMaxLossReal = percentOfMaxLoss;
        percentOfMaxLoss *= 100.0;

        Style percentageStyle = calcStyle(percentOfOriginalSizeReal, 0.8, 0.5);
        // PERCENTAGE
        // TODO: FOr whatever reason this displays 0%...
        displayComponents.add(Component.text("Size: " + formatPercent(percentOfOriginalSize) + "%").style(percentageStyle));
        // LOSS PERCENTAGE
        displayComponents.add(Component.text("Integrity: " + formatPercent(percentOfMaxLoss) + "%").style(calcStyle(percentOfMaxLossReal, TWO_THIRDS, ONE_THIRD)));
        // ORIGINAL SIZE
        displayComponents.add(Component.text("S: " + formatNumber(currentBlockCount) + "/" + formatNumber(originalBlockCount)).style(percentageStyle));
    }

    protected static Style calcStyle(double value, double greenBorder, double yellowBorder) {
        if (value >= greenBorder) {
            return STYLE_COLOR_GREEN;
        }
        if (value >= yellowBorder) {
            return STYLE_COLOR_YELLOW;
        }
        return STYLE_COLOR_RED;
    }

    protected static String formatPercent(double number) {
        return String.format("%.2f", number);
    }

    protected static String formatNumber(int number) {
        String result = "" + number;
        if (number < 100000) {
            return result;
        }
        for(int numberSuffixIndex = 0; numberSuffixIndex < NUMBER_SIZE_MARKERS.length && number > 1000; ++numberSuffixIndex) {
            number /= 1000;
            result = "" + number + NUMBER_SIZE_MARKERS[numberSuffixIndex];
        }
        return result;
    }

    @Override
    protected void performUpdate(Component[] newComponents, SignListener.SignWrapper signWrapper, REFRESH_CAUSE refreshCause) {
        for(int i = 0; i < newComponents.length; ++i) {
            Component newComp = newComponents[i];
            if (newComp != null) {
                signWrapper.line(i, newComp);
            }
        }

        if (refreshCause != REFRESH_CAUSE.SIGN_MOVED_BY_CRAFT && signWrapper.block() != null) {
            signWrapper.block().update(true);
        }
    }

    @Override
    protected void onCraftIsBusy(Player player, Craft craft) {
        // Ignore
    }
}
