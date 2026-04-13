## Fonts (Unicode / Tiếng Việt)

Để hiển thị tiếng Việt có dấu đúng trong `DialogueManager` (và về sau là UI), dự án cần một font `.ttf` có đủ glyph Unicode.

### Khuyến nghị

- **Noto Sans** (Google) — dễ đọc, đủ ký tự tiếng Việt, dùng ổn cho UI/game text.
- **Roboto** (Google) — cũng ổn, phổ biến.

### Cách đặt file

Thả **một trong hai file** sau vào đúng đường dẫn:

- `assets/fonts/NotoSans-Regular.ttf` (ưu tiên)
- hoặc `assets/fonts/Roboto-Regular.ttf`

Sau đó chạy lại game.

### Ghi chú kỹ thuật

- Nếu không có font TTF, code sẽ fallback về `new BitmapFont()` (font mặc định) và **sẽ lỗi dấu tiếng Việt**.
