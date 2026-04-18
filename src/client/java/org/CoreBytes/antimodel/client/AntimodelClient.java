package org.CoreBytes.antimodel.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.CoreBytes.antimodel.client.gui.AntiModelScreen;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
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
                    })
                    .then(literal("hide")
                            .executes(context -> {
                                var client = MinecraftClient.getInstance();
                                if (client.player == null) {
                                    return 0;
                                }

                                var stack = client.player.getMainHandStack();
                                if (stack.isEmpty()) {
                                    client.player.sendMessage(Text.literal("Kein Item in der Mainhand."), true);
                                    return 0;
                                }

                                String key = AntiModelKeyUtil.keyForStackOrFallback(
                                        stack,
                                        "main:" + client.player.getInventory().getSelectedSlot()
                                );
                                String fallback = "main:" + client.player.getInventory().getSelectedSlot();
                                if (AntiModelClientState.get().hasOverride(key) || AntiModelClientState.get().hasOverride(fallback)) {
                                    AntiModelClientState.get().clear(key);
                                    AntiModelClientState.get().clear(fallback);
                                } else {
                                    AntiModelClientState.get().toggle(key);
                                    if (!fallback.equals(key)) {
                                        AntiModelClientState.get().toggle(fallback);
                                    }
                                }
                                client.player.sendMessage(Text.literal("AntiModel: hide/show fuer Mainhand umgeschaltet."), true);
                                return 1;
                            }))
                    .then(literal("clear")
                            .executes(context -> {
                                var client = MinecraftClient.getInstance();
                                if (client.player == null) {
                                    return 0;
                                }

                                clearForHand(client, Hand.MAIN_HAND);
                                clearForHand(client, Hand.OFF_HAND);
                                client.player.sendMessage(Text.literal("AntiModel: Overrides fuer Mainhand + Offhand geloescht."), true);
                                return 1;
                            }))
                    .then(literal("cmd")
                            .then(argument("value", integer(0))
                                    .executes(context -> {
                                        var client = MinecraftClient.getInstance();
                                        if (client.player == null) {
                                            return 0;
                                        }

                                        var stack = client.player.getMainHandStack();
                                        if (stack.isEmpty()) {
                                            client.player.sendMessage(Text.literal("Kein Item in der Mainhand."), true);
                                            return 0;
                                        }

                                        int value = getInteger(context, "value");
                                        String key = AntiModelKeyUtil.keyForStackOrFallback(
                                                stack,
                                                "main:" + client.player.getInventory().getSelectedSlot()
                                        );
                                        String fallback = "main:" + client.player.getInventory().getSelectedSlot();

                                        AntiModelClientState.get().setCustomModelData(key, value);
                                        if (!fallback.equals(key)) {
                                            AntiModelClientState.get().setCustomModelData(fallback, value);
                                        }
                                        client.player.sendMessage(Text.literal("AntiModel: zeige CMD=" + value + " (nur clientseitig)."), true);
                                        return 1;
                                    }))));
        });
    }

    private static void clearForHand(MinecraftClient client, Hand hand) {
        if (client.player == null) {
            return;
        }

        String fallback = hand == Hand.MAIN_HAND
                ? ("main:" + client.player.getInventory().getSelectedSlot())
                : ("main:" + PlayerInventory.OFF_HAND_SLOT);

        var stack = client.player.getStackInHand(hand);
        if (stack.isEmpty()) {
            return;
        }

        String key = AntiModelKeyUtil.keyForStackOrFallback(stack, fallback);
        AntiModelClientState.get().clear(key);
        AntiModelClientState.get().clear(fallback);
    }
}
