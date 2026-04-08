package com.ChronosDetective.game.Managers;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.ChronosDetective.game.Entities.Item;
import com.ChronosDetective.game.Entities.NPC;
import com.ChronosDetective.game.Entities.Player;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.ArrayList;

public class EntityManager {
    private ArrayList<Item> items;
    private ArrayList<NPC> npcs;
    private Sprite pointerSprite;

    public EntityManager(Sprite pointer) {
        this.items = new ArrayList<>();
        this.npcs = new ArrayList<>();
        this.pointerSprite = pointer;
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public void addNPC(NPC npc) {
        npcs.add(npc);
    }

    public void update(float delta, Player player, DialogueManager dialogueManager, InventoryManager inventory) {
        // 1. Kiểm tra xem Dialogue có đang mở không
    if (dialogueManager.isActive()) {
        // Nếu đang mở mà bấm E thì chỉ lo việc đóng nó lại thôi, không làm gì khác
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            // Tìm xem đang tương tác với Item hay NPC nào để xử lý đóng
            handleClosingInteraction(dialogueManager, items, inventory, player);
        }
        return; // Thoát hàm luôn, không cho phép kích hoạt mở mới trong cùng 1 frame
    }
        for (Item item : items) {
            if (!item.isCollected() && player.isNear(item)) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                   dialogueManager.startDialogue("Thám tử", "Đây là " + item.getName() + ". Tôi sẽ lấy nó.");
                    return;
                }
            }
        }

       for (NPC npc : npcs) {
        if (player.isNear(npc)) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                dialogueManager.startDialogue(npc.getName(), npc.getDialogue());
                return;
                }
            }
        }
    }
    // Hàm phụ để xử lý việc đóng hội thoại và nhặt đồ
private void handleClosingInteraction(DialogueManager dm, ArrayList<Item> items, InventoryManager inv, Player player) {
    dm.closeDialogue();
    
    // Kiểm tra xem có item nào ở gần mà thám tử vừa mới "đọc" xong không để thu hồi
    for (Item item : items) {
        if (!item.isCollected() && player.isNear(item)) {
            item.collect();
            inv.addItem(item);
            player.addItem(item.getName().toLowerCase());
            break; 
        }
    }
}

    public void handleItemInteraction(Item item, DialogueManager dm, InventoryManager inventory, Player player) {
        if (!dm.isActive()) {
            dm.startDialogue("Tham tu", "Day la " + item.getName() + ". Toi se lay no.");
        }
        else {
            dm.closeDialogue();
            item.collect();
            inventory.addItem(item);
            player.addItem(item.getName().toLowerCase());
        }
    }

    public void handleNPCInteraction(NPC npc, DialogueManager dm) {
        if (!dm.isActive()) {
            dm.startDialogue(npc.getName(), npc.getDialogue());
        }
        else {
            dm.closeDialogue();
        }
    }

    public void draw(SpriteBatch batch, Player player) {
        for (Item item : items) {
            if (!item.isCollected()) {
                item.draw(batch);
                if (player.isNear(item)) drawPointer(batch, item.getX(), item.getY(), item.getWidth(), item.getHeight());
            }
        }

        for (NPC npc : npcs) {
            npc.draw(batch);
            if (player.isNear(npc)) drawPointer(batch, npc.getX(), npc.getY(), npc.getWidth(), npc.getHeight());
        }
    }

    private void drawPointer(SpriteBatch batch, float x, float y, float w, float h) {
        pointerSprite.setPosition(x + w / 2 - pointerSprite.getWidth() / 2, y + h + 5);
        pointerSprite.draw(batch);
    }

    public void clearEntities() {
        items.clear();
        npcs.clear();
    }
    public ArrayList<Item> getItems() {
    return items;
}
}
