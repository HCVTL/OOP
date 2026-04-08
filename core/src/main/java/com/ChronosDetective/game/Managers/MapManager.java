package com.ChronosDetective.game.Managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.ChronosDetective.game.Entities.Item;
import com.ChronosDetective.game.Entities.Player;
import com.badlogic.gdx.math.Rectangle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapManager {
    private TiledMap currentMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private EntityManager entityManager;

    private Set<String> collectedItems = new HashSet<>();
    private Map<String, Texture> itemLibrary = new HashMap<>();

    private void loadItemLibrary() {
        itemLibrary.put("apple", new Texture("apple.png"));
    }

    public MapManager (EntityManager entityManager) {
        this.entityManager = entityManager;
        loadItemLibrary();
    }

    public void loadMap(String mapPath, Player player, float spawnX, float spawnY) {
        if (currentMap != null) currentMap.dispose();

        currentMap = new TmxMapLoader().load(mapPath);
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

    public Set<String> getCollectedItems() {
        return collectedItems;
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


}
