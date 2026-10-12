package net.minecraftforge.fml;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

/** A typed extension a mod registers on its container (Forge 1.20.1 fmlcore), such as a config screen factory. */
public interface IExtensionPoint<T extends Record> {
    /** Whether a remote's version of the mod is acceptable, and what this side reports (multiplayer server list). */
    record DisplayTest(Supplier<String> suppliedVersion, BiPredicate<String, Boolean> remoteVersionTest) implements IExtensionPoint<DisplayTest> {
    }
}
