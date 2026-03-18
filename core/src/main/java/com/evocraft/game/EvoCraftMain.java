package com.evocraft.game;

import java.util.ArrayList;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.evocraft.game.Entities.Item;
import com.evocraft.game.Entities.Player;

public class EvoCraftMain extends ApplicationAdapter {
    private SpriteBatch batch;
    private Player player;
    private ShapeRenderer shapeRenderer;

    private OrthographicCamera camera;
    private FitViewport viewport;

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;

    private ArrayList<Item> items;
    private Texture appleTexture;

    @Override
    public void create() {
        batch = new SpriteBatch();

        // 1. Thiết lập Camera (Nhìn vào vùng không gian 800x480 đơn vị)
        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);


        // 3. Load Bản đồ (Nếu bạn đã có file .tmx, nếu chưa hãy tạm comment 2 dòng này)
         map = new TmxMapLoader().load("map.tmx");
         mapRenderer = new OrthogonalTiledMapRenderer(map);

        // 2. Load Nhân vật (Đảm bảo file player.png nằm trong thư mục assets)
        Texture playerTexture = new Texture("player2.png");
        player = new Player(playerTexture, 0, 0, map);

        items = new ArrayList<>();
        appleTexture = new Texture("apple.png");

        items.add(new Item(appleTexture, 200, 250, "Apple"));
        items.add(new Item(appleTexture, 400, 300, "Apple"));

        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render() {
        // 1. Lấy thời gian trôi qua giữa 2 khung hình
        float delta = Gdx.graphics.getDeltaTime();

        // 2. Cập nhật logic (Quan trọng!)
        player.update(delta);

        // 3. Cập nhật Camera đuổi theo nhân vật
        camera.position.set(player.getX(), player.getY(), 0);
        camera.update();

        // 4. Xóa màn hình
        Gdx.gl.glClearColor(0.4f, 0.6f, 0.6f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        for (Item item: items) {
            if (!item.isCollected() && player.getBounds().overlaps(item.getBounds())) {
                item.collect();
                Gdx.app.log("Game", "Collected" + item.getName());
            }
        }

        // 5. Vẽ Bản đồ
        mapRenderer.setView(camera);
        mapRenderer.render();


        // 6. Vẽ Nhân vật
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Item item: items) {
            if (!item.isCollected()) item.draw(batch);
        }
        player.draw(batch);
        batch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(com.badlogic.gdx.graphics.Color.RED);

        // Vẽ 2 điểm kiểm tra va chạm dưới chân (giả sử padding là 10)
        float p = 0f;
        shapeRenderer.circle(player.getX() + p, player.getY() + 2, 2); // Điểm trái
        shapeRenderer.circle(player.getX() + player.getWidth() - p, player.getY() + 2, 2); // Điểm phải

        // Vẽ khung bao quanh vùng chân
        shapeRenderer.rect(player.getX() + p, player.getY(), player.getWidth() - 2*p, 5);

        shapeRenderer.end();

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        player.dispose();
        map.dispose();
    }
}
