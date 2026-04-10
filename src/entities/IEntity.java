package entities;

import java.awt.Graphics;

public interface IEntity {
    void draw(Graphics g);                               // Vẽ đối tượng
    boolean checkCollision(int pX, int pY, int pSize);   // Kiểm tra va chạm để nhặt/tương tác
    void setHighlight(boolean highlight);               // Bật/tắt hiệu ứng phát sáng
    String getName();                                    // Lấy tên đối tượng
    int getX();                                          // Lấy tọa độ X
    int getY();                                          // Lấy tọa độ Y
}