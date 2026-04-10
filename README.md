# ChronosDetective

Game trinh tham 2D duoc xay dung bang [libGDX](https://libgdx.com/) + LWJGL3.

## Cau truc module

- `core`: logic game (screen, entity, manager, save/load).
- `lwjgl3`: launcher desktop va cau hinh cua so.
- `assets`: map, sprite, font, save local runtime.

## Chay game (Windows)

```bat
run-game.bat
```

`run-game.bat` se:
- tu tim JDK neu chua co `JAVA_HOME`,
- tu kill instance game cu (neu dang chay),
- `gradlew --stop`, cho 1 giay, roi xoa `core/build` va `lwjgl3/build` neu con (giam loi Windows khoa file khi build),
- chay `gradlew.bat lwjgl3:run`.

Neu van bao loi khong xoa duoc thu muc `build`, hay tat game/IDE dang mo file trong do, hoac dong Cursor/IntelliJ roi chay lai.

## Phim dieu khien chinh

- `W A S D`: di chuyen.
- `E`: tuong tac item/NPC; trong `kitchen.tmx` dung gan cua se hien goi y mo/dong cua, cua dong thi bi chan khong cho di vao.
- `O`: mo form Save game.
- `ESC`: mo form hoi thoat game (ve menu neu chon "Co").

## Save/Load session

- **NEW GAME**: tao session moi.
- **CONTINUE**: vao session gan nhat (neu chua co se tao moi).
- **LOAD**: mo danh sach session de chon load.
- Trong game, bam **O** de luu vao session hien tai hoac session duoc chon.

Du lieu save duoc luu trong:
- `assets/saves/index.json` (danh sach session + session gan nhat)
- `assets/saves/<sessionId>.json` (du lieu tung session)

## Font tieng Viet

He thong UI va dialogue da dung font Unicode qua FreeType.

Dat 1 trong 2 file sau vao `assets/fonts/`:
- `NotoSans-Regular.ttf` (khuyen nghi)
- `Roboto-Regular.ttf`

Neu khong co file trong assets, desktop Windows se fallback sang font he thong.

## Build nhanh

```bat
gradlew.bat clean
gradlew.bat :core:compileJava :lwjgl3:compileJava
gradlew.bat lwjgl3:jar
```

Jar output: `lwjgl3/build/libs/`
