package com.ChronosDetective.game.Managers;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.ChronosDetective.game.Entities.Item;
import com.ChronosDetective.game.Entities.NPC;
import com.ChronosDetective.game.Entities.Player;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;

public class EntityManager {
    private ArrayList<Item> items;
    private ArrayList<NPC> npcs;
    private Sprite pointerSprite;

    private float interactionCooldown = 0f;

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

    public void update(float delta, Player player, DialogueManager dialogueManager, InventoryManager inventory, MapManager mapManager, StoryManager storyManager) {
        if (interactionCooldown > 0) interactionCooldown -= delta;
        if (dialogueManager.isActive() || interactionCooldown > 0) return;

        for (Item item : items) {
            if (!item.isCollected() && player.isNear(item)) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    handleItemInteraction(item, dialogueManager, inventory, mapManager, storyManager);
                    interactionCooldown = 2f;
                    return;
                }
            }
        }

        for (NPC npc : npcs) {
            if (player.isNear(npc)) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    handleNPCInteraction(npc, dialogueManager, storyManager);
                    interactionCooldown = 2f;
                    return;
                }
            }
        }
    }

    public void handleItemInteraction(Item item, DialogueManager dm, InventoryManager inventory, MapManager mapManager, StoryManager story) {
        String id = item.getID();

        if (!dm.isActive()) {
            // Lấy dữ liệu tên và thoại từ cấu trúc JSON mới
            String name = story.getEntityName(id);
            String[] dialogue = story.getValidInteraction(id);

            dm.startDialogue("Thám tử", dialogue);

            // Xử lý nhặt đồ (Giữ nguyên logic loại ITEM)
            String type = item.getProperties().get("type", String.class);
            if ("ITEM".equals(type)) {
                if (!item.isCollected()) {
                    item.collect();
                    inventory.addItem(item);
                    mapManager.getCollectedItems().add(id);
                }
            }
            else if ("CONTAINER".equals(type)) {
                handleContainerLogic(item, dm, inventory, mapManager, story);
            }
        }
    }

    // Tách riêng logic Container cho sạch code
    private void handleContainerLogic(Item item, DialogueManager dm, InventoryManager inventory, MapManager mapManager, StoryManager story) {
        String itemInsideName = item.getProperties().get("containsItem", String.class);

        if (itemInsideName != null && !itemInsideName.isEmpty()) {
            Texture texKey = mapManager.getItemLibrary().get(itemInsideName);
            if (texKey != null) {
                // Tạo item mới từ trong container
                Item newItem = new Item(texKey, 0, 0, itemInsideName, "FOUND_" + itemInsideName);
                inventory.addItem(newItem);

                // Ghi nhận flag đã mở container vào StoryManager
                story.setFlag("OPENED_" + item.getID());

                // Cập nhật trạng thái item ngay trong Tiled Properties để tránh lặp lại
                item.getProperties().put("containsItem", "");

                // Thông báo nhặt được đồ
                dm.startDialogue("Thám tử", new String[]{"Tôi tìm thấy một " + itemInsideName + " bên trong!"});
            }
        }
    }

    public void handleNPCInteraction(NPC npc, DialogueManager dm, StoryManager story) {
        if (!dm.isActive()) {
            // Class StoryManager sẽ tự biết lấy câu nào dựa trên Flag[cite: 1, 2]
            String[] dialogue = story.getValidInteraction(npc.getID());
            String name = story.getEntityName(npc.getID());

            dm.startDialogue(name, dialogue);
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
