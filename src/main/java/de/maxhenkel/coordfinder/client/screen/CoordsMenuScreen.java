package de.maxhenkel.coordfinder.client.screen;

import de.maxhenkel.coordfinder.CoordFinder;
import de.maxhenkel.coordfinder.client.ClientPlaces;
import de.maxhenkel.coordfinder.client.ClientTargetStatus;
import de.maxhenkel.coordfinder.client.KeyMappings;
import de.maxhenkel.coordfinder.network.RequestPlacesPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

public class CoordsMenuScreen extends CoordFinderScreenBase {

    private static final int PANEL_WIDTH = 252;
    private static final int PANEL_HEIGHT = 260;
    private static final int INNER_MARGIN = 12;
    private static final int LIST_TOP_OFFSET = 56;
    private static final int TARGET_PANEL_HEIGHT = 58;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 8;
    private static final int MIN_LIST_HEIGHT = 80;

    private static final Component BUTTON_SET_LABEL = Component.literal("Set");
    private static final Component BUTTON_CLEAR_LABEL = Component.literal("Clear");
    private static final Component BUTTON_REFRESH_LABEL = Component.literal("Reload");
    private static final Component EMPTY_PLACES_LABEL = Component.translatable("screen.coordfinder.menu.list_empty");
    private static final int HEADER_TEXT_COLOR = 0xFF3F3F40;
    private static final int SECTION_LABEL_COLOR = 0xFF3F3F40;
    private static final int PANEL_BORDER_COLOR = 0xFFB7B1A6;
    private static final int PANEL_TOP_COLOR = 0xFFFDFBF4;
    private static final int PANEL_BOTTOM_COLOR = 0xFFF1ECE2;

    private PlaceListWidget placeList;
    private Button setTargetButton;
    private Button clearTargetButton;
    private Button refreshButton;
    private Runnable placeListener;

    private int listX;
    private int listWidth;
    private int listTop;
    private int listHeight;
    private int targetPanelTop;
    private int buttonY;

    public CoordsMenuScreen() {
        super(Component.translatable("screen.coordfinder.menu.title"), PANEL_WIDTH, PANEL_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();

        listWidth = panelWidth - INNER_MARGIN * 2;
        listX = guiLeft + INNER_MARGIN;
        listTop = guiTop + LIST_TOP_OFFSET;
        int availableListHeight = panelHeight - (LIST_TOP_OFFSET + TARGET_PANEL_HEIGHT + BUTTON_HEIGHT + 26);
        listHeight = Math.max(MIN_LIST_HEIGHT, availableListHeight);
        targetPanelTop = listTop + listHeight + 8;
        buttonY = targetPanelTop + TARGET_PANEL_HEIGHT + 8;

        placeList = new PlaceListWidget(this.minecraft, listWidth, listHeight, listTop, 26, this::handleEntryClick);
        placeList.updateSizeAndPosition(listWidth, listHeight, listX, listTop);
        addRenderableWidget(placeList);

        int buttonWidth = 72;
        int totalWidth = buttonWidth * 3 + BUTTON_SPACING * 2;
        int startX = guiLeft + (panelWidth - totalWidth) / 2;

        setTargetButton = addRenderableWidget(Button.builder(BUTTON_SET_LABEL, button -> setSelectedTarget())
                .pos(startX, buttonY)
                .size(buttonWidth, BUTTON_HEIGHT)
                .build());

        clearTargetButton = addRenderableWidget(Button.builder(BUTTON_CLEAR_LABEL, button -> clearTarget())
                .pos(startX + buttonWidth + BUTTON_SPACING, buttonY)
                .size(buttonWidth, BUTTON_HEIGHT)
                .build());

        refreshButton = addRenderableWidget(Button.builder(BUTTON_REFRESH_LABEL, button -> requestPlaces())
                .pos(startX + (buttonWidth + BUTTON_SPACING) * 2, buttonY)
                .size(buttonWidth, BUTTON_HEIGHT)
                .build());

        placeListener = () -> {
            if (this.minecraft != null) {
                this.minecraft.execute(this::refreshPlaces);
            }
        };
        ClientPlaces.addListener(placeListener);
        refreshPlaces();
        requestPlaces();
    }

    @Override
    public void removed() {
        super.removed();
        if (placeListener != null) {
            ClientPlaces.removeListener(placeListener);
        }
    }

    private void refreshPlaces() {
        if (placeList != null) {
            placeList.refreshEntries();
            updateButtons();
        }
    }

    private void handleEntryClick(PlaceListWidget.Entry entry) {
        placeList.setFocusedEntry(entry);
        placeList.setSelectedEntry(entry);
        updateButtons();
    }

    private void setSelectedTarget() {
        PlaceListWidget.Entry entry = placeList.getSelectedEntry();
        if (entry == null || this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        this.minecraft.player.connection.sendCommand("coords settarget " + entry.getName());
    }

    private void clearTarget() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        this.minecraft.player.connection.sendCommand("coords cleartarget");
    }

    private void requestPlaces() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        CoordFinder.LOGGER.info("Requesting place list refresh");
        ClientPlayNetworking.send(new RequestPlacesPayload());
    }

    private void updateButtons() {
        if (placeList == null) {
            return;
        }
        if (setTargetButton != null) {
            setTargetButton.active = placeList.getSelectedEntry() != null;
        }
        if (clearTargetButton != null) {
            clearTargetButton.active = ClientTargetStatus.hasTarget();
        }
    }

    @Override
    public void tick() {
        super.tick();
        updateButtons();
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (KeyMappings.OPEN_MENU != null && KeyMappings.OPEN_MENU.matches(keyEvent)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    @Override
    protected void renderPanelBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int left = guiLeft;
        int top = guiTop;
        int right = guiLeft + panelWidth;
        int bottom = guiTop + panelHeight;

        int borderColor = 0xFF000000;
        int panelColor = 0xFFE0E0E0;
        int panelShadow = 0xFFB0B0B0;
        guiGraphics.fill(left, top, right, bottom, panelShadow);
        guiGraphics.fill(left + 1, top + 1, right - 1, bottom - 1, panelColor);
        guiGraphics.fill(left, top, right, top + 1, borderColor);
        guiGraphics.fill(left, bottom - 1, right, bottom, borderColor);
        guiGraphics.fill(left, top, left + 1, bottom, borderColor);
        guiGraphics.fill(right - 1, top, right, bottom, borderColor);

        guiGraphics.fill(left + 3, top + 28, right - 3, top + 29, 0xFF9E9E9E);

        drawInsetPanel(guiGraphics, listX, listTop, listWidth, listHeight);
        drawInsetPanel(guiGraphics, listX, targetPanelTop, listWidth, TARGET_PANEL_HEIGHT);
    }

    @Override
    protected void renderPanelForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int titleWidth = this.font.width(this.title);
        int titleX = guiLeft + (panelWidth - titleWidth) / 2;
        guiGraphics.drawString(this.font, this.title, titleX, guiTop + 12, HEADER_TEXT_COLOR, false);

        Component placesLabel = Component.translatable("screen.coordfinder.menu.list_label");
        guiGraphics.drawString(this.font, placesLabel, listX, listTop - 12, SECTION_LABEL_COLOR, false);
        if (placeList != null && placeList.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, EMPTY_PLACES_LABEL, guiLeft + panelWidth / 2, listTop + listHeight / 2 - this.font.lineHeight / 2, 0x66999999);
        }
        renderTargetDetails(guiGraphics);
    }

    private void renderTargetDetails(GuiGraphics guiGraphics) {
        int textX = listX + 6;
        int headerY = targetPanelTop + 6;
        guiGraphics.drawString(this.font, Component.translatable("screen.coordfinder.menu.target_header"), textX, headerY, HEADER_TEXT_COLOR, false);

        ClientTargetStatus.getTargetName().ifPresentOrElse(name -> {
            guiGraphics.drawString(this.font,
                    Component.translatable("screen.coordfinder.menu.target_name", name),
                textX, headerY + 12, 0xFF1C1C1C, false);
            ClientTargetStatus.getTargetLocation().ifPresentOrElse(location -> {
                String dim = location.dimension().toString();
                guiGraphics.drawString(this.font,
                        Component.translatable("screen.coordfinder.menu.target_position",
                                location.position().getX(),
                                location.position().getY(),
                                location.position().getZ(),
                                dim),
                textX, headerY + 24, 0xFF5A5A5A, false);
            }, () -> guiGraphics.drawString(this.font,
                    Component.translatable("screen.coordfinder.menu.target_position_unknown"),
                textX, headerY + 24, 0xFF5A5A5A, false));
        }, () -> guiGraphics.drawString(this.font,
                Component.translatable("screen.coordfinder.menu.target.none"),
            textX, headerY + 18, 0xFF5A5A5A, false));
    }

    private void drawInsetPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int right = x + width;
        int bottom = y + height;
        guiGraphics.fill(x, y, right, bottom, PANEL_BOTTOM_COLOR);
        int gradientBottom = Math.min(bottom - 1, y + 18);
        if (gradientBottom > y + 1) {
            guiGraphics.fillGradient(x + 1, y + 1, right - 1, gradientBottom, PANEL_TOP_COLOR, PANEL_BOTTOM_COLOR);
        }
        guiGraphics.fill(x, y, right, y + 1, PANEL_BORDER_COLOR);
        guiGraphics.fill(x, bottom - 1, right, bottom, PANEL_BORDER_COLOR);
        guiGraphics.fill(x, y, x + 1, bottom, PANEL_BORDER_COLOR);
        guiGraphics.fill(right - 1, y, right, bottom, PANEL_BORDER_COLOR);
    }
}
