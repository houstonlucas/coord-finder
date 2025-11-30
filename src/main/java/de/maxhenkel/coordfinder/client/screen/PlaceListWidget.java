package de.maxhenkel.coordfinder.client.screen;

import de.maxhenkel.coordfinder.Location;
import de.maxhenkel.coordfinder.client.ClientPlaces;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3x2fStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PlaceListWidget extends ObjectSelectionList<PlaceListWidget.Entry> {

    private final Callback callback;
    private List<ClientPlaces.Entry> cachedEntries = List.of();
    private String searchQuery = "";
    @Nullable
    private ResourceLocation dimensionFilter;

    public PlaceListWidget(Minecraft minecraft, int width, int height, int top, int itemHeight, Callback callback) {
        super(minecraft, width, height, top, itemHeight);
        this.callback = callback;
        refreshEntries();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean inside) {
        if (mouseButtonEvent.button() == 0) {
            Entry entry = getEntryAtPosition(mouseButtonEvent.x(), mouseButtonEvent.y());
            if (entry != null) {
                setFocused(entry);
                setSelected(entry);
                callback.onEntryClicked(entry);
                return true;
            }
        }
        return super.mouseClicked(mouseButtonEvent, inside);
    }

    @Override
    public int getRowWidth() {
        return Math.max(0, this.width - 12);
    }

    @Override
    protected void renderListBackground(GuiGraphics guiGraphics) {
        // Background handled by the parent screen
    }

    @Override
    protected void renderListSeparators(GuiGraphics guiGraphics) {
        // No separators to keep the panel clean
    }

    @Override
    protected void renderSelection(GuiGraphics guiGraphics, Entry entry, int color) {
        int left = entry.getX();
        int top = entry.getY();
        int right = left + entry.getWidth();
        int bottom = top + entry.getHeight();
        guiGraphics.fill(left, top, right, bottom, 0xFFE1E1E1);
        guiGraphics.fill(left + 1, top + 1, right - 1, bottom - 1, 0xFFC8C8C8);
    }

    public void refreshEntries() {
        cachedEntries = ClientPlaces.getEntries();
        applyFilters();
    }

    public boolean isEmpty() {
        return children().isEmpty();
    }

    public void setSelectedEntry(@Nullable Entry entry) {
        super.setSelected(entry);
    }

    public void setFocusedEntry(@Nullable Entry entry) {
        setFocused(entry);
    }

    public Entry getSelectedEntry() {
        return (Entry) super.getSelected();
    }

    public void setSearchQuery(String query) {
        searchQuery = query == null ? "" : query.trim();
        applyFilters();
    }

    public void setDimensionFilter(@Nullable ResourceLocation dimension) {
        dimensionFilter = dimension;
        applyFilters();
    }

    private void applyFilters() {
        String selectedName = null;
        Entry selected = getSelectedEntry();
        if (selected != null) {
            selectedName = selected.getName();
        }

        clearEntries();
        List<Entry> newEntries = new ArrayList<>();
        for (ClientPlaces.Entry place : cachedEntries) {
            if (!matchesDimension(place)) {
                continue;
            }
            if (!matchesSearch(place)) {
                continue;
            }
            newEntries.add(new Entry(place));
        }
        Entry newSelection = null;
        for (Entry entry : newEntries) {
            addEntry(entry);
            if (selectedName != null && selectedName.equals(entry.getName())) {
                newSelection = entry;
            }
        }
        if (newSelection != null) {
            setSelectedEntry(newSelection);
            setFocusedEntry(newSelection);
        } else {
            setSelectedEntry(null);
            setFocusedEntry(null);
        }
    }

    private boolean matchesDimension(ClientPlaces.Entry place) {
        if (dimensionFilter == null) {
            return true;
        }
        return dimensionFilter.equals(place.location().dimension());
    }

    private boolean matchesSearch(ClientPlaces.Entry place) {
        if (searchQuery.isEmpty()) {
            return true;
        }
        return fuzzyMatch(place.name(), searchQuery);
    }

    private static boolean fuzzyMatch(String value, String pattern) {
        String text = value.toLowerCase();
        String query = pattern.toLowerCase();
        int index = 0;
        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);
            index = text.indexOf(c, index);
            if (index == -1) {
                return false;
            }
            index++;
        }
        return true;
    }

    public class Entry extends ObjectSelectionList.Entry<Entry> {

        private final ClientPlaces.Entry place;

        public Entry(ClientPlaces.Entry place) {
            this.place = place;
        }

        public String getName() {
            return place.name();
        }

        public Location getLocation() {
            return place.location();
        }

        @Override
        public Component getNarration() {
            return Component.literal(place.name());
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            int x = getContentX();
            int y = getContentY();
            guiGraphics.drawString(PlaceListWidget.this.minecraft.font, place.name(), x, y, 0xFF1C1C1C, false);
            Location location = place.location();
            String subtitle = "%d, %d, %d • %s".formatted(
                    location.position().getX(),
                    location.position().getY(),
                location.position().getZ(),
                location.dimension().getPath());
            Matrix3x2fStack poseStack = guiGraphics.pose();
            poseStack.pushMatrix();
            poseStack.translate(x, y + 10);
            poseStack.scale(0.9F, 0.9F);
            guiGraphics.drawString(PlaceListWidget.this.minecraft.font, subtitle, 0, 0, 0xFF5A5A5A, false);
            poseStack.popMatrix();
        }

    }

    public interface Callback {
        void onEntryClicked(Entry entry);
    }
}
