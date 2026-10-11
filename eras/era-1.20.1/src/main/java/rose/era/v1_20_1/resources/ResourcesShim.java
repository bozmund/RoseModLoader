package rose.era.v1_20_1.resources;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

/** Redirect targets for 1.20.1 resource manager calls: 26.x filters listings with a Selector, not a Predicate. */
public final class ResourcesShim {
    public static Map<Identifier, Resource> listResources(ResourceManager self, String directory, Predicate<Identifier> filter) {
        return self.listResources(directory, filter::test);
    }

    public static Map<Identifier, List<Resource>> listResourceStacks(ResourceManager self, String directory, Predicate<Identifier> filter) {
        return self.listResourceStacks(directory, filter::test);
    }

    private ResourcesShim() {}
}
