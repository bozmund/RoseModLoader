package rose.testmods.addoncheck;

import static rose.testmods.addoncheck.AddonCheck.call;
import static rose.testmods.addoncheck.AddonCheck.modClass;
import static rose.testmods.addoncheck.AddonCheck.skip;
import static rose.testmods.addoncheck.AddonCheck.staticField;
import static rose.testmods.addoncheck.AddonCheck.test;

import com.google.gson.Gson;
import net.minecraft.gametest.framework.GameTestHelper;

/** GeckoLib 4.8.4 (a library mod): it initialized, and its Bedrock animation loader reads an animation. */
final class GeckoLibChecks {
    private static final String GL = "geckolib";

    private GeckoLibChecks() {}

    static void register() {
        test("gl_initialized", GeckoLibChecks::initialized);
        test("gl_reads_bedrock_animations", GeckoLibChecks::readsAnimations);
    }

    private static void initialized(GameTestHelper helper) {
        if (skip(helper, GL)) return;
        Object done = staticField(modClass("software.bernie.geckolib.GeckoLib"), "hasInitialized");
        helper.assertTrue(Boolean.TRUE.equals(done), "GeckoLib.initialize() didn't run");
        helper.succeed();
    }

    /** BakedAnimationsAdapter reads lengths and bones through GsonHelper; lengths are kept in ticks. */
    private static void readsAnimations(GameTestHelper helper) {
        if (skip(helper, GL)) return;
        Gson gson = (Gson) staticField(modClass("software.bernie.geckolib.util.JsonUtil"), "GEO_GSON");
        Class<?> baked = modClass("software.bernie.geckolib.loading.object.BakedAnimations");
        String json = """
                {"animation.test.walk": {"loop": true, "animation_length": 1.5,
                  "bones": {"body": {"rotation": {"0.0": [0, 0, 0], "1.5": [0, 90, 0]}}}}}""";
        Object animations = gson.fromJson(json, baked);
        Object walk;
        try {
            walk = baked.getMethod("getAnimation", String.class).invoke(animations, "animation.test.walk");
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        helper.assertTrue(walk != null, "the walk animation wasn't read");
        double length = (double) call(walk, "length");
        helper.assertTrue(length == 30.0, "walk lasts " + length + " ticks, not 30");
        Object[] bones = (Object[]) call(walk, "boneAnimations");
        helper.assertTrue(bones.length == 1, "walk animates " + bones.length + " bones");
        helper.succeed();
    }
}
