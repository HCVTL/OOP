package com.ChronosDetective.game.Managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;

public class DialogueManager implements com.badlogic.gdx.InputProcessor{
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private Viewport viewport;
    private OrthographicCamera camera;

    private boolean isActive = false;
    private String speakerName = "";
    private String fullText = "";     // Nội dung đầy đủ
    private String currentText = "";  // Nội dung đang hiển thị dần dần

    // Hiển thị trang thoại
    private String[] pages;
    private int pageIndex = 0;

    // ĐIỀU KHIỂN CHỮ CHẠY
    private int charIndex = 0;
    private float timeCounter = 0;
    private final float CHAR_SPEED = 0.035f; // Tốc độ chạy chữ (giây/ký tự)

    public DialogueManager(Viewport viewport, OrthographicCamera camera) {
        this.viewport = viewport;
        this.camera = camera;
        this.shapeRenderer = new ShapeRenderer();

        // --- KHỞI TẠO FONT CHẤT LƯỢNG CAO (GIỐNG SWING) ---
        // Thay "fonts/arial.ttf" bằng đường dẫn font của bạn
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arial.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 22; // Kích thước chữ vừa vặn, sắc nét
        parameter.color = Color.WHITE;
        parameter.borderWidth = 1.2f; // Tạo viền đen cho chữ dễ đọc
        parameter.borderColor = Color.BLACK;

        // Bật khử răng cưa (Linear Filter) giúp chữ mịn như Java Swing
        parameter.magFilter = Texture.TextureFilter.Linear;
        parameter.minFilter = Texture.TextureFilter.Linear;

        // Hỗ trợ đầy đủ tiếng Việt
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS +
            "áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđ" +
            "ÁÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬÉÈẺẼẸÊẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰÝỲỶỸỊĐ";

        this.font = generator.generateFont(parameter);
        generator.dispose(); // Giải phóng generator sau khi đã tạo xong font
    }

    // Hàm bắt đầu hội thoại - Gọi khi tương tác với NPC/Vật phẩm
    public void startDialogue(String name, String[] textPages) {
        this.isActive = true;
        this.speakerName = name;
        this.pages = textPages;
        this.pageIndex = 0;

        //Thiết lập trang đầu tiên
        setupPage();
    }

    //Thiết lập trang hện tại
    private void setupPage() {
        if (pages != null && pageIndex < pages.length) {
            this.fullText = pages[pageIndex];
            this.currentText = "";
            this.charIndex = 0;
            this.timeCounter = 0;
        }
    }

    // Lật trang tiếp theo
    public void nextPage() {
        if (charIndex < fullText.length()) {
            charIndex = fullText.length();
            currentText = fullText;
        }
        else {
            pageIndex++;
            if (pageIndex < pages.length) {
                setupPage();
            }
            else {
                closeDialogue();
            }
        }
    }

    public boolean isLastPage() {
        return pages == null || pageIndex >= pages.length - 1;
    }

    public void closeDialogue() {
        this.isActive = false;
    }

    public boolean isActive() { return isActive; }

    public boolean isFinished() { return charIndex >= fullText.length(); }

    public void draw(SpriteBatch batch) {
        if (!isActive) return;

        // --- 1. LOGIC CẬP NHẬT CHỮ CHẠY ---
        if (charIndex < fullText.length()) {
            timeCounter += Gdx.graphics.getDeltaTime();
            if (timeCounter >= CHAR_SPEED) {
                charIndex++;
                currentText = fullText.substring(0, charIndex);
                timeCounter = 0;
            }
        }

        float zoom = camera.zoom;

        // --- 2. VẼ KHUNG ĐỐI THOẠI (DÙNG SHAPERENDERER) ---
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Màu nền đen mờ (Alpha = 0.8)
        shapeRenderer.setColor(0, 0, 0, 0.8f);

        float boxW = (viewport.getWorldWidth() - 80) * zoom;
        float boxH = 110 * zoom;
        float boxX = camera.position.x - boxW / 2;
        float boxY = camera.position.y - (viewport.getWorldHeight() / 2 * zoom) + (zoom * 40);

        shapeRenderer.rect(boxX, boxY, boxW, boxH);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // --- 3. VẼ CHỮ (DÙNG SPRITEBATCH) ---
        batch.begin();

        // Giữ nguyên scale gốc để font sắc nét nhất
        font.getData().setScale(zoom);

        // Vẽ tên người nói (Màu vàng Cyan hoặc Yellow)
        font.setColor(Color.CYAN);
        font.draw(batch, speakerName + ":", boxX + (30 * zoom), boxY + boxH - (20 * zoom));

        // Vẽ nội dung (Màu trắng mờ nhẹ cho đỡ chói)
        font.setColor(new Color(0.95f, 0.95f, 0.95f, 1f));
        font.draw(batch, currentText, boxX + (30 * zoom), boxY + boxH - (55 * zoom), boxW - (60 * zoom), -1, true);

        // Vẽ hint khi chạy xong chữ
        if (isFinished()) {
            font.getData().setScale(camera.zoom * 0.75f);
            font.setColor(Color.GRAY);
            String hint = isLastPage() ? "[PRESS E TO CLOSE]" : "[PRESS E FOR NEXT]";
            font.draw(batch, hint, boxX + boxW - (180 * camera.zoom), boxY + (25 * camera.zoom));
        }
        batch.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (isActive && keycode == com.badlogic.gdx.Input.Keys.E) {
            nextPage(); // Tự xử lý lật trang khi nhấn E
            return true; // "Nuốt" phím E để các hệ thống khác không nhận được nữa
        }
        return false;
    }

    // Các hàm khác của InputProcessor chỉ cần return false
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
}
