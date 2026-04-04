package com.ChronosDetective.game.Managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;

public class DialogueManager {
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private Viewport viewport;
    private OrthographicCamera camera;

    private boolean isActive = false;
    private String speakerName = "";
    private String dialogueText = "";



    public DialogueManager(Viewport viewport, OrthographicCamera camera) {
        this.viewport = viewport;
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.camera = camera;
        this.font.getData().setScale(1.2f);
    }

    // Hàm kích hoạt hội thoại
    public void startDialogue(String name, String text) {
        this.isActive = true;
        this.speakerName = name;
        this.dialogueText = text;
    }

    // Hàm đóng hội thoại
    public void closeDialogue() {
        this.isActive = false;
    }

    public boolean isActive() { return isActive; }

    public void draw(SpriteBatch batch) {
        if (!isActive) return;

        float zoom = camera.zoom;

        // 1. Vẽ khung đen mờ (Dùng ShapeRenderer)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);

        float boxW = (viewport.getWorldWidth() - 40) * zoom;
        float boxH = 80 * zoom;
        float boxX = camera.position.x - boxW / 2;
        float boxY = camera.position.y - (viewport.getWorldHeight() / 2 * zoom) + (zoom * 20);

        shapeRenderer.rect(boxX, boxY, boxW, boxH);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // 2. Vẽ chữ (Dùng Batch)
        batch.begin();
        font.getData().setScale(1.2f * zoom);
        font.setColor(Color.YELLOW);
        font.draw(batch, speakerName + ":", boxX + (20 * zoom), boxY + boxH - (15* zoom));
        font.setColor(Color.WHITE);
        font.draw(batch, dialogueText, boxX + (20 * zoom), boxY + boxH - (40 * zoom), boxW - (40 * zoom), -1, true);
        batch.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
    }
}
