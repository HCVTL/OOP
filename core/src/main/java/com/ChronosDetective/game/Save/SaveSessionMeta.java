package com.ChronosDetective.game.Save;

public class SaveSessionMeta {
    public String id;
    public long lastSavedAtEpochMs;

    public SaveSessionMeta() {}

    public SaveSessionMeta(String id, long lastSavedAtEpochMs) {
        this.id = id;
        this.lastSavedAtEpochMs = lastSavedAtEpochMs;
    }
}
