package de.maxhenkel.coordfinder.mixin;

import de.maxhenkel.coordfinder.CoordFinder;
import de.maxhenkel.coordfinder.Location;
import de.maxhenkel.coordfinder.Waypoints;
import de.maxhenkel.coordfinder.client.ClientTargetStatus;
import net.minecraft.client.waypoints.ClientWaypointManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.waypoints.TrackedWaypoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ClientWaypointManager.class)
public class ClientWaypointManagerMixin {

    @Unique
    private static Location coordfinder$cachedLocation;

    @Unique
    private static TrackedWaypoint coordfinder$cachedWaypoint;

    @Unique
    private static boolean coordfinder$loggedMissingTarget;

    @Unique
    private static boolean coordfinder$loggedDimMismatch;

    @Inject(method = "forEachWaypoint", at = @At("HEAD"))
    private void coordfinder$addSavedTargetWaypoint(Entity entity, Consumer<TrackedWaypoint> consumer, CallbackInfo ci) {
        Level level = entity.level();
        Location location = ClientTargetStatus.getTargetLocation().orElse(null);
        if (location == null) {
            coordfinder$cachedLocation = null;
            coordfinder$cachedWaypoint = null;
            coordfinder$loggedDimMismatch = false;
            if (!coordfinder$loggedMissingTarget) {
                CoordFinder.LOGGER.info("No client target location available for waypoint injection");
                coordfinder$loggedMissingTarget = true;
            }
            return;
        }
        coordfinder$loggedMissingTarget = false;

        boolean dimMatches = level.dimension().location().equals(location.dimension());
        if (!dimMatches) {
            if (!coordfinder$loggedDimMismatch) {
                CoordFinder.LOGGER.info("Skipping waypoint injection because dimension does not match (playerDim={}, targetDim={})",
                        level.dimension().location(),
                        location.dimension());
                coordfinder$loggedDimMismatch = true;
            }
            return;
        }
        coordfinder$loggedDimMismatch = false;

        TrackedWaypoint waypoint = coordfinder$getOrCreateWaypoint(location);
        consumer.accept(waypoint);
    }

    @Unique
    private static TrackedWaypoint coordfinder$getOrCreateWaypoint(Location location) {
        if (coordfinder$cachedWaypoint == null || coordfinder$cachedLocation == null || !coordfinder$cachedLocation.equals(location)) {
            coordfinder$cachedLocation = location;
            coordfinder$cachedWaypoint = TrackedWaypoint.setPosition(Waypoints.TARGET_ID, Waypoints.TARGET_ICON, location.position());
            CoordFinder.LOGGER.info("Injected client waypoint at {}", location.position());
        }
        return coordfinder$cachedWaypoint;
    }
}
