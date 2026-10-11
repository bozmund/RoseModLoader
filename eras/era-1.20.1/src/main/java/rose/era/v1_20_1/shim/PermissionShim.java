package rose.era.v1_20_1.shim;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

/** 1.20.1 numeric permission levels; 26.x checks a {@link Permission} against the source's permission set. */
public final class PermissionShim {
    /** 1.20.1 {@code CommandSourceStack.hasPermission(level)} (2 = gamemaster, as commands checked it). */
    public static boolean hasPermission(CommandSourceStack source, int level) {
        return source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(level)));
    }

    private PermissionShim() {}
}
