#!/usr/bin/env bash
# Deliberately broken: the shim exists and compiles, but its signature doesn't match the task (wrong parameter
# type), so the redirect can't resolve. The analyze gate must report RULE_BROKEN and refuse to land it.
set -euo pipefail
mkdir -p "$(dirname "$SHIM_FILE")"
class=$(basename "$SHIM_FILE" .java)
method=$(printf '%s' "$RULE_LINE" | cut -f2 | sed 's/.*\.//')
cat > "$SHIM_FILE" <<JAVA
package rose.era.v1_20_1.shim;

/** Deliberately wrong (foundry gate test). */
public final class $class {
    public static void $method(String wrong) {
    }

    private $class() {}
}
JAVA
printf '%s\n' "${RULE_LINE/<evidence>/deliberately wrong signature (gate test)}" >> rosetta/rules/forge-1.20.1/redirects.tsv
echo "wrote a shim with the wrong signature"
