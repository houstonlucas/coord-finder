package de.maxhenkel.coordfinder.client.screen;

import de.maxhenkel.coordfinder.CoordFinder;
import de.maxhenkel.coordfinder.client.ClientPlaces;
import de.maxhenkel.coordfinder.client.ClientTargetStatus;
import de.maxhenkel.coordfinder.client.KeyMappings;
import de.maxhenkel.coordfinder.network.RequestPlacesPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2fStack;

import javax.annotation.Nullable;
import java.util.List;

public class CoordsMenuScreen extends CoordFinderScreenBase {

    private static final int PANEL_WIDTH = 252;
    private static final int PANEL_HEIGHT = 260;
    private static final int BORDER_MARGIN = 12;
    private static final int GAP_SMALL = 6;
    private static final int LIST_TOP_OFFSET = 80;
    private static final int TARGET_SECTION_SPACING = 16;
    private static final int TARGET_PANEL_TOP_PADDING = 6;
    private static final int TARGET_PANEL_BOTTOM_PADDING = 10;
    private static final int TARGET_PANEL_LINE_GAP = 14;
    private static final int TARGET_PANEL_MIN_HEIGHT = 42;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 8;
    private static final int BUTTON_SECTION_GAP = 14;
    private static final int PANEL_BOTTOM_MARGIN = 18;
    private static final int MAX_BUTTON_WIDTH = 78;
    private static final int MIN_LIST_HEIGHT = 80;
    private static final int SEARCH_BOX_HEIGHT = 16;
    private static final int SEARCH_ROW_SPACING = GAP_SMALL;
    private static final int SEARCH_SECTION_PADDING = 4;
    private static final int MIN_SEARCH_FIELD_WIDTH = 72;
    private static final int SEARCH_CHECKBOX_GAP = 10;
    private static final int DIMENSION_LABEL_CLICK_PADDING = 8;
    private static final int DIMENSION_LABEL_MAX_WIDTH = 100;
    private static final float DIMENSION_LABEL_SCALE = 0.5F;
    private static final int DIMENSION_LABEL_LINE_SPACING = 1;
    private static final int MIN_SEARCH_TOP_OFFSET = 38;
    private static final int MIN_SEARCH_LIST_GAP = 14;
    private static final int SEARCH_LABEL_ABOVE_SECTION = GAP_SMALL;

    private static final Component BUTTON_SET_LABEL = Component.literal("Set Target");
    private static final Component BUTTON_CLEAR_LABEL = Component.literal("Clear Target");
    private static final Component BUTTON_REFRESH_LABEL = Component.literal("Reload");
    private static final Component EMPTY_PLACES_LABEL = Component.translatable("screen.coordfinder.menu.list_empty");
    private static final Component SEARCH_LABEL = Component.translatable("screen.coordfinder.menu.search_label");
    private static final Component SEARCH_HINT = Component.translatable("screen.coordfinder.menu.search_hint");
    private static final Component DIMENSION_FILTER_LABEL = Component.translatable("screen.coordfinder.menu.filter_same_dimension");
    private static final int HEADER_TEXT_COLOR = 0xFF3F3F40;
    private static final int SECTION_LABEL_COLOR = 0xFF3F3F40;
    private static final int PANEL_BORDER_COLOR = 0xFFB7B1A6;
    private static final int PANEL_TOP_COLOR = 0xFFFDFBF4;
    private static final int PANEL_BOTTOM_COLOR = 0xFFF1ECE2;
    private static final int SEARCH_SECTION_BORDER = 0xFFD9D1C5;
    private static final int SEARCH_SECTION_TOP = 0xFFF8F4EC;
    private static final int SEARCH_SECTION_BOTTOM = 0xFFECE4D7;
    private static final int SEARCH_FIELD_BORDER = 0xFFCFC5B6;
    private static final int SEARCH_FIELD_FILL = 0xFFFDFBF4;
    private static final int SEARCH_TEXT_COLOR = HEADER_TEXT_COLOR;
    private static final int SEARCH_PLACEHOLDER_COLOR = 0xFF8D877D;
    private static final int SEARCH_LABEL_COLOR = 0xFF3F3F40;
    private static final int DIMENSION_LABEL_COLOR = 0xFF3F3F40;
    private static final int DIMENSION_LABEL_DISABLED_COLOR = 0xFF9F9588;
    private static final int DIMENSION_LABEL_ACTIVE_COLOR = 0xFF1C1C1C;
    private static final float TITLE_SCALE = 1.15F;
    private static final float COORD_TEXT_SCALE = 0.9F;
    private static final InputWithModifiers DUMMY_INPUT = new InputWithModifiers() {
        @Override
        public int input() {
            return 0;
        }

        @Override
        public int modifiers() {
            return 0;
        }
    };

    private PlaceListWidget placeList;
    private Button setTargetButton;
    private Button clearTargetButton;
    private Button refreshButton;
    private Checkbox dimensionFilterCheckbox;
    private EditBox searchBox;
    private Runnable placeListener;

    private Layout layout;

    public CoordsMenuScreen() {
        super(Component.translatable("screen.coordfinder.menu.title"), PANEL_WIDTH, PANEL_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();

        layout = calculateLayout();

        placeList = new PlaceListWidget(this.minecraft, layout.list().width(), layout.list().height(), layout.list().y(), 26, this::handleEntryClick);
        placeList.updateSizeAndPosition(layout.list().width(), layout.list().height(), layout.list().x(), layout.list().y());
        addRenderableWidget(placeList);

        searchBox = new EditBox(this.font, layout.searchField().x(), layout.searchField().y(), layout.searchField().width(), layout.searchField().height(), SEARCH_HINT);
        searchBox.setResponder(this::onSearchChanged);
        searchBox.setMaxLength(64);
        searchBox.setBordered(false);
        searchBox.setTextColor(SEARCH_TEXT_COLOR);
        searchBox.setTextColorUneditable(SEARCH_TEXT_COLOR);
        addRenderableWidget(searchBox);

        dimensionFilterCheckbox = Checkbox.builder(Component.empty(), this.font)
            .pos(layout.checkbox().x(), layout.checkbox().y())
            .maxWidth(layout.checkbox().maxWidth())
            .selected(false)
            .onValueChange((checkbox, selected) -> updateDimensionFilter())
            .build();
        dimensionFilterCheckbox.setTooltip(Tooltip.create(DIMENSION_FILTER_LABEL));
        addRenderableWidget(dimensionFilterCheckbox);

        int availableButtonWidth = Math.max(30, panelWidth - BORDER_MARGIN * 2);
        int buttonWidth = Math.min(MAX_BUTTON_WIDTH, (availableButtonWidth - BUTTON_SPACING * 2) / 3);
        int totalWidth = buttonWidth * 3 + BUTTON_SPACING * 2;
        int startX = guiLeft + (panelWidth - totalWidth) / 2;

        setTargetButton = addRenderableWidget(Button.builder(BUTTON_SET_LABEL, button -> setSelectedTarget())
                .pos(startX, layout.buttonY())
                .size(buttonWidth, BUTTON_HEIGHT)
                .build());

        clearTargetButton = addRenderableWidget(Button.builder(BUTTON_CLEAR_LABEL, button -> clearTarget())
                .pos(startX + buttonWidth + BUTTON_SPACING, layout.buttonY())
                .size(buttonWidth, BUTTON_HEIGHT)
                .build());

        refreshButton = addRenderableWidget(Button.builder(BUTTON_REFRESH_LABEL, button -> requestPlaces())
                .pos(startX + (buttonWidth + BUTTON_SPACING) * 2, layout.buttonY())
                .size(buttonWidth, BUTTON_HEIGHT)
                .build());
        refreshButton.setTooltip(null);

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

    private void onSearchChanged(String value) {
        if (placeList != null) {
            placeList.setSearchQuery(value);
        }
    }

    private void updateDimensionFilter() {
        if (placeList == null) {
            return;
        }
        ResourceLocation filterDimension = isDimensionFilterEnabled() ? getPlayerDimension() : null;
        placeList.setDimensionFilter(filterDimension);
    }

    private boolean isDimensionFilterEnabled() {
        return dimensionFilterCheckbox != null && dimensionFilterCheckbox.selected();
    }

    private void updateDimensionCheckboxState() {
        if (dimensionFilterCheckbox == null) {
            return;
        }
        boolean hasPlayer = this.minecraft != null && this.minecraft.player != null;
        dimensionFilterCheckbox.active = hasPlayer;
        if (!hasPlayer && dimensionFilterCheckbox.selected()) {
            dimensionFilterCheckbox.onPress(DUMMY_INPUT);
            updateDimensionFilter();
        } else if (hasPlayer && dimensionFilterCheckbox.selected()) {
            updateDimensionFilter();
        }
    }

    @Nullable
    private ResourceLocation getPlayerDimension() {
        if (this.minecraft == null || this.minecraft.player == null || this.minecraft.player.level() == null) {
            return null;
        }
        return this.minecraft.player.level().dimension().location();
    }

    @Override
    public void tick() {
        super.tick();
        updateDimensionCheckboxState();
        updateButtons();
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        boolean searchFocused = searchBox != null && searchBox.isFocused();
        if (!searchFocused && KeyMappings.OPEN_MENU != null && KeyMappings.OPEN_MENU.matches(keyEvent)) {
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
        if (layout == null) {
            return;
        }
        renderSearchSectionBackground(guiGraphics);

        drawInsetPanel(guiGraphics, layout.list().x(), layout.list().y(), layout.list().width(), layout.list().height());
        drawInsetPanel(guiGraphics, layout.targetPanel().x(), layout.targetPanel().y(), layout.targetPanel().width(), layout.targetPanel().height());
    }

    @Override
    protected void renderPanelForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Matrix3x2fStack poseStack = guiGraphics.pose();
        poseStack.pushMatrix();
        poseStack.translate(guiLeft + panelWidth / 2F, guiTop + 12);
        poseStack.scale(TITLE_SCALE, TITLE_SCALE);
        guiGraphics.drawString(this.font, this.title, -this.font.width(this.title) / 2, 0, HEADER_TEXT_COLOR, false);
        poseStack.popMatrix();

        if (layout == null) {
            return;
        }

        Component placesLabel = Component.translatable("screen.coordfinder.menu.list_label");
        int listX = layout.list().x();
        int placesLabelY = layout.list().y() - TARGET_SECTION_SPACING + GAP_SMALL;
        guiGraphics.drawString(this.font, placesLabel, listX, placesLabelY, SECTION_LABEL_COLOR, false);
        int targetLabelY = layout.targetPanel().y() - TARGET_SECTION_SPACING + GAP_SMALL;
        guiGraphics.drawString(this.font, Component.translatable("screen.coordfinder.menu.target_header"), listX, targetLabelY, SECTION_LABEL_COLOR, false);
        if (placeList != null && placeList.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, EMPTY_PLACES_LABEL, guiLeft + panelWidth / 2, layout.list().y() + layout.list().height() / 2 - this.font.lineHeight / 2, 0x66999999);
        }
        renderTargetDetails(guiGraphics);
        renderRefreshButtonIcon(guiGraphics);
        renderSearchSectionForeground(guiGraphics);
    }

    private void renderSearchSectionBackground(GuiGraphics guiGraphics) {
        if (searchBox == null || layout == null) {
            return;
        }
        int left = layout.list().x();
        int right = layout.list().x() + layout.list().width();
        int top = layout.search().y();
        int bottom = layout.search().y() + layout.search().height();
        guiGraphics.fill(left, top, right, bottom, SEARCH_SECTION_BORDER);
        guiGraphics.fillGradient(left + 1, top + 1, right - 1, bottom - 1, SEARCH_SECTION_TOP, SEARCH_SECTION_BOTTOM);

        int fieldLeft = searchBox.getX() - 3;
        int fieldTop = searchBox.getY() - 2;
        int fieldRight = searchBox.getX() + searchBox.getWidth() + 3;
        int fieldBottom = searchBox.getY() + searchBox.getHeight() + 2;
        guiGraphics.fill(fieldLeft, fieldTop, fieldRight, fieldBottom, SEARCH_FIELD_BORDER);
        guiGraphics.fill(fieldLeft + 1, fieldTop + 1, fieldRight - 1, fieldBottom - 1, SEARCH_FIELD_FILL);
    }

    private void renderSearchSectionForeground(GuiGraphics guiGraphics) {
        if (searchBox != null && layout != null) {
            guiGraphics.drawString(this.font, SEARCH_LABEL, layout.list().x(), layout.search().y() - SEARCH_LABEL_ABOVE_SECTION, SEARCH_LABEL_COLOR, false);
            if (searchBox.getValue().isEmpty()) {
                int textX = searchBox.getX() + 2;
                int textY = searchBox.getY() + (searchBox.getHeight() - this.font.lineHeight) / 2;
                guiGraphics.drawString(this.font, SEARCH_HINT, textX, textY, SEARCH_PLACEHOLDER_COLOR, false);
            }
        }
        if (dimensionFilterCheckbox == null) {
            return;
        }
        int color;
        if (!dimensionFilterCheckbox.active) {
            color = DIMENSION_LABEL_DISABLED_COLOR;
        } else if (dimensionFilterCheckbox.selected()) {
            color = DIMENSION_LABEL_ACTIVE_COLOR;
        } else {
            color = DIMENSION_LABEL_COLOR;
        }
        renderDimensionLabel(guiGraphics, color);
    }

    private Layout calculateLayout() {
        int listWidth = panelWidth - BORDER_MARGIN * 2;
        int listX = guiLeft + BORDER_MARGIN;
        int listTop = guiTop + LIST_TOP_OFFSET;

        int checkboxBoxSize = Checkbox.getBoxSize(this.font);
        int dimensionLabelLineHeight = Math.max(8, Math.round(this.font.lineHeight * DIMENSION_LABEL_SCALE));
        List<FormattedCharSequence> dimensionLabelLines = this.font.split(DIMENSION_FILTER_LABEL, DIMENSION_LABEL_MAX_WIDTH);
        int dimensionLabelHeight = dimensionLabelLines.size() * dimensionLabelLineHeight + Math.max(0, dimensionLabelLines.size() - 1) * DIMENSION_LABEL_LINE_SPACING;
        int dimensionLabelWidth = (int) Math.ceil(DIMENSION_LABEL_MAX_WIDTH * DIMENSION_LABEL_SCALE) + DIMENSION_LABEL_CLICK_PADDING;
        int checkboxAreaWidth = checkboxBoxSize + GAP_SMALL + dimensionLabelWidth;
        int maxSearchRowSpace = Math.max(0, listWidth - checkboxAreaWidth - SEARCH_CHECKBOX_GAP);
        boolean stackedFilterLayout = maxSearchRowSpace < MIN_SEARCH_FIELD_WIDTH;
        int searchFieldWidth = stackedFilterLayout ? listWidth : maxSearchRowSpace;
        int searchAreaHeight = SEARCH_SECTION_PADDING * 2 + SEARCH_BOX_HEIGHT + (stackedFilterLayout ? GAP_SMALL + checkboxBoxSize : 0);

        int preferredSearchTop = listTop - searchAreaHeight - SEARCH_ROW_SPACING;
        int minSearchTop = guiTop + MIN_SEARCH_TOP_OFFSET;
        int searchAreaTop;
        if (preferredSearchTop < minSearchTop) {
            searchAreaTop = minSearchTop;
            listTop = searchAreaTop + searchAreaHeight + SEARCH_ROW_SPACING;
        } else {
            searchAreaTop = preferredSearchTop;
        }

        int minGapAboveList = MIN_SEARCH_LIST_GAP;
        listTop = Math.max(listTop, searchAreaTop + searchAreaHeight + minGapAboveList);

        int targetPanelHeight = calculateTargetPanelHeight();
        int buttonY = guiTop + panelHeight - PANEL_BOTTOM_MARGIN - BUTTON_HEIGHT;
        int targetPanelTop = buttonY - BUTTON_SECTION_GAP - targetPanelHeight;
        int availableListHeight = Math.max(0, targetPanelTop - TARGET_SECTION_SPACING - listTop);
        int listHeight = Math.max(MIN_LIST_HEIGHT, availableListHeight);
        if (listTop + listHeight + TARGET_SECTION_SPACING > targetPanelTop) {
            listHeight = availableListHeight;
        }
        targetPanelTop = listTop + listHeight + TARGET_SECTION_SPACING;
        buttonY = targetPanelTop + targetPanelHeight + BUTTON_SECTION_GAP;
        buttonY = Math.min(buttonY, guiTop + panelHeight - PANEL_BOTTOM_MARGIN - BUTTON_HEIGHT);

        int searchFieldX = listX + 3;
        int contentWidth = Math.max(20, listWidth - GAP_SMALL);
        int targetWidth = stackedFilterLayout ? listWidth - GAP_SMALL : Math.min(searchFieldWidth, contentWidth);
        int searchInputWidth = Math.max(60, targetWidth);
        int searchInputY = searchAreaTop + SEARCH_SECTION_PADDING;

        int checkboxX = stackedFilterLayout ? listX : listX + listWidth - checkboxAreaWidth;
        int checkboxY = stackedFilterLayout
            ? searchInputY + SEARCH_BOX_HEIGHT + GAP_SMALL
            : searchAreaTop + (searchAreaHeight - checkboxBoxSize) / 2;
        int dimensionLabelX = checkboxX + checkboxBoxSize + GAP_SMALL;
        int labelYOffset = Math.max(0, (checkboxBoxSize - dimensionLabelHeight) / 2);
        int dimensionLabelY = checkboxY + labelYOffset;
        int checkboxMaxWidth = Math.max(checkboxBoxSize + 4, stackedFilterLayout ? listWidth : checkboxAreaWidth);

        Rect listRect = new Rect(listX, listTop, listWidth, listHeight);
        Rect searchArea = new Rect(listX, searchAreaTop, listWidth, searchAreaHeight);
        Rect searchField = new Rect(searchFieldX, searchInputY, searchInputWidth, SEARCH_BOX_HEIGHT);
        Rect targetPanel = new Rect(listX, targetPanelTop, listWidth, targetPanelHeight);
        CheckboxLayout checkbox = new CheckboxLayout(checkboxX, checkboxY, checkboxMaxWidth);
        DimensionLabelLayout dimensionLabel = new DimensionLabelLayout(
            dimensionLabelX,
            dimensionLabelY,
            dimensionLabelWidth,
            dimensionLabelHeight,
            List.copyOf(dimensionLabelLines),
            dimensionLabelLineHeight
        );

        return new Layout(listRect, searchArea, searchField, targetPanel, checkbox, dimensionLabel, buttonY);
    }

    private int calculateTargetPanelHeight() {
        int scaledLineHeight = Math.max(8, Math.round(this.font.lineHeight * COORD_TEXT_SCALE));
        int packedHeight = TARGET_PANEL_TOP_PADDING + TARGET_PANEL_BOTTOM_PADDING + this.font.lineHeight + TARGET_PANEL_LINE_GAP + scaledLineHeight;
        return Math.max(TARGET_PANEL_MIN_HEIGHT, packedHeight);
    }

    private void renderDimensionLabel(GuiGraphics guiGraphics, int color) {
        if (layout == null || layout.dimensionLabel().lines().isEmpty()) {
            return;
        }
        DimensionLabelLayout dimension = layout.dimensionLabel();
        int currentY = dimension.y();
        for (FormattedCharSequence line : dimension.lines()) {
            Matrix3x2fStack poseStack = guiGraphics.pose();
            poseStack.pushMatrix();
            poseStack.translate(dimension.x(), currentY);
            poseStack.scale(DIMENSION_LABEL_SCALE, DIMENSION_LABEL_SCALE);
            guiGraphics.drawString(this.font, line, 0, 0, color, false);
            poseStack.popMatrix();
            currentY += dimension.lineHeight() + DIMENSION_LABEL_LINE_SPACING;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean inside) {
        if (isWithinDimensionLabel(mouseButtonEvent.x(), mouseButtonEvent.y())) {
            dimensionFilterCheckbox.onPress(DUMMY_INPUT);
            return true;
        }
        return super.mouseClicked(mouseButtonEvent, inside);
    }

    private boolean isWithinDimensionLabel(double mouseX, double mouseY) {
        if (dimensionFilterCheckbox == null || layout == null) {
            return false;
        }
        DimensionLabelLayout dimension = layout.dimensionLabel();
        int labelYTop = dimension.y() - 2;
        int labelBottom = dimension.y() + dimension.height() + 2;
        return mouseX >= dimension.x() && mouseX <= dimension.x() + dimension.width() && mouseY >= labelYTop && mouseY <= labelBottom;
    }

    private void renderTargetDetails(GuiGraphics guiGraphics) {
        if (layout == null) {
            return;
        }
        int textX = layout.list().x() + GAP_SMALL;
        int baseY = layout.targetPanel().y() + TARGET_PANEL_TOP_PADDING;

        ClientTargetStatus.getTargetName().ifPresentOrElse(name -> {
            guiGraphics.drawString(this.font,
                Component.translatable("screen.coordfinder.menu.target_name", name),
                textX, baseY, 0xFF1C1C1C, false);
            ClientTargetStatus.getTargetLocation().ifPresentOrElse(location -> {
            Component position = Component.translatable("screen.coordfinder.menu.target_position",
                location.position().getX(),
                location.position().getY(),
                location.position().getZ(),
                formatDimension(location.dimension()));
            drawScaledString(guiGraphics, position, textX, baseY + 14, 0xFF5A5A5A, COORD_TEXT_SCALE);
            }, () -> guiGraphics.drawString(this.font,
                Component.translatable("screen.coordfinder.menu.target_position_unknown"),
                textX, baseY + 14, 0xFF5A5A5A, false));
        }, () -> guiGraphics.drawString(this.font,
            Component.translatable("screen.coordfinder.menu.target.none"),
            textX, baseY + 4, 0xFF5A5A5A, false));
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

    private void drawScaledString(GuiGraphics guiGraphics, Component text, int x, int y, int color, float scale) {
        Matrix3x2fStack poseStack = guiGraphics.pose();
        poseStack.pushMatrix();
        poseStack.translate(x, y);
        poseStack.scale(scale, scale);
        guiGraphics.drawString(this.font, text, 0, 0, color, false);
        poseStack.popMatrix();
    }

    private void renderRefreshButtonIcon(GuiGraphics guiGraphics) {
    }

    private static String formatDimension(ResourceLocation dimension) {
        return dimension.getPath();
    }

    private record Layout(Rect list, Rect search, Rect searchField, Rect targetPanel, CheckboxLayout checkbox, DimensionLabelLayout dimensionLabel, int buttonY) {}

    private record Rect(int x, int y, int width, int height) {}

    private record CheckboxLayout(int x, int y, int maxWidth) {}

    private record DimensionLabelLayout(int x, int y, int width, int height, List<FormattedCharSequence> lines, int lineHeight) {}
}
