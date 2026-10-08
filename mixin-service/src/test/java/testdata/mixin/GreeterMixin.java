package testdata.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import testdata.Greeter;

@Mixin(Greeter.class)
public abstract class GreeterMixin {
    @Inject(method = "greet", at = @At("HEAD"), cancellable = true)
    private void test$greet(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue("mixed");
    }
}
