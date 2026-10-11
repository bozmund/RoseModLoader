package rose.rosetta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class NameLayerBuilderTest {
    /**
     * 1.21.11 intermediary gives field_28859 to two fields (ServerLevel.sleepStatus and a Blocks constant): fields are
     * matched by owner and name, and a bare name only when it is unique.
     */
    @Test
    void intermediaryFieldsAreMatchedWithTheirOwner() {
        Mappings intermediary = new Mappings();
        intermediary.classes.put("aif", "net/minecraft/class_3218");
        intermediary.classes.put("dlp", "net/minecraft/class_2246");
        intermediary.fields.put("aif.O", "field_28859");
        intermediary.fields.put("dlp.gr", "field_28859");
        intermediary.fields.put("dlp.a", "field_1");
        Mappings mojang = new Mappings();
        mojang.fields.put("aif.O", "sleepStatus");
        mojang.fields.put("dlp.gr", "DEEPSLATE_BRICKS");
        mojang.fields.put("dlp.a", "AIR");

        var qualified = NameLayerBuilder.joinQualified(intermediary, mojang.fields);
        assertEquals("sleepStatus", qualified.get("net/minecraft/class_3218.field_28859"));
        assertEquals("DEEPSLATE_BRICKS", qualified.get("net/minecraft/class_2246.field_28859"));
        var unique = NameLayerBuilder.joinUnique(intermediary.fields, mojang.fields);
        assertNull(unique.get("field_28859"), "ambiguous on its own");
        assertEquals("AIR", unique.get("field_1"));
    }
}
