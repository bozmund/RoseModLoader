package rose.testmods.sample;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import rose.api.gametest.RoseGameTests;

/**
 * GameTests for the sample mod. Each needs a matching {@code data/sample/test_instance/<name>.json}.
 * Run with {@code ./gradlew runGameTests}.
 */
final class SampleGameTests {
    private static final BlockPos POS = new BlockPos(0, 1, 0);

    static void register() {
        RoseGameTests.register(SampleMod.id("counter_increments"), SampleGameTests::counterIncrements);
        RoseGameTests.register(SampleMod.id("recipe_loaded"), SampleGameTests::recipeLoaded);
        RoseGameTests.register(SampleMod.id("payload_roundtrip"), SampleGameTests::payloadRoundtrip);
    }

    /** Block + block entity registration, and the block's use() logic. */
    private static void counterIncrements(GameTestHelper helper) {
        helper.setBlock(POS, SampleMod.COUNTER_BLOCK);
        helper.useBlock(POS, helper.makeMockPlayer(GameType.SURVIVAL));
        helper.useBlock(POS, helper.makeMockPlayer(GameType.SURVIVAL));
        CounterBlockEntity counter = helper.getBlockEntity(POS, CounterBlockEntity.class);
        helper.assertTrue(counter.count() == 2, "expected count 2, got " + counter.count());
        helper.succeed();
    }

    /** The mod's data pack (served by Rose's mod pack source) is loaded: its recipe exists. */
    private static void recipeLoaded(GameTestHelper helper) {
        var key = ResourceKey.create(Registries.RECIPE, SampleMod.id("counter_block"));
        helper.assertTrue(helper.getLevel().recipeAccess().byKey(key).isPresent(), "recipe sample:counter_block missing");
        helper.succeed();
    }

    /** Rose's payload codec hook: the vanilla gameplay codec can encode and decode a Rose payload. */
    private static void payloadRoundtrip(GameTestHelper helper) {
        var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        var sent = new ClientboundCustomPayloadPacket(new CountPayload(new BlockPos(1, 2, 3), 42));
        ClientboundCustomPayloadPacket.GAMEPLAY_STREAM_CODEC.encode(buf, sent);
        var received = ClientboundCustomPayloadPacket.GAMEPLAY_STREAM_CODEC.decode(buf);
        helper.assertTrue(sent.payload().equals(received.payload()),
                "payload changed in transit: " + received.payload());
        helper.succeed();
    }

    private SampleGameTests() {}
}
