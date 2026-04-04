package com.ChronosDetective.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.ChronosDetective.game.Screens.MenuScreen;
import com.kotcrab.vis.ui.VisUI;

public class ChronosDetectiveGame extends Game {
    public SpriteBatch batch;

    @Override
    public void create() {
        VisUI.load();
        batch = new SpriteBatch();
        this.setScreen(new MenuScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        VisUI.dispose();
        batch.dispose();
    }
}
