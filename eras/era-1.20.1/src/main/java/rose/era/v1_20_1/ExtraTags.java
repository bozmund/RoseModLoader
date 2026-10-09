package rose.era.v1_20_1;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;

/**
 * Tag entries added by code. 1.20.1 mods extended hard-coded sets ({@code Chicken.FOOD_ITEMS},
 * {@code Parrot.TAME_FOOD}, ...) that 26.3 replaced with tags; their additions are merged into the tag whenever tags
 * load (TagLoaderMixin), as if a data pack had listed them. Entries are optional, so a missing element is skipped.
 */
public final class ExtraTags {
    /** tags directory (e.g. {@code tags/item}) -> tag id -> entries */
    private static final Map<String, Map<Identifier, List<TagEntry>>> ENTRIES = new ConcurrentHashMap<>();

    public static void addElement(TagKey<?> tag, Identifier element) {
        add(tag, TagEntry.optionalElement(element));
    }

    public static void addTag(TagKey<?> tag, Identifier other) {
        if (!other.equals(tag.location())) add(tag, TagEntry.optionalTag(other));
    }

    /** Adds everything a holder set names: its tag, or its elements. */
    public static <T> void addAll(TagKey<T> tag, HolderSet<T> values) {
        values.unwrap().ifLeft(other -> addTag(tag, other.location()))
                .ifRight(elements -> elements.forEach(h -> h.unwrapKey().ifPresent(k -> addElement(tag, k.identifier()))));
    }

    private static synchronized void add(TagKey<?> tag, TagEntry entry) {
        List<TagEntry> list = ENTRIES.computeIfAbsent(Registries.tagsDirPath(tag.registry()), d -> new LinkedHashMap<>())
                .computeIfAbsent(tag.location(), id -> new ArrayList<>());
        if (!list.contains(entry)) list.add(entry);
    }

    /** The added entries for tags loaded from {@code directory}. */
    public static synchronized Map<Identifier, List<TagEntry>> entries(String directory) {
        Map<Identifier, List<TagEntry>> found = ENTRIES.get(directory);
        if (found == null) return Map.of();
        Map<Identifier, List<TagEntry>> copy = new LinkedHashMap<>();
        found.forEach((id, list) -> copy.put(id, List.copyOf(list)));
        return copy;
    }

    private ExtraTags() {}
}
