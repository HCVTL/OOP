package com.evocraft.game.Entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

import java.awt.*;

public class Item extends Entity{
    private boolean collected = false;
    private String name;
    private float stateTime = 0;

    public Item(Texture texture, float x, float y, String name) {
            super(texture, x, y);
            this.name = name;
            this.sprite.setSize(16, 16);
            this.sprite.setPosition(x, y);
    }

    public Rectangle getBounds() {
        return sprite.getBoundingRectangle();
    }

    @Override
    public void update (float delta) {
        stateTime += delta;

        float alpha = 0.5f + (float)Math.sin(stateTime * 5f) * 0.5f;
        sprite.setAlpha(alpha);
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        this.collected = true;
    }

    public String getName() {
        return name;
    }
}
