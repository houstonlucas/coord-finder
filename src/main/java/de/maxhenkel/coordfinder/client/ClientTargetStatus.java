package de.maxhenkel.coordfinder.client;

import javax.annotation.Nullable;
import java.util.Optional;

import de.maxhenkel.coordfinder.CoordFinder;
import de.maxhenkel.coordfinder.Location;

public class ClientTargetStatus {

    @Nullable
    private static String targetName;
    @Nullable
    private static Location targetLocation;

    private ClientTargetStatus() {
    }

    public static synchronized void update(@Nullable String name, @Nullable Location location) {
        targetName = name;
        targetLocation = location;
        CoordFinder.LOGGER.info("Client target update: name={}, location={}", name, location);
    }

    public static synchronized boolean hasTarget() {
        return targetName != null;
    }

    public static synchronized Optional<String> getTargetName() {
        return Optional.ofNullable(targetName);
    }

    public static synchronized Optional<Location> getTargetLocation() {
        return Optional.ofNullable(targetLocation);
    }
}
