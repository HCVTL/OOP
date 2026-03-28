package entities;
import entities.*;

import java.awt.*;

public class Evidence implements IEntity {
    private int x, y, size = 20;
    private String name;
    private boolean isNearby = false;

    public Evidence(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    @Override
    public int getX() { return x; }

    @Override
    public int getY() { return y; }

    @Override
    public String getName() { return name; }

    @Override
    public void setHighlight(boolean highlight) {
        this.isNearby = highlight;
    }

    @Override
    public void draw(Graphics g) {
        // Nếu người chơi ở gần, vẽ viền sáng và hiện tên
        if (isNearby) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(3));
            g2.setColor(new Color(255, 255, 255, 180));
            g2.drawOval(x - 5, y - 5, size + 10, size + 10);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString(name.toUpperCase(), x - 15, y - 15);
        }

        // Vẽ vật phẩm chính (hình tròn vàng)
        g.setColor(Color.YELLOW);
        g.fillOval(x, y, size, size);
    }

    @Override
    public boolean checkCollision(int pX, int pY, int pSize) {
        Rectangle playerRect = new Rectangle(pX, pY, pSize, pSize);
        Rectangle itemRect = new Rectangle(x, y, size, size);
        return playerRect.intersects(itemRect);
    }
}