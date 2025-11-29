package de.maxhenkel.coordfinder.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Simple base screen that mimics the centered panel layout used by the Simple Voice Chat GUI.
 */
public abstract class CoordFinderScreenBase extends Screen {

    protected final int panelWidth;
    protected final int panelHeight;
    protected int guiLeft;
    protected int guiTop;

    protected CoordFinderScreenBase(Component title, int panelWidth, int panelHeight) {
        super(title);
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
    }

    @Override
    protected void init() {
        super.init();
        guiLeft = (width - panelWidth) / 2;
        guiTop = (height - panelHeight) / 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (minecraft != null && minecraft.level != null) {
            renderTransparentBackground(guiGraphics);
        } else {
            renderPanorama(guiGraphics, partialTick);
            renderBlurredBackground(guiGraphics);
        }
        renderPanelBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderPanelForeground(guiGraphics, mouseX, mouseY, partialTick);
    }

    protected abstract void renderPanelBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);

    protected abstract void renderPanelForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);
}
