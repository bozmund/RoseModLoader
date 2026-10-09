package rose.dialect.forge.v1_20_1.shim;

import net.minecraft.client.model.HumanoidModel;
import net.minecraftforge.client.IArmPoseTransformer;
import rose.dialect.forge.v1_20_1.Unsupported;

/** Redirect targets for Forge 1.20.1 client extensions. */
public final class ForgeClientShim {
    /**
     * Forge {@code ArmPose.create(name, twoHanded, transformer)} added an enum constant at runtime. 26.3's ArmPose is
     * a plain enum, so the item is held with the closest vanilla pose and the custom arm animation is not applied.
     */
    public static HumanoidModel.ArmPose armPose(String name, boolean twoHanded, IArmPoseTransformer transformer) {
        Unsupported.feature("armpose:" + name, "custom arm pose " + name + " (Forge enum extension) is shown as a vanilla pose");
        return twoHanded ? HumanoidModel.ArmPose.CROSSBOW_HOLD : HumanoidModel.ArmPose.ITEM;
    }

    private ForgeClientShim() {}
}
