package entities;
import entities.*;

import java.awt.*;
import javax.swing.JOptionPane;

public class NPC implements IEntity {
    private int x, y, size = 35; // entities.NPC to hơn vật phẩm
    private String name;
    private String dialogue;
    private boolean isNearby = false;

    public NPC(String name, int x, int y, String dialogue) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.dialogue = dialogue;
    }

    @Override
    public void draw(Graphics g) {
        if (isNearby) {
            g.setColor(Color.WHITE);
            g.drawRect(x - 2, y - 2, size + 4, size + 4);
            g.drawString("Hỏi chuyện " + name, x - 10, y - 10);
        }
        // Vẽ entities.NPC màu hồng tím để phân biệt
        g.setColor(new Color(255, 0, 255));
        g.fillRect(x, y, size, size);
        g.setColor(Color.BLACK);
        g.drawString("entities.NPC", x + 5, y + 20);
    }

    @Override
    public boolean checkCollision(int pX, int pY, int pSize) {
        return new Rectangle(pX, pY, pSize, pSize).intersects(new Rectangle(x, y, size, size));
    }

    // Khi tương tác với entities.NPC, ta hiện hộp thoại thay vì xóa entities.NPC
    public void speak(Component parent) {
        JOptionPane.showMessageDialog(parent, name + ": \"" + dialogue + "\"", "Đối thoại", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void setHighlight(boolean highlight) { this.isNearby = highlight; }

    @Override
    public String getName() { return name; }

    @Override
    public int getX() { return x; }

    @Override
    public int getY() { return y; }

    public String getDialogue() {
        return this.dialogue; // 'dialogue' là biến String chứa lời thoại của entities.NPC
    }
}