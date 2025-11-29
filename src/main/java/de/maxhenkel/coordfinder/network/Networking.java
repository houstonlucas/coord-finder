package de.maxhenkel.coordfinder.network;

import de.maxhenkel.coordfinder.CoordFinder;
import de.maxhenkel.coordfinder.Location;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.List;

public class Networking {

    public static final ResourceLocation TARGET_STATUS_ID = id("target_status");
    private static boolean initialized;

    private Networking() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        PayloadTypeRegistry.playS2C().register(TargetStatusPayload.TYPE, TargetStatusPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PlaceListPayload.TYPE, PlaceListPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestPlacesPayload.TYPE, RequestPlacesPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RequestPlacesPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player == null) {
                return;
            }
            CoordFinder.LOGGER.info("Received place list request from {}", player.getName().getString());
            MinecraftServer server = player.level().getServer();
            if (server != null) {
                server.execute(() -> sendPlaces(player));
            }
        });
        initialized = true;
    }

    static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(CoordFinder.MODID, path);
    }

    public static void sendTargetStatus(ServerPlayer player, @Nullable String targetName, @Nullable Location location) {
        ServerPlayNetworking.send(player, new TargetStatusPayload(
                targetName,
                location != null ? location.dimension() : null,
                location != null ? location.position() : null
        ));
    }

    public static void sendPlaces(ServerPlayer player) {
        if (CoordFinder.PLACE_CONFIG == null) {
            return;
        }
        List<PlaceListPayload.PlaceEntry> entries = CoordFinder.PLACE_CONFIG.getPlaces().entrySet().stream()
                .map(entry -> new PlaceListPayload.PlaceEntry(entry.getKey(), entry.getValue().dimension(), entry.getValue().position()))
                .toList();
        CoordFinder.LOGGER.info("Sending {} places to {}", entries.size(), player.getName().getString());
        ServerPlayNetworking.send(player, new PlaceListPayload(entries));
    }

    public static void broadcastPlaces(MinecraftServer server) {
        CoordFinder.LOGGER.info("Broadcasting place list to {} players", server.getPlayerList().getPlayerCount());
        server.getPlayerList().getPlayers().forEach(Networking::sendPlaces);
    }
}
