package entities;

import java.awt.*;

public class Wall implements IEntity {
    // Để các biến là protected để class Door (kế thừa) có thể truy cập trực tiếp
    protected int x, y, width, height;
    protected Color wallColor = new Color(70, 70, 70); // Màu xám mặc định

    public Wall(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(Graphics g) {
        // Vẽ khối tường
        g.setColor(wallColor);
        g.fillRect(x, y, width, height);

        // Vẽ viền đen để tách biệt các khối tường khi xếp cạnh nhau
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);
    }

    // Trả về hình chữ nhật đại diện để tính toán va chạm (AABB)
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    @Override
    public boolean checkCollision(int pX, int pY, int pSize) {
        // Kiểm tra xem hình chữ nhật của người chơi có giao với tường không
        Rectangle playerRect = new Rectangle(pX, pY, pSize, pSize);
        return playerRect.intersects(getBounds());
    }

    // Các phương thức Getters cần thiết cho việc tính toán tọa độ động
    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    // Các phương thức bắt buộc từ IEntity nhưng Wall không dùng đến
    @Override public void setHighlight(boolean h) {}
    @Override public String getName() { return "Wall"; }
}