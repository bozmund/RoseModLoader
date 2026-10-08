package rose.foundry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * The single document an agent gets for a task: what to change, the exact deliverables, how to check its work,
 * and every piece of source it needs (old method, 26.3 class, the mod's call sites, a worked example). Built so a
 * small local model can finish without searching.
 */
final class ContextPack {
    static String build(Task task, Path home, String feedback) throws IOException {
        String readable = task.get("readable");
        String readableName = readable.substring(readable.lastIndexOf('.', readable.indexOf('(')) + 1, readable.indexOf('('));
        String oldOwner = task.get("symbol").substring(0, task.get("symbol").lastIndexOf('.', task.get("symbol").indexOf('(')));
        String newOwner = task.get("newOwner");

        StringBuilder md = new StringBuilder();
        md.append("# Rose foundry task ").append(task.id()).append("\n\n");
        md.append("""
                You are fixing one compatibility gap in Rose Mod Loader (see AGENTS.md for the project rules).
                An old Minecraft 1.20.1 mod calls a vanilla method that changed in Minecraft 26.3. Rose will replace every
                such call with a call to a static **shim** method you write. The shim must do what the 1.20.1 method did,
                using only Minecraft 26.3 APIs.

                ## Deliverables (exactly these, nothing else)

                """);
        md.append("1. In `").append(task.get("shimFile")).append("` (create it if missing, package `")
                .append(Planner.SHIM_PACKAGE).append("`, a `final` class with a private constructor), add:\n\n");
        md.append("   ```java\n   ").append(task.get("shimSignature")).append(" { ... }\n   ```\n\n");
        md.append("   Add a one-line Javadoc saying what the 1.20.1 method did. If the file already exists, add the method; don't change other methods.\n\n");
        md.append("2. Append this line to `rosetta/rules/forge-1.20.1/redirects.tsv`. The separators are TAB characters. ")
                .append("Replace `<evidence>` with one short sentence: what 1.20.1 did and which 26.3 API you used.\n\n");
        md.append("   ```\n   ").append(task.get("ruleLine").replace("\t", "<TAB>")).append("\n   ```\n\n");
        md.append("""
                ## Rules
                - Change only those two files. Don't edit tests, build files or other code; the gates reject that.
                - Use only classes and methods that exist in 26.3: check `corpus/minecraft/26.3/src/` (real source, readable names).
                - Prefer the simplest faithful implementation. If 26.3 truly has no way to do it, stop and explain why in your final message.
                - Check your work: `./gradlew -q :eras:era-1.20.1:compileJava` must succeed.
                - Finish with a short summary of what you did.

                After you finish, the foundry runs the gates: full build + tests, `rose analyze` (this call must be resolved and
                nothing new may break), and the in-game GameTests.

                """);

        if (feedback != null && !feedback.isBlank()) {
            md.append("## Your previous attempt failed\n\nFix this, keeping what was right:\n\n```\n").append(feedback).append("\n```\n\n");
        }

        md.append("## Reference\n\n");
        md.append("### What the mod calls (1.20.1)\n\n`").append(readable).append("`, ")
                .append("true".equals(task.get("static")) ? "a static method" : "an instance method (so the shim's first parameter is the receiver, `self`)")
                .append(", used ").append(task.get("uses")).append(" times.\n\n");

        // Show the classes that really declare the method (it is often inherited), old and new.
        Path oldJar = home.resolve("corpus/minecraft/1.20.1/client-named.jar");
        List<String> oldDeclaring = Declarations.declaringClasses(oldJar, oldOwner, readableName, 2);
        if (oldDeclaring.isEmpty()) oldDeclaring = List.of(oldOwner);
        for (String declaring : oldDeclaring) {
            appendMethods(md, "1.20.1 source of `" + readableName + "` in " + simple(declaring),
                    SourceSnippets.methods(outerSource(home, "1.20.1", declaring), readableName, 4, 60));
        }

        Path newJar = home.resolve("corpus/minecraft/26.3/client.jar");
        List<String> newDeclaring = Declarations.declaringClasses(newJar, newOwner, readableName, 3);
        Path newSrc = outerSource(home, "26.3", newOwner);
        List<String> newMethods = new ArrayList<>();
        for (String declaring : newDeclaring) {
            for (String m : SourceSnippets.methods(outerSource(home, "26.3", declaring), readableName, 6, 40)) {
                newMethods.add("// in " + simple(declaring) + "\n" + m);
            }
        }
        if (!newMethods.isEmpty()) {
            appendMethods(md, "26.3 methods named `" + readableName + "` in the hierarchy of " + simple(newOwner), newMethods);
        } else {
            md.append("### 26.3 `").append(simple(newOwner)).append("` has no method named `").append(readableName)
                    .append("`; its public methods:\n\n```java\n");
            SourceSnippets.signatures(newSrc, 80).forEach(s -> md.append(s).append('\n'));
            md.append("```\n\n");
        }
        md.append("Full sources: `corpus/minecraft/1.20.1/src/").append(oldOwner.replaceAll("\\$.*", "")).append(".java` and `corpus/minecraft/26.3/src/")
                .append(newOwner.replaceAll("\\$.*", "")).append(".java`.\n\n");

        md.append("### Where the mod uses it (decompiled, 1.20.1 names)\n\n");
        Path modSrc = home.resolve(ModSources.dir(Path.of(task.get("jar"))));
        int shown = 0;
        for (String cls : task.get("usedIn").split(",")) {
            if (shown >= 4) break;
            Path file = modSrc.resolve(cls.replaceAll("\\$.*", "") + ".java");
            for (String usage : SourceSnippets.usages(file, readableName, 3, 2)) {
                md.append("`").append(simple(cls)).append("`:\n```java\n").append(usage).append("```\n");
                shown++;
            }
        }
        if (shown == 0) md.append("(no decompiled usage found; the call may be inside a lambda)\n");

        md.append("\n### Worked example (an accepted redirect)\n\n");
        Path example = home.resolve("eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemStackShim.java");
        if (Files.exists(example)) md.append("```java\n").append(Files.readString(example)).append("```\n\n");
        md.append("Its rule line:\n```\n");
        for (String line : Files.readAllLines(home.resolve("rosetta/rules/forge-1.20.1/redirects.tsv"))) {
            if (line.contains("ItemStackShim.is")) md.append(line.replace("\t", "<TAB>")).append('\n');
        }
        md.append("```\n");
        return md.toString();
    }

    private static void appendMethods(StringBuilder md, String title, List<String> methods) {
        md.append("### ").append(title).append("\n\n");
        if (methods.isEmpty()) {
            md.append("(not found in this class or its supertypes; search corpus/minecraft/<version>/src)\n\n");
            return;
        }
        md.append("```java\n");
        methods.forEach(m -> md.append(m).append('\n'));
        md.append("```\n\n");
    }

    /** Nested classes live in their outer class's file. */
    private static Path outerSource(Path home, String version, String internalName) {
        return home.resolve("corpus/minecraft").resolve(version).resolve("src").resolve(internalName.replaceAll("\\$.*", "") + ".java");
    }

    private static String simple(String internalName) {
        return internalName.substring(internalName.lastIndexOf('/') + 1);
    }

    private ContextPack() {}
}
