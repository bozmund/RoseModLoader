package net.minecraftforge.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraftforge.fml.config.IConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import rose.dialect.forge.v1_20_1.Toml;

/**
 * Forge's typed config: a mod declares options with a {@link Builder} and reads them through {@link ConfigValue}s.
 * Values come from {@code config/<file>.toml}; invalid or missing ones fall back to the default.
 */
public class ForgeConfigSpec implements IConfigSpec<ForgeConfigSpec> {
    private final List<ConfigValue<?>> values;
    private final Map<List<String>, List<String>> sectionComments;
    private boolean loaded;

    private ForgeConfigSpec(List<ConfigValue<?>> values, Map<List<String>, List<String>> sectionComments) {
        this.values = List.copyOf(values);
        this.sectionComments = Map.copyOf(sectionComments);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void save() {}

    @Override
    public void rose$load(Map<String, Object> raw) {
        for (ConfigValue<?> value : values) value.accept(raw.get(String.join(".", value.path)));
        loaded = true;
    }

    @Override
    public String rose$write() {
        // Group values by table, root values first, in declaration order.
        Map<List<String>, List<ConfigValue<?>>> tables = new LinkedHashMap<>();
        tables.put(List.of(), new ArrayList<>());
        for (ConfigValue<?> v : values) tables.computeIfAbsent(v.path.subList(0, v.path.size() - 1), k -> new ArrayList<>()).add(v);
        StringBuilder out = new StringBuilder();
        for (var table : tables.entrySet()) {
            if (table.getValue().isEmpty()) continue;
            if (!table.getKey().isEmpty()) {
                out.append('\n');
                for (String c : sectionComments.getOrDefault(table.getKey(), List.of())) out.append("#").append(c).append('\n');
                out.append('[').append(String.join(".", table.getKey())).append("]\n");
            }
            for (ConfigValue<?> v : table.getValue()) {
                for (String c : v.comment) out.append("\t#").append(c).append('\n');
                if (v.rangeComment != null) out.append("\t#").append(v.rangeComment).append('\n');
                out.append('\t').append(v.path.getLast()).append(" = ").append(Toml.format(v.get())).append('\n');
            }
        }
        return out.toString();
    }

    private static List<String> split(String path) {
        return Arrays.asList(path.split("\\."));
    }

    public static class Builder {
        private final List<ConfigValue<?>> values = new ArrayList<>();
        private final Map<List<String>, List<String>> sectionComments = new LinkedHashMap<>();
        private final List<String> currentPath = new ArrayList<>();
        private List<String> comment = List.of();

        public Builder comment(String comment) {
            this.comment = Arrays.asList(comment.split("\n"));
            return this;
        }

        public Builder comment(String... comment) {
            this.comment = Arrays.asList(comment);
            return this;
        }

        public Builder translation(String translationKey) {
            return this;
        }

        public Builder worldRestart() {
            return this;
        }

        public Builder push(String path) {
            return push(split(path));
        }

        public Builder push(List<String> path) {
            currentPath.addAll(path);
            if (!comment.isEmpty()) sectionComments.put(List.copyOf(currentPath), comment);
            comment = List.of();
            return this;
        }

        public Builder pop() {
            return pop(1);
        }

        public Builder pop(int count) {
            for (int i = 0; i < count; i++) currentPath.removeLast();
            return this;
        }

        public <T> Pair<T, ForgeConfigSpec> configure(Function<Builder, T> consumer) {
            T result = consumer.apply(this);
            return Pair.of(result, build());
        }

        public ForgeConfigSpec build() {
            return new ForgeConfigSpec(values, sectionComments);
        }

        private <V extends ConfigValue<?>> V add(V value) {
            values.add(value);
            comment = List.of();
            return value;
        }

        private List<String> fullPath(List<String> path) {
            List<String> out = new ArrayList<>(currentPath);
            out.addAll(path);
            return List.copyOf(out);
        }

        // --- generic values ---

        public <T> ConfigValue<T> define(String path, T defaultValue) {
            return define(split(path), defaultValue);
        }

        public <T> ConfigValue<T> define(List<String> path, T defaultValue) {
            return define(path, defaultValue, o -> o != null && defaultValue.getClass().isInstance(o));
        }

        public <T> ConfigValue<T> define(String path, T defaultValue, Predicate<Object> validator) {
            return define(split(path), defaultValue, validator);
        }

        public <T> ConfigValue<T> define(List<String> path, T defaultValue, Predicate<Object> validator) {
            return define(path, () -> defaultValue, validator);
        }

        public <T> ConfigValue<T> define(String path, Supplier<T> defaultSupplier, Predicate<Object> validator) {
            return define(split(path), defaultSupplier, validator);
        }

        public <T> ConfigValue<T> define(List<String> path, Supplier<T> defaultSupplier, Predicate<Object> validator) {
            return add(new ConfigValue<>(fullPath(path), comment, defaultSupplier, validator, null));
        }

        public <V extends Comparable<? super V>> ConfigValue<V> defineInRange(String path, V defaultValue, V min, V max, Class<V> clazz) {
            return add(new ConfigValue<>(fullPath(split(path)), comment, () -> defaultValue,
                    o -> clazz.isInstance(o) && clazz.cast(o).compareTo(min) >= 0 && clazz.cast(o).compareTo(max) <= 0,
                    "Range: " + min + " ~ " + max));
        }

        public <T> ConfigValue<T> defineInList(String path, T defaultValue, Collection<? extends T> acceptableValues) {
            return define(path, defaultValue, acceptableValues::contains);
        }

        public <T> ConfigValue<List<? extends T>> defineList(String path, List<? extends T> defaultValue, Predicate<Object> elementValidator) {
            return defineList(split(path), defaultValue, elementValidator);
        }

        public <T> ConfigValue<List<? extends T>> defineList(String path, Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
            return defineList(split(path), defaultSupplier, elementValidator);
        }

        public <T> ConfigValue<List<? extends T>> defineList(List<String> path, List<? extends T> defaultValue, Predicate<Object> elementValidator) {
            return defineList(path, () -> defaultValue, elementValidator);
        }

        public <T> ConfigValue<List<? extends T>> defineList(List<String> path, Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
            return add(new ConfigValue<>(fullPath(path), comment, defaultSupplier,
                    o -> o instanceof List<?> list && !list.isEmpty() && list.stream().allMatch(elementValidator), null));
        }

        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(String path, List<? extends T> defaultValue, Predicate<Object> elementValidator) {
            return defineListAllowEmpty(split(path), () -> defaultValue, elementValidator);
        }

        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(List<String> path, List<? extends T> defaultValue, Predicate<Object> elementValidator) {
            return defineListAllowEmpty(path, () -> defaultValue, elementValidator);
        }

        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(List<String> path, Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
            return add(new ConfigValue<>(fullPath(path), comment, defaultSupplier,
                    o -> o instanceof List<?> list && list.stream().allMatch(elementValidator), null));
        }

        // --- enums ---

        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue) {
            return defineEnum(path, defaultValue, defaultValue.getDeclaringClass().getEnumConstants());
        }

        @SafeVarargs
        public final <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue, V... acceptableValues) {
            List<V> acceptable = Arrays.asList(acceptableValues);
            return defineEnum(path, defaultValue, o -> acceptable.contains(o));
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue, Predicate<Object> validator) {
            return add(new EnumValue<>(fullPath(split(path)), comment, defaultValue, validator));
        }

        // --- primitives ---

        public BooleanValue define(String path, boolean defaultValue) {
            return define(split(path), defaultValue);
        }

        public BooleanValue define(List<String> path, boolean defaultValue) {
            return define(path, (Supplier<Boolean>) () -> defaultValue);
        }

        public BooleanValue define(String path, Supplier<Boolean> defaultSupplier) {
            return define(split(path), defaultSupplier);
        }

        public BooleanValue define(List<String> path, Supplier<Boolean> defaultSupplier) {
            return add(new BooleanValue(fullPath(path), comment, defaultSupplier));
        }

        public IntValue defineInRange(String path, int defaultValue, int min, int max) {
            return defineInRange(split(path), defaultValue, min, max);
        }

        public IntValue defineInRange(List<String> path, int defaultValue, int min, int max) {
            return add(new IntValue(fullPath(path), comment, defaultValue, min, max));
        }

        public LongValue defineInRange(String path, long defaultValue, long min, long max) {
            return defineInRange(split(path), defaultValue, min, max);
        }

        public LongValue defineInRange(List<String> path, long defaultValue, long min, long max) {
            return add(new LongValue(fullPath(path), comment, defaultValue, min, max));
        }

        public DoubleValue defineInRange(String path, double defaultValue, double min, double max) {
            return defineInRange(split(path), defaultValue, min, max);
        }

        public DoubleValue defineInRange(List<String> path, double defaultValue, double min, double max) {
            return add(new DoubleValue(fullPath(path), comment, defaultValue, min, max));
        }
    }

    public static class ConfigValue<T> implements Supplier<T> {
        final List<String> path;
        final List<String> comment;
        final String rangeComment;
        private final Supplier<T> defaultSupplier;
        private final Predicate<Object> validator;
        private T value;

        ConfigValue(List<String> path, List<String> comment, Supplier<T> defaultSupplier, Predicate<Object> validator, String rangeComment) {
            this.path = path;
            this.comment = comment;
            this.defaultSupplier = defaultSupplier;
            this.validator = validator;
            this.rangeComment = rangeComment;
        }

        public List<String> getPath() {
            return new ArrayList<>(path);
        }

        @Override
        public T get() {
            return value != null ? value : defaultSupplier.get();
        }

        public T getDefault() {
            return defaultSupplier.get();
        }

        public void set(T value) {
            this.value = value;
        }

        public void save() {}

        public void clearCache() {}

        /** Converts a raw TOML value to this value's type; null or invalid means "use the default". */
        @SuppressWarnings("unchecked")
        protected T convert(Object raw) {
            Object def = getDefault();
            if (def instanceof Integer && raw instanceof Number n) return (T) Integer.valueOf(n.intValue());
            if (def instanceof Long && raw instanceof Number n) return (T) Long.valueOf(n.longValue());
            if (def instanceof Double && raw instanceof Number n) return (T) Double.valueOf(n.doubleValue());
            return (T) raw;
        }

        void accept(Object raw) {
            if (raw == null) {
                value = null;
                return;
            }
            T converted = convert(raw);
            value = converted != null && validator.test(converted) ? converted : null;
        }
    }

    public static class BooleanValue extends ConfigValue<Boolean> {
        BooleanValue(List<String> path, List<String> comment, Supplier<Boolean> defaultSupplier) {
            super(path, comment, defaultSupplier, o -> o instanceof Boolean, null);
        }
    }

    public static class IntValue extends ConfigValue<Integer> {
        IntValue(List<String> path, List<String> comment, int defaultValue, int min, int max) {
            super(path, comment, () -> defaultValue, o -> o instanceof Integer i && i >= min && i <= max, "Range: " + min + " ~ " + max);
        }

        public int getAsInt() {
            return get();
        }
    }

    public static class LongValue extends ConfigValue<Long> {
        LongValue(List<String> path, List<String> comment, long defaultValue, long min, long max) {
            super(path, comment, () -> defaultValue, o -> o instanceof Long l && l >= min && l <= max, "Range: " + min + " ~ " + max);
        }

        public long getAsLong() {
            return get();
        }
    }

    public static class DoubleValue extends ConfigValue<Double> {
        DoubleValue(List<String> path, List<String> comment, double defaultValue, double min, double max) {
            super(path, comment, () -> defaultValue, o -> o instanceof Double d && d >= min && d <= max, "Range: " + min + " ~ " + max);
        }

        public double getAsDouble() {
            return get();
        }
    }

    public static class EnumValue<T extends Enum<T>> extends ConfigValue<T> {
        private final Class<T> type;

        EnumValue(List<String> path, List<String> comment, T defaultValue, Predicate<Object> validator) {
            super(path, comment, () -> defaultValue, validator, "Allowed Values: " + String.join(", ",
                    Arrays.stream(defaultValue.getDeclaringClass().getEnumConstants()).map(Enum::name).toList()));
            this.type = defaultValue.getDeclaringClass();
        }

        @Override
        protected T convert(Object raw) {
            if (!(raw instanceof String s)) return null;
            try {
                return Enum.valueOf(type, s);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}
