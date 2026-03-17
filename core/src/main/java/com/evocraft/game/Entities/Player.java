package com.evocraft.game.Entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;

import java.awt.*;

public class Player extends Entity{
    private float speed = 200f;
    private TiledMap map;
    private float hunger = 100f;

    public Player(Texture texture, float x, float y, TiledMap map) {
        super(texture, x, y);
        this.map = map;
        this.sprite.setSize(32, 32);
    }

    @Override
    public void update(float delta) {
        float oldX = x, oldY = y;
        float move = speed * delta;
        float p = 12f;

        // Di chuyển trục X trước
        if (Gdx.input.isKeyPressed(Input.Keys.A)) x -= move;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) x += move;

        // Kiểm tra va chạm trục X (kiểm tra vùng chân nhân vật)
        if (isCollision(x + p, y + 2) || isCollision(x + sprite.getWidth() - p, y + 2)) {
            x = oldX;
        }

        // Di chuyển trục Y sau
        if (Gdx.input.isKeyPressed(Input.Keys.W)) y += move;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) y -= move;

        // Kiểm tra va chạm trục Y
        if (isCollision(x + p, y + 2) || isCollision(x + sprite.getWidth() - p, y + 2)) {
            y = oldY;
        }

        sprite.setPosition(x, y);
    }

    public boolean isCollision(float worldX, float worldY) {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("Fences");
        if (layer == null) return false;

        int tileX = (int)(worldX / 16);
        int tileY = (int)(worldY / 16);

        TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);

        if (tileX < 0 || tileY < 0 || tileX >= layer.getWidth() || tileY >= layer.getHeight()) {
            return true;
        }

        return cell != null;
    }

    public float getWidth() {
        return sprite.getWidth();
    }

    public float getHeight() {
        return sprite.getHeight();
    }

    public float getHunger() {
        return hunger;
    }

    public void eat(float amount) {
        this.hunger += amount;
        if (this.hunger > 100) this.hunger = 100;
    }

    public Rectangle getBounds() {
        return sprite.getBoundingRectangle();
    }

    public void dispose() {
        sprite.getTexture().dispose();
    }
}
