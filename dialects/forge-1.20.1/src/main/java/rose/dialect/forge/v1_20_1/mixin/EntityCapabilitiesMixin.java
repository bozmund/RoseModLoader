package rose.dialect.forge.v1_20_1.mixin;

import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.dialect.forge.v1_20_1.CapabilityHolder;
import rose.dialect.forge.v1_20_1.CapabilityObject;

/**
 * Forge 1.20.1 entity capabilities: gathered when the entity is constructed (AttachCapabilitiesEvent&lt;Entity&gt;), saved
 * with it under ForgeCaps.
 */
@Mixin(Entity.class)
public abstract class EntityCapabilitiesMixin implements CapabilityObject {
    @Unique
    private CapabilityHolder rose$caps;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void rose$gatherCapabilities(EntityType<?> type, Level level, CallbackInfo ci) {
        rose$caps = CapabilityHolder.gather(Entity.class, (Entity) (Object) this, Map.of());
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return rose$caps == null ? LazyOptional.empty() : rose$caps.getCapability(cap, side);
    }

    @Override
    public void reviveCaps() {
        if (rose$caps != null) rose$caps.revive();
    }

    @Override
    public void invalidateCaps() {
        if (rose$caps != null) rose$caps.invalidate();
    }

    /** A removed entity's capabilities stop answering (Forge's Entity.setRemoved patch); a clone handler revives them. */
    @Inject(method = "setRemoved", at = @At("TAIL"))
    private void rose$invalidateOnRemoval(Entity.RemovalReason reason, CallbackInfo ci) {
        invalidateCaps();
    }

    @Inject(method = "saveWithoutId", at = @At("TAIL"))
    private void rose$saveCapabilities(ValueOutput output, CallbackInfo ci) {
        CompoundTag saved = rose$caps == null ? null : rose$caps.save();
        if (saved != null) output.store(CapabilityHolder.SAVE_KEY, CompoundTag.CODEC, saved);
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void rose$loadCapabilities(ValueInput input, CallbackInfo ci) {
        if (rose$caps != null) input.read(CapabilityHolder.SAVE_KEY, CompoundTag.CODEC).ifPresent(rose$caps::load);
    }
}
