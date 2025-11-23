package de.maxhenkel.coordfinder.target;

import de.maxhenkel.coordfinder.CoordFinder;
import de.maxhenkel.coordfinder.Location;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class PlayerTargetManager {

    private PlayerTargetManager() {
    }

    public static void setTarget(ServerPlayer player, String placeName) {
        setTarget(player.getUUID(), placeName);
        handleTargetChanged(player);
    }

    public static void setTarget(UUID player, String placeName) {
        CoordFinder.TARGET_CONFIG.setTarget(player, placeName);
    }

    public static void clearTarget(ServerPlayer player) {
        clearTarget(player.getUUID());
        handleTargetChanged(player);
    }

    public static void clearTarget(UUID player) {
        CoordFinder.TARGET_CONFIG.clearTarget(player);
    }

    @Nullable
    public static String getTarget(ServerPlayer player) {
        return getTarget(player.getUUID());
    }

    @Nullable
    public static String getTarget(UUID player) {
        return CoordFinder.TARGET_CONFIG.getTarget(player);
    }

    public static Optional<Location> getTargetLocation(ServerPlayer player) {
        return getTargetLocation(player.getUUID());
    }

    public static Optional<Location> getTargetLocation(UUID player) {
        String placeName = CoordFinder.TARGET_CONFIG.getTarget(player);
        if (placeName == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(CoordFinder.PLACE_CONFIG.getPlace(placeName));
    }

    private static void handleTargetChanged(ServerPlayer player) {
        // TODO: send target updates to the client to keep the locator bar in sync.
    }
}
