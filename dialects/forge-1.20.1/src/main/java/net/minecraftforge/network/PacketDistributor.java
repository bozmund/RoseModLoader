package net.minecraftforge.network;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import rose.api.network.RoseNetworking;

/** Who a message goes to: players (server to client), or the server ({@link #SERVER}, client to server). */
public class PacketDistributor<T> {
    public static final PacketDistributor<ServerPlayer> PLAYER = new PacketDistributor<>(player -> payload -> RoseNetworking.sendToPlayer(player, payload));
    public static final PacketDistributor<Void> ALL = new PacketDistributor<>(v -> payload -> {
        var server = rose.dialect.forge.v1_20_1.RegistryAccessHolder.server();
        if (server != null) server.getPlayerList().getPlayers().forEach(p -> RoseNetworking.sendToPlayer(p, payload));
    });
    public static final PacketDistributor<net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>> DIMENSION = new PacketDistributor<>(dimension -> payload -> {
        var server = rose.dialect.forge.v1_20_1.RegistryAccessHolder.server();
        if (server == null) return;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) if (p.level().dimension() == dimension) RoseNetworking.sendToPlayer(p, payload);
    });
    public static final PacketDistributor<TargetPoint> NEAR = new PacketDistributor<>(point -> payload -> {
        var server = rose.dialect.forge.v1_20_1.RegistryAccessHolder.server();
        if (server == null) return;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (p != point.excluded && p.level().dimension() == point.dim && p.distanceToSqr(point.x, point.y, point.z) < point.r2) {
                RoseNetworking.sendToPlayer(p, payload);
            }
        }
    });
    /** To the server, from the client (CPacket-style messages, such as Curios opening its screen). */
    public static final PacketDistributor<Void> SERVER = new PacketDistributor<>(v -> payload -> rose.dialect.forge.v1_20_1.client.ClientChannels.sendToServer(payload),
            NetworkDirection.PLAY_TO_SERVER); // a lambda, not a method reference: ClientChannels is only resolved on the client
    public static final PacketDistributor<java.util.List<net.minecraft.network.Connection>> NMLIST = new PacketDistributor<>(connections -> payload -> {
        var server = rose.dialect.forge.v1_20_1.RegistryAccessHolder.server();
        if (server == null) return;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (p.connection != null && connections.contains(p.connection.connection)) RoseNetworking.sendToPlayer(p, payload);
        }
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
    private final NetworkDirection direction;

    private PacketDistributor(java.util.function.Function<T, Consumer<CustomPacketPayload>> sender) {
        this(sender, NetworkDirection.PLAY_TO_CLIENT);
    }

    private PacketDistributor(java.util.function.Function<T, Consumer<CustomPacketPayload>> sender, NetworkDirection direction) {
        this.sender = sender;
        this.direction = direction;
    }

    public PacketTarget with(Supplier<T> input) {
        return new PacketTarget(sender.apply(input.get()), direction);
    }

    public PacketTarget noArg() {
        return new PacketTarget(sender.apply(null), direction);
    }

    public static class PacketTarget {
        private final Consumer<CustomPacketPayload> sender;
        private final NetworkDirection direction;

        PacketTarget(Consumer<CustomPacketPayload> sender, NetworkDirection direction) {
            this.sender = sender;
            this.direction = direction;
        }

        public void send(CustomPacketPayload payload) {
            sender.accept(payload);
        }

        public NetworkDirection getDirection() {
            return direction;
        }
    }

    /** Players within {@code sqrt(r2)} of a point in a dimension, except {@code excluded}. */
    public static final class TargetPoint {
        private final ServerPlayer excluded;
        private final double x;
        private final double y;
        private final double z;
        private final double r2;
        private final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim;

        public TargetPoint(ServerPlayer excluded, double x, double y, double z, double r2, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim) {
            this.excluded = excluded;
            this.x = x;
            this.y = y;
            this.z = z;
            this.r2 = r2;
            this.dim = dim;
        }

        public TargetPoint(double x, double y, double z, double r2, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim) {
            this(null, x, y, z, r2, dim);
        }

        public static Supplier<TargetPoint> p(double x, double y, double z, double r2, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim) {
            TargetPoint point = new TargetPoint(x, y, z, r2, dim);
            return () -> point;
        }
    }
}
