package rose.analyzer;

import java.util.Set;
import java.util.TreeSet;

/**
 * One distinct unresolved (or noteworthy) symbol, with how often and where the mod uses it.
 *
 * @param status   what is wrong
 * @param kind     class, method, field, override, mixin-target or mixin-member
 * @param symbol   the reference in the mod's original names, e.g. {@code net/minecraft/world/level/block/Block.m_6227_(...)}
 * @param readable the same reference with readable 1.20.1 names, when known
 * @param target   what Rosetta translated it to (or "-")
 */
public final class Finding {
    public enum Status {
        RULE_BROKEN("rule", "A redirect rule exists, but its shim is missing or has the wrong signature"),
        CLASS_GONE("era-bridge", "Class has no counterpart in 26.3"),
        CLASS_MISSING("era-bridge", "Class translated, but it is not in 26.3"),
        METHOD_GONE("era-bridge", "Method has no counterpart in 26.3 (removed, split or merged)"),
        FIELD_GONE("era-bridge", "Field has no counterpart in 26.3"),
        OVERRIDE_GONE("era-bridge", "Mod overrides a vanilla method that no longer exists: needs an inheritance bridge"),
        SIGNATURE_CHANGED("era-bridge", "Name still exists in 26.3 but with different parameters/return type"),
        OVERRIDE_SIGNATURE_CHANGED("era-bridge", "Mod overrides a vanilla method whose signature changed"),
        METHOD_MISSING("era-bridge", "Method translated, but not found in 26.3"),
        FIELD_MISSING("era-bridge", "Field translated, but not found in 26.3"),
        FORGE_EXTENSION("dialect", "Method Forge added to a vanilla class (IForge* extension)"),
        FORGE_API("dialect", "Forge API the dialect must provide"),
        UNKNOWN_CLASS("dependency", "Class from outside Minecraft, Forge and this mod (another mod or a library)"),
        HEURISTIC_CLASS("review", "Resolved by a package-move guess; confirm and promote to a rule");

        public final String work;
        public final String meaning;

        Status(String work, String meaning) {
            this.work = work;
            this.meaning = meaning;
        }
    }

    final Status status;
    final String kind;
    final String symbol;
    final String readable;
    final String target;
    int count;
    /** Used by at least one class that always runs in the game. */
    boolean runtime;
    /** Used by an optional-integration class (runs only when another mod is installed). */
    boolean integration;

    /** Where the problem matters: runtime, integration (optional mods) or data-generation (build time only). */
    String context() {
        return runtime ? "runtime" : integration ? "integration" : "data-generation";
    }
    final Set<String> usedIn = new TreeSet<>();

    Finding(Status status, String kind, String symbol, String readable, String target) {
        this.status = status;
        this.kind = kind;
        this.symbol = symbol;
        this.readable = readable;
        this.target = target;
    }

    String key() {
        return status + "|" + kind + "|" + symbol;
    }
}
