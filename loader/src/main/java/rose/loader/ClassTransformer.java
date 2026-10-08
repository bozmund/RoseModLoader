package rose.loader;

/**
 * Rewrites a class's bytecode before it is defined. Rose's translation engine, Mixin and the era bridges
 * all plug in here.
 */
@FunctionalInterface
public interface ClassTransformer {
    /**
     * @param internalName the class name with slashes, e.g. {@code net/minecraft/world/item/ItemStack}
     * @param bytes        the current bytecode
     * @return the new bytecode, or {@code bytes} unchanged
     */
    byte[] transform(String internalName, byte[] bytes);
}
