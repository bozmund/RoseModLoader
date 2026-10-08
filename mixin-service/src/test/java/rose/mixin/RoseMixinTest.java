package rose.mixin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import rose.loader.RoseClassLoader;

/** Applies a real Mixin through Rose's class loader, without Minecraft. Mixin is global, so one test only. */
class RoseMixinTest {
    @Test
    void appliesMixinToClassesDefinedByRose() throws Exception {
        Path classes = Path.of(RoseMixinTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path resources = Path.of(RoseMixinTest.class.getResource("/test.mixins.json").toURI()).getParent();
        RoseClassLoader loader = new RoseClassLoader(List.of(classes, resources), RoseMixinTest.class.getClassLoader());

        RoseMixin.bootstrap(loader, RoseMixin.SERVER, List.of("test.mixins.json"));

        Class<?> greeter = loader.loadClass("testdata.Greeter");
        assertSame(loader, greeter.getClassLoader());
        Object instance = greeter.getConstructor().newInstance();
        assertEquals("mixed", greeter.getMethod("greet").invoke(instance));
    }
}
