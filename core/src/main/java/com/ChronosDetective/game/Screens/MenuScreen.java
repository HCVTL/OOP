package com.ChronosDetective.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle; // Đã sửa để hết bị unused
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;   // Đã sửa để hết bị unused
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
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
        // Nạp font từ file arial.ttf (Lưu ý đường dẫn fonts/arial.ttf trong assets)
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arial.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 26;
        parameter.borderWidth = 1.2f;
        parameter.borderColor = Color.BLACK;
        parameter.magFilter = Texture.TextureFilter.Linear;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + VIETNAMESE_CHARS;

        menuFont = generator.generateFont(parameter);
        generator.dispose();

        // Ép font vào VisUI Skin
        if (VisUI.isLoaded()) {
            VisUI.getSkin().add("default-font", menuFont, BitmapFont.class);

            // Cập nhật Style cho Button
            VisTextButton.VisTextButtonStyle buttonStyle = VisUI.getSkin().get(VisTextButton.VisTextButtonStyle.class);
            buttonStyle.font = menuFont;

            // Cập nhật Style cho Label (Làm sáng dòng import LabelStyle)
            LabelStyle labelStyle = VisUI.getSkin().get(LabelStyle.class);
            labelStyle.font = menuFont;

            // Cập nhật Style cho List (Làm sáng dòng import ListStyle)
            ListStyle listStyle = VisUI.getSkin().get(ListStyle.class);
            listStyle.font = menuFont;
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
}
