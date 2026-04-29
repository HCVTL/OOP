package com.ChronosDetective.game.Managers;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
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

    public void update(float delta, Player player, DialogueManager dialogueManager, InventoryManager inventory, MapManager mapManager) {
        for (Item item : items) {
            if (!item.isCollected() && player.isNear(item)) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    handleItemInteraction(item, dialogueManager, inventory, mapManager);
                }
            }
        }

        for (NPC npc : npcs) {
            if (player.isNear(npc)) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    handleNPCInteraction(npc, dialogueManager);
                }
            }
        }
    }

    public void handleItemInteraction(Item item, DialogueManager dm, InventoryManager inventory, MapManager mapManager) {
        // 1. Lấy thông tin từ Properties của Item (đã set từ Tiled)
        String type = item.getProperties().get("type", String.class);
        String customDialogue = item.getProperties().get("dialogue", String.class);

        // Nếu không có thoại riêng trong Tiled, dùng thoại mặc định
        if (customDialogue == null) {
            customDialogue = "Đây là " + item.getName() + ".";
        }

        if (!dm.isActive()) {
            // Lần nhấn E đầu tiên: Hiện thoại
            dm.startDialogue("Thám tử", customDialogue);
        }
        else {
            // Lần nhấn E thứ hai (khi hộp thoại đang mở): Xử lý logic
            dm.closeDialogue();

            if ("ITEM".equals(type)) {
                // LOGIC CHO CAFE: Nhặt xong là biến mất
                item.collect();
                inventory.addItem(item);
                mapManager.getCollectedItems().add(item.getID());
                System.out.println("Đã nhặt: " + item.getName());
            }
            else if ("CONTAINER".equals(type)) {
                // LOGIC CHO NGĂN TỦ: Nhả chìa khóa nhưng tủ KHÔNG biến mất
                String itemInsideName = item.getProperties().get("containsItem", String.class);

                if (itemInsideName != null && !itemInsideName.isEmpty()) {
                    Texture texKey = mapManager.getItemLibrary().get(itemInsideName);

                    if (texKey != null) {
                        Item newItem = new Item(texKey, 0, 0, itemInsideName, "FOUND_" + itemInsideName);

                        inventory.addItem(newItem);

                        item.getProperties().put("containsItem", "");
                        item.getProperties().put("dialogue", item.getName() + " bây giờ trống rỗng.");

                        dm.startDialogue("Thám tử", "Tôi tìm thấy một " + itemInsideName + "!");
                    }
                }
                else {
                    dm.closeDialogue();
                }
            }
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
}
