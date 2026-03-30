package com.ChronosDetective.game.Save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

public class SaveRepository {
    private static final String SAVES_DIR = "saves";
    private static final String INDEX_FILE = "index.json";

    private final Json json;

    public SaveRepository() {
        json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        json.setIgnoreUnknownFields(true);
    }

    public String createNewSessionId() {
        return UUID.randomUUID().toString();
    }

    public SaveIndex loadIndex() {
        try {
            FileHandle fh = indexFile();
            if (!fh.exists()) return new SaveIndex();
            SaveIndex idx = json.fromJson(SaveIndex.class, fh);
            if (idx == null) return new SaveIndex();
            if (idx.sessions == null) idx.sessions = new ArrayList<>();
            return idx;
        } catch (Exception ignored) {
            return new SaveIndex();
        }
    }

    public void saveIndex(SaveIndex index) {
        ensureSavesDir();
        indexFile().writeString(json.prettyPrint(index), false, "UTF-8");
    }

    public ArrayList<SaveSessionMeta> listSessionsNewestFirst() {
        SaveIndex idx = loadIndex();
        ArrayList<SaveSessionMeta> sessions = new ArrayList<>(idx.sessions);
        sessions.sort(Comparator.comparingLong((SaveSessionMeta m) -> m.lastSavedAtEpochMs).reversed());
        return sessions;
    }

    public void setLastSessionId(String sessionId) {
        SaveIndex idx = loadIndex();
        idx.lastSessionId = sessionId;
        saveIndex(idx);
    }

    public String getLastSessionId() {
        return loadIndex().lastSessionId;
    }

    public void saveGame(SaveData data) {
        if (data == null || data.sessionId == null) return;
        ensureSavesDir();

        if (data.savedAtEpochMs <= 0) data.savedAtEpochMs = System.currentTimeMillis();

        sessionFile(data.sessionId).writeString(json.prettyPrint(data), false, "UTF-8");

        SaveIndex idx = loadIndex();
        upsertSessionMeta(idx, data.sessionId, data.savedAtEpochMs);
        idx.lastSessionId = data.sessionId;
        saveIndex(idx);
    }

    public SaveData loadGame(String sessionId) {
        if (sessionId == null) return null;
        try {
            FileHandle fh = sessionFile(sessionId);
            if (!fh.exists()) return null;
            return json.fromJson(SaveData.class, fh);
        } catch (Exception ignored) {
            return null;
        }
    }

    public boolean hasSession(String sessionId) {
        return sessionId != null && sessionFile(sessionId).exists();
    }

    private void upsertSessionMeta(SaveIndex idx, String id, long savedAt) {
        if (idx.sessions == null) idx.sessions = new ArrayList<>();
        for (SaveSessionMeta meta : idx.sessions) {
            if (meta != null && id.equals(meta.id)) {
                meta.lastSavedAtEpochMs = savedAt;
                return;
            }
        }
        idx.sessions.add(new SaveSessionMeta(id, savedAt));
    }

    private void ensureSavesDir() {
        FileHandle dir = Gdx.files.local(SAVES_DIR);
        if (!dir.exists()) dir.mkdirs();
    }

    private FileHandle indexFile() {
        return Gdx.files.local(SAVES_DIR + "/" + INDEX_FILE);
    }

    private FileHandle sessionFile(String sessionId) {
        return Gdx.files.local(SAVES_DIR + "/" + sessionId + ".json");
    }
}
