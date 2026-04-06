package ui;

import entities.Evidence;

import java.util.ArrayList;

public class Inventory {
    private ArrayList<Evidence> items;

    public Inventory() {
        items = new ArrayList<>();
    }

    // Thêm vật phẩm vào túi
    public void add(Evidence item) {
        items.add(item);
        System.out.println("Đã thêm vào túi đồ: " + item.getName());
    }

    // Lấy danh sách để hiển thị
    public ArrayList<Evidence> getItems() {
        return items;
    }

    // Kiểm tra xem đã có món đồ nào đó chưa (Dùng cho logic giải đố sau này)
    public boolean hasItem(String itemName) {
        for (Evidence e : items) {
            if (e.getName().equalsIgnoreCase(itemName)) return true;
        }
        return false;
    }
}