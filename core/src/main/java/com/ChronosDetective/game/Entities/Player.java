package com.ChronosDetective.game.Entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Player extends Entity {
    private TiledMap map;

    private enum State { IDLE, WALKING }
    private enum Direction { UP, DOWN, LEFT, RIGHT }

    private State currentState = State.IDLE;
    private Direction currentDirection = Direction.DOWN;

    private Animation<TextureRegion> walkUp, walkDown, walkLeft, walkRight;
    private Animation<TextureRegion> idleUp, idleDown, idleLeft, idleRight;

    private float stateTime = 0f;
    private final float SPEED = 100f;
    private Vector2 position;
    private float width = 64, height = 64;

    public Player(Texture texture, float x, float y, TiledMap map) {
        super(texture, x, y);
        this.position = new Vector2(x, y);
        this.map = map;

        int FRAME_COLS = 6;
        int FRAME_ROWS = 6;

        TextureRegion[][] tmp = TextureRegion.split(texture, 
                texture.getWidth() / FRAME_COLS,
                texture.getHeight() / FRAME_ROWS);

        float frameDuration = 0.2f;

        // Thiết lập Animations
        walkDown = new Animation<>(frameDuration, tmp[3]);
        walkUp = new Animation<>(frameDuration, tmp[5]);
        walkRight = new Animation<>(frameDuration, tmp[4]);
        
        idleDown = new Animation<>(frameDuration, tmp[0]);
        idleUp = new Animation<>(frameDuration, tmp[2]);
        idleRight = new Animation<>(frameDuration, tmp[1]);

        // Xử lý quay trái (Flip từ frame bên phải)
        TextureRegion[] walkLeftFrames = new TextureRegion[FRAME_COLS];
        TextureRegion[] idleLeftFrames = new TextureRegion[FRAME_COLS];
        for (int i = 0; i < FRAME_COLS; i++) {
            walkLeftFrames[i] = new TextureRegion(tmp[4][i]);
            walkLeftFrames[i].flip(true, false);
            idleLeftFrames[i] = new TextureRegion(tmp[1][i]);
            idleLeftFrames[i].flip(true, false);
        }
        walkLeft = new Animation<>(frameDuration, walkLeftFrames);
        idleLeft = new Animation<>(frameDuration, idleLeftFrames);

        // Thiết lập chế độ Loop cho tất cả
        Animation[] allAnims = {walkUp, walkDown, walkLeft, walkRight, idleUp, idleDown, idleLeft, idleRight};
        for (Animation a : allAnims) a.setPlayMode(Animation.PlayMode.LOOP);
    }

    @Override
    public void update(float delta) {
        currentState = State.IDLE;
        float oldX = position.x;
        float oldY = position.y;
        float moveX = 0, moveY = 0;

        // Xử lý Input
        if (Gdx.input.isKeyPressed(Input.Keys.W)) { moveY += SPEED * delta; currentDirection = Direction.UP; currentState = State.WALKING; }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) { moveY -= SPEED * delta; currentDirection = Direction.DOWN; currentState = State.WALKING; }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) { moveX -= SPEED * delta; currentDirection = Direction.LEFT; currentState = State.WALKING; }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) { moveX += SPEED * delta; currentDirection = Direction.RIGHT; currentState = State.WALKING; }

        // Di chuyển và check va chạm X
        position.x += moveX;
        if (checkCollisionAtPoints(position.x, position.y)) position.x = oldX;

        // Di chuyển và check va chạm Y
        position.y += moveY;
        if (checkCollisionAtPoints(position.x, position.y)) position.y = oldY;

        // Cập nhật tọa độ Entity cha để đồng bộ logic tương tác
        this.x = position.x;
        this.y = position.y;
        stateTime += delta;
    }

    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame;
        
        // Chọn Animation dựa trên trạng thái
        if (currentState == State.WALKING) {
            switch (currentDirection) {
                case UP:    currentFrame = walkUp.getKeyFrame(stateTime); break;
                case LEFT:  currentFrame = walkLeft.getKeyFrame(stateTime); break;
                case RIGHT: currentFrame = walkRight.getKeyFrame(stateTime); break;
                default:    currentFrame = walkDown.getKeyFrame(stateTime); break;
            }
        } else {
            switch (currentDirection) {
                case UP:    currentFrame = idleUp.getKeyFrame(stateTime); break;
                case LEFT:  currentFrame = idleLeft.getKeyFrame(stateTime); break;
                case RIGHT: currentFrame = idleRight.getKeyFrame(stateTime); break;
                default:    currentFrame = idleDown.getKeyFrame(stateTime); break;
            }
        }

        // Áp dụng offset 4f khi đi bộ để frame mượt hơn
        float drawY = (currentState == State.WALKING) ? position.y - 4f : position.y;
        batch.draw(currentFrame, position.x, drawY, width, height);
    }

    public boolean isCollision(float worldX, float worldY) {
        // Danh sách layer cần chặn (Lưu ý: Phải khớp với tên Layer trong Tiled của bạn Hưng)
        String[] solidLayers = {"Wall", "Fences", "Decor"};

        int tileX = (int)(worldX / 16);
        int tileY = (int)(worldY / 16);

        for (String name : solidLayers) {
            TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(name);
            if (layer == null) continue;

            if (tileX < 0 || tileY < 0 || tileX >= layer.getWidth() || tileY >= layer.getHeight()) return true;
            if (layer.getCell(tileX, tileY) != null) return true;
        }
        return false;
    }

    private boolean checkCollisionAtPoints(float x, float y) {
        float pX = 20f; // Biên ngang (càng lớn khung va chạm càng hẹp)
        float pY = 10f; // Biên dọc (càng lớn khung va chạm càng thấp)
        return (isCollision(x + pX, y + pY) || 
                isCollision(x + width - pX, y + pY) ||
                isCollision(x + pX, y + height / 2) || 
                isCollision(x + width - pX, y + height / 2));
    }

    public Rectangle getBounds() {
        float offsetX = (width - 20) / 2f;
        return new Rectangle(position.x + offsetX, position.y + 5f, 20, 20);
    }

    public boolean isNear(Entity entity) {
        if (entity == null) return false;
        float centerX = this.x + width / 2;
        float centerY = this.y + height / 2;
        float targetX = entity.getX() + entity.getWidth() / 2;
        float targetY = entity.getY() + entity.getHeight() / 2;
        return Vector2.dst(centerX, centerY, targetX, targetY) < 50f;
    }

    public void setMap(TiledMap newMap) { this.map = newMap; }
    public void setPosition(float x, float y) { this.position.set(x, y); }
    public float getWidth() { return width; }
    public float getHeight() { return height; }

    public void dispose() {
        // Chỉ dispose nếu texture này không dùng chung với các entity khác
        // Thường thì texture player load riêng nên ok
    }
}