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
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
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

    //Biến lưu trữ cho Deduction
    private Runnable endCallback;

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

    //Hàm để thám tử cài đặt hành động sau khi thoại kết thúc
    public void setDialogueEndCallback(Runnable callback) {
        this.endCallback = callback;
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

        if (endCallback != null) {
            endCallback.run();
            endCallback = null;
        }
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

    // --- 2. TÍNH TOÁN KÍCH THƯỚC HỘP ĐỘNG ---
    float zoom = camera.zoom;
    font.getData().setScale(zoom);

    float boxW = (viewport.getWorldWidth() - 80) * zoom;
    float textTargetWidth = boxW - (60 * zoom);

    // Dùng GlyphLayout để tính chiều cao thực tế của nội dung đầy đủ (fullText)
    // Điều này giúp khung hình không bị nhảy kích thước khi chữ đang chạy
    com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
    layout.setText(font, fullText, Color.WHITE, textTargetWidth, com.badlogic.gdx.utils.Align.left, true);
    
    float textHeight = layout.height;
    float paddingTop = 25 * zoom;
    float paddingBottom = 45 * zoom;

    // Chiều cao khung chính phụ thuộc vào nội dung
    float boxH = paddingTop + textHeight + paddingBottom;
    if (boxH < 100 * zoom) boxH = 100 * zoom; 

    float boxX = camera.position.x - boxW / 2;
    float boxY = camera.position.y - (viewport.getWorldHeight() / 2 * zoom) + (zoom * 40);

    // --- TÍNH TOÁN KHUNG TÊN (NAME BOX) ---
    float nameBoxW = 0, nameBoxH = 0, nameBoxX = 0, nameBoxY = 0;
    boolean hasName = speakerName != null && !speakerName.trim().isEmpty();

    if (hasName) {
        layout.setText(font, speakerName);
        float nameTextWidth = layout.width;
        float nameTextHeight = layout.height;

        nameBoxW = nameTextWidth + (40 * zoom);
        nameBoxH = nameTextHeight + (20 * zoom);
        nameBoxX = boxX + (20 * zoom);
        nameBoxY = boxY + boxH - (10 * zoom);
    }

    // --- 3. VẼ CÁC KHUNG (SHAPERENDERER) ---
    // Phải kết thúc batch trước khi dùng ShapeRenderer nếu batch đang chạy
    if (batch.isDrawing()) batch.end();

    Gdx.gl.glEnable(GL20.GL_BLEND);
    shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

    float radius = 12 * zoom;
    float border = 3 * zoom;
    Color borderColor = new Color(0.8f, 0.8f, 0.8f, 1f);
    Color bgColor = new Color(0, 0, 0, 0.8f);

    // 3.1. Vẽ Border và Khung Chính
    shapeRenderer.setColor(borderColor);
    drawFilledRoundedRect(boxX - border, boxY - border, boxW + border * 2, boxH + border * 2, radius + border);
    shapeRenderer.setColor(bgColor);
    drawFilledRoundedRect(boxX, boxY, boxW, boxH, radius);

    // 3.2. Vẽ Khung Tên
    if (hasName) {
        float nameRadius = 8 * zoom;
        shapeRenderer.setColor(borderColor);
        drawFilledRoundedRect(nameBoxX - border, nameBoxY - border, nameBoxW + border * 2, nameBoxH + border * 2, nameRadius + border);
        shapeRenderer.setColor(bgColor);
        drawFilledRoundedRect(nameBoxX, nameBoxY, nameBoxW, nameBoxH, nameRadius);
    }

    shapeRenderer.end();
    Gdx.gl.glDisable(GL20.GL_BLEND);

    // --- 4. VẼ CHỮ (SPRITEBATCH) ---
    batch.begin();
    font.getData().setScale(zoom); // Đảm bảo scale đúng sau khi tính toán

    // Vẽ Tên
    if (hasName) {
        font.setColor(Color.CYAN);
        float nameDrawX = nameBoxX + (20 * zoom);
        float nameDrawY = nameBoxY + nameBoxH - (10 * zoom);
        font.draw(batch, speakerName, nameDrawX, nameDrawY);
    }

    // Vẽ Nội Dung (currentText để có hiệu ứng chữ chạy)
    float currentY = boxY + boxH - paddingTop;
    font.setColor(new Color(0.95f, 0.95f, 0.95f, 1f));
    font.draw(batch, currentText, boxX + (30 * zoom), currentY, textTargetWidth, com.badlogic.gdx.utils.Align.left, true);

    // Vẽ Hint khi hoàn thành
    if (isFinished()) {
        font.getData().setScale(zoom * 0.75f);
        font.setColor(Color.GRAY);
        String hintText = isLastPage() ? "[PRESS E TO CLOSE]" : "[PRESS E FOR NEXT]";
        // Căn lề phải cho hint
        font.draw(batch, hintText, boxX + boxW - (200 * zoom), boxY + (25 * zoom));
    }
    
    // Lưu ý: Không đóng batch ở đây nếu bạn muốn các đối tượng khác vẽ tiếp, 
    // nhưng theo cấu trúc cũ của bạn thì để batch.end() là hợp lý.
    batch.end();
}

/**
 * Hàm hỗ trợ vẽ hình chữ nhật bo góc (vì ShapeRenderer mặc định không có)
 */
private void drawFilledRoundedRect(float x, float y, float width, float height, float radius) {
    // Vẽ 2 hình chữ nhật tạo thành chữ thập
    shapeRenderer.rect(x + radius, y, width - 2 * radius, height);
    shapeRenderer.rect(x, y + radius, width, height - 2 * radius);

    // Vẽ 4 góc hình tròn
    shapeRenderer.arc(x + radius, y + radius, radius, 180f, 90f);
    shapeRenderer.arc(x + width - radius, y + radius, radius, 270f, 90f);
    shapeRenderer.arc(x + width - radius, y + height - radius, radius, 0f, 90f);
    shapeRenderer.arc(x + radius, y + height - radius, radius, 90f, 90f);
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
