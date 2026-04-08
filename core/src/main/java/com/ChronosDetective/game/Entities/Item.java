package com.ChronosDetective.game.Entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class Item extends Entity{
    private boolean collected = false;
    private String name;
    private String ID;
    private float stateTime = 0;

    public Item(Texture texture, float x, float y, String name, String ID) {
            super(texture, x, y);
            this.name = name;
            this.sprite.setSize(16, 16);
            this.sprite.setPosition(x, y);
            this.ID = ID;
    }

    public Rectangle getBounds() {
        return sprite.getBoundingRectangle();
    }

    @Override
    public void update (float delta) {
        // stateTime += delta;

        // float alpha = 0.5f + (float)Math.sin(stateTime * 5f) * 0.5f;
        // sprite.setAlpha(alpha);
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        this.collected = true;
    }

    public float getWidth() {
        return sprite.getWidth();
    }

    public float getHeight() {
        return sprite.getHeight();
    }

    public String getName() {
        return name;
    }

    public String getID() { return ID;}
}
