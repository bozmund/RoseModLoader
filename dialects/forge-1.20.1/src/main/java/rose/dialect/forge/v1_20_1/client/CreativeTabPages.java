package rose.dialect.forge.v1_20_1.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;

/**
 * Forge 1.20.1 paginated mod creative tabs; 26.3 has fixed tab positions. Mod tabs keep unique positions past
 * vanilla's (column 100 and up, ForgeVanillaShim.creativeTabBuilder) and are shown on pages after vanilla's: ten per
 * page in the regular tab places, with vanilla's search, hotbar and inventory tabs on every page.
 */
public final class CreativeTabPages {
    public static final int FIRST_MOD_COLUMN = 100;
    private static final int PER_PAGE = 10;
    private static Map<CreativeModeTab, Integer> modTabIndex;
    private static int page;
    private static boolean displaying;

    /** Mod tabs in registration (column) order, by their stored column. */
    private static synchronized Map<CreativeModeTab, Integer> modTabs() {
        if (modTabIndex == null) {
            List<CreativeModeTab> tabs = new ArrayList<>();
            for (CreativeModeTab tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
                if (((PagedTab) tab).rose$storedColumn() >= FIRST_MOD_COLUMN) tabs.add(tab);
            }
            tabs.sort(Comparator.comparingInt(t -> ((PagedTab) t).rose$storedColumn()));
            modTabIndex = new IdentityHashMap<>();
            for (int i = 0; i < tabs.size(); i++) modTabIndex.put(tabs.get(i), i);
        }
        return modTabIndex;
    }

    public static int pages() {
        return 1 + (modTabs().size() + PER_PAGE - 1) / PER_PAGE;
    }

    public static int page() {
        return page;
    }

    public static void turn(int delta) {
        page = Math.floorMod(page + delta, pages());
    }

    /** The page a tab is on: mod tabs on theirs, vanilla's special tabs on every page (-1), other vanilla tabs on 0. */
    public static int pageOf(CreativeModeTab tab) {
        Integer index = modTabs().get(tab);
        if (index != null) return 1 + index / PER_PAGE;
        return tab.getType() == CreativeModeTab.Type.CATEGORY ? 0 : -1;
    }

    /** The tabs the creative screen shows on the current page. */
    public static List<CreativeModeTab> visible(List<CreativeModeTab> tabs) {
        List<CreativeModeTab> out = new ArrayList<>();
        for (CreativeModeTab tab : tabs) {
            int p = pageOf(tab);
            if (p == -1 || p == page) out.add(tab);
        }
        return out;
    }

    /** While the creative screen lays out tabs, a mod tab reports the place it has on its page. */
    public static void displaying(boolean on) {
        displaying = on;
    }

    /** {row, column} for a mod tab while displaying, else null. */
    public static int[] displayPosition(CreativeModeTab tab) {
        if (!displaying) return null;
        Integer index = modTabs().get(tab);
        if (index == null) return null;
        int slot = index % PER_PAGE;
        return new int[] {slot < 5 ? 0 : 1, slot % 5};
    }

    /** Implemented by CreativeModeTab (CreativeTabPositionMixin). */
    public interface PagedTab {
        int rose$storedColumn();
    }

    private CreativeTabPages() {}
}
