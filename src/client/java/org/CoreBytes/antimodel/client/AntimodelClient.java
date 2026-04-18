package org.CoreBytes.antimodel.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.CoreBytes.antimodel.client.gui.AntiModelScreen;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class AntimodelClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AntiModelClientState.get().init();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("antimodel")
                    .executes(context -> {
                        var client = MinecraftClient.getInstance();
                        if (client.player == null) {
                            return 0;
                        }

                        client.execute(() -> client.setScreen(new AntiModelScreen(Text.literal("AntiModel"))));
                        return 1;
                    }));
        });
    }
}
