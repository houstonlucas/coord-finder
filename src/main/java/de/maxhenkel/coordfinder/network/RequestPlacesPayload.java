package de.maxhenkel.coordfinder.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RequestPlacesPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RequestPlacesPayload> TYPE = new CustomPacketPayload.Type<>(Networking.id("request_places"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestPlacesPayload> CODEC = CustomPacketPayload.codec((payload, buf) -> {}, RequestPlacesPayload::new);

    public RequestPlacesPayload(RegistryFriendlyByteBuf buf) {
        this();
    }

    @Override
    public CustomPacketPayload.Type<RequestPlacesPayload> type() {
        return TYPE;
    }
}
