package rose.boot.mojang;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

class RulesTest {
    private static JsonObject json(String s) {
        return JsonParser.parseString(s).getAsJsonObject();
    }

    @Test
    void noRulesMeansAllowed() {
        assertTrue(Rules.allowed(json("{\"name\":\"lib\"}")));
    }

    @Test
    void osRuleOnlyAllowsThatOs() {
        JsonObject forCurrent = json("{\"rules\":[{\"action\":\"allow\",\"os\":{\"name\":\"" + Rules.OS_NAME + "\"}}]}");
        JsonObject forOther = json("{\"rules\":[{\"action\":\"allow\",\"os\":{\"name\":\"plan9\"}}]}");
        assertTrue(Rules.allowed(forCurrent));
        assertFalse(Rules.allowed(forOther));
    }

    @Test
    void laterDisallowOverridesAllow() {
        JsonObject lib = json("{\"rules\":[{\"action\":\"allow\"},{\"action\":\"disallow\",\"os\":{\"name\":\"" + Rules.OS_NAME + "\"}}]}");
        assertFalse(Rules.allowed(lib));
    }

    @Test
    void featureRulesAreNotMet() {
        assertFalse(Rules.allowed(json("{\"rules\":[{\"action\":\"allow\",\"features\":{\"is_demo_user\":true}}]}")));
    }
}
