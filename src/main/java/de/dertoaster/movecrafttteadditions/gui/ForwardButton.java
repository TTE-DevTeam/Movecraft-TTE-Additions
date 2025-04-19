package de.dertoaster.movecrafttteadditions.gui;

import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.controlitem.PageItem;

public class ForwardButton extends PageItem {

    public ForwardButton() {
        super(true);
    }

    @Override
    public ItemProvider getItemProvider(PagedGui<?> pagedGui) {
        ItemBuilder builder = new ItemBuilder(PlayerHeads.ARROW_RIGHT);
        builder.addLoreLines(pagedGui.hasNextPage()
                ? "Go to page " + (pagedGui.getCurrentPage() + 2) + "/" + pagedGui.getPageAmount()
                : "There are no more pages");
        return builder;
    }

}
