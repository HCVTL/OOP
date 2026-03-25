package com.evocraft.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.evocraft.game.ChronosDetectiveGame;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class MenuScreen implements Screen {
    private final ChronosDetectiveGame game;
    private Stage stage;
    private Skin skin;

    public MenuScreen (ChronosDetectiveGame game) {
        this.game = game;
        this.stage = new Stage(new FitViewport(800, 480));
        Gdx.input.setInputProcessor(stage);

        Table table = new Table();
        table.setFillParent(true); // Bảng bao phủ toàn màn hình
        stage.addActor(table);

        VisTextButton newGameBtn = new VisTextButton("NEW GAME");
        VisTextButton continueBtn = new VisTextButton("CONTINUE");
        VisTextButton settingsBtn = new VisTextButton("SETTINGS");
        VisTextButton exitBtn = new VisTextButton("EXIT");

        newGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Chuyển sang màn hình chơi game (PlayScreen)
                 game.setScreen(new GameScreen(game));
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
        table.add(settingsBtn).fillX().uniformX().pad(10);
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
    @Override public void dispose() { stage.dispose(); skin.dispose(); }
}
