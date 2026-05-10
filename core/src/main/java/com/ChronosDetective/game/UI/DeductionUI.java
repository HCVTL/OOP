package com.ChronosDetective.game.UI;

import com.ChronosDetective.game.Managers.DialogueManager;
import com.ChronosDetective.game.Managers.StoryManager;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class DeductionUI {
    private Table table;
    private VisLabel questionLabel;
    private Stage stage;
    private Skin skin;

    public DeductionUI(Stage stage, Skin skin) {
        this.stage = stage;
        this.skin = skin;
        this.table = new Table(skin);
        this.table.setFillParent(true);
        this.table.center();
        // Tạo nền tối mờ để người chơi tập trung suy luận
        this.table.setBackground(skin.getDrawable("window-bg"));
        stage.addActor(table);
        table.setVisible(false);
    }

    /**
     * Cài đặt dữ liệu suy luận. Hàm này sẽ được gọi lại mỗi khi sang Chapter mới.
     */
    public void setupDeduction(JsonValue deductionData, StoryManager story, DialogueManager dm) {
        table.clear(); // Xóa sạch dữ liệu của Chapter cũ

        questionLabel = new VisLabel(deductionData.getString("question"));
        table.add(questionLabel).padBottom(30).row();

        for (JsonValue option : deductionData.get("options")) {
            VisTextButton btn = new VisTextButton(option.getString("text"));
            btn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    hide();
                    String[] response = option.get("response").asStringArray();

                    // Chạy thoại kết quả
                    dm.startDialogue("Suy luận", response);

                    if (option.getBoolean("is_correct")) {
                        // Nếu đúng: Bật flag thành công
                        story.setFlag(option.getString("set_flag", "DEDUCTION_CORRECT"));
                    } else {
                        // Nếu sai: Sau khi đọc hết thoại thì hiện lại bảng chọn
                        dm.setDialogueEndCallback(() -> show());
                    }
                }
            });
            table.add(btn).width(450).height(50).pad(10).row();
        }
    }

    public void show() { table.setVisible(true); }
    public void hide() { table.setVisible(false); }
    public boolean isVisible() { return table.isVisible(); }
}
