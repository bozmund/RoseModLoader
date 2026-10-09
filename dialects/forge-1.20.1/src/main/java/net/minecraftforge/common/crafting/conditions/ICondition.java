package net.minecraftforge.common.crafting.conditions;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

/** A condition a recipe or other data entry needs to load (e.g. a config option or another mod). */
public interface ICondition {
    Identifier getID();

    boolean test(IContext context);

    interface IContext {
        IContext EMPTY = new IContext() {
            @Override
            public <T> Map<Identifier, Collection<Holder<T>>> getAllTags(ResourceKey<? extends Registry<T>> registry) {
                return Collections.emptyMap();
            }
        };

        IContext TAGS_INVALID = EMPTY;

        default <T> Collection<Holder<T>> getTag(TagKey<T> key) {
            return getAllTags(key.registry()).getOrDefault(key.location(), Collections.emptySet());
        }

        <T> Map<Identifier, Collection<Holder<T>>> getAllTags(ResourceKey<? extends Registry<T>> registry);
    }
}
