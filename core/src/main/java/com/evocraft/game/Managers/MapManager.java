package com.evocraft.game.Managers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.evocraft.game.Entities.Item;
import com.evocraft.game.Entities.Player;
import com.badlogic.gdx.math.Rectangle;

public class MapManager {
    private TiledMap currentMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private EntityManager entityManager;

    private Texture appleTex;

    public MapManager (EntityManager entityManager) {
        this.entityManager = entityManager;

        this.appleTex = new Texture("apple.png");
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
        setupEntitiesForMap(mapPath);
    }

    private void setupEntitiesForMap(String mapPath) {
        entityManager.clearEntities();
        if (mapPath.equals("map.tmx")) {
            entityManager.addItem(new Item(appleTex, 200, 250, "Qua tao"));
        }
    }

    public  void checkPortals(Player player, MapTransitionListener listener) {
        // Lấy vùng để xử lý logic chuyển map
        MapLayer layer = currentMap.getLayers().get("Portals");

        if (layer == null) return;

        for (MapObject object : layer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object). getRectangle();
                if (player.getBounds().overlaps(rect)) {
                    String target = object.getProperties().get("targetMap", String.class);
                    float tx = object.getProperties().get("targetSpawnX", Float.class);
                    float ty = object.getProperties().get("targetSpawnY", Float.class);

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


}
