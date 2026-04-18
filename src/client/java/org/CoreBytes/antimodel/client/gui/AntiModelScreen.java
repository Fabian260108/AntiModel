package org.CoreBytes.antimodel.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.CoreBytes.antimodel.client.AntiModelClientState;
import org.CoreBytes.antimodel.client.AntiModelKeyUtil;

import java.util.ArrayList;
import java.util.List;

public final class AntiModelScreen extends Screen {
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_INNER = 16;
    private static final int PADDING = 10;

    private final List<SlotRef> slots = new ArrayList<>();

    public AntiModelScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
        rebuildSlots();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        rebuildSlots();
    }

    private void rebuildSlots() {
        slots.clear();

        var client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        PlayerInventory inv = client.player.getInventory();

        int invColumns = 9;
        int mainRows = 3;
        int hotbarRows = 1;

        int invWidth = invColumns * SLOT_SIZE;
        int invHeight = (mainRows + hotbarRows) * SLOT_SIZE;

        int armorColumns = 1;
        int armorRows = 5; // + offhand
        int armorWidth = armorColumns * SLOT_SIZE;
        int armorHeight = armorRows * SLOT_SIZE;

        int totalWidth = armorWidth + PADDING + invWidth;
        int totalHeight = Math.max(armorHeight, invHeight);

        int left = (this.width - totalWidth) / 2;
        int top = (this.height - totalHeight) / 2;

        // Armor (top -> bottom): helmet, chest, legs, boots
        EquipmentSlot[] armorSlots = new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int row = 0; row < armorSlots.length; row++) {
            int x = left;
            int y = top + row * SLOT_SIZE;
            slots.add(SlotRef.equipment(armorSlots[row], x, y));
        }

        // Offhand (toggle affects in-hand offhand rendering too)
        slots.add(SlotRef.main(PlayerInventory.OFF_HAND_SLOT, left, top + 4 * SLOT_SIZE));

        int invLeft = left + armorWidth + PADDING;
        int invTop = top;

        // Main inventory (3 rows): indices 9..35 in PlayerInventory main stacks
        for (int row = 0; row < mainRows; row++) {
            for (int col = 0; col < invColumns; col++) {
                int x = invLeft + col * SLOT_SIZE;
                int y = invTop + row * SLOT_SIZE;
                int mainIndex = 9 + row * invColumns + col;
                slots.add(SlotRef.main(mainIndex, x, y));
            }
        }

        // Hotbar: indices 0..8
        int hotbarTop = invTop + mainRows * SLOT_SIZE;
        for (int col = 0; col < invColumns; col++) {
            int x = invLeft + col * SLOT_SIZE;
            int y = hotbarTop;
            int hotbarIndex = col;
            slots.add(SlotRef.main(hotbarIndex, x, y));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Avoid calling Screen#renderBackground here: the game render pipeline already handles blur/background
        // and calling it again can trigger "Can only blur once per frame".
        context.fill(0, 0, this.width, this.height, 0x88000000);

        var client = MinecraftClient.getInstance();
        if (client.player == null) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("No player"), width / 2, height / 2, 0xFFFFFF);
            return;
        }

        context.drawCenteredTextWithShadow(textRenderer, this.title, this.width / 2, Math.max(8, (this.height / 2) - 90), 0xFFFFFF);

        SlotRef hovered = null;
        for (SlotRef slot : slots) {
            slot.draw(context, client.player);
            if (slot.rect.contains(mouseX, mouseY)) {
                hovered = slot;
            }
        }

        if (hovered != null) {
            ItemStack stack = hovered.getStack(client.player);
            if (!stack.isEmpty()) {
                List<Text> tooltip = new ArrayList<>(Screen.getTooltipFromItem(client, stack));
                String key = hovered.keyForStack(client.player);
                String state = AntiModelClientState.get().describe(key);
                tooltip.add(Text.literal("AntiModel: " + state).formatted(Formatting.GRAY));
                tooltip.add(Text.literal("LMB: hide/show").formatted(Formatting.DARK_GRAY));
                tooltip.add(Text.literal("/antimodel cmd <zahl> fuer Hand-Item").formatted(Formatting.DARK_GRAY));
                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean captured) {
        if (click.button() != 0) {
            return super.mouseClicked(click, captured);
        }

        for (SlotRef slot : slots) {
            if (slot.rect.contains((int) click.x(), (int) click.y())) {
                var client = MinecraftClient.getInstance();
                if (client.player == null) {
                    return true;
                }

                ItemStack stack = slot.getStack(client.player);
                if (stack.isEmpty()) {
                    return true;
                }

                AntiModelClientState.get().toggle(slot.keyForStack(client.player));
                return true;
            }
        }

        return super.mouseClicked(click, captured);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static final class SlotRef {
        private final Rect2i rect;
        private final Kind kind;
        private final int index;
        private final EquipmentSlot equipmentSlot;

        private SlotRef(Kind kind, int index, EquipmentSlot equipmentSlot, int x, int y) {
            this.kind = kind;
            this.index = index;
            this.equipmentSlot = equipmentSlot;
            this.rect = new Rect2i(x, y, SLOT_SIZE, SLOT_SIZE);
        }

        static SlotRef main(int mainIndex, int x, int y) {
            return new SlotRef(Kind.MAIN, mainIndex, null, x, y);
        }

        static SlotRef equipment(EquipmentSlot slot, int x, int y) {
            return new SlotRef(Kind.EQUIPMENT, -1, slot, x, y);
        }

        String fallbackKey() {
            return switch (kind) {
                case MAIN -> "main:" + index;
                case EQUIPMENT -> "equip:" + equipmentSlot.getName();
            };
        }

        String keyForStack(net.minecraft.entity.player.PlayerEntity player) {
            ItemStack stack = getStack(player);
            return AntiModelKeyUtil.keyForStackOrFallback(stack, fallbackKey());
        }

        ItemStack getStack(net.minecraft.entity.player.PlayerEntity player) {
            return switch (kind) {
                case MAIN -> player.getInventory().getStack(index);
                case EQUIPMENT -> player.getEquippedStack(equipmentSlot);
            };
        }

        void draw(DrawContext context, net.minecraft.entity.player.PlayerEntity player) {
            int x = rect.getX();
            int y = rect.getY();

            // Slot background
            context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xAA000000);
            context.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, 0x66000000);

            ItemStack stack = getStack(player);
            if (stack.isEmpty()) {
                return;
            }

            String key = keyForStack(player);
            boolean overridden = AntiModelClientState.get().hasOverride(key);
            ItemStack toRender = AntiModelClientState.get().apply(stack, key);

            int ix = x + 1;
            int iy = y + 1;

            context.drawItem(toRender, ix, iy);
            context.drawStackOverlay(MinecraftClient.getInstance().textRenderer, toRender, ix, iy);

            if (overridden) {
                // Blue overlay to indicate any active display override.
                context.fill(ix, iy, ix + SLOT_INNER, iy + SLOT_INNER, 0x3344AAFF);
            }
        }
    }

    private enum Kind {
        MAIN,
        EQUIPMENT
    }
}
