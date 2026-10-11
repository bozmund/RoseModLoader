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

    /** Forge's fluent channel setup; the version strings are not checked (see {@link #newSimpleChannel}). */
    public static class ChannelBuilder {
        private Identifier name;
        private Supplier<String> version = () -> "1";
        private Predicate<String> clientAccepted = v -> true;
        private Predicate<String> serverAccepted = v -> true;

        public static ChannelBuilder named(Identifier name) {
            ChannelBuilder builder = new ChannelBuilder();
            builder.name = name;
            return builder;
        }

        public ChannelBuilder networkProtocolVersion(Supplier<String> version) {
            this.version = version;
            return this;
        }

        public ChannelBuilder clientAcceptedVersions(Predicate<String> accepted) {
            this.clientAccepted = accepted;
            return this;
        }

        public ChannelBuilder serverAcceptedVersions(Predicate<String> accepted) {
            this.serverAccepted = accepted;
            return this;
        }

        public SimpleChannel simpleChannel() {
            return newSimpleChannel(name, version, clientAccepted, serverAccepted);
        }
    }

    private NetworkRegistry() {}
}
