package net.minecraftforge.common.util;

import java.util.List;
import java.util.function.Predicate;

/** Matches inputs to ingredients one-to-one (shapeless recipes with more than one matching order). */
public final class RecipeMatcher {
    /** For each input, the index of the test it was matched to; {@code null} if no full matching exists. */
    public static <T> int[] findMatches(List<T> inputs, List<? extends Predicate<T>> tests) {
        int n = inputs.size();
        if (n != tests.size()) return null;
        int[] testForInput = new int[n];
        int[] inputForTest = new int[n];
        java.util.Arrays.fill(testForInput, -1);
        java.util.Arrays.fill(inputForTest, -1);
        for (int i = 0; i < n; i++) {
            if (!augment(i, inputs, tests, testForInput, inputForTest, new boolean[n])) return null;
        }
        return testForInput;
    }

    /** Kuhn's augmenting path step: find a test for input {@code i}, moving earlier assignments if needed. */
    private static <T> boolean augment(int i, List<T> inputs, List<? extends Predicate<T>> tests,
                                       int[] testForInput, int[] inputForTest, boolean[] seen) {
        for (int t = 0; t < tests.size(); t++) {
            if (seen[t] || !tests.get(t).test(inputs.get(i))) continue;
            seen[t] = true;
            if (inputForTest[t] < 0 || augment(inputForTest[t], inputs, tests, testForInput, inputForTest, seen)) {
                inputForTest[t] = i;
                testForInput[i] = t;
                return true;
            }
        }
        return false;
    }

    private RecipeMatcher() {}
}
