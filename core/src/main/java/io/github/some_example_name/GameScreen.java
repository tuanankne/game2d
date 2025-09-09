package io.github.some_example_name;

// Import các thư viện cần thiết từ libGDX
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

// Lớp quản lý màn hình chơi game chính, xử lý input cử chỉ người chơi
public class GameScreen implements Screen, GestureListener {
    private final Main game;                    // Tham chiếu đến game chính
    private OrthographicCamera camera;          // Camera theo dõi game
    private TiledMap map;                       // Bản đồ tile
    private OrthogonalTiledMapRenderer renderer;// Renderer cho bản đồ
    private Array<Enemy> enemies;               // Danh sách quái vật
    private float CAMERA_SPEED = 500f;          // Tốc độ di chuyển camera
    private Array<Array<Vector2>> paths;        // Danh sách các đường đi của quái
    private WaveManager waveManager;            // Quản lý các đợt tấn công
    private Viewport viewport;                  // Quản lý khung nhìn
    private Vector3 lastTouch;                  // Vị trí chạm cuối cùng
    private float mapWidth;                     // Chiều rộng bản đồ
    private float mapHeight;                    // Chiều cao bản đồ
    private com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer;  // Vẽ debug

    // Các giới hạn di chuyển cho camera
    private float minCameraX;                   // Giới hạn trái
    private float maxCameraX;                   // Giới hạn phải
    private float minCameraY;                   // Giới hạn dưới
    private float maxCameraY;                   // Giới hạn trên

    // Constructor khởi tạo màn hình game
    public GameScreen(final Main game) {
        this.game = game;

        // Bật chế độ debug log
        Gdx.app.setLogLevel(com.badlogic.gdx.utils.Logger.DEBUG);

        // Khởi tạo các mảng và công cụ cần thiết
        enemies = new Array<>();                // Mảng chứa quái vật
        paths = new Array<>();                  // Mảng chứa đường đi
        shapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();  // Công cụ vẽ debug
        lastTouch = new Vector3();              // Vector lưu vị trí chạm

        // Tạo và khởi tạo game với cấu hình
        MapConfig config = createMapConfig();   // Tạo cấu hình map và wave
        initializeGame(config);                 // Khởi tạo game với cấu hình

        // Tính toán kích thước thực của bản đồ
        int tileWidth = map.getProperties().get("tilewidth", Integer.class);      // Chiều rộng mỗi tile
        int tileHeight = map.getProperties().get("tileheight", Integer.class);    // Chiều cao mỗi tile
        int mapWidthInTiles = map.getProperties().get("width", Integer.class);    // Số tile theo chiều rộng
        int mapHeightInTiles = map.getProperties().get("height", Integer.class);  // Số tile theo chiều cao
        mapWidth = tileWidth * mapWidthInTiles;    // Chiều rộng thực của map
        mapHeight = tileHeight * mapHeightInTiles; // Chiều cao thực của map

        // Thiết lập camera và viewport
        float screenWidth = Gdx.graphics.getWidth();    // Chiều rộng màn hình
        float screenHeight = Gdx.graphics.getHeight();  // Chiều cao màn hình
        camera = new OrthographicCamera();              // Tạo camera
        viewport = new StretchViewport(screenWidth, screenHeight, camera);  // Tạo viewport co giãn
        viewport.apply(true);                          // Áp dụng viewport

        // Thiết lập giới hạn di chuyển cho camera
        minCameraX = screenWidth / 2;                  // Giới hạn trái
        maxCameraX = mapWidth - screenWidth / 2;       // Giới hạn phải
        minCameraY = screenHeight / 2;                 // Giới hạn dưới
        maxCameraY = mapHeight - screenHeight / 2;     // Giới hạn trên
        camera.position.set(minCameraX, minCameraY, 0);  // Đặt vị trí camera
        camera.update();                               // Cập nhật camera

        // Thiết lập xử lý input từ người chơi
        Gdx.input.setInputProcessor(new GestureDetector(this));

        // Tải các đường đi từ bản đồ
        loadPathsFromMap();
    }

    // Tải các đường đi của quái từ bản đồ
    private void loadPathsFromMap() {
        try {
            // Lấy layer chứa đường đi từ map
            MapLayer pathLayer = map.getLayers().get("path");
            if (pathLayer != null) {
                MapObjects objects = pathLayer.getObjects();
                if (objects != null) {
                    // Duyệt qua từng đối tượng trong layer
                    for (MapObject object : objects) {
                        if (object instanceof PolylineMapObject) {
                            // Lấy các điểm từ đường polyline
                            PolylineMapObject polyline = (PolylineMapObject) object;
                            float[] vertices = polyline.getPolyline().getTransformedVertices();

                            // Tạo đường đi mới từ các điểm
                            Array<Vector2> path = new Array<>();
                            for (int i = 0; i < vertices.length; i += 2) {
                                path.add(new Vector2(vertices[i], vertices[i + 1]));
                            }
                            paths.add(path);  // Thêm đường đi vào danh sách
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

    // Tạo cấu hình cho map và các wave
    private MapConfig createMapConfig() {
        // Tạo cấu hình map với đường dẫn, tên layer path và thời gian giữa các wave
        MapConfig config = new MapConfig("map1/map1.tmx", "path", 5f);
        
        // Wave 1: Chỉ có quái thường
        WaveConfig wave1 = new WaveConfig(2f);  // 2 giây giữa mỗi lần sinh quái
        wave1.addEnemy(EnemyType.NORMAL, 5);    // 5 quái thường
        config.addWaveConfig(wave1);
        
        // Wave 2: Kết hợp quái thường và quái nhanh
        WaveConfig wave2 = new WaveConfig(1.5f);  // 1.5 giây giữa mỗi lần sinh quái
        wave2.addEnemy(EnemyType.NORMAL, 3);      // 3 quái thường
        wave2.addEnemy(EnemyType.FAST, 3);        // 3 quái nhanh
        config.addWaveConfig(wave2);
        
        // Wave 3: Tất cả các loại quái
        WaveConfig wave3 = new WaveConfig(1f);    // 1 giây giữa mỗi lần sinh quái
        wave3.addEnemy(EnemyType.NORMAL, 2);      // 2 quái thường
        wave3.addEnemy(EnemyType.FAST, 2);        // 2 quái nhanh
        wave3.addEnemy(EnemyType.TANK, 2);        // 2 quái tank
        config.addWaveConfig(wave3);
        
        return config;
    }

    // Khởi tạo game với cấu hình đã cho
    private void initializeGame(MapConfig config) {
        // Tải và thiết lập bản đồ
        map = new TmxMapLoader().load(config.getMapPath());
        renderer = new OrthogonalTiledMapRenderer(map, 1f);
        
        // Khởi tạo wave manager
        waveManager = new WaveManager(config.getTimeBetweenWaves());
        
        // Tạo các wave từ cấu hình
        for (WaveConfig waveConfig : config.getWaveConfigs()) {
            waveManager.addWave(waveConfig.createWave());
        }
    }

    // Sinh quái mới với loại chỉ định
    private void spawnEnemy(EnemyType type) {
        if (paths.size > 0) {
            // Chọn ngẫu nhiên một đường đi cho quái
            int pathIndex = MathUtils.random(paths.size - 1);
            Array<Vector2> path = paths.get(pathIndex);

            if (path.size > 0) {
                // Lấy điểm bắt đầu của đường đi
                Vector2 startPoint = path.first();
                // Tạo quái mới tại điểm bắt đầu
                Enemy enemy = new Enemy(startPoint.x, startPoint.y, type);
                enemy.setPath(path);  // Thiết lập đường đi cho quái
                enemies.add(enemy);   // Thêm quái vào danh sách
                
                // Ghi log debug
                Gdx.app.log("GameScreen", "Spawned enemy at " + startPoint.x + "," + startPoint.y + 
                    " with path size: " + path.size);
            }
        }
    }

    // Cập nhật trạng thái và vẽ tất cả quái
    private void updateEnemies(float delta) {
        // Duyệt ngược danh sách quái để có thể xóa an toàn
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.update(delta);     // Cập nhật trạng thái
            enemy.render(game.batch);  // Vẽ quái
            
            // Kiểm tra và xóa quái đã đến đích
            if (enemy.hasReachedEnd()) {
                enemies.removeIndex(i);  // Xóa khỏi danh sách
                enemy.dispose();         // Giải phóng tài nguyên
                Gdx.app.log("GameScreen", "Enemy reached destination. Remaining: " + enemies.size);
            }
        }
    }

    // Vẽ và cập nhật trạng thái game mỗi frame
    @Override
    public void render(float delta) {
        // Xóa màn hình với màu đen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Cập nhật camera
        camera.update();

        // Vẽ bản đồ
        renderer.setView(camera);
        renderer.render();

        // Cập nhật trạng thái wave manager
        waveManager.update(delta);
        Wave currentWave = waveManager.getCurrentWave();

        if (currentWave != null) {
            // Kiểm tra và sinh quái mới nếu đến thời điểm
            if (waveManager.shouldSpawnEnemy(delta)) {
                EnemyType type = currentWave.getNextEnemy();
                if (type != null) {
                    spawnEnemy(type);  // Sinh quái mới
                    // Ghi log thông tin
                    Gdx.app.log("GameScreen", String.format("Wave: Spawned %s enemy. Total enemies alive: %d",
                        type.name(), enemies.size));
                }
            }

            // Kiểm tra hoàn thành wave
            if (currentWave.isComplete() && enemies.size == 0) {
                Gdx.app.log("GameScreen", "All enemies in current wave reached destination");
                waveManager.waveCompleted();  // Chuyển sang wave tiếp theo
            }
        }

        // Cập nhật và vẽ các quái vật
        game.batch.begin();
        game.batch.setProjectionMatrix(camera.combined);
        updateEnemies(delta);
        // Vẽ thông tin wave
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

    // Xử lý sự kiện chạm màn hình
    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        lastTouch.set(x, y, 0);  // Lưu vị trí chạm
        return false;
    }

    // Xử lý sự kiện kéo màn hình
    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        // Chuyển đổi khoảng cách kéo từ tọa độ màn hình sang tọa độ thế giới
        float panSpeed = 1.0f;  // Hệ số tốc độ di chuyển camera
        // Tính toán khoảng cách di chuyển theo tỷ lệ màn hình
        float worldDeltaX = -deltaX * (camera.viewportWidth / Gdx.graphics.getWidth()) * panSpeed;
        float worldDeltaY = deltaY * (camera.viewportHeight / Gdx.graphics.getHeight()) * panSpeed;

        // Di chuyển camera trong giới hạn cho phép
        camera.position.x = clamp(camera.position.x + worldDeltaX, minCameraX, maxCameraX);
        camera.position.y = clamp(camera.position.y + worldDeltaY, minCameraY, maxCameraY);

        camera.update();  // Cập nhật camera
        return true;
    }

    // Giới hạn giá trị trong khoảng min-max
    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    // Các phương thức xử lý cử chỉ khác (không sử dụng)
    @Override public boolean tap(float x, float y, int count, int button) { return false; }
    @Override public boolean longPress(float x, float y) { return false; }
    @Override public boolean fling(float velocityX, float velocityY, int button) { return false; }
    @Override public boolean panStop(float x, float y, int pointer, int button) { return false; }
    @Override public boolean zoom(float initialDistance, float distance) { return false; }
    @Override public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) { return false; }
    @Override public void pinchStop() { }

    // Xử lý sự kiện thay đổi kích thước màn hình
    @Override
    public void resize(int width, int height) {
        // Cập nhật viewport theo kích thước mới
        viewport.update(width, height, true);

        // Tính toán lại kích thước thế giới game
        float worldWidth = width * (mapWidth / Gdx.graphics.getWidth());
        float worldHeight = height * (mapHeight / Gdx.graphics.getHeight());

        // Cập nhật giới hạn di chuyển camera
        minCameraX = width / 2;
        maxCameraX = worldWidth - width / 2;
        minCameraY = height / 2;
        maxCameraY = worldHeight - height / 2;

        // Đặt camera vào giữa màn hình
        camera.position.set(width/2, height/2, 0);
        camera.update();

        // Ghi log thông tin resize
        Gdx.app.log("GameScreen", String.format(
            "Resize to: %dx%d, World: %.0fx%.0f",
            width, height, worldWidth, worldHeight
        ));
    }

    // Các phương thức vòng đời màn hình
    @Override
    public void show() {}    // Được gọi khi màn hình được hiển thị

    @Override
    public void hide() {}    // Được gọi khi màn hình bị ẩn

    @Override
    public void pause() {}   // Được gọi khi game tạm dừng

    @Override
    public void resume() {}  // Được gọi khi game tiếp tục

    // Giải phóng tài nguyên
    @Override
    public void dispose() {
        if (map != null) map.dispose();                 // Giải phóng bản đồ
        if (renderer != null) renderer.dispose();       // Giải phóng renderer
        if (shapeRenderer != null) shapeRenderer.dispose();  // Giải phóng shape renderer
        if (waveManager != null) waveManager.dispose(); // Giải phóng wave manager
        // Giải phóng tất cả quái vật
        if (enemies != null) {
            for (Enemy enemy : enemies) {
                enemy.dispose();
            }
        }
    }
}
