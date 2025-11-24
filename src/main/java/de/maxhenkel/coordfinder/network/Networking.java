package de.maxhenkel.coordfinder.network;

import de.maxhenkel.coordfinder.CoordFinder;
import de.maxhenkel.coordfinder.Location;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

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
}
