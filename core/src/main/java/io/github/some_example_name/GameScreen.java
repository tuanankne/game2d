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
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
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
    private Array<TowerMenu> towerMenus;        // Danh sách menu xây dựng tháp
    private TileSelector tileSelector;         // Hiển thị ô được chọn
    private Array<Tower> towers;               // Danh sách tháp đã xây
    private Vector2 lastMousePos;              // Vị trí chuột cuối cùng
    private int tileWidth;                     // Chiều rộng của một ô tile
    private int tileHeight;                    // Chiều cao của một ô tile

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
        towers = new Array<>();                 // Mảng chứa tháp
        shapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();  // Công cụ vẽ debug
        lastTouch = new Vector3();              // Vector lưu vị trí chạm
        lastMousePos = new Vector2();           // Vector lưu vị trí chuột

        // Tạo và khởi tạo game với cấu hình
        MapConfig config = createMapConfig();   // Tạo cấu hình map và wave
        initializeGame(config);                 // Khởi tạo game với cấu hình

        // Tính toán kích thước thực của bản đồ
        tileWidth = map.getProperties().get("tilewidth", Integer.class);      // Chiều rộng mỗi tile
        tileHeight = map.getProperties().get("tileheight", Integer.class);    // Chiều cao mỗi tile
        int mapWidthInTiles = map.getProperties().get("width", Integer.class);    // Số tile theo chiều rộng
        int mapHeightInTiles = map.getProperties().get("height", Integer.class);  // Số tile theo chiều cao
        mapWidth = tileWidth * mapWidthInTiles;    // Chiều rộng thực của map
        mapHeight = tileHeight * mapHeightInTiles; // Chiều cao thực của map

        // Khởi tạo menu xây dựng tháp và tile selector
        towerMenus = new Array<>();
        tileSelector = new TileSelector(tileWidth, tileHeight);

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
                        }
                    }
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

        // Wave 1: Quái thường với số lượng vừa phải
        WaveConfig wave1 = new WaveConfig(1.5f);  // 1.5 giây giữa mỗi lần sinh quái
        wave1.addEnemy(EnemyType.NORMAL, 8);      // 8 quái thường
        config.addWaveConfig(wave1);

        // Wave 2: Kết hợp quái thường và quái nhanh
        WaveConfig wave2 = new WaveConfig(1.2f);  // 1.2 giây giữa mỗi lần sinh quái
        wave2.addEnemy(EnemyType.NORMAL, 6);      // 6 quái thường
        wave2.addEnemy(EnemyType.FAST, 5);        // 5 quái nhanh
        config.addWaveConfig(wave2);

        // Wave 3: Thêm quái tank
        WaveConfig wave3 = new WaveConfig(1.0f);  // 1.0 giây giữa mỗi lần sinh quái
        wave3.addEnemy(EnemyType.NORMAL, 5);      // 5 quái thường
        wave3.addEnemy(EnemyType.FAST, 4);        // 4 quái nhanh
        wave3.addEnemy(EnemyType.TANK, 3);        // 3 quái tank
        config.addWaveConfig(wave3);

        // Wave 4: Đợt tấn công lớn
        WaveConfig wave4 = new WaveConfig(0.8f);  // 0.8 giây giữa mỗi lần sinh quái
        wave4.addEnemy(EnemyType.NORMAL, 8);      // 8 quái thường
        wave4.addEnemy(EnemyType.FAST, 6);        // 6 quái nhanh
        wave4.addEnemy(EnemyType.TANK, 4);        // 4 quái tank
        config.addWaveConfig(wave4);

        // Wave 5: Boss wave
        WaveConfig wave5 = new WaveConfig(0.5f);  // 0.5 giây giữa mỗi lần sinh quái
        wave5.addEnemy(EnemyType.NORMAL, 10);     // 10 quái thường
        wave5.addEnemy(EnemyType.FAST, 8);        // 8 quái nhanh
        wave5.addEnemy(EnemyType.TANK, 6);        // 6 quái tank
        config.addWaveConfig(wave5);

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
            }
        }
    }

    // Vẽ và cập nhật trạng thái game mỗi frame
    @SuppressWarnings("DefaultLocale")
    @Override
    public void render(float delta) {
        // Xóa màn hình với màu đen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Cập nhật camera
        camera.update();

        // 1. Vẽ map layers
        renderer.setView(camera);
        renderer.render();

        // 2. Cập nhật game logic
        waveManager.update(delta);
        Wave currentWave = waveManager.getCurrentWave();

        if (currentWave != null) {
            // Kiểm tra và sinh quái mới
            if (waveManager.shouldSpawnEnemy(delta)) {
                EnemyType type = currentWave.getNextEnemy();
                if (type != null) {
                    spawnEnemy(type);
                }
            }

            // Kiểm tra hoàn thành wave
//            Gdx.app.log("GameScreen", "enemies size: " + enemies.size + " wave complete: " + currentWave.isComplete());

            if (currentWave.isComplete()) {
                if (enemies.size == 0) {
                    // Không còn quái nào trên màn hình
                    if (!waveManager.isWaitingForNextWave()) {
                        // Bắt đầu đếm ngược để qua wave mới
                        waveManager.startWaitingForNextWave();
                        Gdx.app.log("GameScreen", "Wave cleared! Waiting 5 seconds before next wave...");
                    }
                }
            }
        }

        // Cập nhật vị trí chuột
        if (Gdx.input.isTouched()) {
            Vector3 mousePos = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            lastMousePos.set(mousePos.x, mousePos.y);
        }

        // 3. Vẽ game world
        game.batch.begin();
        game.batch.setProjectionMatrix(camera.combined);

        // Vẽ obstacles
        MapLayer obstacleLayer = map.getLayers().get("Obstacle Layer");
        if (obstacleLayer != null) {
            for (MapObject object : obstacleLayer.getObjects()) {
                if (object instanceof com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject) {
                    com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject tileObject =
                        (com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject) object;
                    if (tileObject.isVisible() && tileObject.getTile() != null) {
                        com.badlogic.gdx.graphics.g2d.TextureRegion texture = tileObject.getTile().getTextureRegion();
                        game.batch.draw(
                            texture,
                            tileObject.getX(),
                            tileObject.getY(),
                            tileObject.getOriginX(),
                            tileObject.getOriginY(),
                            texture.getRegionWidth(),
                            texture.getRegionHeight(),
                            tileObject.getScaleX(),
                            tileObject.getScaleY(),
                            tileObject.getRotation()
                        );
                    }
                }
            }
        }

        // Vẽ enemies
        updateEnemies(delta);
        waveManager.render(game.batch);

        // Vẽ towers
        for (Tower tower : towers) {
            tower.update(delta, enemies);
            tower.render(game.batch);
        }

        // Vẽ UI elements
        if (tileSelector != null) {
            tileSelector.render(game.batch);
        }
        for (TowerMenu menu : towerMenus) {
            if (menu.isVisible()) {
                menu.render(game.batch);
            }
        }

        game.batch.end();

        // 4. Vẽ UI overlay
        game.batch.begin();
        if (currentWave != null) {
            String waveInfo = String.format("Wave %d/%d - Enemies: %d",
                waveManager.getCurrentWaveIndex() + 1,
                waveManager.getTotalWaves(),
                enemies.size);
            game.font.draw(game.batch, waveInfo, 10, Gdx.graphics.getHeight() - 10);
        }
        game.batch.end();

        // 5. Vẽ debug info
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line);
        for (Enemy enemy : enemies) {
            enemy.renderDebug(shapeRenderer);
        }
        shapeRenderer.end();

    }

    // Kiểm tra xem một ô có thuộc Ground Layer và không bị che bởi Obstacle Layer không
    private boolean isValidGroundTile(int tileX, int tileY) {
        // Lấy layer từ map
        MapLayer groundMapLayer = map.getLayers().get("Ground Layer");
        MapLayer obstacleMapLayer = map.getLayers().get("Obstacle Layer");

        // Log thông tin về Obstacle Layer
        Gdx.app.debug("GameScreen", "Checking Obstacle Layer: " +
            (obstacleMapLayer != null ? "Found" : "Not found") +
            (obstacleMapLayer instanceof TiledMapTileLayer ? " (TiledMapTileLayer)" : " (Not TiledMapTileLayer)"));

        // Kiểm tra xem layer có tồn tại và có phải là TiledMapTileLayer không
        if (!(groundMapLayer instanceof TiledMapTileLayer)) {
            Gdx.app.error("GameScreen", "Ground Layer không phải là TiledMapTileLayer");
            return false;
        }

        TiledMapTileLayer groundLayer = (TiledMapTileLayer) groundMapLayer;

        // Kiểm tra xem có phải là ô Ground Layer không
        Cell groundCell = groundLayer.getCell(tileX, tileY);
        if (groundCell == null) return false;

        // Kiểm tra xem có bị che bởi Obstacle Layer không
        if (obstacleMapLayer != null) {
            MapObjects objects = obstacleMapLayer.getObjects();
            float worldX = tileX * tileWidth;
            float worldY = tileY * tileHeight;

            for (MapObject object : objects) {
                if (object instanceof com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject) {
                    com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject tileObject =
                        (com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject) object;

                    float obstacleX = tileObject.getX();
                    float obstacleY = tileObject.getY();

                    // Kiểm tra xem có phải cùng một ô không
                    if (Math.floor(worldX / tileWidth) == Math.floor(obstacleX / tileWidth) &&
                        Math.floor(worldY / tileHeight) == Math.floor(obstacleY / tileHeight)) {
                        Gdx.app.debug("GameScreen", String.format("Tile [%d,%d] has obstacle", tileX, tileY));
                        return false;
                    }
                }
            }
        }

        return true;
    }

    // Xử lý sự kiện chạm màn hình
    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        lastTouch.set(x, y, 0);  // Lưu vị trí chạm

        // Chuyển đổi tọa độ màn hình thành tọa độ thế giới
        Vector3 worldCoords = camera.unproject(new Vector3(x, y, 0));

        // Nếu có menu đang hiển thị, kiểm tra xem có click vào menu không
        if (towerMenus.size > 0) {
            for (TowerMenu menu : towerMenus) {
                if (menu.isVisible()) {
                    int selectedOption = menu.checkClick(worldCoords.x, worldCoords.y);
                    if (selectedOption != -1) {
                            if (selectedOption == 3) {  // Click vào nút Cancel
                                hideAllMenus();
                                tileSelector.hide();
                            } else {  // Click vào một trong các tháp
                                // Xác định loại tháp dựa vào selectedOption
                                Tower.Type towerType;
                                switch (selectedOption) {
                                    case 0:
                                        towerType = Tower.Type.CANNON;
                                        break;
                                    case 1:
                                        towerType = Tower.Type.MISSILE;
                                        break;
                                    case 2:
                                        towerType = Tower.Type.LASER;
                                        break;
                                    default:
                                        return true;
                                }

                                // Lấy vị trí tile đã chọn từ TileSelector
                                float tileX = tileSelector.getSelectedTileX();
                                float tileY = tileSelector.getSelectedTileY();

                                // Tạo tháp mới với vị trí tile đã chọn
                                Tower tower = new Tower(towerType, tileX, tileY, tileWidth);
                                towers.add(tower);

                                hideAllMenus();
                                tileSelector.hide();
                            }
                        return true;
                    }
                }
            }
            // Nếu click ra ngoài menu, ẩn tất cả menu
            hideAllMenus();
            tileSelector.hide();
            return true;
        }

        // Tính toán vị trí ô tile được click
        int tileX = (int) (worldCoords.x / tileWidth);
        int tileY = (int) (worldCoords.y / tileHeight);

        // Kiểm tra xem có click vào quái không
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                float dx = enemy.getX() - worldCoords.x;
                float dy = enemy.getY() - worldCoords.y;
                if (dx * dx + dy * dy < 20 * 20) { // Bán kính click 20 pixel
                    // Tìm tháp gần nhất có thể bắn quái này
                    for (Tower tower : towers) {
                        if (tower.isInRange(enemy)) {
                            tower.setManualTarget(enemy);
                            tower.toggleRange(); // Hiển thị vùng tầm bắn
                            return true;
                        }
                    }
                }
            }
        }

        // Kiểm tra xem có click vào tháp không
        for (Tower tower : towers) {
            Vector2 towerPos = tower.getPosition();
            float towerSize = tower.getTileSize();
            if (worldCoords.x >= towerPos.x && worldCoords.x < towerPos.x + towerSize &&
                worldCoords.y >= towerPos.y && worldCoords.y < towerPos.y + towerSize) {
                // Click vào tháp, hiển thị/ẩn vùng tầm bắn
                tower.toggleRange();
                return true;
            }
        }

        // Kiểm tra xem có phải là ô Ground Layer hợp lệ không
        if (isValidGroundTile(tileX, tileY)) {
            // Hiển thị overlay tại ô được chọn
            tileSelector.selectTile(tileX, tileY);

            // Tạo 4 menu ở 4 hướng quanh ô được chọn
            for (int i = 0; i < 4; i++) {
                Vector2 menuPos = tileSelector.getMenuPosition(i);
                if (menuPos != null) {
                    TowerMenu menu = new TowerMenu();
                    menu.setOptionType(i);
                    menu.show(menuPos.x, menuPos.y);
                    towerMenus.add(menu);
                }
            }

            return true;
        }

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

    // Ẩn và xóa tất cả menu
    private void hideAllMenus() {
        for (TowerMenu menu : towerMenus) {
            menu.dispose();
        }
        towerMenus.clear();
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
    @SuppressWarnings("DefaultLocale")
    @Override
    public void resize(int width, int height) {
        // Cập nhật viewport theo kích thước mới
        viewport.update(width, height, true);

        // Tính toán lại kích thước thế giới game
        float worldWidth = width * (mapWidth / Gdx.graphics.getWidth());
        float worldHeight = height * (mapHeight / Gdx.graphics.getHeight());

        // Cập nhật giới hạn di chuyển camera
        minCameraX = (float) width / 2;
        maxCameraX = worldWidth - (float) width / 2;
        minCameraY = (float) height / 2;
        maxCameraY = worldHeight - (float) height / 2;

        // Đặt camera vào giữa màn hình
        camera.position.set((float) width /2, (float) height /2, 0);
        camera.update();

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
        // Giải phóng tất cả tháp
        if (towers != null) {
            for (Tower tower : towers) {
                tower.dispose();
            }
        }
        hideAllMenus();
        if (tileSelector != null) {
            tileSelector.dispose();
        }
    }
}
