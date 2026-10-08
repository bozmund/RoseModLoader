package rose.analyzer;

import rose.translate.ClassIndex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rose.analyzer.Finding.Status;
import rose.rosetta.NameLayer;
import rose.rosetta.NameLayer.ClassEntry;
import rose.rosetta.NameLayer.How;
import rose.rosetta.NameLayer.MemberEntry;

/**
 * A miniature "old version" and "26.3" built from synthetic classes:
 * <ul>
 *   <li>{@code net/minecraft/Old} was renamed to {@code net/minecraft/New};</li>
 *   <li>its method {@code m_1_} (readable "tick") kept its name, {@code m_2_} ("use") is gone, and {@code m_3_}
 *       ("render") still exists but with a new signature.</li>
 * </ul>
 */
class AnalyzerTest {
    private static final NameLayer LAYER = new NameLayer("test",
            Map.of("net/minecraft/Old", new ClassEntry("net/minecraft/Old", "net/minecraft/New", How.INTERMEDIARY),
                    "net/minecraft/Removed", new ClassEntry("net/minecraft/Removed", "-", How.GONE)),
            Map.of("m_1_", new MemberEntry("m_1_", "tick", "tick", How.SAME),
                    "m_2_", new MemberEntry("m_2_", "use", "-", How.GONE),
                    "m_3_", new MemberEntry("m_3_", "render", "render", How.SAME)),
            Map.of());

    private Report analyze(Path dir, Map<String, byte[]> modClasses) throws Exception {
        Path target = TestJars.jar(dir, "target.jar", Map.of(
                "net/minecraft/New", TestJars.type("net/minecraft/New", "java/lang/Object", "tick()V", "render(I)V")));
        Path old = TestJars.jar(dir, "old.jar", Map.of(
                "net/minecraft/Old", TestJars.type("net/minecraft/Old", "java/lang/Object", "tick()V", "use()V", "render()V")));
        Path mod = TestJars.jar(dir, "mod.jar", modClasses);
        Analyzer analyzer = new Analyzer(LAYER, ClassIndex.of(List.of(target), true), ClassIndex.of(List.of(old), true));
        return analyzer.analyze(mod);
    }

    private static Map<String, Status> byReadable(Report report) {
        return report.findings().stream().collect(Collectors.toMap(f -> f.readable, f -> f.status, (a, b) -> a));
    }

    @Test
    void pureRenamesResolveWithoutFindings(@TempDir Path dir) throws Exception {
        Report report = analyze(dir, Map.of("mod/A", TestJars.caller("mod/A", "net/minecraft/Old", "m_1_", "()V")));
        assertEquals(0, report.blocking(), () -> "unexpected: " + byReadable(report));
    }

    @Test
    void removedMethodsAreReported(@TempDir Path dir) throws Exception {
        Report report = analyze(dir, Map.of("mod/A", TestJars.caller("mod/A", "net/minecraft/Old", "m_2_", "()V")));
        assertEquals(Status.METHOD_GONE, byReadable(report).get("net/minecraft/Old.use()V"));
    }

    @Test
    void sameNameWithNewSignatureIsASignatureChange(@TempDir Path dir) throws Exception {
        Report report = analyze(dir, Map.of("mod/A", TestJars.caller("mod/A", "net/minecraft/Old", "m_3_", "()V")));
        assertEquals(Status.SIGNATURE_CHANGED, byReadable(report).get("net/minecraft/Old.render()V"));
    }

    @Test
    void overridesOfRemovedMethodsNeedABridge(@TempDir Path dir) throws Exception {
        Report report = analyze(dir, Map.of("mod/MyBlock", TestJars.type("mod/MyBlock", "net/minecraft/Old", "m_1_()V", "m_2_()V")));
        Map<String, Status> found = byReadable(report);
        assertEquals(Status.OVERRIDE_GONE, found.get("mod/MyBlock.use()V"));
        assertTrue(!found.containsKey("mod/MyBlock.tick()V"), "tick still exists and is overridden correctly: " + found);
    }

    @Test
    void removedClassesAreReportedOnceNotPerMember(@TempDir Path dir) throws Exception {
        Report report = analyze(dir, Map.of("mod/A", TestJars.caller("mod/A", "net/minecraft/Removed", "m_9_", "()V")));
        assertEquals(Map.of("net/minecraft/Removed", Status.CLASS_GONE), byReadable(report));
    }

    @Test
    void redirectRulesResolveCallsOnlyWhenTheShimMatches(@TempDir Path dir) throws Exception {
        Path rules = dir.resolve("redirects.tsv");
        java.nio.file.Files.writeString(rules, "net/minecraft/Old.m_2_()V\tshim/OldShim.use\tevidence\n");
        Map<String, byte[]> mod = Map.of("mod/A", TestJars.caller("mod/A", "net/minecraft/Old", "m_2_", "()V"));

        // Shim with the right signature: static void use(New self)
        Path good = TestJars.jar(dir, "good-shim.jar", Map.of("shim/OldShim", staticShim("use", "(Lnet/minecraft/New;)V")));
        Report fixed = analyzeWith(dir, rules, good, mod);
        assertEquals(0, fixed.blocking(), () -> "unexpected: " + byReadable(fixed));
        assertEquals(1, fixed.redirected());

        // Shim with the wrong signature: the rule is reported as broken
        Path bad = TestJars.jar(dir, "bad-shim.jar", Map.of("shim/OldShim", staticShim("use", "(Ljava/lang/String;)V")));
        Report broken = analyzeWith(dir, rules, bad, mod);
        assertEquals(Status.RULE_BROKEN, byReadable(broken).get("net/minecraft/Old.use()V"));
    }

    private Report analyzeWith(Path dir, Path rules, Path shimJar, Map<String, byte[]> modClasses) throws Exception {
        Path target = TestJars.jar(dir, "target.jar", Map.of(
                "net/minecraft/New", TestJars.type("net/minecraft/New", "java/lang/Object", "tick()V", "render(I)V")));
        Path old = TestJars.jar(dir, "old.jar", Map.of(
                "net/minecraft/Old", TestJars.type("net/minecraft/Old", "java/lang/Object", "tick()V", "use()V", "render()V")));
        Path mod = TestJars.jar(dir, "mod.jar", modClasses);
        return new Analyzer(LAYER, rose.rosetta.RedirectRules.read(rules), ClassIndex.of(List.of(target, shimJar), true),
                ClassIndex.of(List.of(old), true)).analyze(mod);
    }

    private static byte[] staticShim(String name, String desc) {
        var cw = new org.objectweb.asm.ClassWriter(org.objectweb.asm.ClassWriter.COMPUTE_MAXS);
        cw.visit(org.objectweb.asm.Opcodes.V21, org.objectweb.asm.Opcodes.ACC_PUBLIC, "shim/OldShim", null, "java/lang/Object", null);
        var mv = cw.visitMethod(org.objectweb.asm.Opcodes.ACC_PUBLIC | org.objectweb.asm.Opcodes.ACC_STATIC, name, desc, null, null);
        mv.visitCode();
        mv.visitInsn(org.objectweb.asm.Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    @Test
    void integrationPackagesAreNotRuntimeProblems(@TempDir Path dir) throws Exception {
        Report report = analyze(dir, Map.of(
                "mod/integration/jei/Plugin", TestJars.caller("mod/integration/jei/Plugin", "mezz/jei/api/IModPlugin", "register", "()V")));
        assertEquals(0, report.blocking());
        assertEquals(1, report.problems("integration"));
    }
}
