package rose.era.v1_20_1.mixin;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** LateRegistration reopens a frozen registry for one entry; LegacyData drops placeholders of skipped entries. */
@Mixin(MappedRegistry.class)
public interface MappedRegistryAccess {
    @Accessor("frozen")
    boolean rose$frozen();

    @Accessor("frozen")
    void rose$setFrozen(boolean frozen);

    @Accessor("byKey")
    Map<ResourceKey<?>, Holder.Reference<?>> rose$byKey();

    @Accessor("byLocation")
    Map<Identifier, Holder.Reference<?>> rose$byLocation();
}
