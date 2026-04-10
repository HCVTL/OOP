package com.ChronosDetective.game.Managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.ChronosDetective.game.Entities.Item;
import com.ChronosDetective.game.Entities.Player;
import com.badlogic.gdx.math.Rectangle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

public class MapManager {
    private TiledMap currentMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private EntityManager entityManager;
    private String currentMapName = "";

    private Set<String> collectedItems = new HashSet<>();
    private Map<String, Texture> itemLibrary = new HashMap<>();
    private String currentMapPath;

    // Door animation (kitchen only)
    private static final int DOOR_TILE_SIZE = 16;
    private static final int DOOR_W = 2;
    private static final int DOOR_H = 3;
    private static final float DOOR_FRAME_DURATION = 0.08f;
    private static final String DOOR_BLOCK_LAYER = "DoorBlock";
    private boolean kitchenDoorOpen = false;
    private boolean kitchenDoorAnimating = false;
    private float doorAnimTimer = 0f;
    private int doorAnimFrame = 0;
    private boolean kitchenDoorTargetOpen = false;
    private Rectangle kitchenDoorCollisionRect = null;
    private int kitchenDoorX = 0;
    private int kitchenDoorY = 1;

    // Frame 0 = dong, Frame 1 = mo (ngay o cot ben phai cua frame dong).
    private static final int[][][] DOOR_OPEN_FRAMES = new int[][][] {
        {
            {2062, 2063},
            {2108, 2109},
            {2154, 2155}
        },
        {
            {2064, 2065},
            {2110, 2111},
            {2156, 2157}
        }
    };

    private void loadItemLibrary() {
        itemLibrary.put("apple", new Texture("apple.png"));
    }

    public MapManager (EntityManager entityManager) {
        this.entityManager = entityManager;
        loadItemLibrary();
    }

    public void loadMap(String mapPath, String targetMap, Player player, float spawnX, float spawnY) {
        if (currentMap != null) currentMap.dispose();
        this.currentMapName= targetMap;
        currentMap = new TmxMapLoader().load(mapPath);
        currentMapPath = mapPath;
        resetKitchenDoorState();
        detectKitchenDoorBounds();
        setupKitchenDoorCollisionRect();
        syncKitchenDoorCollision();
        if (mapRenderer == null) {
            mapRenderer = new OrthogonalTiledMapRenderer(currentMap);
        }
        else {
            mapRenderer.setMap(currentMap);
        }

        // Cập nhật map và vị trí của player
        player.setMap(currentMap);
        player.setPosition(spawnX, spawnY);

        // Load Item/NPC riêng cho từng map
        setupEntitiesForMap(currentMap);
    }

    private void setupEntitiesForMap(TiledMap map) {
        entityManager.clearEntities();

        MapLayer itemLayer = map.getLayers().get("Items");
        if (itemLayer == null) return;

        for (MapObject obj : itemLayer.getObjects()) {
            if (obj instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) obj).getRectangle();

                String itemID = obj.getProperties().get("ID", String.class);
                if (collectedItems.contains(itemID)) {
                    continue;
                }

                String name = obj.getName();
                String texKey = obj.getProperties().get("texture", String.class);
                String dialogue = obj.getProperties().get("dialogue", String.class);

                Texture tex = itemLibrary.get(texKey);


                // debug
                if (tex != null) {
                    // Tạo item mới và thêm vào manager
                    // Lưu ý: Tọa độ x, y lấy trực tiếp từ hình chữ nhật bạn vẽ trong Tiled
                    entityManager.addItem(new Item(tex, rect.x, rect.y, name, itemID));
                } else {
                    Gdx.app.log("MapManager", "Lỗi: Không tìm thấy texture cho key: " + texKey);
                }
            }
        }
    }

    public  void checkPortals(Player player, MapTransitionListener listener) {
        // Lấy vùng để xử lý logic chuyển map
        MapLayer layer = currentMap.getLayers().get("Portals");

        if (layer == null) {
            System.out.println("Lỗi: Không tìm thấy lớp nào tên Portals");
            return;
        }

        for (MapObject object : layer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object). getRectangle();
                if (player.getBounds().overlaps(rect)) {
                    // Ở bếp: chỉ cho qua portal khi cửa đã mở.
                    if ("kitchen.tmx".equals(currentMapPath) && !kitchenDoorOpen) {
                        continue;
                    }
                    String target = object.getProperties().get("targetMap", String.class);
                    float tx = object.getProperties().get("targetSpawnX", Number.class).floatValue();
                    float ty = object.getProperties().get("targetSpawnY", Number.class).floatValue();

                    System.out.println("Va chạm cửa! Chuyển đến: " + target); // Thêm dòng này để kiểm tra console
                    listener.onTransition(target, tx, ty);
                    break;
                }
            }
        }
    }

    public void update(float delta) {
        if (!kitchenDoorAnimating) return;

        doorAnimTimer += delta;
        if (doorAnimTimer < DOOR_FRAME_DURATION) return;
        doorAnimTimer = 0f;

        doorAnimFrame++;
        if (doorAnimFrame >= DOOR_OPEN_FRAMES.length) {
            doorAnimFrame = DOOR_OPEN_FRAMES.length - 1;
            kitchenDoorAnimating = false;
            kitchenDoorOpen = kitchenDoorTargetOpen;
            syncKitchenDoorCollision();
        }
        applyKitchenDoorFrame(doorAnimFrame);
    }

    public boolean tryToggleKitchenDoor(Player player) {
        if (!"kitchen.tmx".equals(currentMapPath)) return false;
        if (kitchenDoorAnimating) return false;
        if (!isPlayerNearKitchenDoor(player)) return false;

        kitchenDoorTargetOpen = !kitchenDoorOpen;
        kitchenDoorAnimating = true;
        doorAnimTimer = 0f;
        doorAnimFrame = kitchenDoorOpen ? (DOOR_OPEN_FRAMES.length - 1) : 0;
        applyKitchenDoorFrame(doorAnimFrame);
        return true;
    }

    public boolean shouldShowKitchenDoorHint(Player player) {
        if (!"kitchen.tmx".equals(currentMapPath)) return false;
        if (kitchenDoorOpen) return false;
        if (kitchenDoorAnimating) return false;
        if (kitchenDoorCollisionRect == null) return false;
        return player.getBounds().overlaps(kitchenDoorCollisionRect);
    }

    public boolean isKitchenDoorOpen() {
        return kitchenDoorOpen;
    }

    private void resetKitchenDoorState() {
        kitchenDoorOpen = false;
        kitchenDoorAnimating = false;
        doorAnimTimer = 0f;
        doorAnimFrame = 0;
        kitchenDoorTargetOpen = false;
        kitchenDoorCollisionRect = null;
    }

    private boolean isPlayerNearKitchenDoor(Player player) {
        if (kitchenDoorCollisionRect == null) return false;
        Rectangle trigger = new Rectangle(
            kitchenDoorCollisionRect.x - 18f,
            kitchenDoorCollisionRect.y - 18f,
            kitchenDoorCollisionRect.width + 36f,
            kitchenDoorCollisionRect.height + 36f
        );
        return trigger.overlaps(player.getBounds());
    }

    private void applyKitchenDoorFrame(int frameIndex) {
        if (currentMap == null) return;

        TiledMapTileLayer doorLayer = (TiledMapTileLayer) currentMap.getLayers().get("door");
        if (doorLayer == null) return;

        int index = frameIndex;
        if (!kitchenDoorTargetOpen) {
            index = (DOOR_OPEN_FRAMES.length - 1) - frameIndex;
        }
        int[][] frame = DOOR_OPEN_FRAMES[index];
        for (int row = 0; row < DOOR_H; row++) {
            for (int col = 0; col < DOOR_W; col++) {
                TiledMapTileLayer.Cell cell = doorLayer.getCell(kitchenDoorX + col, kitchenDoorY + row);
                if (cell == null) {
                    cell = new TiledMapTileLayer.Cell();
                    doorLayer.setCell(kitchenDoorX + col, kitchenDoorY + row, cell);
                }
                TiledMapTile tile = currentMap.getTileSets().getTile(frame[row][col]);
                if (tile != null) cell.setTile(tile);
            }
        }
    }

    private void syncKitchenDoorCollision() {
        if (currentMap == null || !"kitchen.tmx".equals(currentMapPath)) return;

        TiledMapTileLayer blockLayer = (TiledMapTileLayer) currentMap.getLayers().get(DOOR_BLOCK_LAYER);
        if (blockLayer == null) {
            MapLayer anyLayer = currentMap.getLayers().get(0);
            int width = ((TiledMapTileLayer) anyLayer).getWidth();
            int height = ((TiledMapTileLayer) anyLayer).getHeight();
            blockLayer = new TiledMapTileLayer(width, height, DOOR_TILE_SIZE, DOOR_TILE_SIZE);
            blockLayer.setName(DOOR_BLOCK_LAYER);
            currentMap.getLayers().add(blockLayer);
        }

        if (kitchenDoorCollisionRect == null) return;

        int minX = Math.max(0, (int) Math.floor(kitchenDoorCollisionRect.x / DOOR_TILE_SIZE));
        int maxX = Math.min(blockLayer.getWidth() - 1, (int) Math.floor((kitchenDoorCollisionRect.x + kitchenDoorCollisionRect.width) / DOOR_TILE_SIZE));
        int minY = Math.max(0, (int) Math.floor(kitchenDoorCollisionRect.y / DOOR_TILE_SIZE));
        int maxY = Math.min(blockLayer.getHeight() - 1, (int) Math.floor((kitchenDoorCollisionRect.y + kitchenDoorCollisionRect.height) / DOOR_TILE_SIZE));

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (kitchenDoorOpen) {
                    blockLayer.setCell(x, y, null);
                } else if (blockLayer.getCell(x, y) == null) {
                    blockLayer.setCell(x, y, new TiledMapTileLayer.Cell());
                }
            }
        }
    }

    private void setupKitchenDoorCollisionRect() {
        kitchenDoorCollisionRect = null;
        if (currentMap == null || !"kitchen.tmx".equals(currentMapPath)) return;
        kitchenDoorCollisionRect = new Rectangle(
            kitchenDoorX * DOOR_TILE_SIZE,
            kitchenDoorY * DOOR_TILE_SIZE,
            DOOR_W * DOOR_TILE_SIZE,
            DOOR_H * DOOR_TILE_SIZE
        );
    }

    private void detectKitchenDoorBounds() {
        if (currentMap == null || !"kitchen.tmx".equals(currentMapPath)) return;

        TiledMapTileLayer doorLayer = (TiledMapTileLayer) currentMap.getLayers().get("door");
        if (doorLayer == null) return;

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (int y = 0; y < doorLayer.getHeight(); y++) {
            for (int x = 0; x < doorLayer.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = doorLayer.getCell(x, y);
                if (cell == null || cell.getTile() == null) continue;

                int id = cell.getTile().getId();
                if (id < 2062 || id > 2161) continue;

                if (x < minX) minX = x;
                if (y < minY) minY = y;
                if (x > maxX) maxX = x;
                if (y > maxY) maxY = y;
            }
        }

        if (minX == Integer.MAX_VALUE) return;
        kitchenDoorX = minX;
        kitchenDoorY = minY;
    }

    public Set<String> getCollectedItems() {
        return collectedItems;
    }
    public void setCollectedItems(ArrayList<String> savedItems) {
        if (savedItems != null) {
            this.collectedItems.clear(); // Xóa dữ liệu cũ
            this.collectedItems.addAll(savedItems); // Thêm dữ liệu từ save
        }
    }

    // 2. Tái tạo lại Item để nhét vào túi đồ khi Load Game
    public Item createItemFromId(String id) {
        Texture tex = null;
        String itemName = "Vật phẩm";

        // TODO: Bạn cần sửa các chữ "id_cua_qua_tao" thành đúng ID bạn đặt trong TiledMap
        if (id.equals("apple_map")) {
            tex = itemLibrary.get("apple");
            itemName = "Quả táo";
        }
        // Thêm các món đồ khác của bạn ở đây...
        /*
        else if (id.equals("id_chia_khoa")) {
            tex = itemLibrary.get("key");
            itemName = "Chìa khóa";
        }
        */

        // Nếu tìm thấy đồ, tạo Item ở tọa độ (0,0) vì nó chỉ nằm trong túi, không vẽ ra map
        if (tex != null) {
            return new Item(tex, 0, 0, itemName, id);
        }

        System.out.println("Lỗi Load Game: Không nhận diện được vật phẩm có ID: " + id);
        return null;
    }

    //Giao diện để callback về GameScreen
    public interface MapTransitionListener {
        void onTransition(String targetMap, float x, float y);
    }

    public TiledMap getCurrentMap() {
        return currentMap;
    }

    public OrthogonalTiledMapRenderer getRenderer() {
        return mapRenderer;
    }

    public void dispose() {
        currentMap.dispose();
        mapRenderer.dispose();
    }

    public String getCurrentMapName(){
        return currentMapName;
    }


}
