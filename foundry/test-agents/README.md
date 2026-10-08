# Test agents

Scripted stand-ins for a model, used to prove the foundry's gates work (`--agent script:<file>`). They get
`TASK_ID`, `TASK_FILE`, `SHIM_FILE`, `SHIM_SIGNATURE` and `RULE_LINE` in the environment and run in the task's worktree.

- `good-blockstate-is.sh`: a correct fix for `BlockState.is(Block)`; the gates must **land** it.
- `bad-signature.sh`: a shim with the wrong signature; the analyze gate must **reject** it (RULE_BROKEN).
- `bad-scope.sh`: a correct-looking shim that also edits a test; the scope gate must **reject** it.
