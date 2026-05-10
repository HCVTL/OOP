package com.ChronosDetective.game.Managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;
import java.util.HashSet;

public class StoryManager {
    private JsonValue root;
    private HashSet<String> activeFlags;

    public StoryManager() {
        JsonReader reader = new JsonReader();
        // Nạp file JSON cấu trúc mới
        root = reader.parse(Gdx.files.internal("Scripts/story_chapter1.json"));
        activeFlags = new HashSet<>();
        activeFlags.add("DEFAULT");
    }

    // Quản lys FLAGS
    public void setFlag(String flag) {
        if (flag == null || flag.isEmpty()) return;
        if (activeFlags.contains(flag)) return;

        activeFlags.add(flag);
        checkLogicTrigger();
    }

    public boolean hasFlag(String flag) {
        return activeFlags.contains(flag);
    }

    // --- XỬ LÝ HỘI THOẠI DỰA TRÊN FLAG ---

    /**
     * Tự động tìm đoạn hội thoại phù hợp nhất dựa trên các Flag hiện có[cite: 1, 2]
     */

    public String[] getValidInteraction(String id) {
        JsonValue entity = findEntityByID(id);
        if (entity == null) return new String[]{"Không có gì đặc biệt"};

        JsonValue interactions = entity.get("interactions");
        // Duyệt ngược từ dưới lên để lấy tương tác có điều kiện cao nhất (mới nhất)
        for (int i = interactions.size() - 1; i >= 0; i--) {
            JsonValue inter = interactions.get(i);
            String condition = inter.getString("condition", "DEFAULT");

            if (hasFlag(condition)) {
                if (inter.has("set_flag")) {
                    setFlag(inter.getString("set_flag"));
                }
                return inter.get("text").asStringArray();
            }
        }

        return new String[]{"..."};
    }

    // --- LOGIC TRIGGERS (Mạch truyện tự động) ---

    /**
     * Kiểm tra các cổng chặn logic (ví dụ: đủ 4 bằng chứng thì mở suy luận)[cite: 2]
     */

    private void checkLogicTrigger() {
        if (root.has("logic_triggers")) {
            JsonValue triggers  = root.get("logic_triggers");
            for (JsonValue trigger : triggers) {
                String[] required = trigger.get("required_flags").asStringArray();
                boolean allMatch = true;
                for (String req : required) {
                    if (!hasFlag(req)){
                        allMatch = false;
                        break;
                    }
                }

                if (allMatch) {
                    setFlag(trigger.getString("set_flag"));
                }
            }
        }
    }

    // Hàm lấy tien ich
    public String[] getIntro() {
        JsonValue intro = root.get("global_events").get("intro");
        setFlag(intro.getString("set_flag", ""));
        return intro.get("text").asStringArray();
    }

    public String getEntityName(String id) {
        JsonValue entity = findEntityByID(id);
        return (entity != null) ? entity.getString("name", "???") : "???";
    }

    public JsonValue findEntityByID(String id) {
        JsonValue entities = root.get("entities");
        for (JsonValue entity : entities) {
            if (entity.getString("id").equals(id)) return entity;
        }

        return null;
    }

    public JsonValue getRoot(){
        return root;
    }

    public JsonValue getDeductionData() {
        if (root.has("deduction_phase")) {
            return root.get("deduction_phase");
        }

        return null;
    }
}
