package rose.boot;

import rose.loader.RoseLoader;

/** What {@link RoseLaunch} starts. */
public enum LaunchTarget {
    CLIENT("client", RoseLoader.Side.CLIENT),
    SERVER("server", RoseLoader.Side.SERVER),
    /** Vanilla's GameTest server: runs test instances headless and exits. Needs no EULA. */
    GAMETEST("gametest", RoseLoader.Side.SERVER);

    private final String folder;
    private final RoseLoader.Side side;

    LaunchTarget(String folder, RoseLoader.Side side) {
        this.folder = folder;
        this.side = side;
    }

    /** Game directory name under the run folder. */
    public String folder() { return folder; }

    public RoseLoader.Side side() { return side; }
}
