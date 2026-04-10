package com.ChronosDetective.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle; // Đã sửa để hết bị unused
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;   // Đã sửa để hết bị unused
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.ChronosDetective.game.ChronosDetectiveGame;
import com.ChronosDetective.game.Save.SaveRepository;
import com.ChronosDetective.game.Save.SaveSessionMeta;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisList;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MenuScreen implements Screen {
    private final ChronosDetectiveGame game;
    private Stage stage;
    private final SaveRepository saves = new SaveRepository();

    private BitmapFont menuFont;
    private static final String VIETNAMESE_CHARS = "áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđÁÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬÉÈẺẼẸÊẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰÝỲỶỸỊĐ";

    public MenuScreen (ChronosDetectiveGame game) {
        this.game = game;

        // 1. Khởi tạo Font Tiếng Việt và nạp vào Skin hệ thống
        setupMenuFont();

        this.stage = new Stage(new FitViewport(800, 480));
        Gdx.input.setInputProcessor(stage);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // 2. Các nút bấm sử dụng Tiếng Việt trực tiếp
        VisTextButton newGameBtn = new VisTextButton("CHƠI MỚI");
        VisTextButton continueBtn = new VisTextButton("TIẾP TỤC");
        VisTextButton loadBtn = new VisTextButton("TẢI DỮ LIỆU");
        VisTextButton exitBtn = new VisTextButton("THOÁT GAME");

        // --- Cài đặt Listener ---
        newGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String sessionId = saves.createNewSessionId();
                saves.setLastSessionId(sessionId);
                game.setScreen(new GameScreen(game, sessionId, false));
            }
        });

        continueBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String last = saves.getLastSessionId();
                if (last != null && saves.hasSession(last)) {
                    game.setScreen(new GameScreen(game, last, true));
                } else {
                    String sessionId = saves.createNewSessionId();
                    saves.setLastSessionId(sessionId);
                    game.setScreen(new GameScreen(game, sessionId, false));
                }
            }
        });

        loadBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showLoadDialog();
            }
        });

        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        // Thêm vào bảng điều hướng
        table.add(newGameBtn).width(220).pad(10);
        table.row();
        table.add(continueBtn).width(220).pad(10);
        table.row();
        table.add(loadBtn).width(220).pad(10);
        table.row();
        table.add(exitBtn).width(220).pad(10);
    }

    private void setupMenuFont() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arial.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 28;
        parameter.borderWidth = 1.5f;
        parameter.borderColor = Color.BLACK;
        parameter.magFilter = Texture.TextureFilter.Linear;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + VIETNAMESE_CHARS;

        menuFont = generator.generateFont(parameter);
        generator.dispose();

        if (VisUI.isLoaded()) {
            com.badlogic.gdx.scenes.scene2d.ui.Skin skin = VisUI.getSkin();
            skin.add("default-font", menuFont, BitmapFont.class);

            // --- THIẾT KẾ BO GÓC KHÔNG CẦN IMPORT THÊM ---

            // 1. Tạo nền Dialog (Bo góc 20px)
            Drawable dialogBg = createRoundedDrawable(100, 100, 20, new Color(0.05f, 0.05f, 0.1f, 0.88f));

            // Thay vì dùng VisDialogStyle (lỗi), ta lấy trực tiếp WindowStyle từ Skin
            // VisDialog sử dụng chung style với Window
            skin.get(com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle.class).background = dialogBg;
            skin.get(com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle.class).titleFont = menuFont;

            // 2. Tạo nền Nút bấm bo góc (12px)
            Drawable btnUp = createRoundedDrawable(100, 40, 12, new Color(0.15f, 0.15f, 0.25f, 1f));
            Drawable btnOver = createRoundedDrawable(100, 40, 12, new Color(0.25f, 0.25f, 0.45f, 1f));

            VisTextButton.VisTextButtonStyle buttonStyle = skin.get(VisTextButton.VisTextButtonStyle.class);
            buttonStyle.up = btnUp;
            buttonStyle.over = btnOver;
            buttonStyle.font = menuFont;

            // 3. Cập nhật Font cho Label và List (Sử dụng các class bạn đã import)
            skin.get(LabelStyle.class).font = menuFont;
            skin.get(ListStyle.class).font = menuFont;
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        // Không dispose menuFont ở đây vì Skin có thể vẫn cần dùng nó
    }

    private void showLoadDialog() {
        ArrayList<SaveSessionMeta> sessions = saves.listSessionsNewestFirst();

        final VisList<String> list = new VisList<>();
        VisDialog dialog = new VisDialog("DANH SÁCH LƯU") {
            @Override
            protected void result(Object object) {
                boolean shouldLoad = object instanceof Boolean && (Boolean) object;
                if (!shouldLoad) return;

                int idx = list.getSelectedIndex();
                if (idx >= 0 && idx < sessions.size()) {
                    String sessionId = sessions.get(idx).id;
                    saves.setLastSessionId(sessionId);
                    game.setScreen(new GameScreen(game, sessionId, true));
                }
            }
        };
        dialog.setModal(true);
        dialog.setMovable(false);

        if (sessions.isEmpty()) {
            dialog.text("Hiện chưa có dữ liệu thám tử nào.");
            dialog.button("Quay lại", false);
            dialog.show(stage);
            return;
        }

        Array<String> items = new Array<>();
        for (SaveSessionMeta s : sessions) {
            items.add(formatSessionLine(s));
        }

        list.setItems(items);
        list.setSelectedIndex(0);

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFadeScrollBars(false);

        dialog.getContentTable().add(new VisLabel("Chọn hồ sơ vụ án:")).left().padBottom(6).row();
        dialog.getContentTable().add(scroll).width(550).height(250).row();

        dialog.button("Tiếp tục hồ sơ", true);
        dialog.button("Hủy", false);

        dialog.show(stage);
    }

    private String formatSessionLine(SaveSessionMeta meta) {
        String time = meta.lastSavedAtEpochMs > 0
            ? new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(meta.lastSavedAtEpochMs))
            : "Chưa lưu";
        return "ID: " + meta.id + "   -   Ngày: " + time;
    }
    private Drawable createRoundedDrawable(int width, int height, int radius, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);

        // Vẽ 2 hình chữ nhật chồng lên nhau tạo khung chữ thập
        pixmap.fillRectangle(0, radius, width, height - 2 * radius);
        pixmap.fillRectangle(radius, 0, width - 2 * radius, height);

        // Vẽ 4 góc hình tròn
        pixmap.fillCircle(radius, radius, radius);
        pixmap.fillCircle(width - radius, radius, radius);
        pixmap.fillCircle(radius, height - radius, radius);
        pixmap.fillCircle(width - radius, height - radius, radius);

        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear); // Làm mịn viền
        pixmap.dispose();

        return new TextureRegionDrawable(new TextureRegion(texture));
    }
}
