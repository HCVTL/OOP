package com.ChronosDetective.game.Save;

import java.util.ArrayList;

public class SaveData {
    public String sessionId;
    public long savedAtEpochMs;

    public float playerX;
    public float playerY;
    public String currentMapName;
    public ArrayList<String> inventoryItemIds = new ArrayList<>();
    public ArrayList<String> collectedItemIds = new ArrayList<>();
    public SaveData() {}
}
