package com.evocraft.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.evocraft.game.ChronosDetectiveGame;
import com.evocraft.game.Entities.Item;
import com.evocraft.game.Entities.Player;
import com.evocraft.game.Managers.DialogueManager;
import com.evocraft.game.Managers.EntityManager;
import com.evocraft.game.Managers.InventoryManager;
import com.evocraft.game.UI.InventoryUI;

public class GameScreen implements Screen {
    private SpriteBatch batch;
    private Player player;

    private OrthographicCamera camera;
    private FitViewport viewport;

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;

    private EntityManager entityManager;
    private DialogueManager dialogueManager;
    private InventoryManager inventoryManager;

    private Sprite pointerSprite;

    private Stage stage;
    private InventoryUI inventoryUI;
    private FitViewport uiViewport;

    private final ChronosDetectiveGame game;

    public GameScreen(ChronosDetectiveGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        // 1. Setup Camera & Viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);

        uiViewport = new FitViewport(800, 480);
        stage = new Stage(uiViewport, batch);

        // 2. Load Map
        map = new TmxMapLoader().load("map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);

        // 3. Load Mũi tên trên item và npc
        Texture arrowTex = new Texture("arrow.png");
        pointerSprite = new Sprite(arrowTex);
        pointerSprite.setSize(16, 16);

        // 4. Khởi tạo Managers
        dialogueManager = new DialogueManager(viewport, camera);
        entityManager = new EntityManager(pointerSprite);
        inventoryManager= new InventoryManager();

        // 5. Khởi tạo Player
        Texture playerTexture = new Texture("player_animation.png");
        player = new Player(playerTexture, 100, 100, map); // Cho player đứng ở (100,100)

        camera.zoom = 0.8f;
        camera.update();

        // 6. Thêm Item và NPC mẫu (Ví dụ)
        Texture appleTex = new Texture("apple.png");
        entityManager.addItem(new Item(appleTex, 200, 250, "Qua tao"));

        //Texture butlerTex = new Texture("butler.png");
        //entityManager.addNPC(new NPC(butlerTex, 400, 300, "Quan gia", "Toi da thay mot bong den..."));

        Gdx.input.setInputProcessor(stage);

        inventoryUI = new InventoryUI(stage);
    }


    @Override
    public void render(float delta) {
        stage.act(delta);

        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            inventoryUI.toggle(inventoryManager);
        }

        // 2. Cập nhật logic (Quan trọng!)
        if (!dialogueManager.isActive() && !inventoryUI.isVisible()) { // Chỉ cập nhật khi hộp thoại và inventory không hoạt động
            player.update(delta);
        }
        entityManager.update(delta, player, dialogueManager, inventoryManager);


        // 3. Cập nhật Camera đuổi theo nhân vật
        float lerp = 0.1f; // Tốc độ đuổi theo (0.1 là khá mượt)
        camera.position.x += (player.getX() - camera.position.x) * lerp;
        camera.position.y += (player.getY() - camera.position.y) * lerp;
        camera.update();

        // Xóa màn hình
        Gdx.gl.glClearColor(0.4f, 0.6f, 0.6f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 4. Vẽ Bản đồ
        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
            player.draw(batch);
            entityManager.draw(batch, player);
        batch.end();

        // 3. Vẽ UI (Hộp thoại trên cùng)
        dialogueManager.draw(batch);

        // Vẽ uiInventory lên trên cùng nếu đang mở
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        // Hàm này gọi khi bạn chuyển sang Screen khác
        // Thường dùng để gọi dispose() hoặc dừng nhạc
    }

    @Override
    public void dispose() {
        batch.dispose();
        player.dispose();
        map.dispose();
        dialogueManager.dispose();
        pointerSprite.getTexture().dispose();
        stage.dispose();
    }
}
