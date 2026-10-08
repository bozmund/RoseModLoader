package rose.bridge.methods;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import rose.bridge.BridgeMod;
import rose.bridge.BridgeServer;
import rose.bridge.GameThread;
import rose.bridge.Params;

/** Methods that act on the running server: dedicated, GameTest, or the client's integrated server. */
public final class ServerMethods {
    public static void register(BridgeServer bridge) {
        bridge.register("server.status", "", "Whether a server runs, its players, levels and tick count.", p -> {
            MinecraftServer server = BridgeMod.serverOrNull();
            JsonObject out = new JsonObject();
            out.addProperty("running", server != null);
            if (server == null) return out;
            return GameThread.onServer(server, () -> {
                out.addProperty("tickCount", server.getTickCount());
                out.addProperty("dedicated", server.isDedicatedServer());
                JsonArray levels = new JsonArray();
                server.getAllLevels().forEach(l -> levels.add(l.dimension().identifier().toString()));
                out.add("levels", levels);
                out.add("players", players(server));
                return out;
            });
        });

        bridge.register("server.command", "command:string (without leading /)",
                "Runs a command with full permissions as the server console. Returns the feedback lines it produced.",
                p -> {
                    MinecraftServer server = BridgeMod.requireServer();
                    String command = p.string("command");
                    return GameThread.onServer(server, () -> {
                        List<String> lines = new ArrayList<>();
                        CommandSourceStack source = server.createCommandSourceStack().withSource(new Collector(lines));
                        server.getCommands().performPrefixedCommand(source, command);
                        JsonObject out = new JsonObject();
                        JsonArray output = new JsonArray();
                        lines.forEach(output::add);
                        out.add("output", output);
                        return out;
                    });
                });

        bridge.register("server.players", "", "Online players with position, dimension, health and game mode.",
                p -> {
                    MinecraftServer server = BridgeMod.requireServer();
                    return GameThread.onServer(server, () -> players(server));
                });

        bridge.register("world.getBlock", "x:int, y:int, z:int, dimension?:string (default minecraft:overworld)",
                "The block state at a position and its block entity data (SNBT), if any.", p -> {
                    MinecraftServer server = BridgeMod.requireServer();
                    BlockPos pos = pos(p);
                    return GameThread.onServer(server, () -> {
                        ServerLevel level = level(server, p);
                        BlockState state = level.getBlockState(pos);
                        JsonObject out = new JsonObject();
                        out.addProperty("block", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
                        out.addProperty("state", state.toString());
                        BlockEntity blockEntity = level.getBlockEntity(pos);
                        if (blockEntity != null) {
                            out.addProperty("blockEntity", blockEntity.saveWithFullMetadata(level.registryAccess()).toString());
                        }
                        return out;
                    });
                });

        bridge.register("world.setBlock", "x:int, y:int, z:int, block:string (e.g. sample:counter_block or minecraft:oak_stairs[facing=east]), dimension?:string",
                "Places a block state, replacing what was there.", p -> {
                    MinecraftServer server = BridgeMod.requireServer();
                    BlockPos pos = pos(p);
                    String block = p.string("block");
                    return GameThread.onServer(server, () -> {
                        ServerLevel level = level(server, p);
                        BlockState state;
                        try {
                            state = BlockStateParser.parseForBlock(server.registryAccess().lookupOrThrow(Registries.BLOCK), block, false)
                                    .blockState();
                        } catch (Exception e) {
                            throw new Params.InvalidParams("bad block '" + block + "': " + e.getMessage());
                        }
                        JsonObject out = new JsonObject();
                        out.addProperty("changed", level.setBlockAndUpdate(pos, state));
                        out.addProperty("state", state.toString());
                        return out;
                    });
                });

        bridge.register("player.useBlock", "player:string, x:int, y:int, z:int, face?:string (default up), hand?:main|off",
                "Makes a server-side player (real or bot) right-click a block with what they hold, as if they had "
                        + "clicked it. Returns the interaction result.", p -> {
                    MinecraftServer server = BridgeMod.requireServer();
                    BlockPos pos = pos(p);
                    Direction face = Direction.byName(p.string("face", "up").toLowerCase(Locale.ROOT));
                    if (face == null) throw new Params.InvalidParams("face must be one of down, up, north, south, west, east");
                    InteractionHand hand = "off".equals(p.string("hand", "main")) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
                    String name = p.string("player");
                    return GameThread.onServer(server, () -> {
                        ServerPlayer player = server.getPlayerList().getPlayerByName(name);
                        if (player == null) throw new IllegalArgumentException("no player named " + name);
                        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), face, pos, false);
                        var result = player.gameMode.useItemOn(player, player.level(), player.getItemInHand(hand), hand, hit);
                        JsonObject out = new JsonObject();
                        out.addProperty("result", result.toString());
                        return out;
                    });
                });

        bridge.register("server.reload", "", "Reloads data packs (recipes, loot tables, tags, test instances) like /reload.",
                p -> {
                    MinecraftServer server = BridgeMod.requireServer();
                    return GameThread.onServer(server, () -> {
                        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "reload");
                        return new JsonObject();
                    });
                });

        bridge.register("server.stop", "", "Stops the server (on a dedicated or GameTest server this ends the process).",
                p -> {
                    MinecraftServer server = BridgeMod.requireServer();
                    server.halt(false);
                    return new JsonObject();
                });
    }

    static JsonArray players(MinecraftServer server) {
        JsonArray out = new JsonArray();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            JsonObject p = new JsonObject();
            p.addProperty("name", player.getName().getString());
            p.addProperty("dimension", player.level().dimension().identifier().toString());
            p.addProperty("x", player.getX());
            p.addProperty("y", player.getY());
            p.addProperty("z", player.getZ());
            p.addProperty("health", player.getHealth());
            p.addProperty("gameMode", player.gameMode.getGameModeForPlayer().getName());
            p.addProperty("bot", BotMethods.isBot(player));
            out.add(p);
        }
        return out;
    }

    static BlockPos pos(Params p) {
        return new BlockPos(p.integer("x"), p.integer("y"), p.integer("z"));
    }

    static ServerLevel level(MinecraftServer server, Params p) {
        String dimension = p.string("dimension", "minecraft:overworld");
        ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse(dimension)));
        if (level == null) throw new Params.InvalidParams("unknown dimension " + dimension);
        return level;
    }

    /** Collects command feedback lines. */
    private record Collector(List<String> lines) implements CommandSource {
        @Override public void sendSystemMessage(Component message) { lines.add(message.getString()); }
        @Override public boolean acceptsSuccess() { return true; }
        @Override public boolean acceptsFailure() { return true; }
        @Override public boolean shouldInformAdmins() { return false; }
    }

    private ServerMethods() {}
}
