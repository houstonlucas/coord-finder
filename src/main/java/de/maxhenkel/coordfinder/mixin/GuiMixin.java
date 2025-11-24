package de.maxhenkel.coordfinder.mixin;

import de.maxhenkel.coordfinder.client.ClientTargetStatus;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.waypoints.ClientWaypointManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Gui.class)
public class GuiMixin {

    @Redirect(method = "nextContextualInfoState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/waypoints/ClientWaypointManager;hasWaypoints()Z"))
    private boolean coordfinder$keepLocatorBarVisible(ClientWaypointManager manager) {
        boolean hasWaypoints = ((ClientWaypointManagerAccessor) manager).coordfinder$invokeHasWaypoints();
        if (hasWaypoints) {
            return true;
        }
        return ClientTargetStatus.hasTarget();
    }
}
