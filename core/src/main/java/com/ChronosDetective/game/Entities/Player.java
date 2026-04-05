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

public class Player extends Entity{
    private TiledMap map;

    private enum State {IDLE, WALKING}
    private enum Direction {UP, DOWN, LEFT, RIGHT}

    private State currentState = State.IDLE;
    private Direction currentDirection = Direction.DOWN;

    private Animation<TextureRegion> walkUp;
    private Animation<TextureRegion> walkDown;
    private Animation<TextureRegion> walkLeft;
    private Animation<TextureRegion> walkRight;

    private Animation<TextureRegion> idleUp;
    private Animation<TextureRegion> idleDown;
    private Animation<TextureRegion> idleLeft;
    private Animation<TextureRegion> idleRight;

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

        TextureRegion[][] tmp = TextureRegion.split(texture, texture.getWidth() / FRAME_COLS,
                                                    texture.getHeight() / FRAME_ROWS);

        float frameDuration = 0.2f;
        walkRight = new Animation<>(frameDuration, tmp[4]);
        idleRight = new Animation<>(frameDuration, tmp[1]);

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

        walkDown = new Animation<>(frameDuration, tmp[3]);
        walkUp = new Animation<>(frameDuration, tmp[5]);
        idleDown = new Animation<>(frameDuration, tmp[0]);
        idleUp = new Animation<>(frameDuration, tmp[2]);

        walkDown.setPlayMode(Animation.PlayMode.LOOP);
        walkUp.setPlayMode(Animation.PlayMode.LOOP);
        walkLeft.setPlayMode(Animation.PlayMode.LOOP);
        walkRight.setPlayMode(Animation.PlayMode.LOOP);

        idleDown.setPlayMode(Animation.PlayMode.LOOP);
        idleUp.setPlayMode(Animation.PlayMode.LOOP);
        idleLeft.setPlayMode(Animation.PlayMode.LOOP);
        idleRight.setPlayMode(Animation.PlayMode.LOOP);

    }

    @Override
    public void update(float delta) {
        currentState = State.IDLE;

        float oldX = position.x;
        float oldY = position.y;

        float moveX = 0;
        float moveY = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) { moveY += SPEED * delta; currentDirection = Direction.UP; currentState = State.WALKING; }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) { moveY -= SPEED * delta; currentDirection = Direction.DOWN; currentState = State.WALKING; }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) { moveX -= SPEED * delta; currentDirection = Direction.LEFT; currentState = State.WALKING; }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) { moveX += SPEED * delta; currentDirection = Direction.RIGHT; currentState = State.WALKING; }

        position.x += moveX;
        if (checkCollisionAtPoints(position.x, position.y)) {
            position.x = oldX;
        }

        position.y += moveY;
        if (checkCollisionAtPoints(position.x, position.y)) {
            position.y = oldY;
        }


        this.x = position.x;
        this.y = position.y;
        stateTime += delta;
    }

    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame;

        if (currentState == State.WALKING) {
            switch (currentDirection) {
                case UP: currentFrame = walkUp.getKeyFrame(stateTime); break;
                case DOWN: currentFrame = walkDown.getKeyFrame(stateTime); break;
                case LEFT: currentFrame = walkLeft.getKeyFrame(stateTime); break;
                case RIGHT: currentFrame = walkRight.getKeyFrame(stateTime); break;
                default: currentFrame = walkDown.getKeyFrame(stateTime); break;
            }
        }
        else {
            switch (currentDirection) {
                case UP:    currentFrame = idleUp.getKeyFrame(stateTime); break;
                case DOWN:  currentFrame = idleDown.getKeyFrame(stateTime); break;
                case LEFT:  currentFrame = idleLeft.getKeyFrame(stateTime); break;
                case RIGHT: currentFrame = idleRight.getKeyFrame(stateTime); break;
                default:    currentFrame = idleDown.getKeyFrame(stateTime); break;
            }
        }

        batch.draw(currentFrame, position.x, position.y, width, height);
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

    private boolean checkCollisionAtPoints(float x, float y) {
        float pX = 20f;
        float pY = 10f;

        return (isCollision(x + pX, y + pY) || isCollision(x + width - pX, y + pY) ||
                isCollision(x + pX, y + height / 2) || isCollision(x + width - pX, y + height / 2));
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public Rectangle getBounds() {
        // 64 (chiều rộng nhân vật) - 20 (chiều rộng khung) = 44
        // Chia đôi ra là 22 để khung nằm chính giữa theo chiều ngang
        float offsetX = (width - 20) / 2f;

        // Y giữ nguyên hoặc cộng thêm một chút nếu muốn khung nằm ở chân
        float offsetY = 5f;

        return new Rectangle(position.x + offsetX, position.y + offsetY, 20, 20);
    }

    // Sửa Item thành Entity để dùng chung cho cả NPC và Vật chứng
    public boolean isNear(Entity entity) {
        if (entity == null) return false;

        float centerX = this.x + getWidth() / 2;
        float centerY = this.y + getHeight() / 2;

        float targetX = entity.getX() + entity.getWidth() / 2;
        float targetY = entity.getY() + entity.getHeight() / 2;

        float distance = Vector2.dst(centerX, centerY, targetX, targetY);

        return distance < 50f;
    }

    public void setMap(TiledMap newMap) {
        this.map = newMap;
    }

    public void setPosition(float x, float y) {
        position.x = x;
        position.y = y;
    }

    public void dispose() {
        sprite.getTexture().dispose();
    }
}
