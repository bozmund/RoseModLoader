package rose.foundry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/**
 * One git worktree per task ({@code foundry/worktrees/<id>}, branch {@code foundry/<id>} from {@code develop}),
 * so agents never touch the main checkout. The worktree links to the main checkout's {@code corpus/} and
 * {@code run/} (game files, mappings, decompiled sources) instead of copying gigabytes.
 *
 * <p>Those links are removed with a plain link delete before the worktree is removed, so removing a worktree can
 * never delete the shared folders behind them.
 */
final class Worktrees {
    static final String INTEGRATION_BRANCH = "develop";
    private static final List<String> SHARED = List.of("corpus", "run");

    private final Path home;

    Worktrees(Path home) {
        this.home = home;
    }

    Path path(String id) {
        return home.resolve("foundry").resolve("worktrees").resolve(id);
    }

    String branch(String id) {
        return "foundry/" + id;
    }

    void ensureIntegrationBranch() throws IOException, InterruptedException {
        try {
            Proc.git(home, "rev-parse", "--verify", "--quiet", INTEGRATION_BRANCH);
        } catch (IOException missing) {
            Proc.git(home, "branch", INTEGRATION_BRANCH, "HEAD");
        }
    }

    /** Creates (or reuses) the task's worktree, branched from the current integration branch. */
    Path open(String id) throws IOException, InterruptedException {
        Path dir = path(id);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir.getParent());
            boolean branchExists;
            try {
                Proc.git(home, "rev-parse", "--verify", "--quiet", branch(id));
                branchExists = true;
            } catch (IOException e) {
                branchExists = false;
            }
            if (branchExists) Proc.git(home, "worktree", "add", dir.toString(), branch(id));
            else Proc.git(home, "worktree", "add", "-b", branch(id), dir.toString(), INTEGRATION_BRANCH);
        }
        for (String shared : SHARED) link(dir.resolve(shared), home.resolve(shared));
        return dir;
    }

    /** Throws away the agent's uncommitted changes (ignored folders such as the shared links stay). */
    void reset(String id) throws IOException, InterruptedException {
        Path dir = path(id);
        Proc.git(dir, "reset", "--hard", "-q");
        Proc.git(dir, "clean", "-fdq", "-e", ".foundry");
    }

    /** Files the agent changed or added relative to the branch point, as repo-relative paths. */
    List<String> changedFiles(String id) throws IOException, InterruptedException {
        String out = Proc.git(path(id), "status", "--porcelain", "--untracked-files=all");
        return out.isBlank() ? List.of() : out.lines().map(l -> l.substring(3).trim().replace('\\', '/')).toList();
    }

    void commit(String id, String message) throws IOException, InterruptedException {
        Path dir = path(id);
        Proc.git(dir, "add", "-A");
        Proc.git(dir, "commit", "-q", "-m", message);
    }

    /**
     * Moves the integration branch to the task's branch. Tasks run one at a time from the latest integration
     * branch, so this is normally a fast-forward; anything else is refused.
     */
    void land(String id) throws IOException, InterruptedException {
        Path dir = path(id);
        Proc.git(dir, "rebase", "-q", INTEGRATION_BRANCH);
        Proc.git(home, "merge-base", "--is-ancestor", INTEGRATION_BRANCH, branch(id));
        Proc.git(home, "branch", "-f", INTEGRATION_BRANCH, branch(id));
    }

    /** Removes the worktree (and optionally its branch), unlinking the shared folders first. */
    void close(String id, boolean deleteBranch) throws IOException, InterruptedException {
        Path dir = path(id);
        if (Files.exists(dir)) {
            for (String shared : SHARED) unlink(dir.resolve(shared));
            Proc.git(home, "worktree", "remove", "--force", dir.toString());
        }
        if (deleteBranch) {
            try {
                Proc.git(home, "branch", "-D", branch(id));
            } catch (IOException ignored) {
                // already gone
            }
        }
    }

    private static void link(Path link, Path target) throws IOException, InterruptedException {
        if (Files.exists(link, LinkOption.NOFOLLOW_LINKS)) return;
        if (!Files.exists(target)) Files.createDirectories(target);
        if (Proc.WINDOWS) {
            // Junctions need no admin rights, unlike symlinks.
            Proc.run(link.getParent(), List.of("cmd", "/c", "mklink", "/J", link.toString(), target.toString()),
                    link.resolveSibling(".foundry-link.log"), 1);
            Files.deleteIfExists(link.resolveSibling(".foundry-link.log"));
            if (!Files.exists(link)) throw new IOException("could not create junction " + link + " -> " + target);
        } else {
            Files.createSymbolicLink(link, target);
        }
    }

    /** Deletes only the link itself. Refuses to touch a real directory. */
    private static void unlink(Path link) throws IOException {
        if (!Files.exists(link, LinkOption.NOFOLLOW_LINKS)) return;
        BasicFileAttributes attrs = Files.readAttributes(link, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        if (!(attrs.isSymbolicLink() || attrs.isOther())) {
            throw new IOException("refusing to delete " + link + ": it is a real directory, not a link");
        }
        Files.delete(link); // removes the junction/symlink, never its target's contents
    }
}
