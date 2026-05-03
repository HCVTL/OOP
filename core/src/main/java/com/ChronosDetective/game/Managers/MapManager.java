package com.ChronosDetective.game.Managers;

import com.ChronosDetective.game.Entities.NPC;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.ChronosDetective.game.Entities.Item;
import com.ChronosDetective.game.Entities.Player;
import com.badlogic.gdx.math.Rectangle;

import java.util.*;

public class MapManager {
    private TiledMap currentMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private EntityManager entityManager;
    private String currentMapName = "";

    private ArrayList<String> collectedItems = new ArrayList<>();
    private Map<String, Texture> itemLibrary = new HashMap<>();
    private String currentMapPath;



    private void loadItemLibrary() {

        itemLibrary.put("apple", new Texture("apple.png"));
        itemLibrary.put("cafe", new Texture("cafe.png"));
        itemLibrary.put("Chìa khóa", new Texture("key.png"));
        itemLibrary.put("none", new Texture("invisible.png"));

        itemLibrary.put("fran", new Texture("Characters/Fran.png"));
    }

    public MapManager (EntityManager entityManager) {
        this.entityManager = entityManager;
        loadItemLibrary();
        this.collectedItems = new ArrayList<>();
    }

    public void loadMap(String mapPath,String mapName, Player player, float spawnX, float spawnY) {
        if (currentMap != null) currentMap.dispose();

        currentMap = new TmxMapLoader().load(mapPath);
        currentMapPath = mapPath;
        currentMapName = mapName;
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
                MapProperties props = obj.getProperties();

                String ID = props.get("ID", String.class);
                String type = props.get("type", String.class);
                String name = obj.getName();
                String texKey = props.get("texture", String.class);
                String rawDialogue = props.get("dialogue", String.class);

                Texture tex = itemLibrary.get(texKey);


                // debug
                if (tex != null) {
                    if ("NPC".equals(type)) {
                        NPC newNPC = new NPC(tex, rect.x, rect.y, name, ID);
                        newNPC.getProperties().putAll(props);

                        if (rawDialogue != null) {
                            String[] pages = rawDialogue.split("\\|");
                            newNPC.addDialogueBranch("DEFAULT", pages);
                        }

                        entityManager.addNPC(newNPC);
                        //System.out.println("Da them NPC" + name);
                    }
                    else {
                        if (ID != null && collectedItems.contains(ID)) {
                            continue;
                        }

                        Item newItem = new Item(tex, rect.x, rect.y, name, ID);
                        newItem.getProperties().putAll(props);
                        entityManager.addItem(newItem);
                        //System.out.println("Da them items" + name);
                    }

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

public Map<String, Texture> getItemLibrary() {
    return itemLibrary;
}
    public String getCurrentMapName(){
        return currentMapName;
    }
    // 1. Lấy danh sách sổ đen ra
    public ArrayList<String> getCollectedItems() {
        return collectedItems;
    }

    // 2. Ghi đè sổ đen (Dùng khi Load Game)
    public void setCollectedItems(ArrayList<String> items) {
        this.collectedItems.clear();
        if (items != null) {
            this.collectedItems.addAll(items);
        }
    }
        // 4. Hàm ma thuật: Nặn ra Item từ ID (Dùng để bỏ lại vào túi khi Load Game)
        public Item createItemFromId(String id) {
            com.badlogic.gdx.graphics.Texture tex = null;
            String itemName = "Vật phẩm";

            if ("apple_map".equals(id)) {
                tex = itemLibrary.get("apple");
                itemName = "Quả táo dai";
            }
            else if ("cafe_kitchen".equals(id )) {
                tex = itemLibrary.get("cafe");
                itemName = "cốc cafe";
            }
            // Sau này có đồ mới thì cứ thêm 'else if' ở đây

            if (tex != null) {
                Item newItem = new Item(tex, 0, 0, itemName, id);
                newItem.getProperties().put("type", "ITEM"); // Đóng dấu nó là ITEM
                return newItem;
            }
            return null;
        }
    }

