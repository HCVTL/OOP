# Changelog

Mọi thay đổi đáng chú ý của dự án được ghi ở đây. Định dạng gợi ý theo [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added

- Thư mục `docs/` với `CODE_FLOW.md` mô tả luồng code và kiến trúc module.
- `changelog.md` để theo dõi thay đổi theo thời gian.
- `AGENTS.md` ở root repo: hướng dẫn cho AI coding agent (build, cấu trúc, quy ước), kèm rule luôn cập nhật `docs/` và `AGENTS.md` khi có thay đổi.
- Save system cơ bản: session + thời gian lưu, lưu/đọc từ local `saves/` (JSON).

### Changed

- Đổi namespace/package cũ sang `com.ChronosDetective...` ở `core` và `lwjgl3`.
- Nâng cấp `run-game.bat`: trước khi chạy game sẽ tự tìm và kill instance game cũ (`Lwjgl3Launcher`) rồi mới `gradlew lwjgl3:run`, giúp reload bản mới nhanh hơn khi dev.
- `MapManager` + `GameScreen`: thêm animation mở cửa nhà bếp khi đứng gần cửa và bấm **E** (chạy theo nhiều frame tile trên layer `door` của `kitchen.tmx`), đồng thời hiển thị gợi ý mở cửa và chặn qua portal khi cửa còn đóng.
- `MenuScreen`: thêm luồng **CONTINUE** (resume session gần nhất) và **LOAD** (chọn session để load).
- `GameScreen`: bấm **O** mở form save (chọn session + lưu), bấm **ESC** xác nhận thoát về menu.

### Notes

- Baseline game: libGDX + VisUI, màn `MenuScreen` → `GameScreen`, tilemap `map.tmx`, layer va chạm `Fences`, tương tác item/NPC bằng phím E.

---

Khi commit tính năng mới, thêm mục dưới `[Unreleased]` hoặc tạo section phiên bản (ví dụ `## [0.1.0] - YYYY-MM-DD`) rồi gộp các mục Added/Changed/Fixed/Removed tương ứng.
