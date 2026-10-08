package rose.core.mixin;

import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.core.pack.ModPackSource;

/**
 * Adds mods' {@code data/} and {@code assets/} as built-in packs. A repository built with vanilla's own built-in
 * source (server data or client resources) gets a matching mod source, so every world and the client's resources
 * see mod content.
 */
@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin {
    @Shadow @Final @Mutable private Set<RepositorySource> sources;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void rose$addModPacks(RepositorySource[] vanillaSources, CallbackInfo ci) {
        for (RepositorySource source : vanillaSources) {
            if (source instanceof BuiltInPackSource builtIn) {
                Set<RepositorySource> withMods = new LinkedHashSet<>(sources);
                withMods.add(new ModPackSource(((BuiltInPackSourceAccessor) builtIn).rose$packType()));
                sources = Set.copyOf(withMods);
                return;
            }
        }
    }
}
