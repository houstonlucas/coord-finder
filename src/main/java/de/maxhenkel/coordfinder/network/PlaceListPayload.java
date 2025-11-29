package de.maxhenkel.coordfinder.network;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PlaceListPayload(List<PlaceEntry> places) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PlaceListPayload> TYPE = new CustomPacketPayload.Type<>(Networking.id("place_list"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlaceListPayload> CODEC = CustomPacketPayload.codec(PlaceListPayload::write, PlaceListPayload::new);
    private static final int MAX_NAME_LENGTH = 64;

    public PlaceListPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readList(data -> new PlaceEntry(
                data.readUtf(MAX_NAME_LENGTH),
                data.readResourceLocation(),
                data.readBlockPos()
        )));
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeCollection(places, (data, entry) -> {
            data.writeUtf(entry.name(), MAX_NAME_LENGTH);
            data.writeResourceLocation(entry.dimension());
            data.writeBlockPos(entry.position());
        });
    }

    @Override
    public CustomPacketPayload.Type<PlaceListPayload> type() {
        return TYPE;
    }

    public record PlaceEntry(String name, ResourceLocation dimension, BlockPos position) {
    }
}
