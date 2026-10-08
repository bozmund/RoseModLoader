package rose.foundry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import rose.rosetta.NameLayer;
import rose.translate.RosettaRemapper;

/**
 * Turns {@code rose analyze} findings into tasks. Today it plans one kind of S-tier task:
 * <b>redirect</b> — a call site to an old vanilla method that is gone or changed gets a static shim in the era
 * bridge plus one rule line. The task states the shim's exact Java signature, so a small model only has to write
 * the body.
 */
public final class Planner {
    public static final String SHIM_PACKAGE = "rose.era.v1_20_1.shim";
    private static final Set<String> REDIRECTABLE = Set.of("METHOD_GONE", "SIGNATURE_CHANGED", "METHOD_MISSING");

    private final NameLayer layer;
    private final RosettaRemapper remapper;
    private final Path vanillaOldJar;

    public Planner(NameLayer layer, Path vanillaOldJar) {
        this.layer = layer;
        this.remapper = new RosettaRemapper(layer);
        this.vanillaOldJar = vanillaOldJar;
    }

    /** Creates tasks for findings that don't have one yet. Returns the new tasks. */
    public List<Task> plan(Path reportJson, Path modJar, TaskStore store) throws IOException {
        JsonObject report = JsonParser.parseString(Files.readString(reportJson)).getAsJsonObject();
        String mod = report.has("modId") && !report.get("modId").isJsonNull()
                ? report.get("modId").getAsString() : modJar.getFileName().toString();
        List<Task> created = new ArrayList<>();
        // Open tasks whose finding is gone (fixed by a rule, an adaptation or another task) are closed.
        Set<String> current = new HashSet<>();
        report.getAsJsonArray("findings").forEach(e -> current.add(e.getAsJsonObject().get("symbol").getAsString()));
        for (Task open : store.list(Task.State.OPEN)) {
            if (mod.equals(open.get("mod")) && !current.contains(open.get("symbol"))) {
                open.appendBody("\n## Closed " + Foundry.now() + "\n\nNo longer reported by `rose analyze` (resolved elsewhere).\n");
                open.set("closedReason", "resolved-elsewhere");
                store.move(open, Task.State.REJECTED);
                Foundry.log("closed " + open.id() + " (resolved elsewhere): " + open.get("readable"));
            }
        }
        try (ZipFile vanilla = new ZipFile(vanillaOldJar.toFile())) {
            for (JsonElement e : report.getAsJsonArray("findings")) {
                JsonObject f = e.getAsJsonObject();
                if (!"runtime".equals(f.get("context").getAsString())) continue;
                if (!"method".equals(f.get("kind").getAsString())) continue;
                if (!REDIRECTABLE.contains(f.get("status").getAsString())) continue;
                Task task = redirectTask(mod, modJar, f, vanilla);
                if (task == null) continue;
                var existing = store.find(task.id());
                if (existing.isPresent()) {
                    // A task closed only because its finding vanished comes back if the finding does.
                    Task old = existing.get();
                    if (old.state() == Task.State.REJECTED && "resolved-elsewhere".equals(old.get("closedReason"))) {
                        old.set("closedReason", "-");
                        old.appendBody("\n## Reopened " + Foundry.now() + "\n\nReported again by `rose analyze`.\n");
                        store.move(old, Task.State.OPEN);
                        Foundry.log("reopened " + old.id() + ": " + old.get("readable"));
                    }
                    continue;
                }
                store.save(task);
                created.add(task);
            }
        }
        return created;
    }

    private Task redirectTask(String mod, Path modJar, JsonObject f, ZipFile vanilla) throws IOException {
        String symbol = f.get("symbol").getAsString();
        String readable = f.get("readable").getAsString();
        int paren = symbol.indexOf('(');
        int dot = symbol.lastIndexOf('.', paren);
        String owner = symbol.substring(0, dot);
        String name = symbol.substring(dot + 1, paren);
        String desc = symbol.substring(paren);
        String readableName = readable.substring(readable.lastIndexOf('.', readable.indexOf('(')) + 1, readable.indexOf('('));
        if (name.equals("<init>")) return null; // constructors need a different kind of rule

        Boolean isStatic = isStatic(vanilla, owner, readableName, desc);
        if (isStatic == null) return null; // can't tell; leave it for a human or a later planner

        String newOwner = remapper.map(owner);
        String newDesc = remapper.mapMethodDesc(desc);
        String shimSimple = shimClassName(newOwner);
        String shimInternal = SHIM_PACKAGE.replace('.', '/') + "/" + shimSimple;
        String signature = javaSignature(readableName, newOwner, newDesc, isStatic);

        Task t = new Task(Task.State.OPEN, Map.of(), "");
        t.set("id", mod + "-" + shortHash(symbol));
        t.set("kind", "redirect");
        t.set("tier", "S");
        t.set("mod", mod);
        t.set("jar", modJar.toString().replace('\\', '/'));
        t.set("finding", f.get("status").getAsString());
        t.set("symbol", symbol);
        t.set("readable", readable);
        t.set("static", isStatic);
        t.set("newOwner", newOwner);
        t.set("uses", f.get("count").getAsInt());
        List<String> usedIn = new ArrayList<>();
        f.getAsJsonArray("usedIn").forEach(u -> usedIn.add(u.getAsString()));
        t.set("usedIn", String.join(",", usedIn.subList(0, Math.min(10, usedIn.size()))));
        t.set("shimClass", SHIM_PACKAGE + "." + shimSimple);
        t.set("shimFile", "eras/era-1.20.1/src/main/java/" + shimInternal + ".java");
        t.set("shimSignature", signature);
        t.set("ruleLine", symbol + "\t" + shimInternal + "." + readableName + "\t<evidence>");
        t.set("attempts", 0);
        t.set("created", LocalDate.now());
        String body = """

                # Redirect `%s`

                The mod calls **%s** (%d uses), which %s in Minecraft 26.3.
                Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

                - Shim: `%s` in `%s`
                - Signature: `%s`
                - Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
                  `%s`
                """.formatted(readable, readable, f.get("count").getAsInt(), describe(f.get("status").getAsString()),
                shimSimple, t.get("shimFile"), signature, t.get("ruleLine").replace("\t", "<TAB>"));
        t.appendBody(body);
        return t;
    }

    private static String describe(String status) {
        return switch (status) {
            case "METHOD_GONE" -> "no longer exists";
            case "SIGNATURE_CHANGED" -> "still exists by name but with a different signature";
            default -> "can't be found";
        };
    }

    /** {@code public static boolean is(net.minecraft.world.item.ItemStack self, net.minecraft.world.item.Item item)} */
    static String javaSignature(String name, String newOwner, String newDesc, boolean isStatic) {
        Type method = Type.getMethodType(newDesc);
        List<String> params = new ArrayList<>();
        Set<String> used = new HashSet<>();
        if (!isStatic) {
            params.add(javaName(Type.getObjectType(newOwner)) + " self");
            used.add("self");
        }
        for (Type arg : method.getArgumentTypes()) {
            String base = paramName(arg);
            String n = base;
            for (int i = 2; !used.add(n); i++) n = base + i;
            params.add(javaName(arg) + " " + n);
        }
        return "public static " + javaName(method.getReturnType()) + " " + name + "(" + String.join(", ", params) + ")";
    }

    private static String javaName(Type t) {
        return t.getClassName().replace('$', '.');
    }

    private static String paramName(Type t) {
        Type element = t.getSort() == Type.ARRAY ? t.getElementType() : t;
        String simple = element.getSort() == Type.OBJECT
                ? element.getInternalName().substring(element.getInternalName().lastIndexOf('/') + 1).replaceAll(".*\\$", "")
                : element.getClassName();
        String n = Character.toLowerCase(simple.charAt(0)) + simple.substring(1) + (t.getSort() == Type.ARRAY ? "s" : "");
        return switch (n) {
            case "int", "long", "float", "double", "boolean", "char", "byte", "short" -> n.charAt(0) + "Value";
            default -> n;
        };
    }

    static String shimClassName(String newOwner) {
        String simple = newOwner.substring(newOwner.lastIndexOf('/') + 1);
        return simple.replace("$", "") + "Shim";
    }

    /** Looks the method up in vanilla 1.20.1 (readable names), walking up superclasses. Null if not found. */
    private static Boolean isStatic(ZipFile vanilla, String owner, String readableName, String desc) throws IOException {
        String current = owner;
        for (int depth = 0; current != null && depth < 20; depth++) {
            ZipEntry entry = vanilla.getEntry(current + ".class");
            if (entry == null) return null;
            ClassNode node = new ClassNode();
            try (InputStream in = vanilla.getInputStream(entry)) {
                new ClassReader(in.readAllBytes()).accept(node, ClassReader.SKIP_CODE);
            }
            for (MethodNode m : node.methods) {
                if (m.name.equals(readableName) && m.desc.equals(desc)) return (m.access & Opcodes.ACC_STATIC) != 0;
            }
            current = node.superName;
        }
        return null;
    }

    private static String shortHash(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 8).toLowerCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
