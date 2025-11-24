package de.maxhenkel.coordfinder;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointStyleAssets;

public final class Waypoints {

    public static final UUID TARGET_ID = UUID.nameUUIDFromBytes("coordfinder-target".getBytes(StandardCharsets.UTF_8));
    public static final Waypoint.Icon TARGET_ICON = createIcon();

    private Waypoints() {
    }

    private static Waypoint.Icon createIcon() {
        Waypoint.Icon icon = new Waypoint.Icon();
        icon.style = WaypointStyleAssets.BOWTIE;
        icon.color = Optional.of(0xFFAA00);
        return icon;
    }
}
