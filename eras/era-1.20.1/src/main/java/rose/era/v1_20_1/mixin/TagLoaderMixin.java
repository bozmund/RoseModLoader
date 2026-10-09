package rose.era.v1_20_1.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.era.v1_20_1.ExtraTags;

/** Merges tag entries added by code (ExtraTags) into the tags read from data packs. */
@Mixin(TagLoader.class)
public abstract class TagLoaderMixin {
    @Shadow
    @Final
    private String directory;

    @Inject(method = "load", at = @At("RETURN"))
    private void rose$extraEntries(ResourceManager manager, CallbackInfoReturnable<Map<Identifier, List<TagLoader.EntryWithSource>>> cir) {
        Map<Identifier, List<TagLoader.EntryWithSource>> builders = cir.getReturnValue();
        ExtraTags.entries(directory).forEach((id, entries) -> {
            List<TagLoader.EntryWithSource> list = builders.computeIfAbsent(id, k -> new ArrayList<>());
            for (var entry : entries) list.add(new TagLoader.EntryWithSource(entry, "rose:legacy_code"));
        });
    }
}
