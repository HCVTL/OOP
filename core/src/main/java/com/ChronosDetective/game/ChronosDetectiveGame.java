package com.ChronosDetective.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.ObjectMap;
import com.ChronosDetective.game.Screens.MenuScreen;
import com.ChronosDetective.game.Utils.FontUtils;
import com.kotcrab.vis.ui.VisUI;

public class ChronosDetectiveGame extends Game {
    public SpriteBatch batch;
    private BitmapFont globalUiFont;

    @Override
    public void create() {
        VisUI.load();
        globalUiFont = FontUtils.createUnicodeFont(34);
        applyGlobalUiFont(VisUI.getSkin(), globalUiFont);
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
        if (globalUiFont != null) globalUiFont.dispose();
        batch.dispose();
    }

    private void applyGlobalUiFont(Skin skin, BitmapFont font) {
        if (skin == null || font == null) return;

        // Update commonly used style groups so all VisUI widgets render Unicode properly.
        ObjectMap<String, Label.LabelStyle> labels = skin.getAll(Label.LabelStyle.class);
        for (Label.LabelStyle style : labels.values()) style.font = font;

        ObjectMap<String, TextButton.TextButtonStyle> buttons = skin.getAll(TextButton.TextButtonStyle.class);
        for (TextButton.TextButtonStyle style : buttons.values()) style.font = font;

        ObjectMap<String, TextField.TextFieldStyle> textFields = skin.getAll(TextField.TextFieldStyle.class);
        for (TextField.TextFieldStyle style : textFields.values()) style.font = font;

        ObjectMap<String, List.ListStyle> lists = skin.getAll(List.ListStyle.class);
        for (List.ListStyle style : lists.values()) style.font = font;

        ObjectMap<String, SelectBox.SelectBoxStyle> selectBoxes = skin.getAll(SelectBox.SelectBoxStyle.class);
        for (SelectBox.SelectBoxStyle style : selectBoxes.values()) style.font = font;

        ObjectMap<String, Window.WindowStyle> windows = skin.getAll(Window.WindowStyle.class);
        for (Window.WindowStyle style : windows.values()) style.titleFont = font;

        ObjectMap<String, CheckBox.CheckBoxStyle> checkBoxes = skin.getAll(CheckBox.CheckBoxStyle.class);
        for (CheckBox.CheckBoxStyle style : checkBoxes.values()) style.font = font;
    }
}
