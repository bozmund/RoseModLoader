package rose.dialect.forge.v1_20_1;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

/** Item entities a dying entity spawns, held back until LivingDropsEvent decides (Forge's captureDrops). */
public final class DropCapture {
    private record Capture(Entity entity, List<ItemEntity> drops) {}

    private static final ThreadLocal<Deque<Capture>> CAPTURES = ThreadLocal.withInitial(ArrayDeque::new);

    public static void start(Entity entity) {
        CAPTURES.get().push(new Capture(entity, new ArrayList<>()));
    }

    /** True when {@code drop} was held back for {@code entity}'s death. */
    public static boolean capture(Entity entity, ItemEntity drop) {
        Capture current = CAPTURES.get().peek();
        if (current == null || current.entity() != entity) return false;
        current.drops().add(drop);
        return true;
    }

    public static List<ItemEntity> finish() {
        return CAPTURES.get().pop().drops();
    }

    private DropCapture() {}
}
