package rose.era.v1_20_1.client;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;
import rose.era.v1_20_1.client.render.LegacyBuffers;

/**
 * Item model {@code rose:legacy_renderer}: draws an item with the BlockEntityWithoutLevelRenderer its mod registered
 * (Forge {@code IClientItemExtensions.getCustomRenderer}). Packfix points item definitions whose model had the
 * {@code builtin/entity} parent here; {@code base} is that model, now holding only its display transforms, as the
 * base of 26.3's special models does.
 */
public final class LegacyItemRenderer implements ItemModel {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("rose", "legacy_renderer");
    private static final Map<Item, Special> RENDERERS = new ConcurrentHashMap<>();
    private static final Vector3fc[] UNIT_CUBE = {
            new Vector3f(0, 0, 0), new Vector3f(1, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1),
            new Vector3f(1, 1, 0), new Vector3f(1, 0, 1), new Vector3f(0, 1, 1), new Vector3f(1, 1, 1)};

    /** Registers the renderer an item's mod hands out; it is created on first use (old renderers read the client). */
    public static void register(Item item, Supplier<BlockEntityWithoutLevelRenderer> renderer) {
        RENDERERS.put(item, new Special(Suppliers.memoize(renderer::get)));
    }

    private final ModelRenderProperties properties;
    private final Matrix4fc transformation;

    private LegacyItemRenderer(ModelRenderProperties properties, Matrix4fc transformation) {
        this.properties = properties;
        this.transformation = transformation;
    }

    /** What the old renderer is called with: the stack and how it is shown (26.3's special renderers get neither). */
    private record Call(ItemStack stack, ItemDisplayContext displayContext) {}

    @Override
    public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext,
                       @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        Special renderer = RENDERERS.get(stack.getItem());
        if (renderer == null) return;
        // Old renderers animate freely (FD's skillet flips by game time): never reuse a cached drawing.
        output.setAnimated();
        output.appendModelIdentityElement(this);
        ItemStackRenderState.LayerRenderState layer = output.newLayer();
        layer.setExtents(() -> UNIT_CUBE);
        layer.setLocalTransform(transformation);
        layer.setupSpecialModel(renderer, new Call(stack.copy(), displayContext));
        properties.applyToLayer(layer, displayContext);
    }

    private static final class Special implements SpecialModelRenderer<Call> {
        private final Supplier<BlockEntityWithoutLevelRenderer> renderer;
        private boolean failed;

        Special(Supplier<BlockEntityWithoutLevelRenderer> renderer) {
            this.renderer = renderer;
        }

        @Override
        public void submit(@Nullable Call call, PoseStack pose, SubmitNodeCollector collector, int light, int overlay,
                           boolean hasFoil, int outlineColor) {
            if (call == null || failed) return;
            try {
                BlockEntityWithoutLevelRenderer legacy = renderer.get();
                if (legacy == null) return;
                legacy.renderByItem(call.stack(), call.displayContext(), pose, new LegacyBuffers(collector, legacy.getClass().getName()),
                        light, overlay);
            } catch (RuntimeException | LinkageError e) {
                // An old renderer using an API Rose doesn't bridge yet shouldn't crash the game: it stops drawing.
                if (Boolean.getBoolean("rose.forge.strict")) throw e;
                failed = true;
                LogUtils.getLogger().warn("[rose] item renderer for {} disabled: {}", call.stack().getItem(), e.toString());
            }
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
            for (Vector3fc corner : UNIT_CUBE) output.accept(corner);
        }

        @Override
        public @Nullable Call extractArgument(ItemStack stack) {
            return null;
        }
    }

    public record Unbaked(Identifier base) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf("base").forGetter(Unbaked::base)).apply(i, Unbaked::new));

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(base);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel model = baker.getModel(base);
            return new LegacyItemRenderer(ModelRenderProperties.fromResolvedModel(baker, model, model.getTopTextureSlots()), transformation);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
