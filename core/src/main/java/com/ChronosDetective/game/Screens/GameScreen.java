package com.ChronosDetective.game.Screens;

import com.badlogic.gdx.Gdx;
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
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private EntityManager entityManager;
    private DialogueManager dialogueManager;
    private Sprite pointerSprite;

    private final ChronosDetectiveGame game;
    private final SaveRepository saves = new SaveRepository();
    private final String sessionId;
    private final boolean loadOnStart;

    private Stage uiStage;
    private VisDialog exitConfirmDialog;
    private VisDialog saveDialog;

    // BIẾN CHO FONT VÀ TIẾNG VIỆT
    private BitmapFont gameUiFont;
    private static final String VIETNAMESE_CHARS = "áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđÁÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬÉÈẺẼẸÊẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰÝỲỶỸỊĐ";

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
        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);

        uiStage = new Stage(new FitViewport(800, 480));
        Gdx.input.setInputProcessor(new InputMultiplexer(uiStage));

        // Khởi tạo Font và Giao diện trước khi tạo Dialog
        setupGameUiFont();

        map = new TmxMapLoader().load("map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);

        Texture arrowTex = new Texture("arrow.png");
        pointerSprite = new Sprite(arrowTex);
        pointerSprite.setSize(16, 16);

        dialogueManager = new DialogueManager(viewport, camera);
        entityManager = new EntityManager(pointerSprite);

        Texture playerTexture = new Texture("player2.png");
        player = new Player(playerTexture, 100, 100, map);

        camera.zoom = 0.8f;
        camera.update();

        Texture appleTex = new Texture("apple.png");
        entityManager.addItem(new Item(appleTex, 200, 250, "Quả táo"));

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
        if (Gdx.input.isKeyJustPressed(Input.Keys.O)) {
            if (saveDialog == null || !saveDialog.isVisible()) showSaveDialog();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (exitConfirmDialog == null || !exitConfirmDialog.isVisible()) showExitConfirm();
        }

        boolean isAnyOverlayOpen = (exitConfirmDialog != null && exitConfirmDialog.getStage() != null)
            || (saveDialog != null && saveDialog.getStage() != null);

        if (!dialogueManager.isActive() && !isAnyOverlayOpen) player.update(delta);
        if (!isAnyOverlayOpen) entityManager.update(delta, player, dialogueManager);

        float lerp = 0.1f;
        camera.position.x += (player.getX() - camera.position.x) * lerp;
        camera.position.y += (player.getY() - camera.position.y) * lerp;
        camera.update();

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        player.draw(batch);
        entityManager.draw(batch, player);
        batch.end();

        dialogueManager.draw(batch);

        uiStage.act(delta);
        uiStage.draw();
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

    @Override public void resize(int width, int height) { viewport.update(width, height); uiStage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { batch.dispose(); player.dispose(); map.dispose(); dialogueManager.dispose(); pointerSprite.getTexture().dispose(); uiStage.dispose(); if(gameUiFont != null) gameUiFont.dispose(); }
}
