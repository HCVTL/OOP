package com.ChronosDetective.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
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
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
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
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisList;
import com.kotcrab.vis.ui.widget.VisTextButton;

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

    private ShapeRenderer debugRenderer;

    // BIẾN CHO FONT VÀ TIẾNG VIỆT
    private BitmapFont gameUiFont;
    private static final String VIETNAMESE_CHARS = "áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđÁÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬÉÈẺẼẸÊẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰÝỲỶỸỊĐ";

    public GameScreen(ChronosDetectiveGame game) {
        this(game, new SaveRepository().createNewSessionId(), false);
    }
    private boolean showSimpleInventory = false;
    private int selectedSlot = 0;
    private Texture appleIcon;
    public GameScreen(ChronosDetectiveGame game, String sessionId, boolean loadOnStart) {
        this.game = game;
        this.sessionId = sessionId;
        this.loadOnStart = loadOnStart;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        debugRenderer = new ShapeRenderer();

        // 1. Setup Camera & Viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);

        uiViewport = new FitViewport(800, 480);
        stage = new Stage(uiViewport, batch);
        // UI overlay (ESC confirm)
        uiStage = new Stage(uiViewport, batch);
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(uiStage);
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);

        // Khởi tạo Font và Giao diện trước khi tạo Dialog
        setupGameUiFont();

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

    private Drawable createSolidBackground(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    private void setupGameUiFont() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arial.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 22;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + VIETNAMESE_CHARS;
        parameter.magFilter = Texture.TextureFilter.Linear;
        parameter.minFilter = Texture.TextureFilter.Linear;

        gameUiFont = generator.generateFont(parameter);
        generator.dispose();

        if (VisUI.isLoaded()) {
            com.badlogic.gdx.scenes.scene2d.ui.Skin skin = VisUI.getSkin();
            skin.add("default-font", gameUiFont, BitmapFont.class);

            // Nền đen tuyền không bo góc để tránh loang lổ
            Drawable blackBg = createSolidBackground(new Color(0, 0, 0, 1f));
            Drawable buttonUp = createSolidBackground(new Color(0.15f, 0.15f, 0.15f, 1f));
            Drawable buttonOver = createSolidBackground(new Color(0.25f, 0.25f, 0.25f, 1f));

            WindowStyle windowStyle = skin.get(WindowStyle.class);
            windowStyle.background = blackBg;
            windowStyle.titleFont = gameUiFont;

            TextButtonStyle btnStyle = skin.get(TextButtonStyle.class);
            btnStyle.up = buttonUp;
            btnStyle.over = buttonOver;
            btnStyle.font = gameUiFont;

            skin.get(LabelStyle.class).font = gameUiFont;
            skin.get(ListStyle.class).font = gameUiFont;
        }
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        // O -> open save dialog
        if (Gdx.input.isKeyJustPressed(Input.Keys.O)) showSaveDialog();

        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) inventoryUI.toggle(inventoryManager);
        // ESC -> show confirm dialog
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) showExitConfirm();
    // bấm I để mở
    if (Gdx.input.isKeyJustPressed(Input.Keys.I)) { 
            showSimpleInventory = !showSimpleInventory;
        }
        if (showSimpleInventory) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) selectedSlot = (selectedSlot + 1) % 8;
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) selectedSlot = (selectedSlot - 1 + 8) % 8;
        }

       // 1. Chia làm 2 loại khóa: Khóa di chuyển và Khóa hệ thống
        boolean isMenuOpen = (exitConfirmDialog != null && exitConfirmDialog.isVisible())
                || (saveDialog != null && saveDialog.isVisible());

        boolean isMovementBlocked = isMenuOpen 
                || inventoryUI.isVisible() 
                || dialogueManager.isActive() 
                || showSimpleInventory;

        // 2. Cập nhật Player & Portal (Chặn khi đang nói chuyện/mở túi đồ)
        if (!isMovementBlocked) {
            player.update(delta);
            mapManager.checkPortals(player, (targetMap, x, y) -> {
                mapManager.loadMap(targetMap, player, x, y);
            });
        }

        // 3. Cập nhật Tương tác (Phải chạy khi đang nói chuyện để bấm E mà đóng)
        if (!isMenuOpen) {
            entityManager.update(delta, player, dialogueManager, inventoryManager);
        }
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
        stage.draw();
        // 4. Vẽ overlay UI (ESC confirm)
        uiStage.act(delta);
        uiStage.draw();
        if (showSimpleInventory) {
            drawSimpleInventoryUI();
        }
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
        debugRenderer.dispose();
    }

    private void showExitConfirm() {
        exitConfirmDialog = new VisDialog("XÁC NHẬN") {
            @Override
            protected void result(Object object) {
                if (object instanceof Boolean && (Boolean) object) game.setScreen(new MenuScreen(game));
                exitConfirmDialog = null;
            }
        };

        // 1. Chỉnh bảng nội dung
        Table content = exitConfirmDialog.getContentTable();
        content.clear();
        // Tăng Pad Bottom để chừa chỗ cho các nút bấm chui vào trong
        content.pad(40, 50, 80, 50);

        VisLabel msg = new VisLabel("Bạn có muốn tạm dừng điều tra\nvà quay về Menu chính không?");
        msg.setAlignment(Align.center);
        msg.setWrap(true);
        content.add(msg).width(450).center().row();

        // 2. Thêm nút bấm theo cách chuẩn của Dialog
        VisTextButton yesBtn = new VisTextButton("THOÁT GAME");
        VisTextButton noBtn = new VisTextButton("TIẾP TỤC");

        exitConfirmDialog.button(yesBtn, true);
        exitConfirmDialog.button(noBtn, false);

        // 3. ĐƯA CÁC NÚT VÀO TRONG KHUNG
        // Lấy bảng nút mặc định của Dialog
        Table bTable = exitConfirmDialog.getButtonsTable();
        // Đẩy bảng nút lên trên một chút để nó nằm trong background đen
        bTable.padBottom(30);

        bTable.getCell(yesBtn).width(180).height(50).padRight(20);
        bTable.getCell(noBtn).width(180).height(50);

        exitConfirmDialog.show(uiStage);
    }

    private void showSaveDialog() {
        final ArrayList<SaveSessionMeta> sessions = saves.listSessionsNewestFirst();

        boolean hasCurrent = false;
        for (SaveSessionMeta m : sessions) {
            if (m != null && sessionId.equals(m.id)) { hasCurrent = true; break; }
        }
        if (!hasCurrent) sessions.add(0, new SaveSessionMeta(sessionId, 0));

        final VisList<String> list = new VisList<>();
        Array<String> items = new Array<>();
        for (SaveSessionMeta s : sessions) items.add(formatSessionLine(s));
        list.setItems(items);

        int currentIdx = 0;
        for (int i = 0; i < sessions.size(); i++) {
            if (sessionId.equals(sessions.get(i).id)) { currentIdx = i; break; }
        }
        list.setSelectedIndex(currentIdx);

        saveDialog = new VisDialog("LƯU GAME") {
            @Override
            protected void result(Object object) {
                if (object instanceof Boolean && (Boolean) object) {
                    int idx = list.getSelectedIndex();
                    String targetSession = (idx >= 0 && idx < sessions.size()) ? sessions.get(idx).id : sessionId;
                    SaveData data = new SaveData();
                    data.sessionId = targetSession;
                    data.playerX = player.getX();
                    data.playerY = player.getY();
                    data.savedAtEpochMs = System.currentTimeMillis();
                    saves.saveGame(data);
                }
                saveDialog = null;
            }
        };

        Table content = saveDialog.getContentTable();
        content.pad(30, 40, 30, 40);
        content.add(new VisLabel("Chọn hồ sơ để lưu dữ liệu:")).left().padBottom(10).row();

        ScrollPane scroll = new ScrollPane(list);
        content.add(scroll).width(500).height(200).padBottom(20).row();

        Table btnTable = new Table();
        VisTextButton saveBtn = new VisTextButton("XÁC NHẬN LƯU");
        VisTextButton cancelBtn = new VisTextButton("HỦY");
        btnTable.add(saveBtn).width(200).height(45).padRight(15);
        btnTable.add(cancelBtn).width(150).height(45);

        saveDialog.button(saveBtn, true);
        saveDialog.button(cancelBtn, false);
        content.add(btnTable).center();

        saveDialog.setModal(true);
        saveDialog.show(uiStage);
    }

    private String formatSessionLine(SaveSessionMeta meta) {
        String time = meta.lastSavedAtEpochMs > 0
            ? new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(meta.lastSavedAtEpochMs))
            : "Mới";
        String prefix = sessionId.equals(meta.id) ? "[HIỆN TẠI] " : "";
        return prefix + meta.id + " | " + time;
    }
    //vẽ tui đồ
   private void drawSimpleInventoryUI() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        debugRenderer.setProjectionMatrix(uiViewport.getCamera().combined);
        batch.setProjectionMatrix(uiViewport.getCamera().combined);

        float pW = 340, pH = 400; 
        float sX = (800 - pW) / 2;
        float sY = (480 - pH) / 2;

        debugRenderer.begin(ShapeRenderer.ShapeType.Filled);
        debugRenderer.setColor(new Color(0, 0, 0, 0.85f));
        debugRenderer.rect(sX, sY, pW, pH);
        debugRenderer.setColor(new Color(0.15f, 0.15f, 0.15f, 1));
        debugRenderer.rect(sX + 15, sY + 185, pW - 30, 195); 
        debugRenderer.end();

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

        batch.begin();
        if (appleIcon == null) appleIcon = new Texture("apple.png");
        
        ArrayList<String> items = player.getInventory(); 
        
        // 1. XỬ LÝ TÊN HIỂN THỊ (VIỆT HÓA)
        if (selectedSlot < items.size()) {
            String rawName = items.get(selectedSlot).toLowerCase();
            String displayName = rawName; // Mặc định dùng tên gốc

            // Nếu trong code là "apple" hoặc "qua tao" thì hiện "QUẢ TÁO"
            if (rawName.contains("apple") || rawName.contains("tao")) {
                displayName = "QUẢ TÁO";
            }

            gameUiFont.setColor(Color.GOLD);
            gameUiFont.draw(batch, "VẬT CHỨNG: " + displayName, sX + 30, sY + 360);
        }

        // 2. XỬ LÝ VẼ HÌNH ẢNH
        for (int i = 0; i < items.size(); i++) {
            int row = i / 4; int col = i % 4;
            float x = gX + col * (slotSize + pad);
            float y = gY + (1 - row) * (slotSize + pad);
            
            String itemName = items.get(i).toLowerCase();

            // Nếu tên chứa "apple" HOẶC "tao" thì đều vẽ hình quả táo
            if (itemName.contains("apple") || itemName.contains("tao")) {
                batch.draw(appleIcon, x + 5, y + 5, slotSize - 10, slotSize - 10);
            }
        }
        batch.end();
    }
}