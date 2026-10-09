package net.minecraftforge.network;

import net.minecraftforge.fml.LogicalSide;

public enum NetworkDirection {
    PLAY_TO_SERVER(LogicalSide.SERVER),
    PLAY_TO_CLIENT(LogicalSide.CLIENT),
    LOGIN_TO_SERVER(LogicalSide.SERVER),
    LOGIN_TO_CLIENT(LogicalSide.CLIENT);

    private final LogicalSide receptionSide;

    NetworkDirection(LogicalSide receptionSide) {
        this.receptionSide = receptionSide;
    }

    public LogicalSide getReceptionSide() {
        return receptionSide;
    }

    public NetworkDirection reply() {
        return switch (this) {
            case PLAY_TO_SERVER -> PLAY_TO_CLIENT;
            case PLAY_TO_CLIENT -> PLAY_TO_SERVER;
            case LOGIN_TO_SERVER -> LOGIN_TO_CLIENT;
            case LOGIN_TO_CLIENT -> LOGIN_TO_SERVER;
        };
    }
}
