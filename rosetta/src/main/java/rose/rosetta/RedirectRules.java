package rose.rosetta;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Call redirects: a call to an old method that no longer exists (or changed meaning) is replaced by a call to a
 * static "shim" method in Rose's era bridge that does the same job on 26.3.
 *
 * <p>File format ({@code rosetta/rules/<source>/redirects.tsv}, tab-separated, {@code #} comments):
 * <pre>
 * symbol                                                            shim                                         evidence
 * net/minecraft/world/item/ItemStack.m_150930_(Lnet/minecraft/world/item/Item;)Z  rose/era/v1_20_1/shim/ItemStackShim.is  1.20.1: getItem() == item; ...
 * </pre>
 * {@code symbol} is the call exactly as it appears in old mod bytecode (owner, runtime name, old descriptor).
 * The shim takes the receiver first for instance calls, then the original arguments, all in 26.3 types:
 * {@code ItemStack.is(Item)} becomes {@code static boolean is(ItemStack self, Item item)}.
 * A constructor ({@code owner.<init>(args)V}) becomes a factory {@code static Owner name(args)}. A field read
 * ({@code owner.name:desc}) becomes {@code static Desc name()} for static fields, {@code static Desc name(Owner self)}
 * for instance fields; writes are not redirected.
 */
public final class RedirectRules {
    public record Redirect(String symbol, String shimOwner, String shimName, String evidence) {
        /**
         * The shim's descriptor in the <em>old</em> names (translate it with the name layer). A constructor's shim is
         * a factory: same arguments, returns the owner type.
         */
        public String shimDescriptor(boolean isStatic) {
            String owner = owner();
            if (isField()) {
                String type = symbol.substring(symbol.indexOf(':') + 1);
                return isStatic ? "()" + type : "(L" + owner + ";)" + type;
            }
            String desc = symbol.substring(symbol.indexOf('('));
            if (isConstructor()) return desc.substring(0, desc.indexOf(')') + 1) + "L" + owner + ";";
            if (isStatic) return desc;
            return "(L" + owner + ";" + desc.substring(1);
        }

        public String owner() {
            int end = isField() ? symbol.indexOf(':') : symbol.indexOf('(');
            return symbol.substring(0, symbol.lastIndexOf('.', end));
        }

        public boolean isField() {
            return !symbol.contains("(");
        }

        public boolean isConstructor() {
            return symbol.contains(".<init>(");
        }
    }

    private final Map<String, Redirect> bySymbol;
    /** Rules for SRG-named members by name and descriptor: code reaches them through subclasses too. */
    private final Map<String, Redirect> bySrgMember = new java.util.HashMap<>();
    private final boolean hasConstructorRules;

    public RedirectRules(Map<String, Redirect> bySymbol) {
        this.bySymbol = Map.copyOf(bySymbol);
        this.hasConstructorRules = bySymbol.values().stream().anyMatch(Redirect::isConstructor);
        for (Redirect r : bySymbol.values()) {
            String member = r.symbol().substring(r.owner().length() + 1);
            if (member.matches("[mf]_\\d+_[(:].*")) bySrgMember.put(member, r);
        }
    }

    public static RedirectRules empty() {
        return new RedirectRules(Map.of());
    }

    public static RedirectRules read(Path file) throws IOException {
        if (!Files.exists(file)) return empty();
        Map<String, Redirect> rules = new LinkedHashMap<>();
        int lineNo = 0;
        for (String line : Files.readAllLines(file)) {
            lineNo++;
            if (line.isBlank() || line.startsWith("#")) continue;
            String[] p = line.split("\t");
            if (p.length < 3 || p[2].isBlank()) {
                throw new IOException(file + ":" + lineNo + ": expected symbol<TAB>shimOwner.shimName<TAB>evidence (evidence is required)");
            }
            if (!p[0].contains(".") || !(p[0].contains("(") || p[0].contains(":"))) throw new IOException(file + ":" + lineNo + ": bad symbol " + p[0]);
            int dot = p[1].lastIndexOf('.');
            if (dot <= 0) throw new IOException(file + ":" + lineNo + ": shim must be owner/Class.method, got " + p[1]);
            Redirect r = new Redirect(p[0], p[1].substring(0, dot), p[1].substring(dot + 1), p[2]);
            if (rules.putIfAbsent(r.symbol(), r) != null) throw new IOException(file + ":" + lineNo + ": duplicate rule for " + r.symbol());
        }
        return new RedirectRules(rules);
    }

    /** The redirect for a call {@code owner.name desc} in old names, or {@code null}. */
    public Redirect find(String owner, String name, String desc) {
        Redirect exact = bySymbol.get(owner + "." + name + desc);
        return exact != null ? exact : bySrgMember.get(name + desc);
    }

    /** The redirect for reading field {@code owner.name:desc} in old names, or {@code null}. */
    public Redirect findField(String owner, String name, String desc) {
        Redirect exact = bySymbol.get(owner + "." + name + ":" + desc);
        return exact != null ? exact : bySrgMember.get(name + ":" + desc);
    }

    public boolean hasConstructorRules() {
        return hasConstructorRules;
    }

    public Map<String, Redirect> all() {
        return bySymbol;
    }
}
