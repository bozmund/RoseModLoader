package rose.testmods.oracle;

import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.resources.Identifier;
import rose.api.ModInitializer;
import rose.api.event.RoseEvents;
import rose.api.gametest.RoseGameTests;

/**
 * Rose entrypoint: dumps the mods running on Rose once the server has started. {@code ./gradlew :boot:dumpOracle}
 * runs the GameTest server with the test {@code oracle:dump_written}, which checks the dump is complete.
 */
public final class RoseOracle implements ModInitializer {
    @Override
    public void onInitialize() {
        if (OracleDump.requested()) RoseEvents.SERVER_STARTED.register(OracleDump::runFromProperties);

        RoseGameTests.register(Identifier.fromNamespaceAndPath("oracle", "dump_written"), helper -> {
            if (OracleDump.requested()) {
                Path out = Path.of(System.getProperty("rose.oracle.out"));
                for (String file : OracleDump.FILES) {
                    helper.assertTrue(Files.isRegularFile(out.resolve(file + ".json")), "oracle dump file missing: " + file + ".json");
                }
            }
            helper.succeed(); // without -Drose.oracle.out there's nothing to check
        });
    }
}
