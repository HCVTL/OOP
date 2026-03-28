package ui;

import java.awt.*;

public class DialogueManager {
    private String currentText = "";
    private String fullText = "";
    private String speakerName = "";
    private boolean isVisible = false;
    private int charIndex = 0;
    private long lastTick = 0;

    public void showDialogue(String name, String text) {
        this.speakerName = name;
        this.fullText = text;
        this.currentText = "";
        this.charIndex = 0;
        this.isVisible = true;
    }

    public void hide() { isVisible = false; }

    public void update() {
        if (!isVisible || charIndex >= fullText.length()) return;

        // Hiệu ứng chữ chạy (Typewriter): Cứ 30ms hiện 1 chữ
        if (System.currentTimeMillis() - lastTick > 30) {
            currentText += fullText.charAt(charIndex);
            charIndex++;
            lastTick = System.currentTimeMillis();
        }
    }

    public void draw(Graphics g, int width, int height) {
        if (!isVisible) return;

        Graphics2D g2 = (Graphics2D) g;
        // 1. Vẽ khung hình chữ nhật (Bo góc cho đẹp)
        g2.setColor(new Color(0, 0, 0, 200)); // Màu đen trong suốt
        g2.fillRoundRect(50, height - 150, width - 100, 100, 20, 20);

        // 2. Vẽ viền trắng
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(50, height - 150, width - 100, 100, 20, 20);

        // 3. Vẽ tên người nói
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString(speakerName + ":", 70, height - 120);

        // 4. Vẽ nội dung đối thoại
        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        g2.drawString(currentText, 70, height - 90);

        // Gợi ý nhấn Space
        if (charIndex >= fullText.length()) {
            g2.setFont(new Font("Arial", Font.ITALIC, 10));
            g2.drawString("Nhấn Space để đóng...", width - 180, height - 65);
        }
    }

    public boolean isVisible() { return isVisible; }
}