package moonulio.skinpresets.client.gui;

import moonulio.skinpresets.client.mojang.MojangSkinService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.nio.file.Path;

public final class SkinPresetsScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget input;
    private Text status = Text.literal("Готово. Выбери способ: ник / URL / файл.");

    public SkinPresetsScreen(Screen parent) {
        super(Text.literal("Skin Presets"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 2 - 60;

        this.input = new TextFieldWidget(this.textRenderer, centerX - 150, y, 300, 20, Text.literal("input"));
        this.input.setMaxLength(512);
        this.input.setPlaceholder(Text.literal("Ник, https://...png или C:/skins/skin.png"));
        this.addDrawableChild(this.input);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Поставить по нику"), b -> updateByNickname())
                .dimensions(centerX - 150, y + 28, 146, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Поставить по URL"), b -> updateByUrl())
                .dimensions(centerX + 4, y + 28, 146, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Поставить по файлу"), b -> updateByFile())
                .dimensions(centerX - 150, y + 54, 300, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), b -> close())
                .dimensions(centerX - 150, y + 106, 300, 20)
                .build());
    }

    private void updateByNickname() {
        String nickname = this.input.getText().trim();
        if (nickname.isEmpty()) {
            this.status = Text.literal("Введи ник игрока.");
            return;
        }

        runAsync(() -> MojangSkinService.applySkinFromNickname(MinecraftClient.getInstance(), nickname));
    }

    private void updateByUrl() {
        String url = this.input.getText().trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            this.status = Text.literal("Нужна ссылка http/https на PNG.");
            return;
        }

        runAsync(() -> MojangSkinService.applySkinFromUrl(MinecraftClient.getInstance(), url));
    }

    private void updateByFile() {
        String rawPath = this.input.getText().trim();
        if (rawPath.isEmpty()) {
            this.status = Text.literal("Введи путь к PNG файлу.");
            return;
        }

        Path path = Path.of(rawPath);
        runAsync(() -> MojangSkinService.applySkinFromFile(MinecraftClient.getInstance(), path));
    }

    private void runAsync(SkinOperation operation) {
        this.status = Text.literal("Отправляю скин в Mojang...");

        Thread.startVirtualThread(() -> {
            try {
                String result = operation.execute();
                this.client.execute(() -> this.status = Text.literal("Успех: " + result));
            } catch (Exception e) {
                this.client.execute(() -> this.status = Text.literal("Ошибка: " + e.getMessage()));
            }
        });
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, this.height / 2 - 92, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, this.status, centerX, this.height / 2 + 84, 0xA0FFA0);
    }

    @FunctionalInterface
    private interface SkinOperation {
        String execute() throws Exception;
    }
}
