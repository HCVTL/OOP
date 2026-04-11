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
        if (!dm.isActive()) {
            dm.startDialogue("Thám tử", "Đây là " + item.getName() + ". Tôi sẽ lấy nó.");
        }
        else {
            dm.closeDialogue();
            item.collect();
            inventory.addItem(item);
            mapManager.getCollectedItems().add(item.getID());
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
