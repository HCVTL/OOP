package com.ChronosDetective.game.Entities;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapProperties;

import java.util.HashMap;
import java.util.Map;

public class NPC extends Entity {
    private String name;
    private String id;
    private MapProperties properties;
    private Map<String, String[]> dialogueTree;

    public NPC (Texture texture, float x, float y, String name, String id) {
        super(texture, x, y);
        this.name = name;
        this.id = id;
        this.properties = new MapProperties();
        this.dialogueTree = new HashMap<>();
        this.sprite.setSize(32, 48);
        this.sprite.setPosition(x, y);
    }

    @Override
    public void update(float delta) {
    }

    public void addDialogueBranch(String brachKey, String[] pages) {
        dialogueTree.put(brachKey, pages);
    }

    public String[] getDialogue(String brancKey) {
        return dialogueTree.getOrDefault(brancKey, new String[]{"..."});
    }

    public String getName() { return name; }
    public String getID() {return id;}
    public MapProperties getProperties() {return properties;}

    public float getX() { return sprite.getX(); }
    public float getY() { return sprite.getY(); }
    public float getWidth() { return sprite.getWidth(); }
    public float getHeight() { return sprite.getHeight(); }

}


