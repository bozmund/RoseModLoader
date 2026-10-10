package rose.packfix.v1_20_1;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class SignFixTest {
    private static final byte[] PARTICLE_ONLY = """
            {"variants":{"":{"model":"fd:block/canvas_sign"}}}""".getBytes(StandardCharsets.UTF_8);

    @Test
    void facesMoveToWhereTheBlockTextureHasThem() {
        BufferedImage entity = coordinates(64, 32);
        BufferedImage block = SignFix.relayout(entity, false);
        assertEquals(32, block.getWidth());
        assertEquals(source(2, 2), block.getRGB(0, 2), "the front of the board");
        assertEquals(source(28, 2), block.getRGB(0, 16), "the back of the board");
        assertEquals(source(26, 1), block.getRGB(0, 28), "the underside is flipped vertically");
        assertEquals(0, block.getRGB(26, 0) >>> 24, "unused space stays transparent");

        BufferedImage hanging = SignFix.relayout(entity, true);
        assertEquals(source(20 + 15, 3), hanging.getRGB(0, 9), "flipped both ways");
    }

    @Test
    void highResolutionTexturesKeepTheirScale() {
        BufferedImage block = SignFix.relayout(coordinates(128, 64), false);
        assertEquals(64, block.getWidth());
        assertEquals(source(4, 4), block.getRGB(0, 4));
    }

    @Test
    void particleOnlySignBlockstatesGetVanillasBlockModels() throws IOException {
        SignFix signs = new SignFix();
        assertFalse(signs.offer("assets/fd/models/block/canvas_sign.json", """
                {"textures":{"particle":"minecraft:block/spruce_planks"}}""".getBytes(StandardCharsets.UTF_8)));
        assertFalse(signs.offer("assets/fd/textures/entity/signs/canvas_black.png", png(coordinates(64, 32))));
        assertFalse(signs.offer("assets/fd/textures/entity/signs/hanging/canvas_black.png", png(coordinates(64, 32))));
        assertTrue(signs.offer("assets/fd/blockstates/black_canvas_sign.json", PARTICLE_ONLY));
        assertTrue(signs.offer("assets/fd/blockstates/black_wall_hanging_canvas_sign.json", PARTICLE_ONLY));
        assertTrue(signs.offer("assets/fd/blockstates/black_hanging_canvas_sign.json", PARTICLE_ONLY));
        List<String> report = new ArrayList<>();
        Map<String, byte[]> out = signs.extras(report).stream().collect(Collectors.toMap(DataPackFix.Fixed::path, DataPackFix.Fixed::content));

        JsonObject standing = json(out.get("assets/fd/blockstates/black_canvas_sign.json")).getAsJsonObject("variants");
        assertEquals(16, standing.size());
        assertEquals("fd:block/black_canvas_sign_rot_2", standing.getAsJsonObject("rotation=10").get("model").getAsString());
        assertEquals(180, standing.getAsJsonObject("rotation=10").get("y").getAsInt());
        JsonObject rot0 = json(out.get("assets/fd/models/block/black_canvas_sign_rot_0.json"));
        assertEquals("minecraft:block/template_sign_rot_0", rot0.get("parent").getAsString());
        assertEquals("fd:block/signs/canvas_black", rot0.getAsJsonObject("textures").get("all").getAsString());
        assertEquals("minecraft:block/spruce_planks", rot0.getAsJsonObject("textures").get("particle").getAsString());
        assertEquals(32, ImageIO.read(new ByteArrayInputStream(out.get("assets/fd/textures/block/signs/canvas_black.png"))).getWidth());

        JsonObject wall = json(out.get("assets/fd/blockstates/black_wall_hanging_canvas_sign.json")).getAsJsonObject("variants");
        assertEquals(180, wall.getAsJsonObject("facing=north").get("y").getAsInt());
        assertEquals("minecraft:block/template_wall_hanging_sign",
                json(out.get("assets/fd/models/block/black_wall_hanging_canvas_sign_board.json")).get("parent").getAsString());
        assertEquals("fd:block/signs/hanging/canvas_black", json(out.get("assets/fd/models/block/black_wall_hanging_canvas_sign_board.json"))
                .getAsJsonObject("textures").get("all").getAsString());

        JsonObject ceiling = json(out.get("assets/fd/blockstates/black_hanging_canvas_sign.json")).getAsJsonObject("variants");
        assertEquals(32, ceiling.size());
        assertEquals("fd:block/black_hanging_canvas_sign_attached_rot_1", ceiling.getAsJsonObject("attached=true,rotation=5").get("model").getAsString());
        assertNotNull(out.get("assets/fd/models/block/black_hanging_canvas_sign_attached_rot_3.json"));
        assertEquals(3, report.size(), report.toString());
    }

    @Test
    void blockstatesWithoutAnEntityTextureStayAsTheyWere() {
        SignFix signs = new SignFix();
        signs.offer("assets/fd/models/block/canvas_sign.json", """
                {"textures":{"particle":"minecraft:block/spruce_planks"}}""".getBytes(StandardCharsets.UTF_8));
        assertTrue(signs.offer("assets/fd/blockstates/road_sign.json", PARTICLE_ONLY));
        List<DataPackFix.Fixed> out = signs.extras(new ArrayList<>());
        assertEquals(1, out.size());
        assertEquals("assets/fd/blockstates/road_sign.json", out.getFirst().path());
        assertArrayEquals(PARTICLE_ONLY, out.getFirst().content());
    }

    /** Each pixel's color is its position: {@code x << 8 | y}, opaque. */
    private static BufferedImage coordinates(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) image.setRGB(x, y, source(x, y));
        }
        return image;
    }

    private static int source(int x, int y) {
        return 0xFF000000 | x << 8 | y;
    }

    private static byte[] png(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    private static JsonObject json(byte[] content) {
        return JsonParser.parseString(new String(content, StandardCharsets.UTF_8)).getAsJsonObject();
    }
}
