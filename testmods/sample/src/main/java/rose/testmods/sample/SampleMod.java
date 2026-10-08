package rose.testmods.sample;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rose.api.ModInitializer;
import rose.api.config.RoseConfig;
import rose.api.event.RoseEvents;
import rose.api.network.RoseNetworking;
import rose.api.registry.RoseRegistries;

public final class SampleMod implements ModInitializer {
    public static final String MOD_ID = "sample";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static CounterBlock COUNTER_BLOCK;
    public static BlockEntityType<CounterBlockEntity> COUNTER_BLOCK_ENTITY;
    public static SampleConfig CONFIG;

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        CONFIG = RoseConfig.load(MOD_ID, SampleConfig.class, SampleConfig::new);

        COUNTER_BLOCK = RoseRegistries.block(id("counter_block"), CounterBlock::new,
                BlockBehaviour.Properties.of().strength(1.0f));
        RoseRegistries.blockItem(COUNTER_BLOCK, new Item.Properties());
        COUNTER_BLOCK_ENTITY = RoseRegistries.blockEntity(id("counter_block"), CounterBlockEntity::new, COUNTER_BLOCK);

        RoseNetworking.registerServerToClient(CountPayload.TYPE, CountPayload.STREAM_CODEC);

        RoseEvents.SERVER_STARTED.register(server -> LOGGER.info("[sample] server started"));
        RoseEvents.PLAYER_JOIN.register(player -> LOGGER.info("[sample] {} joined", player.getName().getString()));

        SampleGameTests.register();
        LOGGER.info("[sample] initialized (maxCount={})", CONFIG.maxCount);
    }
}
