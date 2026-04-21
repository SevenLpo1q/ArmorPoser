# E4 Menu Replacer (Fabric 1.21.11)

Клиентский Fabric-мод для **Minecraft 1.21.11**, который:

- заменяет надпись `Minecraft` в главном меню,
- заменяет логотип загрузки вместо Mojang Studios,
- заменяет 3D-панораму меню на обычную плоскую картинку.

## Важно про push и бинарные файлы

Чтобы не было ошибки при отправке в репозиторий ("binary files"), PNG-картинки **не коммитятся**.
Они добавлены в `.gitignore` и должны лежать только локально.

Положи свои 3 картинки по путям:

- `src/main/resources/assets/minecraft/textures/gui/title/minecraft.png`
- `src/main/resources/assets/minecraft/textures/gui/title/mojangstudios.png`
- `src/main/resources/assets/e4menu/textures/gui/main_menu_background.png`

(Подсказка с теми же путями есть в `src/main/resources/assets/README_IMAGES.md`.)

## Сборка

```bash
./gradlew build
```
