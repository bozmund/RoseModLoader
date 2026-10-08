rootProject.name = "rose"

// Each folder is a Gradle subproject. See docs/ARCHITECTURE.md for what each one does.
include(
    "boot",
    "loader",
    "mixin-service",
    "api",
    "core",
    "translate",
    "rosetta",
    "packfix",
    "analyzer",
    "agent-bridge",
    "testing",
    "corpus-tools",
)

include("eras:era-1.20.1")
include("dialects:forge-1.20.1")
