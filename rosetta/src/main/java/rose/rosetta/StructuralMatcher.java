package rose.rosetta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Pairs classes that disappeared between two versions with classes that appeared, by the members they declare.
 * 26.x keeps Mojang's member names, so a class renamed in 26.1 (e.g. {@code FarmBlock} to {@code FarmlandBlock})
 * still declares mostly the same method and field names.
 *
 * <p>Score: shared member names over all member names of both classes, each name weighted by how rare it is
 * among the new classes (a shared {@code isNearWater} says more than a shared {@code tick}). A pair is accepted
 * only if it is each side's best match, scores at least {@link #MIN_SCORE}, and clearly beats the runner-up.
 */
public final class StructuralMatcher {
    static final double MIN_SCORE = 0.5;
    static final double MIN_MARGIN = 0.15;
    static final int MIN_MEMBERS = 3;

    /**
     * @param gone     classes missing in the new version, to their member names
     * @param appeared classes new in the new version, to their member names
     * @param all      every class of the new version to its member names (for name rarity)
     * @return gone class to its new name, for confident matches only
     */
    public static Map<String, String> match(Map<String, Set<String>> gone, Map<String, Set<String>> appeared,
                                            Map<String, Set<String>> all) {
        Map<String, Integer> frequency = new HashMap<>();
        for (Set<String> members : all.values()) for (String m : members) frequency.merge(m, 1, Integer::sum);
        int classes = Math.max(1, all.size());

        Map<String, Set<String>> byMember = new HashMap<>();
        for (var e : appeared.entrySet()) for (String m : e.getValue()) byMember.computeIfAbsent(m, k -> new HashSet<>()).add(e.getKey());

        Map<String, Best> bestForGone = new HashMap<>();
        Map<String, Best> bestForAppeared = new HashMap<>();
        for (var g : gone.entrySet()) {
            if (g.getValue().size() < MIN_MEMBERS) continue;
            Set<String> candidates = new HashSet<>();
            for (String m : g.getValue()) candidates.addAll(byMember.getOrDefault(m, Set.of()));
            Best best = new Best();
            for (String c : candidates) {
                Set<String> members = appeared.get(c);
                if (members.size() < MIN_MEMBERS) continue;
                double score = score(g.getValue(), members, frequency, classes);
                best.offer(c, score);
                bestForAppeared.computeIfAbsent(c, k -> new Best()).offer(g.getKey(), score);
            }
            bestForGone.put(g.getKey(), best);
        }

        Map<String, String> out = new HashMap<>();
        for (var e : bestForGone.entrySet()) {
            Best best = e.getValue();
            if (best.name == null || best.score < MIN_SCORE || best.score - best.second < MIN_MARGIN) continue;
            Best reverse = bestForAppeared.get(best.name);
            if (reverse == null || !e.getKey().equals(reverse.name) || reverse.score - reverse.second < MIN_MARGIN) continue;
            out.put(e.getKey(), best.name);
        }
        return out;
    }

    static double score(Set<String> a, Set<String> b, Map<String, Integer> frequency, int classes) {
        double shared = 0, union = 0;
        for (String m : a) {
            double w = weight(m, frequency, classes);
            union += w;
            if (b.contains(m)) shared += w;
        }
        for (String m : b) if (!a.contains(m)) union += weight(m, frequency, classes);
        return union == 0 ? 0 : shared / union;
    }

    private static double weight(String member, Map<String, Integer> frequency, int classes) {
        return Math.log(1.0 + (double) classes / frequency.getOrDefault(member, 1));
    }

    /** Names that say nothing about which class this is. */
    static boolean isDistinctive(String name) {
        return !name.startsWith("<") && !name.startsWith("lambda$") && !name.startsWith("access$") && !name.startsWith("$")
                && !Set.of("values", "valueOf", "toString", "equals", "hashCode", "CODEC", "STREAM_CODEC", "$VALUES").contains(name);
    }

    private static final class Best {
        String name;
        double score = -1;
        double second = 0;

        void offer(String candidate, double s) {
            if (s > score) {
                second = Math.max(second, score);
                score = s;
                name = candidate;
            } else if (s > second) {
                second = s;
            }
        }
    }

    private StructuralMatcher() {}
}
