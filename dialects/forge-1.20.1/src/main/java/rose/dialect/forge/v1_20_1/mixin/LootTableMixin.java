package rose.dialect.forge.v1_20_1.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.dialect.forge.v1_20_1.ForgeLootModifiers;

/**
 * Forge 1.20.1's LootTable patch: rolling a table (not a table nested in another, which rolls "raw") collects the
 * items, lets the global loot modifiers change them, then hands them out (ForgeLootModifiers).
 */
@Mixin(LootTable.class)
public abstract class LootTableMixin {
    @Shadow @Final private Optional<Identifier> randomSequence;

    @Inject(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private void rose$modifyLoot(LootContext context, Consumer<ItemStack> output, CallbackInfo ci) {
        if (!ForgeLootModifiers.active()) return;
        LootTable self = (LootTable) (Object) this;
        ObjectArrayList<ItemStack> loot = new ObjectArrayList<>();
        self.getRandomItemsRaw(context, LootTable.createStackSplitter(context.getLevel(), loot::add));
        ForgeLootModifiers.modify(self, loot, context).forEach(output);
        ci.cancel();
    }

    @Inject(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private void rose$modifyLoot(LootParams params, Consumer<ItemStack> output, CallbackInfo ci) {
        if (!ForgeLootModifiers.active()) return;
        ((LootTable) (Object) this).getRandomItems(new LootContext.Builder(params).create(randomSequence), output);
        ci.cancel();
    }

    @Inject(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;JLjava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private void rose$modifyLoot(LootParams params, long seed, Consumer<ItemStack> output, CallbackInfo ci) {
        if (!ForgeLootModifiers.active()) return;
        ((LootTable) (Object) this).getRandomItems(new LootContext.Builder(params).withOptionalRandomSeed(seed).create(randomSequence), output);
        ci.cancel();
    }
}
