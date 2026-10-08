#!/usr/bin/env bash
# Correct fix: 1.20.1 BlockStateBase.is(Block) = getBlock() == block; 26.3 has the generic TypedInstance.is(T).
set -euo pipefail
mkdir -p "$(dirname "$SHIM_FILE")"
cat > "$SHIM_FILE" <<'JAVA'
package rose.era.v1_20_1.shim;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Redirect targets for 1.20.1 {@code BlockState} methods that changed in 26.3. */
public final class BlockStateShim {
    /** 1.20.1 {@code BlockState.is(Block)}: {@code getBlock() == block}. */
    public static boolean is(BlockState self, Block block) {
        return self.getBlock() == block;
    }

    private BlockStateShim() {}
}
JAVA
printf '%s\n' "${RULE_LINE/<evidence>/1.20.1 BlockStateBase.is(Block) = getBlock() == block; 26.3 only has generic TypedInstance.is(T) (erased to Object), getBlock() unchanged}" >> rosetta/rules/forge-1.20.1/redirects.tsv
echo "wrote $SHIM_FILE and the rule"
