package rose.foundry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/** Tasks live in {@code foundry/tasks/<state>/<id>.md}; moving a task between states moves its file. */
public final class TaskStore {
    private final Path root;

    public TaskStore(Path root) {
        this.root = root;
    }

    public List<Task> list(Task.State state) throws IOException {
        Path dir = root.resolve(state.folder());
        if (!Files.isDirectory(dir)) return List.of();
        List<Task> out = new ArrayList<>();
        try (Stream<Path> files = Files.list(dir)) {
            for (Path f : files.filter(p -> p.toString().endsWith(".md")).sorted().toList()) {
                out.add(Task.parse(state, Files.readString(f)));
            }
        }
        return out;
    }

    public List<Task> all() throws IOException {
        List<Task> out = new ArrayList<>();
        for (Task.State s : Task.State.values()) out.addAll(list(s));
        return out;
    }

    public Optional<Task> find(String id) throws IOException {
        for (Task.State s : Task.State.values()) {
            Path f = file(s, id);
            if (Files.exists(f)) return Optional.of(Task.parse(s, Files.readString(f)));
        }
        return Optional.empty();
    }

    public boolean exists(String id) throws IOException {
        return find(id).isPresent();
    }

    /** Open tasks, most-used symbols first (fixing those helps the most). */
    public List<Task> openByImpact() throws IOException {
        return list(Task.State.OPEN).stream()
                .sorted(Comparator.comparingInt((Task t) -> -t.getInt("uses", 0)).thenComparing(Task::id))
                .toList();
    }

    public void save(Task task) throws IOException {
        Path f = file(task.state(), task.id());
        Files.createDirectories(f.getParent());
        Files.writeString(f, task.serialize());
    }

    public void move(Task task, Task.State to) throws IOException {
        Path from = file(task.state(), task.id());
        task.state(to);
        save(task);
        if (!from.equals(file(to, task.id()))) Files.deleteIfExists(from);
    }

    private Path file(Task.State state, String id) {
        return root.resolve(state.folder()).resolve(id + ".md");
    }
}
