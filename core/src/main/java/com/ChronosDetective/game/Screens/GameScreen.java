package com.ChronosDetective.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.ChronosDetective.game.ChronosDetectiveGame;
import com.ChronosDetective.game.Entities.Item;
import com.ChronosDetective.game.Entities.Player;
import com.ChronosDetective.game.Managers.DialogueManager;
import com.ChronosDetective.game.Managers.EntityManager;
import com.ChronosDetective.game.Managers.InventoryManager;
import com.ChronosDetective.game.Managers.MapManager;
import com.ChronosDetective.game.UI.InventoryUI;
import com.ChronosDetective.game.Save.SaveData;
import com.ChronosDetective.game.Save.SaveRepository;
import com.ChronosDetective.game.Save.SaveSessionMeta;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class GameScreen implements Screen {
    private SpriteBatch batch;
    private Player player;

    private OrthographicCamera camera;
    private FitViewport viewport;

    private EntityManager entityManager;
    private DialogueManager dialogueManager;
    private InventoryManager inventoryManager;
    private MapManager mapManager;

    private Sprite pointerSprite;

    private Stage stage;
    private InventoryUI inventoryUI;
    private FitViewport uiViewport;

    private final ChronosDetectiveGame game;
    private final SaveRepository saves = new SaveRepository();
    private final String sessionId;
    private final boolean loadOnStart;

    private Stage uiStage;
    private VisDialog exitConfirmDialog;
    private VisDialog saveDialog;

    public GameScreen(ChronosDetectiveGame game) {
        this(game, new SaveRepository().createNewSessionId(), false);
    }

    public GameScreen(ChronosDetectiveGame game, String sessionId, boolean loadOnStart) {
        this.game = game;
        this.sessionId = sessionId;
        this.loadOnStart = loadOnStart;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        // 1. Setup Camera & Viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);

        uiViewport = new FitViewport(800, 480);
        stage = new Stage(uiViewport, batch);
        // UI overlay (ESC confirm)
        uiStage = new Stage(new FitViewport(800, 480));
        Gdx.input.setInputProcessor(new InputMultiplexer(uiStage));


        // 3. Load Mũi tên trên item và npc
        Texture arrowTex = new Texture("arrow.png");
        pointerSprite = new Sprite(arrowTex);
        pointerSprite.setSize(16, 16);

        // 4. Khởi tạo Managers
        dialogueManager = new DialogueManager(viewport, camera);
        entityManager = new EntityManager(pointerSprite);
        inventoryManager= new InventoryManager();
        mapManager = new MapManager(entityManager);


        // 5. Khởi tạo Player
        Texture playerTexture = new Texture("player_animation.png");
        player = new Player(playerTexture, 100, 100, null); // Cho player đứng ở (100,100)

        mapManager.loadMap("map.tmx", player, 100, 100);

        camera.zoom = 0.8f;
        camera.update();

        Gdx.input.setInputProcessor(stage);

        inventoryUI = new InventoryUI(stage);
        //Texture butlerTex = new Texture("butler.png");
        //entityManager.addNPC(new NPC(butlerTex, 400, 300, "Quan gia", "Toi da thay mot bong den..."));

        if (loadOnStart) {
            SaveData data = saves.loadGame(sessionId);
            if (data != null) {
                player.setPosition(data.playerX, data.playerY);
            }
        }
    }


    @Override
    public void render(float delta) {
        stage.act(delta);
        // O -> open save dialog
        if (Gdx.input.isKeyJustPressed(Input.Keys.O)) {
            if (saveDialog == null || !saveDialog.isVisible()) {
                showSaveDialog();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            inventoryUI.toggle(inventoryManager);
        }
        // ESC -> show confirm dialog
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (exitConfirmDialog == null || !exitConfirmDialog.isVisible()) {
                showExitConfirm();
            }
        }

        boolean isExitDialogOpen = exitConfirmDialog != null && exitConfirmDialog.getStage() != null;
        boolean isSaveDialogOpen = saveDialog != null && saveDialog.getStage() != null;
        boolean isAnyOverlayOpen = isExitDialogOpen || isSaveDialogOpen;

        // 2. Cập nhật logic (Quan trọng!)
        if (!dialogueManager.isActive() && !inventoryUI.isVisible()) { // Chỉ cập nhật khi hộp thoại và inventory không hoạt động
            player.update(delta);

            // KIỂM TRA CHUYỂN MAP Ở ĐÂY
            mapManager.checkPortals(player, (targetMap, x, y) -> {
                mapManager.loadMap(targetMap, player, x, y);
            });
        }
        entityManager.update(delta, player, dialogueManager, inventoryManager);

        if (!dialogueManager.isActive() && !isAnyOverlayOpen) player.update(delta); // Chỉ cập nhật khi hộp thoại không hoạt động
        if (!isAnyOverlayOpen) entityManager.update(delta, player, dialogueManager, inventoryManager);

        // 3. Cập nhật Camera đuổi theo nhân vật
        float lerp = 0.1f; // Tốc độ đuổi theo (0.1 là khá mượt)
        camera.position.x += (player.getX() - camera.position.x) * lerp;
        camera.position.y += (player.getY() - camera.position.y) * lerp;
        camera.update();

        // Xóa màn hình
        Gdx.gl.glClearColor(0.4f, 0.6f, 0.6f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Vẽ map
        if (mapManager.getRenderer() != null) {
            mapManager.getRenderer().setView(camera);
            mapManager.getRenderer().render();
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
            player.draw(batch);
            entityManager.draw(batch, player);
        batch.end();


        // Trong GameScreen.render() sau khi vẽ batch.end()
        ShapeRenderer debugRenderer = new ShapeRenderer();
        debugRenderer.setProjectionMatrix(camera.combined);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        debugRenderer.setColor(Color.RED);

        // Vẽ thử cái khung của Portal
        MapLayer layer = mapManager.getCurrentMap().getLayers().get("Portals");
        for (MapObject obj : layer.getObjects()) {
            if (obj instanceof RectangleMapObject) {
                Rectangle r = ((RectangleMapObject) obj).getRectangle();
                debugRenderer.rect(r.x, r.y, r.width, r.height);
            }
        }

        debugRenderer.setColor(Color.BLUE); // Màu xanh cho thám tử
        Rectangle pb = player.getBounds();
        debugRenderer.rect(pb.x, pb.y, pb.width, pb.height);
        debugRenderer.end();


        // 3. Vẽ UI (Hộp thoại trên cùng)
        dialogueManager.draw(batch);

        // Vẽ uiInventory lên trên cùng nếu đang mở
        stage.draw();
        // 4. Vẽ overlay UI (ESC confirm)
        uiStage.act(delta);
        uiStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        uiStage.getViewport().update(width, height, true);
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
        dialogueManager.dispose();
        pointerSprite.getTexture().dispose();
        stage.dispose();
        uiStage.dispose();
    }

    private void showExitConfirm() {
        exitConfirmDialog = new VisDialog("Thoát game?") {
            @Override
            protected void result(Object object) {
                try {
                    boolean shouldExitToMenu = object instanceof Boolean && (Boolean) object;
                    if (shouldExitToMenu) {
                        game.setScreen(new MenuScreen(game));
                        return;
                    }
                } finally {
                    hide();
                    exitConfirmDialog = null;
                }
            }
        };
        exitConfirmDialog.text("Bạn có muốn thoát game không?");
        exitConfirmDialog.button("Có", true);
        exitConfirmDialog.button("Tiếp tục", false);
        exitConfirmDialog.setModal(true);
        exitConfirmDialog.setMovable(false);
        exitConfirmDialog.show(uiStage);
    }

    private void showSaveDialog() {
        final ArrayList<SaveSessionMeta> sessions = saves.listSessionsNewestFirst();

        boolean hasCurrent = false;
        for (SaveSessionMeta m : sessions) {
            if (m != null && sessionId.equals(m.id)) {
                hasCurrent = true;
                break;
            }
        }
        if (!hasCurrent) sessions.add(0, new SaveSessionMeta(sessionId, 0));

        final VisList<String> list = new VisList<>();
        Array<String> items = new Array<>();
        for (SaveSessionMeta s : sessions) items.add(formatSessionLine(s));
        list.setItems(items);

        int currentIdx = 0;
        for (int i = 0; i < sessions.size(); i++) {
            if (sessionId.equals(sessions.get(i).id)) {
                currentIdx = i;
                break;
            }
        }
        list.setSelectedIndex(currentIdx);

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFadeScrollBars(false);

        saveDialog = new VisDialog("Save game") {
            @Override
            protected void result(Object object) {
                try {
                    boolean shouldSave = object instanceof Boolean && (Boolean) object;
                    if (!shouldSave) return;

                    int idx = list.getSelectedIndex();
                    String targetSession = sessionId;
                    if (idx >= 0 && idx < sessions.size()) targetSession = sessions.get(idx).id;

                    SaveData data = new SaveData();
                    data.sessionId = targetSession;
                    data.playerX = player.getX();
                    data.playerY = player.getY();
                    data.savedAtEpochMs = System.currentTimeMillis();
                    saves.saveGame(data);
                } finally {
                    hide();
                    saveDialog = null;
                }
            }
        };
        saveDialog.setModal(true);
        saveDialog.setMovable(false);

        saveDialog.getContentTable().add(new VisLabel("Chọn session để lưu (mặc định: session hiện tại):")).left().padBottom(6).row();
        saveDialog.getContentTable().add(scroll).width(560).height(220).row();

        saveDialog.button("Lưu", true);
        saveDialog.button("Tiếp tục", false);
        saveDialog.show(uiStage);
    }

    private String formatSessionLine(SaveSessionMeta meta) {
        String time = meta.lastSavedAtEpochMs > 0
                ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(meta.lastSavedAtEpochMs))
                : "chưa lưu";
        String prefix = sessionId.equals(meta.id) ? "[CURRENT] " : "";
        return prefix + meta.id + "   |   " + time;
    }
}
