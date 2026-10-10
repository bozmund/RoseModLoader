package net.minecraftforge.client.event;

import rose.era.v1_20_1.client.render.LegacyBlockEntityRendererAdapter;
import rose.era.v1_20_1.client.render.LegacyBlockEntityRendererProvider;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

public abstract class EntityRenderersEvent extends Event implements IModBusEvent {
    /** Register entity and block entity renderers (mod bus, client). */
    public static class RegisterRenderers extends EntityRenderersEvent {
        public <T extends Entity> void registerEntityRenderer(EntityType<? extends T> entityType, EntityRendererProvider<T> provider) {
            EntityRenderers.register(entityType, provider);
        }

        /** Old renderers (1.20.1 render-into-buffers) run through Rose's adapter on 26.3's two-phase rendering. */
        @SuppressWarnings({"unchecked", "rawtypes"})
        public <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<? extends T> type, LegacyBlockEntityRendererProvider<T> provider) {
            BlockEntityRenderers.register((BlockEntityType) type, (BlockEntityRendererProvider) LegacyBlockEntityRendererAdapter.provider(type, provider));
        }
    }
}
