package com.ChronosDetective.game.Managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class StoryManager {
    private JsonValue storyData;

    public boolean foundBody = false, foundWine = false, foundWindow = false, foundKnife = false;

    public StoryManager() {
        JsonReader reader = new JsonReader();
        storyData = reader.parse(Gdx.files.internal("Scripts/story_chapter1.json"));
    }

    public JsonValue getClueData(String clueID) {
        return storyData.get("clues").get(clueID);
    }

    public boolean isAllCluesFound() {
        return foundBody && foundKnife && foundWindow && foundKnife;
    }
}
