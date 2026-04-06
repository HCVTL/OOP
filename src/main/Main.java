import core.GameWindow; // Import lớp GameWindow từ package core của bạn
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Sử dụng invokeLater để đảm bảo giao diện (UI) chạy mượt mà trên một luồng riêng
        SwingUtilities.invokeLater(() -> {
            // Khởi tạo đối tượng GameWindow
            // Khi dòng này chạy, Constructor của GameWindow sẽ được kích hoạt
            new GameWindow();
        });
    }
}