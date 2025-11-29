package de.maxhenkel.coordfinder.client.screen;

import de.maxhenkel.coordfinder.Location;
import de.maxhenkel.coordfinder.client.ClientPlaces;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public class PlaceListWidget extends ObjectSelectionList<PlaceListWidget.Entry> {

    private final Callback callback;
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
        clearEntries();
        List<ClientPlaces.Entry> places = ClientPlaces.getEntries();
        for (ClientPlaces.Entry entry : places) {
            addEntry(new Entry(entry));
        }
    }

    public boolean isEmpty() {
        return children().isEmpty();
    }

    public void setSelectedEntry(Entry entry) {
        super.setSelected(entry);
    }

    public void setFocusedEntry(Entry entry) {
        setFocused(entry);
    }

    public Entry getSelectedEntry() {
        return (Entry) super.getSelected();
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
                    location.dimension());
                guiGraphics.drawString(PlaceListWidget.this.minecraft.font, subtitle, x, y + 10, 0xFF5A5A5A, false);
        }

    }

    public interface Callback {
        void onEntryClicked(Entry entry);
    }
}
