package com.evocraft.game.Entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class Entity {
    protected float x,y;
    protected Sprite sprite;

    public Entity(Texture texture, float x, float y) {
        this.sprite = new Sprite(texture);
        this.x = x;
        this.y = y;
        this.sprite.getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    public abstract void update(float delta);

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }

    public abstract float getWidth();
    public abstract float getHeight();
}
