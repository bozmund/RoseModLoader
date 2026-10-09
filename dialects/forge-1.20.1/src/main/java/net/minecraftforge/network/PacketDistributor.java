package net.minecraftforge.network;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import rose.api.network.RoseNetworking;

/** Who a server-to-client message goes to. */
public class PacketDistributor<T> {
    public static final PacketDistributor<ServerPlayer> PLAYER = new PacketDistributor<>(player -> payload -> RoseNetworking.sendToPlayer(player, payload));
    public static final PacketDistributor<Void> ALL = new PacketDistributor<>(v -> payload -> {
        var server = rose.dialect.forge.v1_20_1.RegistryAccessHolder.server();
        if (server != null) server.getPlayerList().getPlayers().forEach(p -> RoseNetworking.sendToPlayer(p, payload));
    });
    public static final PacketDistributor<Entity> TRACKING_ENTITY = new PacketDistributor<>(entity -> payload -> {
        if (entity.level() instanceof ServerLevel level) {
            for (ServerPlayer p : level.players()) if (p != entity && p.distanceToSqr(entity) < 128 * 128) RoseNetworking.sendToPlayer(p, payload);
        }
    });
    public static final PacketDistributor<Entity> TRACKING_ENTITY_AND_SELF = new PacketDistributor<>(entity -> payload -> {
        if (entity.level() instanceof ServerLevel level) {
            for (ServerPlayer p : level.players()) if (p == entity || p.distanceToSqr(entity) < 128 * 128) RoseNetworking.sendToPlayer(p, payload);
        }
    });
    public static final PacketDistributor<LevelChunk> TRACKING_CHUNK = new PacketDistributor<>(chunk -> payload -> {
        if (chunk.getLevel() instanceof ServerLevel level) {
            for (ServerPlayer p : level.getChunkSource().chunkMap.getPlayers(chunk.getPos(), false)) RoseNetworking.sendToPlayer(p, payload);
        }
    });

    private final java.util.function.Function<T, Consumer<CustomPacketPayload>> sender;

    private PacketDistributor(java.util.function.Function<T, Consumer<CustomPacketPayload>> sender) {
        this.sender = sender;
    }

    public PacketTarget with(Supplier<T> input) {
        return new PacketTarget(sender.apply(input.get()));
    }

    public PacketTarget noArg() {
        return new PacketTarget(sender.apply(null));
    }

    public static class PacketTarget {
        private final Consumer<CustomPacketPayload> sender;

        PacketTarget(Consumer<CustomPacketPayload> sender) {
            this.sender = sender;
        }

        public void send(CustomPacketPayload payload) {
            sender.accept(payload);
        }
    }
}
