package de.maxhenkel.coordfinder.client.screen;

import de.maxhenkel.coordfinder.CoordFinder;
import de.maxhenkel.coordfinder.client.ClientPlaces;
import de.maxhenkel.coordfinder.client.ClientTargetStatus;
import de.maxhenkel.coordfinder.client.KeyMappings;
import de.maxhenkel.coordfinder.network.RequestPlacesPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix3x2fStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class CoordsMenuScreen extends CoordFinderScreenBase {

    private static final int PANEL_WIDTH = 236;
    private static final int PANEL_HEIGHT = 260;
    private static final int BORDER_MARGIN = 12;
    private static final int GAP_SMALL = 6;
    private static final int LIST_TOP_OFFSET = 76;
    private static final int TARGET_SECTION_SPACING = 14;
    private static final int TARGET_PANEL_TOP_PADDING = 4;
    private static final int TARGET_PANEL_BOTTOM_PADDING = 6;
    private static final int TARGET_PANEL_LINE_GAP = 10;
    private static final int TARGET_PANEL_MIN_HEIGHT = 32;
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
    private static final int SEARCH_FILTER_GAP = 10;
    private static final int FILTER_MIN_WIDTH = 84;
    private static final int FILTER_MAX_WIDTH = 132;
    private static final int FILTER_BUTTON_HEIGHT = 18;
    private static final int MIN_SEARCH_TOP_OFFSET = 38;
    private static final int MIN_SEARCH_LIST_GAP = 14;
    private static final int SEARCH_LABEL_ABOVE_SECTION = GAP_SMALL;
    private static final int CONTENT_SIDE_PADDING = 6;

    private static final Component BUTTON_SET_LABEL = Component.literal("Set Target");
    private static final Component BUTTON_CLEAR_LABEL = Component.literal("Clear Target");
    private static final Component BUTTON_REFRESH_LABEL = Component.literal("Reload");
    private static final Component EMPTY_PLACES_LABEL = Component.translatable("screen.coordfinder.menu.list_empty");
    private static final Component SEARCH_LABEL = Component.translatable("screen.coordfinder.menu.search_label");
    private static final Component SEARCH_HINT = Component.translatable("screen.coordfinder.menu.search_hint");
    private static final Component FILTER_HINT = Component.literal("Filters which dimensions to show");
    private static final Component FILTER_LABEL_ALL = Component.literal("All");
    private static final Component FILTER_LABEL_CURRENT = Component.literal("Current");
    private static final ResourceLocation PANEL_TEXTURE = ResourceLocation.fromNamespaceAndPath(CoordFinder.MODID, "textures/gui/voicechat_panel.png");
    private static final int PANEL_HEADER_V = 0;
    private static final int PANEL_HEADER_HEIGHT = 16;
    private static final int PANEL_BODY_V = 36;
    private static final int PANEL_BODY_SLICE_HEIGHT = 24;
    private static final int PANEL_FOOTER_V = 63;
    private static final int PANEL_FOOTER_HEIGHT = 3;
    private static final int PANEL_TEXTURE_WIDTH = 256;
    private static final int PANEL_TEXTURE_HEIGHT = 256;
    private static final int HEADER_TEXT_COLOR = 0xFF3F3F40;
    private static final int SECTION_LABEL_COLOR = 0xFF3F3F40;
    private static final int SEARCH_FIELD_BORDER = 0xFFB89E72;
    private static final int SEARCH_FIELD_FILL = 0xFFFAF2E4;
    private static final int LIST_BACKGROUND_COLOR = 0xFFE7E7E7;
    private static final int LIST_BORDER_COLOR = 0xFFB5B5B5;
    private static final int TARGET_BACKGROUND_COLOR = 0xFFEAEAEA;
    private static final int TARGET_BORDER_COLOR = 0xFFBFBFBF;
    private static final int SEARCH_TEXT_COLOR = HEADER_TEXT_COLOR;
    private static final int SEARCH_PLACEHOLDER_COLOR = 0xFF8D877D;
    private static final int SEARCH_LABEL_COLOR = 0xFF3F3F40;
    private static final float TITLE_SCALE = 1.15F;
    private static final float COORD_TEXT_SCALE = 0.9F;

    private PlaceListWidget placeList;
    private Button setTargetButton;
    private Button clearTargetButton;
    private Button refreshButton;
    private CycleButton<DimensionFilterOption> dimensionFilterSelector;
    private DimensionFilterOption selectedFilter = DimensionFilterOption.all();
    private EditBox searchBox;
    private Runnable placeListener;
    @Nullable
    private ResourceLocation lastKnownPlayerDimension;

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

        searchBox = new CenteredEditBox(this.font, layout.searchField().x(), layout.searchField().y(), layout.searchField().width(), layout.searchField().height(), SEARCH_HINT);
        searchBox.setResponder(this::onSearchChanged);
        searchBox.setMaxLength(64);
        searchBox.setBordered(false);
        searchBox.setTextColor(SEARCH_TEXT_COLOR);
        searchBox.setTextColorUneditable(SEARCH_FIELD_FILL);
        searchBox.setTextShadow(false);
        addRenderableWidget(searchBox);

        rebuildDimensionFilterSelector(false);
        lastKnownPlayerDimension = getPlayerDimension();

        int contentSpan = panelWidth - BORDER_MARGIN * 2 - CONTENT_SIDE_PADDING;
        int availableButtonWidth = Math.max(30, contentSpan);
        int buttonWidth = Math.min(MAX_BUTTON_WIDTH, (availableButtonWidth - BUTTON_SPACING * 2) / 3);
        int totalWidth = buttonWidth * 3 + BUTTON_SPACING * 2;
        int startX = layout.list().x() + Math.max(0, (layout.list().width() - totalWidth) / 2);

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
            rebuildDimensionFilterSelector(true);
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

    private void applySelectedFilter() {
        if (placeList == null || selectedFilter == null) {
            return;
        }
        ResourceLocation filterDimension = selectedFilter.resolve(getPlayerDimension());
        placeList.setDimensionFilter(filterDimension);
    }

    private void rebuildDimensionFilterSelector(boolean keepSelection) {
        if (layout == null) {
            return;
        }
        DimensionFilterOption baseline = keepSelection ? selectedFilter : DimensionFilterOption.all();
        List<DimensionFilterOption> options = collectFilterOptions();
        if (options.isEmpty()) {
            options = List.of(DimensionFilterOption.all());
        }
        DimensionFilterOption selection = findBestSelection(baseline, options);
        CycleButton<DimensionFilterOption> dropdown = createFilterDropdown(layout.filter(), options, selection);
        if (dimensionFilterSelector != null) {
            removeWidget(dimensionFilterSelector);
        }
        dimensionFilterSelector = addRenderableWidget(dropdown);
        selectedFilter = selection;
        applySelectedFilter();
    }

    private CycleButton<DimensionFilterOption> createFilterDropdown(Rect area, List<DimensionFilterOption> options, DimensionFilterOption selection) {
        return CycleButton.<DimensionFilterOption>builder(DimensionFilterOption::displayName)
            .withValues(options)
            .withInitialValue(selection)
            .displayOnlyValue()
            .withTooltip(option -> Tooltip.create(FILTER_HINT))
            .create(area.x(), area.y(), area.width(), area.height(), Component.empty(), (button, option) -> {
                selectedFilter = option;
                applySelectedFilter();
            });
    }

    private List<DimensionFilterOption> collectFilterOptions() {
        List<DimensionFilterOption> options = new ArrayList<>();
        options.add(DimensionFilterOption.all());

        ResourceLocation playerDimension = getPlayerDimension();
        Component currentLabel = playerDimension == null
            ? FILTER_LABEL_CURRENT
            : Component.literal("Current");
        options.add(DimensionFilterOption.current(currentLabel));

        Set<ResourceLocation> dimensions = new TreeSet<>(Comparator.comparing(ResourceLocation::toString));
        for (ClientPlaces.Entry entry : ClientPlaces.getEntries()) {
            dimensions.add(entry.location().dimension());
        }
        for (ResourceLocation dimension : dimensions) {
            options.add(DimensionFilterOption.dimension(dimension, Component.literal(formatDimension(dimension))));
        }
        return options;
    }

    private DimensionFilterOption findBestSelection(DimensionFilterOption desired, List<DimensionFilterOption> options) {
        for (DimensionFilterOption option : options) {
            if (option.matches(desired)) {
                return option;
            }
        }
        return options.get(0);
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
        ResourceLocation playerDimension = getPlayerDimension();
        if (!Objects.equals(playerDimension, lastKnownPlayerDimension)) {
            lastKnownPlayerDimension = playerDimension;
            rebuildDimensionFilterSelector(true);
        } else if (selectedFilter.kind() == FilterKind.CURRENT) {
            applySelectedFilter();
        }
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
        drawPanelBase(guiGraphics);
        if (layout == null) {
            return;
        }
        renderContentBackgrounds(guiGraphics);
        renderSearchFieldFrame(guiGraphics);
    }

    @Override
    protected void renderPanelForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Matrix3x2fStack poseStack = guiGraphics.pose();
        poseStack.pushMatrix();
        poseStack.translate(guiLeft + panelWidth / 2F, guiTop + 4);
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

    private void renderSearchFieldFrame(GuiGraphics guiGraphics) {
        if (searchBox == null || layout == null) {
            return;
        }
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
    }

    private void renderContentBackgrounds(GuiGraphics guiGraphics) {
        if (layout == null) {
            return;
        }
        drawSectionBackground(guiGraphics, layout.list(), LIST_BACKGROUND_COLOR, LIST_BORDER_COLOR);
        drawSectionBackground(guiGraphics, layout.targetPanel(), TARGET_BACKGROUND_COLOR, TARGET_BORDER_COLOR);
    }

    private static void drawSectionBackground(GuiGraphics guiGraphics, Rect rect, int fillColor, int borderColor) {
        if (rect.width() <= 0 || rect.height() <= 0) {
            return;
        }
        int left = rect.x();
        int top = rect.y();
        int right = left + rect.width();
        int bottom = top + rect.height();
        guiGraphics.fill(left, top, right, bottom, fillColor);
        guiGraphics.fill(left, top, right, top + 1, borderColor);
        guiGraphics.fill(left, bottom - 1, right, bottom, borderColor);
        guiGraphics.fill(left, top, left + 1, bottom, borderColor);
        guiGraphics.fill(right - 1, top, right, bottom, borderColor);
    }

    private Layout calculateLayout() {
        int contentInset = CONTENT_SIDE_PADDING;
        int listWidth = panelWidth - BORDER_MARGIN * 2 - contentInset;
        int listX = guiLeft + BORDER_MARGIN + contentInset / 2;
        int listTop = guiTop + LIST_TOP_OFFSET;

        int desiredFilterWidth = Math.min(FILTER_MAX_WIDTH, Math.max(FILTER_MIN_WIDTH, listWidth / 3));
        boolean stackedFilterLayout = listWidth < MIN_SEARCH_FIELD_WIDTH + desiredFilterWidth + SEARCH_FILTER_GAP;
        if (stackedFilterLayout) {
            desiredFilterWidth = listWidth;
        }
        int searchFieldWidth = stackedFilterLayout ? listWidth : Math.max(MIN_SEARCH_FIELD_WIDTH, listWidth - desiredFilterWidth - SEARCH_FILTER_GAP);
        int filterWidth = stackedFilterLayout ? listWidth : Math.max(FILTER_MIN_WIDTH, Math.min(desiredFilterWidth, listWidth - searchFieldWidth - SEARCH_FILTER_GAP));
        int searchAreaHeight = SEARCH_SECTION_PADDING * 2 + SEARCH_BOX_HEIGHT + (stackedFilterLayout ? GAP_SMALL + FILTER_BUTTON_HEIGHT : 0);

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
        int searchInputWidth = Math.max(MIN_SEARCH_FIELD_WIDTH, targetWidth);
        int searchInputY = searchAreaTop + SEARCH_SECTION_PADDING;

        int filterX = stackedFilterLayout ? listX : searchFieldX + searchInputWidth + SEARCH_FILTER_GAP;
        int filterY = stackedFilterLayout
            ? searchInputY + SEARCH_BOX_HEIGHT + GAP_SMALL
            : searchInputY + Math.max(0, (SEARCH_BOX_HEIGHT - FILTER_BUTTON_HEIGHT) / 2);
        Rect filterRect = new Rect(filterX, filterY, Math.max(1, filterWidth), FILTER_BUTTON_HEIGHT);

        Rect listRect = new Rect(listX, listTop, listWidth, listHeight);
        Rect searchArea = new Rect(listX, searchAreaTop, listWidth, searchAreaHeight);
        Rect searchField = new Rect(searchFieldX, searchInputY, searchInputWidth, SEARCH_BOX_HEIGHT);
        Rect targetPanel = new Rect(listX, targetPanelTop, listWidth, targetPanelHeight);
        return new Layout(listRect, searchArea, searchField, filterRect, targetPanel, buttonY);
    }

    private int calculateTargetPanelHeight() {
        int scaledLineHeight = Math.max(8, Math.round(this.font.lineHeight * COORD_TEXT_SCALE));
        int packedHeight = TARGET_PANEL_TOP_PADDING + TARGET_PANEL_BOTTOM_PADDING + this.font.lineHeight + TARGET_PANEL_LINE_GAP + scaledLineHeight;
        return Math.max(TARGET_PANEL_MIN_HEIGHT, packedHeight);
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

    private void drawScaledString(GuiGraphics guiGraphics, Component text, int x, int y, int color, float scale) {
        Matrix3x2fStack poseStack = guiGraphics.pose();
        poseStack.pushMatrix();
        poseStack.translate(x, y);
        poseStack.scale(scale, scale);
        guiGraphics.drawString(this.font, text, 0, 0, color, false);
        poseStack.popMatrix();
    }

    private void drawPanelBase(GuiGraphics guiGraphics) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, PANEL_TEXTURE, guiLeft, guiTop, 0, PANEL_HEADER_V, panelWidth, PANEL_HEADER_HEIGHT, PANEL_TEXTURE_WIDTH, PANEL_TEXTURE_HEIGHT);
        int y = guiTop + PANEL_HEADER_HEIGHT;
        int remaining = Math.max(0, panelHeight - PANEL_HEADER_HEIGHT - PANEL_FOOTER_HEIGHT);
        while (remaining > 0) {
            int drawHeight = Math.min(PANEL_BODY_SLICE_HEIGHT, remaining);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, PANEL_TEXTURE, guiLeft, y, 0, PANEL_BODY_V, panelWidth, drawHeight, PANEL_TEXTURE_WIDTH, PANEL_TEXTURE_HEIGHT);
            y += drawHeight;
            remaining -= drawHeight;
        }
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, PANEL_TEXTURE, guiLeft, guiTop + panelHeight - PANEL_FOOTER_HEIGHT, 0, PANEL_FOOTER_V, panelWidth, PANEL_FOOTER_HEIGHT, PANEL_TEXTURE_WIDTH, PANEL_TEXTURE_HEIGHT);
    }

    private enum FilterKind {
        ALL,
        CURRENT,
        DIMENSION
    }

    private record DimensionFilterOption(FilterKind kind, @Nullable ResourceLocation dimension, Component displayName) {

        static DimensionFilterOption all() {
            return new DimensionFilterOption(FilterKind.ALL, null, FILTER_LABEL_ALL);
        }

        static DimensionFilterOption current(Component label) {
            return new DimensionFilterOption(FilterKind.CURRENT, null, label);
        }

        static DimensionFilterOption dimension(ResourceLocation dimension, Component label) {
            return new DimensionFilterOption(FilterKind.DIMENSION, dimension, label);
        }

        boolean matches(DimensionFilterOption other) {
            return kind == other.kind && Objects.equals(dimension, other.dimension);
        }

        @Nullable
        ResourceLocation resolve(@Nullable ResourceLocation currentDimension) {
            return switch (kind) {
                case ALL -> null;
                case CURRENT -> currentDimension;
                case DIMENSION -> dimension;
            };
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DimensionFilterOption other)) {
                return false;
            }
            return kind == other.kind && Objects.equals(dimension, other.dimension);
        }

        @Override
        public int hashCode() {
            return Objects.hash(kind, dimension);
        }
    }

    private void renderRefreshButtonIcon(GuiGraphics guiGraphics) {
    }

    private static String formatDimension(ResourceLocation dimension) {
        return dimension.getPath();
    }

    private static class CenteredEditBox extends EditBox {
        private final int textYOffset;

        public CenteredEditBox(Font font, int x, int y, int width, int height, Component placeholder) {
            super(font, x, y, width, height, placeholder);
            this.textYOffset = Math.max(0, (height - font.lineHeight) / 2);
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            int originalY = getY();
            super.setY(originalY + textYOffset);
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            super.setY(originalY);
        }
    }

    private record Layout(Rect list, Rect search, Rect searchField, Rect filter, Rect targetPanel, int buttonY) {}

    private record Rect(int x, int y, int width, int height) {}
}
