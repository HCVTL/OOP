# Agents — ChronosDetective

File này theo ý tưởng **[AGENTS.md](https://agentsmd.io/)**: một file Markdown ở root (hoặc từng package con trong monorepo) để **AI coding agent** (Cursor, Copilot, Codex, v.v.) đọc nhanh cách build, kiểm tra và quy ước dự án. Không thay thế README đầy đủ cho người; chi tiết luồng code nằm trong `docs/CODE_FLOW.md`.

## Dự án

- **Stack**: Java, [libGDX](https://libgdx.com/), desktop backend **LWJGL3**, UI **VisUI** (`com.kotcrab.vis:vis-ui`).
- **Package chính**: `com.ChronosDetective.game` (`core`), launcher `com.ChronosDetective.game.lwjgl3` (`lwjgl3`).

## Build & chạy

Trên Windows (từ thư mục repo):

```bat
gradlew.bat lwjgl3:run
```

Build JAR:

```bat
gradlew.bat lwjgl3:jar
```

Artifact: `lwjgl3/build/libs/`. Các task khác: `gradlew.bat build`, `gradlew.bat test`, `gradlew.bat clean`.

## Cấu trúc code (core)

| Khu vực | Mô tả ngắn |
|---------|------------|
| `ChronosDetectiveGame` | `Game` subclass: VisUI, `SpriteBatch`, màn đầu `MenuScreen`. |
| `Screens/` | `MenuScreen`, `GameScreen` (tilemap, player, entity, dialogue). |
| `Entities/` | `Entity`, `Player`, `Item`, `NPC`. |
| `Managers/` | `EntityManager` (tương tác E), `DialogueManager` (hộp thoại). |

Assets: `assets/` (ví dụ `map.tmx`, `player2.png`, `apple.png`, `arrow.png`).

## Quy ước khi sửa code

- Giữ style hiện có: package, tên class, comment tiếng Việt nơi đã dùng.
- Luôn cập nhật lại `docs/` và `AGENTS.md` khi có thay đổi (luồng code, kiến trúc, cách build/chạy, hoặc hành vi gameplay).
- **Bắt buộc cập nhật `README.md` trước khi push code** nếu có thay đổi tính năng, điều khiển, cách chạy/build, hoặc cấu trúc thư mục/module.
- Màn hình libGDX: `show` khởi tạo, `render` cập nhật + vẽ, `dispose` giải phóng texture/batch/map/font/shape.
- Va chạm player: layer Tiled `"Fences"`, tile 16px (xem `Player.isCollision`).
- Tương tác: phím **E** qua `EntityManager`; khi dialogue bật, `GameScreen` không gọi `player.update`.
- Riêng `kitchen.tmx`: phím **E** gần cửa sẽ hiển thị gợi ý mở cửa, kích hoạt animation mở cửa (xử lý tại `MapManager`), và cửa đóng thì chưa qua portal.

## Việc nên kiểm tra sau thay đổi

- `gradlew.bat lwjgl3:run` — vào menu, NEW GAME, di chuyển và thử E gần item.
- Save/Load: vào game bấm **O** lưu, thoát ra menu → **LOAD** chọn session, hoặc **CONTINUE** để vào lại session gần nhất.
- Nếu thêm texture/map: đảm bảo đường dẫn trong `assets/` và `dispose()` tương ứng.

## Tài liệu nội bộ

- Luồng chi tiết: `docs/CODE_FLOW.md`
- Lịch sử thay đổi: `docs/changelog.md`

## Giới hạn / gotcha đã biết

- `run-game.bat` đã tự kill tiến trình game cũ (nếu có) trước khi chạy `lwjgl3:run`, nhưng không kill mọi process Java khác để tránh ảnh hưởng tool ngoài dự án.

---

*Cập nhật file này khi thêm module, đổi lệnh build, hoặc thay đổi kiến trúc lớn.*
