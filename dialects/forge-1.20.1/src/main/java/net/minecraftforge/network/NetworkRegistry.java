package net.minecraftforge.network;

import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;
import net.minecraftforge.network.simple.SimpleChannel;

public final class NetworkRegistry {
    public static final String ABSENT = "ABSENT 🤔";
    public static final String ACCEPTVANILLA = "ALLOWVANILLA 💓💓💓";

    /** Version checks happen in Rose's mod-list handshake, so the predicates are not consulted. */
    public static SimpleChannel newSimpleChannel(Identifier name, Supplier<String> networkProtocolVersion,
                                                 Predicate<String> clientAcceptedVersions, Predicate<String> serverAcceptedVersions) {
        return new SimpleChannel(name);
    }

    private NetworkRegistry() {}
}
