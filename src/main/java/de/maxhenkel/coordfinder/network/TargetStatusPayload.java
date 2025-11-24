package de.maxhenkel.coordfinder.network;

import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Simple payload that mirrors whether a player currently has a tracked target and its display name.
 */
public record TargetStatusPayload(@Nullable String targetName, @Nullable ResourceLocation dimension, @Nullable BlockPos position) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TargetStatusPayload> TYPE = new CustomPacketPayload.Type<>(Networking.id("target_status"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TargetStatusPayload> CODEC = CustomPacketPayload.codec(TargetStatusPayload::write, TargetStatusPayload::new);
    private static final int MAX_NAME_LENGTH = 64;

    public TargetStatusPayload(RegistryFriendlyByteBuf buf) {
        this(
            buf.readOptional(data -> data.readUtf(MAX_NAME_LENGTH)).orElse(null),
            buf.readOptional(data -> data.readResourceLocation()).orElse(null),
            buf.readOptional(data -> data.readBlockPos()).orElse(null)
        );
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeOptional(Optional.ofNullable(targetName), (data, value) -> data.writeUtf(value, MAX_NAME_LENGTH));
        buf.writeOptional(Optional.ofNullable(dimension), (data, value) -> data.writeResourceLocation(value));
        buf.writeOptional(Optional.ofNullable(position), (data, value) -> data.writeBlockPos(value));
    }

    @Override
    public CustomPacketPayload.Type<TargetStatusPayload> type() {
        return TYPE;
    }
}
