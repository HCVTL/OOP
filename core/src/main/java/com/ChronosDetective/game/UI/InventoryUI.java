package com.ChronosDetective.game.UI;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.ChronosDetective.game.Managers.InventoryManager;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.ChronosDetective.game.Entities.Item;

public class InventoryUI {
    private VisWindow window;
    private VisTable contentTable;

    public InventoryUI(Stage stage) {
        // 1. Tạo cửa sổ tiêu đề "Sổ tay vật chứng"
        window = new VisWindow("SỔ TAY VẬT CHỨNG");
        window.setSize(400, 300);
        window.centerWindow(); // Hiện giữa màn hình
        window.setVisible(false); // Mặc định ẩn đi

        // 2. Tạo bảng nội dung bên trong
        contentTable = new VisTable();
        window.add(contentTable).expand().fill().top().pad(10);

        stage.addActor(window);
    }

    public void toggle(InventoryManager inventory) {
        window.setVisible(!window.isVisible());
        if (window.isVisible()) {
            updateList(inventory);
            window.toFront(); // Đưa cửa sổ lên trên cùng
        }
    }

    private void updateList(InventoryManager inventory) {
        contentTable.clearChildren(); // Xóa danh sách cũ để vẽ lại

        if (inventory.getItems().isEmpty()) {
            contentTable.add(new VisLabel("Chưa có bằng chứng nào..."));
        } else {
            for (Item item : inventory.getItems()) {
                // Bạn có thể thêm Icon item.getTexture() vào đây nếu muốn
                contentTable.add(new VisLabel("- " + item.getName())).left();
                contentTable.row();
            }
        }
        window.setSize(400, 300);
        window.centerWindow();
    }

    public boolean isVisible() { return window.isVisible(); }
}
