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
        for (Item item : items) {
            if (!item.isCollected() && player.isNear(item)) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    handleItemInteraction(item, dialogueManager, inventory, mapManager, storyManager);
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

    public void handleItemInteraction(Item item, DialogueManager dm, InventoryManager inventory, MapManager mapManager, StoryManager story) {
        String id = item.getID();

        // 1. Tìm dữ liệu trong JSON thông qua StoryManager

        if (!dm.isActive()) {
            // Lần nhấn E đầu tiên: Lấy thoại từ JSON
            JsonValue clueData = story.getClueData(id);
            if (clueData != null) {
                String name = clueData.getString("name");
                String[] dialogue = clueData.get("dialogue").asStringArray();
                dm.startDialogue(name, dialogue);

                // Đánh dấu đã tìm thấy manh mối trong StoryManager
                markStoryProgress(id, story);
            } else {
                // Nếu không có trong JSON (vật phẩm rác), dùng tên từ Tiled
                dm.startDialogue("Thám tử", new String[]{"Đây là " + item.getName() + "."});
            }
        }
        else {
            // Các lần nhấn E sau: Chạy nốt thoại và nhặt đồ

            boolean wasLastPage = dm.isLastPage() && dm.isFinished();

            dm.nextPage();

            if (wasLastPage) {
                String type = item.getProperties().get("type", String.class);
                if ("ITEM".equals(type)) {
                    item.collect();
                    inventory.addItem(item);
                    mapManager.getCollectedItems().add(id);
                }
                else if ("CONTAINER".equals(type)) {
                    handleContainerLogic(item, dm, inventory, mapManager);
                }
            }
        }
    }

    // Hàm phụ để đánh dấu tiến trình cốt truyện
    private void markStoryProgress(String id, StoryManager story) {
        if ("Clue_Body".equals(id)) story.foundBody = true;
        else if ("Clue_Wine".equals(id)) story.foundWine = true;
        else if ("Clue_Window".equals(id)) story.foundWindow = true;
        else if ("Clue_Knife".equals(id)) story.foundKnife = true;
    }

    // Tách riêng logic Container cho sạch code
    private void handleContainerLogic(Item item, DialogueManager dm, InventoryManager inventory, MapManager mapManager) {
        String itemInsideName = item.getProperties().get("containsItem", String.class);
        if (itemInsideName != null && !itemInsideName.isEmpty()) {
            Texture texKey = mapManager.getItemLibrary().get(itemInsideName);
            if (texKey != null) {
                Item newItem = new Item(texKey, 0, 0, itemInsideName, "FOUND_" + itemInsideName);
                inventory.addItem(newItem);

                // Cập nhật trạng thái trực tiếp vào properties của item để lần sau tương tác sẽ khác
                item.getProperties().put("containsItem", "");
                item.getProperties().put("dialogue", item.getName() + "bây giờ trống không!");
                dm.startDialogue("Thám tử", new String[]{"Tôi tìm thấy một " + itemInsideName + "!"});
            }
        }
    }

    public void handleNPCInteraction(NPC npc, DialogueManager dm) {
        if (!dm.isActive()) {
            String[] pages = npc.getDialogue("DEFAULT");
            dm.startDialogue(npc.getName(), npc.getDialogue("DEFAULT"));
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
