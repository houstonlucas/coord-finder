package de.maxhenkel.coordfinder.target;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import de.maxhenkel.coordfinder.CoordFinder;
import de.maxhenkel.coordfinder.Location;
import de.maxhenkel.coordfinder.Waypoints;
import de.maxhenkel.coordfinder.network.Networking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket;
import net.minecraft.server.level.ServerPlayer;

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

    public static void syncTarget(ServerPlayer player) {
        handleTargetChanged(player);
    }

    private static void handleTargetChanged(ServerPlayer player) {
        if (player.connection == null) {
            return;
        }

        String targetName = getTarget(player);
        Location location = targetName != null ? CoordFinder.PLACE_CONFIG.getPlace(targetName) : null;
        Networking.sendTargetStatus(player, targetName, location);

        if (targetName == null) {
            sendRemoveWaypoint(player);
            return;
        }

        if (location == null) {
            sendRemoveWaypoint(player);
            return;
        }

        if (!isSameDimension(player, location)) {
            sendRemoveWaypoint(player);
            return;
        }

        sendWaypoint(player, location.position());
    }

    private static void sendWaypoint(ServerPlayer player, BlockPos position) {
        ClientboundTrackedWaypointPacket packet = ClientboundTrackedWaypointPacket.addWaypointPosition(
                Waypoints.TARGET_ID,
                Waypoints.TARGET_ICON,
                position
        );
        player.connection.send(packet);
    }

    private static void sendRemoveWaypoint(ServerPlayer player) {
        player.connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(Waypoints.TARGET_ID));
    }

    private static boolean isSameDimension(ServerPlayer player, Location location) {
        return player.level().dimension().location().equals(location.dimension());
    }
}
