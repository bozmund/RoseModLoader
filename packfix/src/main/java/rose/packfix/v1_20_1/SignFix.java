package rose.packfix.v1_20_1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 * Mod signs. 1.20.1 drew a sign's board with its block entity renderer from an entity texture
 * ({@code textures/entity/signs/<name>.png}, 64x32), and the blockstate only named a particle model; 26.x draws the
 * board as a block model (vanilla's {@code template_sign_rot_N} and friends) over a 32x32 block texture, and the
 * renderer only adds the text. For a sign blockstate whose single model is particle-only, this generates what 26.3
 * vanilla signs have: the re-laid-out block texture, the models and the blockstate. The texture is the entity sign
 * texture named by the blockstate's words ({@code black_canvas_sign} and {@code hanging_canvas_sign} use
 * {@code canvas_black} and {@code hanging/canvas}).
 */
final class SignFix {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Pattern BLOCKSTATE = Pattern.compile("assets/([^/]+)/blockstates/([a-z0-9_]+_sign)\\.json");
    private static final Pattern BLOCK_MODEL = Pattern.compile("assets/([^/]+)/models/block/(.+)\\.json");
    private static final Pattern TEXTURE = Pattern.compile("assets/([^/]+)/textures/entity/signs/(hanging/)?([a-z0-9_]+)\\.png");
    private static final Set<String> KIND_WORDS = Set.of("sign", "wall", "hanging");

    /**
     * Where each face of the 1.20.1 entity texture went in the 26.x block texture: {dst x0, y0, x1, y1, src x, y,
     * flip x, flip y} in pixels of a 64x32 / 32x32 pair. Read off vanilla's own conversion: applied to every 1.20.1
     * {@code entity/signs/*.png} and {@code entity/signs/hanging/*.png}, these give 26.3's {@code block/*_sign.png}
     * and {@code block/*_hanging_sign.png} pixel for pixel.
     */
    static final int[][] STANDING = {
            {0, 0, 24, 2, 2, 0, 0, 0}, {0, 2, 24, 14, 2, 2, 0, 0}, {24, 2, 26, 14, 26, 2, 0, 0}, {0, 16, 24, 28, 28, 2, 0, 0},
            {24, 16, 26, 28, 0, 2, 0, 0}, {0, 28, 24, 30, 26, 0, 0, 1}, {28, 16, 30, 30, 6, 16, 0, 0}, {30, 0, 32, 14, 4, 16, 0, 0},
            {28, 0, 30, 14, 2, 16, 0, 0}, {30, 16, 32, 30, 0, 16, 0, 0}, {28, 30, 30, 32, 4, 14, 0, 1}};
    static final int[][] HANGING = {
            {0, 0, 16, 4, 4, 0, 0, 0}, {0, 4, 16, 6, 4, 4, 0, 0}, {0, 7, 16, 9, 24, 4, 0, 0}, {0, 9, 16, 13, 20, 0, 1, 1},
            {0, 16, 2, 26, 0, 14, 0, 0}, {2, 14, 16, 16, 2, 12, 0, 0}, {2, 16, 16, 26, 2, 14, 0, 0}, {2, 26, 16, 28, 16, 12, 0, 1},
            {16, 4, 20, 6, 20, 4, 0, 0}, {16, 7, 20, 9, 0, 4, 0, 0}, {16, 16, 18, 26, 16, 14, 0, 0}, {18, 16, 32, 26, 18, 14, 0, 0},
            {20, 0, 32, 6, 14, 6, 0, 0}, {22, 7, 25, 13, 0, 6, 0, 0}, {28, 8, 31, 12, 6, 7, 0, 0}};

    private record Held(String namespace, String name, String model, byte[] original) {}

    private final List<Held> blockstates = new ArrayList<>();
    /** Particle-only block models ({@code ns:block/path}) and their particle texture. */
    private final Map<String, String> particleModels = new HashMap<>();
    /** Entity sign textures by {@code ns:name} and {@code ns:hanging/name}. */
    private final Map<String, byte[]> textures = new HashMap<>();

    /** Looks at a mod file; {@code true} when it is a sign blockstate held back until {@link #extras}. */
    boolean offer(String path, byte[] content) {
        Matcher m = TEXTURE.matcher(path);
        if (m.matches()) {
            textures.put(m.group(1) + ":" + (m.group(2) == null ? "" : "hanging/") + m.group(3), content);
            return false;
        }
        m = BLOCK_MODEL.matcher(path);
        if (m.matches()) {
            JsonObject model = parse(content);
            if (model != null && model.size() == 1 && model.get("textures") instanceof JsonObject t && t.size() == 1
                    && t.get("particle") != null && t.get("particle").isJsonPrimitive()) {
                particleModels.put(m.group(1) + ":block/" + m.group(2), t.get("particle").getAsString());
            }
            return false;
        }
        m = BLOCKSTATE.matcher(path);
        if (!m.matches()) return false;
        JsonObject state = parse(content);
        if (state == null || !(state.get("variants") instanceof JsonObject variants) || variants.size() != 1
                || !(variants.get("") instanceof JsonObject only) || only.get("model") == null) {
            return false;
        }
        String model = only.get("model").getAsString();
        blockstates.add(new Held(m.group(1), m.group(2), model.contains(":") ? model : "minecraft:" + model, content));
        return true;
    }

    /** The generated files, and the held blockstates that are not signs after all, unchanged. */
    List<DataPackFix.Fixed> extras(List<String> report) {
        Map<String, byte[]> out = new LinkedHashMap<>();
        for (Held held : blockstates) {
            String prefix = "assets/" + held.namespace() + "/";
            String particle = particleModels.get(held.model());
            List<String> words = new ArrayList<>(Arrays.asList(held.name().split("_")));
            boolean hanging = words.contains("hanging");
            boolean wall = words.contains("wall");
            words.removeIf(KIND_WORDS::contains);
            String texture = particle == null ? null : textureFor(held.namespace(), hanging, words);
            byte[] converted = null;
            if (texture != null) {
                try {
                    converted = png(relayout(image(textures.get(held.namespace() + ":" + texture)), hanging));
                } catch (RuntimeException e) {
                    report.add("sign " + held.namespace() + ":" + held.name() + ": texture " + texture + " not converted (" + e + ")");
                }
            }
            if (converted == null) {
                out.put(prefix + "blockstates/" + held.name() + ".json", held.original());
                continue;
            }
            String blockTexture = held.namespace() + ":block/signs/" + texture;
            out.put(prefix + "textures/block/signs/" + texture + ".png", converted);
            JsonObject variants = new JsonObject();
            if (wall) {
                String model = held.name() + "_board";
                out.put(prefix + "models/block/" + model + ".json",
                        model(hanging ? "template_wall_hanging_sign" : "template_wall_sign", blockTexture, particle));
                String[] facings = {"south", "west", "north", "east"};
                for (int i = 0; i < facings.length; i++) variants.add("facing=" + facings[i], variant(held.namespace() + ":block/" + model, i));
            } else {
                for (int r = 0; r < 4; r++) {
                    out.put(prefix + "models/block/" + held.name() + "_rot_" + r + ".json",
                            model((hanging ? "template_hanging_sign_rot_" : "template_sign_rot_") + r, blockTexture, particle));
                    if (hanging) {
                        out.put(prefix + "models/block/" + held.name() + "_attached_rot_" + r + ".json",
                                model("template_attached_hanging_sign_rot_" + r, blockTexture, particle));
                    }
                }
                for (int rotation = 0; rotation < 16; rotation++) {
                    String rot = "_rot_" + (rotation % 4);
                    if (hanging) {
                        variants.add("attached=false,rotation=" + rotation, variant(held.namespace() + ":block/" + held.name() + rot, rotation / 4));
                        variants.add("attached=true,rotation=" + rotation,
                                variant(held.namespace() + ":block/" + held.name() + "_attached" + rot, rotation / 4));
                    } else {
                        variants.add("rotation=" + rotation, variant(held.namespace() + ":block/" + held.name() + rot, rotation / 4));
                    }
                }
            }
            JsonObject state = new JsonObject();
            state.add("variants", variants);
            out.put(prefix + "blockstates/" + held.name() + ".json", GSON.toJson(state).getBytes(StandardCharsets.UTF_8));
            report.add("sign " + held.namespace() + ":" + held.name() + ": block models over entity texture " + texture);
        }
        List<DataPackFix.Fixed> fixed = new ArrayList<>();
        out.forEach((path, content) -> fixed.add(new DataPackFix.Fixed(path, content)));
        return fixed;
    }

    /** The entity sign texture whose words are {@code words} (any order), as {@code [hanging/]name}, or {@code null}. */
    private String textureFor(String namespace, boolean hanging, List<String> words) {
        Set<String> wanted = new HashSet<>(words);
        String folder = namespace + ":" + (hanging ? "hanging/" : "");
        for (String key : textures.keySet()) {
            if (!key.startsWith(folder)) continue;
            String name = key.substring(folder.length());
            if (name.contains("/")) continue;
            List<String> nameWords = Arrays.asList(name.split("_"));
            if (nameWords.size() == words.size() && new HashSet<>(nameWords).equals(wanted)) return (hanging ? "hanging/" : "") + name;
        }
        return null;
    }

    /** A 1.20.1 entity sign texture (64x32, or a multiple) as the 26.x block texture (32x32, or the same multiple). */
    static BufferedImage relayout(BufferedImage entity, boolean hanging) {
        int scale = Math.max(1, entity.getWidth() / 64);
        BufferedImage out = new BufferedImage(32 * scale, 32 * scale, BufferedImage.TYPE_INT_ARGB);
        for (int[] op : hanging ? HANGING : STANDING) {
            int w = (op[2] - op[0]) * scale;
            int h = (op[3] - op[1]) * scale;
            for (int j = 0; j < h; j++) {
                for (int i = 0; i < w; i++) {
                    int sx = op[4] * scale + (op[6] == 1 ? w - 1 - i : i);
                    int sy = op[5] * scale + (op[7] == 1 ? h - 1 - j : j);
                    out.setRGB(op[0] * scale + i, op[1] * scale + j, entity.getRGB(sx, sy));
                }
            }
        }
        return out;
    }

    private static byte[] model(String template, String texture, String particle) {
        JsonObject textures = new JsonObject();
        textures.addProperty("all", texture);
        textures.addProperty("particle", particle);
        JsonObject model = new JsonObject();
        model.addProperty("parent", "minecraft:block/" + template);
        model.add("textures", textures);
        return GSON.toJson(model).getBytes(StandardCharsets.UTF_8);
    }

    private static JsonObject variant(String model, int quarterTurns) {
        JsonObject variant = new JsonObject();
        variant.addProperty("model", model);
        if (quarterTurns > 0) variant.addProperty("y", 90 * quarterTurns);
        return variant;
    }

    private static BufferedImage image(byte[] png) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
            if (image == null) throw new IOException("not an image");
            return image;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static byte[] png(BufferedImage image) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static JsonObject parse(byte[] content) {
        try {
            JsonElement e = JsonParser.parseString(new String(content, StandardCharsets.UTF_8));
            return e.isJsonObject() ? e.getAsJsonObject() : null;
        } catch (RuntimeException e) {
            return null;
        }
    }
}
