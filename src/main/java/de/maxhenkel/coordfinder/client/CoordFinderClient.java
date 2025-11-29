package de.maxhenkel.coordfinder.client;

import de.maxhenkel.coordfinder.Location;
import de.maxhenkel.coordfinder.network.Networking;
import de.maxhenkel.coordfinder.network.TargetStatusPayload;
import de.maxhenkel.coordfinder.network.PlaceListPayload;
import de.maxhenkel.coordfinder.client.ClientPlaces;
import de.maxhenkel.coordfinder.client.screen.CoordsMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class CoordFinderClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Networking.init();
        KeyMappings.register();
        ClientPlayNetworking.registerGlobalReceiver(TargetStatusPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> ClientTargetStatus.update(
                    payload.targetName(),
                    payload.dimension() != null && payload.position() != null ? new Location(payload.dimension(), payload.position()) : null
            ));
        });
        ClientPlayNetworking.registerGlobalReceiver(PlaceListPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ClientPlaces.updatePlaces(payload.places()))
        );
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }
            while (KeyMappings.OPEN_MENU != null && KeyMappings.OPEN_MENU.consumeClick()) {
                client.setScreen(new CoordsMenuScreen());
            }
        });
    }
}
