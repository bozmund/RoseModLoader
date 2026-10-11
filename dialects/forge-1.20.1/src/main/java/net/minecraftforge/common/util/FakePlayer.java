package net.minecraftforge.common.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** A server player with no client behind it, used by machines that act as a player (Forge 1.20.1). */
public class FakePlayer extends ServerPlayer {
    public FakePlayer(ServerLevel level, GameProfile name) {
        super(level.getServer(), level, name, ClientInformation.createDefault());
    }

    @Override
    public boolean isClientAuthoritative() {
        return false;
    }
}
