package rose.bridge.client;

import rose.api.client.ClientModInitializer;
import rose.bridge.BridgeMod;

public final class BridgeClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (BridgeMod.bridge() == null) return;
        ClientMethods.register(BridgeMod.bridge());
        BridgeMod.startBridge();
    }
}
