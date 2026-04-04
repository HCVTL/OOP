package com.ChronosDetective.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.ChronosDetective.game.ChronosDetectiveGame;
import com.ChronosDetective.game.Save.SaveRepository;
import com.ChronosDetective.game.Save.SaveSessionMeta;
import com.badlogic.gdx.utils.Array;
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

    public MenuScreen (ChronosDetectiveGame game) {
        this.game = game;
        this.stage = new Stage(new FitViewport(800, 480));
        Gdx.input.setInputProcessor(stage);

        Table table = new Table();
        table.setFillParent(true); // Bảng bao phủ toàn màn hình
        stage.addActor(table);

        VisTextButton newGameBtn = new VisTextButton("NEW GAME");
        VisTextButton continueBtn = new VisTextButton("CONTINUE");
        VisTextButton loadBtn = new VisTextButton("LOAD");
        VisTextButton exitBtn = new VisTextButton("EXIT");

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

        table.add(newGameBtn).fillX().uniformX().pad(10);
        table.row(); // Xuống dòng
        table.add(continueBtn).fillX().uniformX().pad(10);
        table.row();
        table.add(loadBtn).fillX().uniformX().pad(10);
        table.row();
        table.add(exitBtn).fillX().uniformX().pad(10);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { stage.dispose(); }

    private void showLoadDialog() {
        ArrayList<SaveSessionMeta> sessions = saves.listSessionsNewestFirst();

        final VisList<String> list = new VisList<>();
        VisDialog dialog = new VisDialog("Load session") {
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
            dialog.text("Chưa có session nào để load.");
            dialog.button("Đóng", false);
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

        dialog.getContentTable().add(new VisLabel("Chọn session:")).left().padBottom(6).row();
        dialog.getContentTable().add(scroll).width(520).height(220).row();

        dialog.button("Load", true);
        dialog.button("Hủy", false);

        dialog.show(stage);
    }

    private String formatSessionLine(SaveSessionMeta meta) {
        String time = meta.lastSavedAtEpochMs > 0
                ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(meta.lastSavedAtEpochMs))
                : "chưa lưu";
        return meta.id + "   |   " + time;
    }
}
