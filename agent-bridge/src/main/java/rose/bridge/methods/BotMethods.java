package rose.bridge.methods;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import rose.api.event.RoseEvents;
import rose.bridge.BridgeMod;
import rose.bridge.BridgeServer;
import rose.bridge.GameThread;

/**
 * Bot players: real {@link ServerPlayer}s joined through the normal join path, connected to an in-memory channel
 * instead of a socket (the same technique as vanilla's GameTest mock players). Everything the server sends them is
 * captured, so agents can check that custom payloads were sent without a second client.
 */
public final class BotMethods {
    private static final int MAX_PAYLOADS = 200;
    private static final Map<String, Bot> BOTS = new ConcurrentHashMap<>();

    private record Bot(ServerPlayer player, EmbeddedChannel channel, Deque<JsonObject> payloads) {}

    public static void register(BridgeServer bridge) {
        // Drain what the server sends bots every tick: keep custom payloads, drop the rest (chunks, entities...).
        RoseEvents.SERVER_TICK_END.register(server -> BOTS.values().forEach(BotMethods::drain));

        bridge.register("bot.spawn", "name:string, x?:number, y?:number, z?:number, dimension?:string",
                "Joins a bot player (survival mode). It is a real server player: commands, player.useBlock and events "
                        + "work on it. Optional position teleports it after joining.", p -> {
                    MinecraftServer server = BridgeMod.requireServer();
                    String name = p.string("name");
                    if (!name.matches("[A-Za-z0-9_]{3,16}")) throw new rose.bridge.Params.InvalidParams("name must be 3-16 of A-Z a-z 0-9 _");
                    return GameThread.onServer(server, () -> {
                        if (server.getPlayerList().getPlayerByName(name) != null) {
                            throw new IllegalStateException("a player named " + name + " is already online");
                        }
                        ServerLevel level = ServerMethods.level(server, p);
                        GameProfile profile = new GameProfile(UUIDUtil.createOfflinePlayerUUID(name), name);
                        CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
                        ServerPlayer player = new ServerPlayer(server, level, profile, cookie.clientInformation());
                        Connection connection = new Connection(PacketFlow.SERVERBOUND);
                        EmbeddedChannel channel = new EmbeddedChannel(new ChannelHandler[]{connection});
                        server.getPlayerList().placeNewPlayer(connection, player, cookie);
                        if (p.has("x") && p.has("y") && p.has("z")) {
                            player.teleportTo(p.number("x"), p.number("y"), p.number("z"));
                        }
                        BOTS.put(name, new Bot(player, channel, new ArrayDeque<>()));
                        JsonObject out = new JsonObject();
                        out.addProperty("name", name);
                        out.addProperty("uuid", player.getUUID().toString());
                        return out;
                    });
                });

        bridge.register("bot.list", "", "Names of the bots spawned through the bridge.", p -> {
            JsonArray out = new JsonArray();
            BOTS.keySet().stream().sorted().forEach(out::add);
            return out;
        });

        bridge.register("bot.payloads", "name:string, clear?:bool (default true)",
                "Custom payloads (mod packets) the server sent this bot, oldest first: {id, payload}. Proves server->client "
                        + "networking without a second game client.", p -> {
                    Bot bot = bot(p.string("name"));
                    MinecraftServer server = BridgeMod.requireServer();
                    return GameThread.onServer(server, () -> {
                        drain(bot);
                        JsonArray out = new JsonArray();
                        synchronized (bot.payloads()) {
                            bot.payloads().forEach(out::add);
                            if (p.bool("clear", true)) bot.payloads().clear();
                        }
                        return out;
                    });
                });

        bridge.register("bot.remove", "name:string", "Disconnects a bot.", p -> {
            Bot bot = bot(p.string("name"));
            MinecraftServer server = BridgeMod.requireServer();
            return GameThread.onServer(server, () -> {
                server.getPlayerList().remove(bot.player());
                bot.channel().close();
                BOTS.remove(p.string("name"));
                return new JsonObject();
            });
        });
    }

    static boolean isBot(ServerPlayer player) {
        Bot bot = BOTS.get(player.getName().getString());
        return bot != null && bot.player() == player;
    }

    public static void forgetAll() {
        BOTS.values().forEach(b -> b.channel().close());
        BOTS.clear();
    }

    private static Bot bot(String name) {
        Bot bot = BOTS.get(name);
        if (bot == null) throw new IllegalArgumentException("no bot named " + name + " (see bot.list)");
        return bot;
    }

    private static void drain(Bot bot) {
        Object message;
        while ((message = bot.channel().readOutbound()) != null) {
            if (message instanceof ClientboundCustomPayloadPacket(var payload)) {
                JsonObject entry = new JsonObject();
                entry.addProperty("id", payload.type().id().toString());
                entry.addProperty("payload", payload.toString());
                synchronized (bot.payloads()) {
                    bot.payloads().addLast(entry);
                    if (bot.payloads().size() > MAX_PAYLOADS) bot.payloads().removeFirst();
                }
            }
        }
    }

    private BotMethods() {}
}
