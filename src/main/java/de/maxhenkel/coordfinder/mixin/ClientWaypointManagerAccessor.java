package de.maxhenkel.coordfinder.mixin;

import net.minecraft.client.waypoints.ClientWaypointManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientWaypointManager.class)
public interface ClientWaypointManagerAccessor {

    @Invoker("hasWaypoints")
    boolean coordfinder$invokeHasWaypoints();
}
