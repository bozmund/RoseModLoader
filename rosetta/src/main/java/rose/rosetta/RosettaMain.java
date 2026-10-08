package rose.rosetta;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

/**
 * {@code rosetta build-names} writes the Forge 1.20.1 name layer to
 * {@code corpus/rosetta/names-forge-1.20.1.tsv} and prints how each entry was resolved.
 */
public final class RosettaMain {
    public static final Path FORGE_1201_LAYER = Path.of("corpus", "rosetta", "names-forge-1.20.1.tsv");

    public static void main(String[] args) throws Exception {
        NameLayer layer = NameLayerBuilder.buildForge1201(
                NameLayerBuilder.Inputs.forge1201(Path.of("corpus"), Path.of("rosetta", "rules")));
        layer.write(FORGE_1201_LAYER);
        System.out.println("[rosetta] wrote " + FORGE_1201_LAYER);
        System.out.println("  classes " + count(layer.classes().values().stream().map(NameLayer.ClassEntry::how).toList()));
        System.out.println("  methods " + count(layer.methods().values().stream().map(NameLayer.MemberEntry::how).toList()));
        System.out.println("  fields  " + count(layer.fields().values().stream().map(NameLayer.MemberEntry::how).toList()));
    }

    private static Map<NameLayer.How, Integer> count(java.util.List<NameLayer.How> hows) {
        Map<NameLayer.How, Integer> out = new EnumMap<>(NameLayer.How.class);
        hows.forEach(h -> out.merge(h, 1, Integer::sum));
        return out;
    }

    private RosettaMain() {}
}
