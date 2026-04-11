package com.armorposer.addon.gui;

import com.armorposer.addon.config.AddonConfig;
import com.armorposer.addon.network.ArmorStandAutomationService;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Основное окно аддона: слева библиотека предметов, справа редактор NBT в JSON формате.
 */
public final class AddonScreen extends Screen {
    private static final int PANEL_PADDING = 12;

    private final List<String> demoItems = new ArrayList<>();
    private TextFieldWidget searchField;
    private TextFieldWidget nbtEditor;

    public AddonScreen() {
        super(Text.literal("Armor Poser Addon"));
    }

    @Override
    protected void init() {
        demoItems.clear();
        demoItems.add("minecraft:diamond_sword");
        demoItems.add("minecraft:netherite_helmet");
        demoItems.add("minecraft:totem_of_undying");

        int leftPanelX = PANEL_PADDING;
        int leftPanelY = 30;
        int leftPanelWidth = this.width / 2 - PANEL_PADDING * 2;

        searchField = new TextFieldWidget(this.textRenderer, leftPanelX + 8, leftPanelY + 8, leftPanelWidth - 16, 18, Text.literal("Поиск"));
        searchField.setPlaceholder(Text.literal("Поиск и фильтр по категориям"));
        addDrawableChild(searchField);

        int rightPanelX = this.width / 2 + PANEL_PADDING;
        int rightPanelWidth = this.width / 2 - PANEL_PADDING * 2;
        nbtEditor = new TextFieldWidget(this.textRenderer, rightPanelX + 8, leftPanelY + 8, rightPanelWidth - 16, this.height - 120, Text.literal("NBT JSON"));
        nbtEditor.setMaxLength(AddonConfig.get().maxNbtJsonLength);
        nbtEditor.setDrawsBackground(true);
        nbtEditor.setText("{\"id\":\"minecraft:stone\",\"Count\":1b}");
        addDrawableChild(nbtEditor);

        addDrawableChild(ButtonWidget.builder(Text.literal("Использовать"), button -> {
            String itemId = filteredItems().stream().findFirst().orElse("minecraft:stone");
            if (ArmorStandAutomationService.useSelectedItem(this.client, itemId, nbtEditor.getText())) {
                HudNotifier.showExecuted();
            }
        }).dimensions(rightPanelX, this.height - 70, rightPanelWidth, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int leftPanelX = PANEL_PADDING;
        int panelY = 30;
        int leftPanelWidth = this.width / 2 - PANEL_PADDING * 2;
        int rightPanelX = this.width / 2 + PANEL_PADDING;
        int rightPanelWidth = this.width / 2 - PANEL_PADDING * 2;

        context.fill(leftPanelX, panelY, leftPanelX + leftPanelWidth, this.height - 20, 0x66222222);
        context.fill(rightPanelX, panelY, rightPanelX + rightPanelWidth, this.height - 20, 0x66181818);

        context.drawTextWithShadow(this.textRenderer, "Библиотека предметов", leftPanelX + 8, panelY - 12, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "NBT-редактор (JSON)", rightPanelX + 8, panelY - 12, 0xFFFFFF);

        int y = panelY + 34;
        for (String item : filteredItems()) {
            context.drawTextWithShadow(this.textRenderer, "• " + item, leftPanelX + 10, y, 0xDDDDDD);
            y += 12;
            if (y > this.height - 35) {
                break;
            }
        }
    }

    private List<String> filteredItems() {
        String search = searchField == null ? "" : searchField.getText().toLowerCase();
        return demoItems.stream().filter(item -> item.toLowerCase().contains(search)).toList();
    }
}
