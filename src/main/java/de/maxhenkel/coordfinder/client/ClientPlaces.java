package de.maxhenkel.coordfinder.client;

import de.maxhenkel.coordfinder.CoordFinder;
import de.maxhenkel.coordfinder.Location;
import de.maxhenkel.coordfinder.network.PlaceListPayload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ClientPlaces {

    private static volatile List<Entry> entries = List.of();
    private static final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    private ClientPlaces() {
    }

    public static void updatePlaces(List<PlaceListPayload.PlaceEntry> payloadEntries) {
        List<Entry> updated = new ArrayList<>(payloadEntries.size());
        for (PlaceListPayload.PlaceEntry entry : payloadEntries) {
            updated.add(new Entry(entry.name(), new Location(entry.dimension(), entry.position())));
        }
        updated.sort(Comparator.comparing(Entry::name));
        entries = Collections.unmodifiableList(updated);
        CoordFinder.LOGGER.info("Client received {} places from server", updated.size());
        listeners.forEach(Runnable::run);
    }

    public static List<Entry> getEntries() {
        return entries;
    }

    public static void addListener(Runnable listener) {
        listeners.add(listener);
    }

    public static void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    public record Entry(String name, Location location) {
    }
}
