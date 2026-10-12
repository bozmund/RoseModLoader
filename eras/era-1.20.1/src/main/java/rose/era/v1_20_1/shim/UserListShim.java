package rose.era.v1_20_1.shim;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.StoredUserEntry;
import net.minecraft.server.players.StoredUserList;

/**
 * 1.20.1 lookups in the server's player lists (ops, whitelist, bans), which were keyed by GameProfile; 26.3 keys them
 * by NameAndId. The generic {@code get(K)} still links but casts the key, so a GameProfile threw ClassCastException
 * (Nature's Compass' op check failed every compass use on a dedicated server). IP bans keep their String keys.
 */
public final class UserListShim {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static StoredUserEntry get(StoredUserList self, Object user) {
        return (StoredUserEntry) self.get(key(user));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void remove(StoredUserList self, Object user) {
        self.remove(key(user));
    }

    /** 1.20.1 {@code contains(user)} (private in 26.3). */
    public static boolean contains(StoredUserList<?, ?> self, Object user) {
        return get(self, user) != null;
    }

    private static Object key(Object user) {
        return user instanceof GameProfile profile ? new NameAndId(profile) : user;
    }

    private UserListShim() {}
}
