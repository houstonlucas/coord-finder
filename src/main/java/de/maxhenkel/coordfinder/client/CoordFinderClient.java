package de.maxhenkel.coordfinder.client;

import de.maxhenkel.coordfinder.Location;
import de.maxhenkel.coordfinder.network.Networking;
import de.maxhenkel.coordfinder.network.TargetStatusPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class CoordFinderClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Networking.init();
        ClientPlayNetworking.registerGlobalReceiver(TargetStatusPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> ClientTargetStatus.update(
                    payload.targetName(),
                    payload.dimension() != null && payload.position() != null ? new Location(payload.dimension(), payload.position()) : null
            ));
        });
    }
}
