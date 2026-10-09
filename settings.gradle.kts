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
    "bridge-cli",
    "foundry",
)

include("eras:era-1.20.1")
include("dialects:forge-1.20.1")

// Small mods used to test Rose itself.
include("testmods:hello")
include("testmods:sample")
include("testmods:fdcheck")
