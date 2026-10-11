package rose.translate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MixinConfigTest {
    @Test
    void oldConfigsBecomeOptionalAndDroppedMixinsAreLeftOut() {
        String json = """
                {"required":true,"package":"software.bernie.geckolib.mixin","mixins":["common.EntityMixin"],
                 "client":["client.TextureManagerMixin","client.RenderMixin"]}""";
        var out = JsonParser.parseString(ModTranslator.tolerantMixinConfig(json,
                Set.of("software.bernie.geckolib.mixin.client.TextureManagerMixin"))).getAsJsonObject();
        assertEquals(false, out.get("required").getAsBoolean());
        assertEquals(0, out.getAsJsonObject("injectors").get("defaultRequire").getAsInt());
        assertEquals("[\"client.RenderMixin\"]", out.get("client").toString());
        assertEquals("[\"common.EntityMixin\"]", out.get("mixins").toString());
    }
}
