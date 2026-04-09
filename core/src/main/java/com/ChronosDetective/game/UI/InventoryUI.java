package com.ChronosDetective.game.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.ChronosDetective.game.Managers.InventoryManager;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.ChronosDetective.game.Entities.Item;

import java.util.ArrayList;

public class InventoryUI {
    private boolean visible = false;
    private int selectedSlot = 0;
    private float pW = 340, pH = 400; // Kích thước bảng

    public void toggle() {
        visible = !visible;
    }

    public boolean isVisible() {
        return visible;
    }

    // Hàm vẽ chính - Gọi trong GameScreen.render()
    public void draw(SpriteBatch batch, ShapeRenderer debugRenderer, FitViewport uiViewport, InventoryManager manager, BitmapFont font) {
        if (!visible) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        debugRenderer.setProjectionMatrix(uiViewport.getCamera().combined);
        batch.setProjectionMatrix(uiViewport.getCamera().combined);

        float sX = (uiViewport.getWorldWidth() - pW) / 2;
        float sY = (uiViewport.getWorldHeight() - pH) / 2;

        // 1. Vẽ nền (Background)
        debugRenderer.begin(ShapeRenderer.ShapeType.Filled);
        debugRenderer.setColor(new Color(0, 0, 0, 0.85f));
        debugRenderer.rect(sX, sY, pW, pH);
        debugRenderer.setColor(new Color(0.15f, 0.15f, 0.15f, 1));
        debugRenderer.rect(sX + 15, sY + 185, pW - 30, 195);
        debugRenderer.end();

        // 2. Vẽ các ô Slot
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        float slotSize = 55, pad = 15;
        float gX = sX + 35, gY = sY + 35;
        for (int i = 0; i < 8; i++) {
            int row = i / 4; int col = i % 4;
            float x = gX + col * (slotSize + pad);
            float y = gY + (1 - row) * (slotSize + pad);
            debugRenderer.setColor(i == selectedSlot ? Color.GOLD : Color.GRAY);
            debugRenderer.rect(x, y, slotSize, slotSize);
        }
        debugRenderer.end();

        // 3. Vẽ Item và Text
        batch.begin();
        ArrayList<Item> items = manager.getItems();

        if (selectedSlot < items.size()) {
            String displayName = items.get(selectedSlot).getName().toUpperCase();
            font.setColor(Color.GOLD);
            font.draw(batch, "VẬT CHỨNG: " + displayName, sX + 30, sY + 360);
        }

        for (int i = 0; i < items.size(); i++) {
            int row = i / 4; int col = i % 4;
            float x = gX + col * (slotSize + pad);
            float y = gY + (1 - row) * (slotSize + pad);

            // Lấy trực tiếp Texture từ Item trong kho
            batch.draw(items.get(i).getTexture(), x + 5, y + 5, slotSize - 10, slotSize - 10);
        }
        batch.end();
    }

    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) selectedSlot = (selectedSlot + 1) % 8;
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) selectedSlot = (selectedSlot - 1 + 8) % 8;
    }
}
