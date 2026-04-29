package com.ChronosDetective.game.Entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.maps.MapProperties; // 1. NHỚ IMPORT CÁI NÀY

public class Item extends Entity {
    private boolean collected = false;
    private String name;
    private String ID;
    private float stateTime = 0;
    
    // 2. THÊM BIẾN NÀY ĐỂ LƯU THUỘC TÍNH
    private MapProperties properties;

    public Item(Texture texture, float x, float y, String name, String ID) {
        super(texture, x, y);
        this.name = name;
        this.sprite.setSize(16, 16);
        this.sprite.setPosition(x, y);
        this.ID = ID;
        
        // 3. KHỞI TẠO NÓ TRONG CONSTRUCTOR
        this.properties = new MapProperties();
    }

    // 4. THÊM HÀM GETTER NÀY ĐỂ ENTITYMANAGER GỌI ĐƯỢC
    public MapProperties getProperties() {
        return properties;
    }

    public Rectangle getBounds() {
        return sprite.getBoundingRectangle();
    }

    @Override
    public void update(float delta) {
        // Code update cũ của thám tử
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

    public Texture getTexture() { 
        return sprite.getTexture();
    }

    public String getID() { 
        return ID;
    }
}