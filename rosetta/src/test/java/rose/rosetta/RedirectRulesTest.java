package rose.rosetta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RedirectRulesTest {
    /**
     * Two rules for one SRG member (mmlib's crop calls 1.20.1 isValidBonemealTarget on the BonemealableBlock interface
     * and through super on a game block): the interface owner matches exactly, any other owner gets the first rule.
     */
    @Test
    void ownersMatchExactlyAndOtherOwnersFallBackToTheFirstRule(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("redirects.tsv");
        String member = "m_7370_(Lgame/LevelReader;Lgame/BlockPos;Lgame/BlockState;Z)Z";
        Files.writeString(file, "# super calls first\n"
                + "game/Block." + member + "\tshim/Bonemeal.superTarget\tevidence\n"
                + "game/BonemealableBlock." + member + "\tshim/Bonemeal.target\tevidence\n");
        for (int i = 0; i < 20; i++) { // the fallback mustn't depend on hashing
            RedirectRules rules = RedirectRules.read(file);
            String desc = member.substring(member.indexOf('('));
            assertEquals("target", rules.find("game/BonemealableBlock", "m_7370_", desc).shimName());
            assertEquals("superTarget", rules.find("game/CropBlock", "m_7370_", desc).shimName());
            assertEquals("superTarget", rules.find("game/Block", "m_7370_", desc).shimName());
        }
        assertNull(RedirectRules.read(file).find("game/Block", "m_7371_", "()Z"));
    }
}
