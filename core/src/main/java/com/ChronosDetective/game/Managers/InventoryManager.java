package com.ChronosDetective.game.Managers;

import com.ChronosDetective.game.Entities.Item;
import java.util.ArrayList;

public class InventoryManager {
    private ArrayList<Item> collectedItems;

    public InventoryManager() {
        collectedItems = new ArrayList<>();
    }

    public void addItem(Item item) {
        collectedItems.add(item);
    }

    public ArrayList<Item> getItems() {
        return collectedItems;
    }
}
