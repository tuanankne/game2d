package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector3;

public class GameScreen implements Screen, GestureListener {
    private final Main game;
    private OrthographicCamera camera;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private Array<Enemy> enemies;
    private float CAMERA_SPEED = 500f; // Tốc độ di chuyển camera
    private Array<Array<Vector2>> paths; // Danh sách các đường đi
    private WaveManager waveManager;
    private Viewport viewport;
    private Vector3 lastTouch;
    private float mapWidth;
    private float mapHeight;
    private com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer;

    // Các giới hạn cho camera
    private float minCameraX;
    private float maxCameraX;
    private float minCameraY;
    private float maxCameraY;

    public GameScreen(final Main game) {
        this.game = game;

        // Bật log để debug
        Gdx.app.setLogLevel(com.badlogic.gdx.utils.Logger.DEBUG);

        // Initialize arrays and tools
        enemies = new Array<>();
        paths = new Array<>();
        shapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();
        lastTouch = new Vector3();

        // Khởi tạo game với config
        MapConfig config = createMapConfig();
        initializeGame(config);

        // Tính toán kích thước map
        int tileWidth = map.getProperties().get("tilewidth", Integer.class);
        int tileHeight = map.getProperties().get("tileheight", Integer.class);
        int mapWidthInTiles = map.getProperties().get("width", Integer.class);
        int mapHeightInTiles = map.getProperties().get("height", Integer.class);
        mapWidth = tileWidth * mapWidthInTiles;
        mapHeight = tileHeight * mapHeightInTiles;

        // Setup camera và viewport
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        viewport = new StretchViewport(screenWidth, screenHeight, camera);
        viewport.apply(true);

        // Thiết lập camera bounds và position
        minCameraX = screenWidth / 2;
        maxCameraX = mapWidth - screenWidth / 2;
        minCameraY = screenHeight / 2;
        maxCameraY = mapHeight - screenHeight / 2;
        camera.position.set(minCameraX, minCameraY, 0);
        camera.update();

        // Thiết lập input processor
        Gdx.input.setInputProcessor(new GestureDetector(this));

        // Đọc đường đi từ map
        loadPathsFromMap();
    }

    private void loadPathsFromMap() {
        try {
            // Lấy object layer "path" từ map
            MapLayer pathLayer = map.getLayers().get("path");
            if (pathLayer != null) {
                MapObjects objects = pathLayer.getObjects();
                if (objects != null) {
                    // Duyệt qua từng object trong layer
                    for (MapObject object : objects) {
                        if (object instanceof PolylineMapObject) {
                            // Lấy các điểm từ polyline
                            PolylineMapObject polyline = (PolylineMapObject) object;
                            float[] vertices = polyline.getPolyline().getTransformedVertices();

                            // Tạo đường đi mới
                            Array<Vector2> path = new Array<>();
                            for (int i = 0; i < vertices.length; i += 2) {
                                path.add(new Vector2(vertices[i], vertices[i + 1]));
                            }
                            paths.add(path);
                            Gdx.app.log("GameScreen", "Loaded path with " + (path.size) + " points");
                        }
                    }
                    Gdx.app.log("GameScreen", "Total paths loaded: " + paths.size);
                } else {
                    Gdx.app.error("GameScreen", "No objects found in path layer");
                }
            } else {
                Gdx.app.error("GameScreen", "Path layer not found in map");
            }
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error loading paths: " + e.getMessage());
        }
    }

    private MapConfig createMapConfig() {
        MapConfig config = new MapConfig("map1/map1.tmx", "path", 5f);
        
        // Wave 1: Quái thường
        WaveConfig wave1 = new WaveConfig(2f);
        wave1.addEnemy(EnemyType.NORMAL, 5);
        config.addWaveConfig(wave1);
        
        // Wave 2: Quái thường + nhanh
        WaveConfig wave2 = new WaveConfig(1.5f);
        wave2.addEnemy(EnemyType.NORMAL, 3);
        wave2.addEnemy(EnemyType.FAST, 3);
        config.addWaveConfig(wave2);
        
        // Wave 3: Tất cả các loại
        WaveConfig wave3 = new WaveConfig(1f);
        wave3.addEnemy(EnemyType.NORMAL, 2);
        wave3.addEnemy(EnemyType.FAST, 2);
        wave3.addEnemy(EnemyType.TANK, 2);
        config.addWaveConfig(wave3);
        
        return config;
    }

    private void initializeGame(MapConfig config) {
        // Load map
        map = new TmxMapLoader().load(config.getMapPath());
        renderer = new OrthogonalTiledMapRenderer(map, 1f);
        
        // Initialize managers
        waveManager = new WaveManager(config.getTimeBetweenWaves());
        
        // Create waves from config
        for (WaveConfig waveConfig : config.getWaveConfigs()) {
            waveManager.addWave(waveConfig.createWave());
        }
    }

    private void spawnEnemy(EnemyType type) {
        if (paths.size > 0) {
            // Chọn ngẫu nhiên một đường đi
            int pathIndex = MathUtils.random(paths.size - 1);
            Array<Vector2> path = paths.get(pathIndex);

            if (path.size > 0) {
                Vector2 startPoint = path.first();
                Enemy enemy = new Enemy(startPoint.x, startPoint.y, type);
                enemy.setPath(path); // Gán toàn bộ đường đi cho enemy
                enemies.add(enemy);
                
                Gdx.app.log("GameScreen", "Spawned enemy at " + startPoint.x + "," + startPoint.y + 
                    " with path size: " + path.size);
            }
        }
    }


    private void updateEnemies(float delta) {
        // Cập nhật và render từng enemy
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.update(delta);
            enemy.render(game.batch);
            
            // Xóa quái đã đến đích
            if (enemy.hasReachedEnd()) {
                enemies.removeIndex(i);
                enemy.dispose();
                Gdx.app.log("GameScreen", "Enemy reached destination. Remaining: " + enemies.size);
            }
        }
    }

    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        // Render map
        renderer.setView(camera);
        renderer.render();

        // Update wave manager
        waveManager.update(delta);
        Wave currentWave = waveManager.getCurrentWave();

        if (currentWave != null) {
            // Spawn quái mới nếu cần
            if (waveManager.shouldSpawnEnemy(delta)) {
                EnemyType type = currentWave.getNextEnemy();
                if (type != null) {
                    spawnEnemy(type);
                    Gdx.app.log("GameScreen", String.format("Wave: Spawned %s enemy. Total enemies alive: %d",
                        type.name(), enemies.size));
                }
            }

            // Kiểm tra wave hoàn thành
            if (currentWave.isComplete() && enemies.size == 0) {
                Gdx.app.log("GameScreen", "All enemies in current wave reached destination");
                waveManager.waveCompleted();
            }
        }

        // Update and render enemies
        game.batch.begin();
        game.batch.setProjectionMatrix(camera.combined);
        updateEnemies(delta);
        // Render wave message
        waveManager.render(game.batch);
        game.batch.end();

        // Debug: vẽ hướng di chuyển của quái
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line);
        for (Enemy enemy : enemies) {
            enemy.renderDebug(shapeRenderer);
        }
        shapeRenderer.end();
    }

    // Implement GestureListener methods
    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        lastTouch.set(x, y, 0);
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        // Chuyển đổi delta từ screen coordinates sang world coordinates
        float panSpeed = 1.0f; // Điều chỉnh tốc độ pan
        float worldDeltaX = -deltaX * (camera.viewportWidth / Gdx.graphics.getWidth()) * panSpeed;
        float worldDeltaY = deltaY * (camera.viewportHeight / Gdx.graphics.getHeight()) * panSpeed;

        // Di chuyển camera
        camera.position.x = clamp(camera.position.x + worldDeltaX, minCameraX, maxCameraX);
        camera.position.y = clamp(camera.position.y + worldDeltaY, minCameraY, maxCameraY);

        camera.update();
        return true;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    // Các phương thức GestureListener khác
    @Override public boolean tap(float x, float y, int count, int button) { return false; }
    @Override public boolean longPress(float x, float y) { return false; }
    @Override public boolean fling(float velocityX, float velocityY, int button) { return false; }
    @Override public boolean panStop(float x, float y, int pointer, int button) { return false; }
    @Override public boolean zoom(float initialDistance, float distance) { return false; }
    @Override public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) { return false; }
    @Override public void pinchStop() { }

    @Override
    public void resize(int width, int height) {
        // Cập nhật viewport với kích thước mới
        viewport.update(width, height, true);

        // Cập nhật giới hạn camera
        float worldWidth = width * (mapWidth / Gdx.graphics.getWidth());
        float worldHeight = height * (mapHeight / Gdx.graphics.getHeight());

        minCameraX = width / 2;
        maxCameraX = worldWidth - width / 2;
        minCameraY = height / 2;
        maxCameraY = worldHeight - height / 2;

        // Đặt camera ở giữa
        camera.position.set(width/2, height/2, 0);
        camera.update();

        Gdx.app.log("GameScreen", String.format(
            "Resize to: %dx%d, World: %.0fx%.0f",
            width, height, worldWidth, worldHeight
        ));
    }

    @Override
    public void show() {}

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        if (map != null) map.dispose();
        if (renderer != null) renderer.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (waveManager != null) waveManager.dispose();
        if (enemies != null) {
            for (Enemy enemy : enemies) {
                enemy.dispose();
            }
        }
    }
}
