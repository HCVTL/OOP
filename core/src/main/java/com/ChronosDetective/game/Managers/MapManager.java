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



    private void loadItemLibrary() {

        itemLibrary.put("apple", new Texture("apple.png"));
        itemLibrary.put("cafe", new Texture("cafe.png"));
    }

    public MapManager (EntityManager entityManager) {
        this.entityManager = entityManager;
        loadItemLibrary();
    }

    public void loadMap(String mapPath, Player player, float spawnX, float spawnY) {
        if (currentMap != null) currentMap.dispose();
        loadMap(mapPath, mapPath, player, spawnX, spawnY);
    }

    public void loadMap(String mapPath, String targetMap, Player player, float spawnX, float spawnY) {
        if (currentMap != null) currentMap.dispose();
        this.currentMapName= targetMap;
        currentMap = new TmxMapLoader().load(mapPath);
        currentMapPath = mapPath;
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
        MapLayer layer = currentMap.getLayers().get("Door");

        if (layer == null) {
            System.out.println("Lỗi: Không tìm thấy lớp nào tên Portals");
            return;
        }

        for (MapObject object : layer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object). getRectangle();
                if (player.getBounds().overlaps(rect)) {
                    // Ở bếp: chỉ cho qua portal khi cửa đã mở.
                    String target = object.getProperties().get("targetMap", String.class);
                    float tx = object.getProperties().get("spawnX", Number.class).floatValue();
                    float ty = object.getProperties().get("spawnY", Number.class).floatValue();

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
