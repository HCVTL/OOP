package com.evocraft.game.Entities;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class NPC extends Entity {
    private String name;
    private String dialogue;


    public NPC (Texture texture, float x, float y, String name, String dialogue) {
        super(texture, x, y);
        this.name = name;
        this.dialogue = dialogue;

        this.sprite.setSize(32, 32);
        this.sprite.setPosition(x, y);
    }

    @Override
    public void update(float delta) {
    }

    public String getName() { return name; }
    public String getDialogue() { return dialogue; }

    public float getX() { return sprite.getX(); }
    public float getY() { return sprite.getY(); }
    public float getWidth() { return sprite.getWidth(); }
    public float getHeight() { return sprite.getHeight(); }

}


