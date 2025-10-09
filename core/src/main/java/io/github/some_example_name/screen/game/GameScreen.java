package io.github.some_example_name.screen.game;

// Import các thư viện cần thiết từ libGDX
import static com.badlogic.gdx.Gdx.graphics;
import static com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector3;

import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;

import io.github.some_example_name.screen.menu.MenuScreen;
import io.github.some_example_name.utils.GameControls;
import io.github.some_example_name.Main;
import io.github.some_example_name.config.map.MapProgress;
import io.github.some_example_name.screen.ui.TileSelector;
import io.github.some_example_name.screen.ui.TowerMenu;
import io.github.some_example_name.screen.ui.TowerUpgradeMenu;
import io.github.some_example_name.config.wave.WaveManager;
import io.github.some_example_name.config.map.MapConfig;
import io.github.some_example_name.config.map.MapConfigFactory;
import io.github.some_example_name.config.map.MapType;
import io.github.some_example_name.config.wave.WaveConfig;
import io.github.some_example_name.entities.enemy.Enemy;
import io.github.some_example_name.entities.obstacle.Obstacle;
import io.github.some_example_name.entities.tower.Tower;
import io.github.some_example_name.entities.tower.TowerType;
import io.github.some_example_name.mechanics.wave.Wave;
import io.github.some_example_name.screen.menu.MapSelectionScreen;
import io.github.some_example_name.utils.Currency;
import io.github.some_example_name.utils.GameStats;
import io.github.some_example_name.utils.NumberRenderer;
import io.github.some_example_name.utils.PlayerHealth;
import io.github.some_example_name.utils.GameSoundManager;
import io.github.some_example_name.utils.StarRating;
import io.github.some_example_name.screen.ui.MusicManager;

// Lớp quản lý màn hình chơi game chính, xử lý input cử chỉ người chơi
public class GameScreen implements Screen, GestureListener {
    private final Main game;                    // Tham chiếu đến game chính
    private boolean starsCalculated = false;    // Đã tính sao chưa

    /**
     * Kiểm tra xem game đã hoàn thành chưa
     */
    private boolean isGameCompleted() {
        boolean isFinished = waveManager.isFinished();
        boolean isLastWave = waveManager.getCurrentWaveIndex() == waveManager.getTotalWaves() - 1;
        boolean waveExists = waveManager.getCurrentWave() != null;
        boolean waveComplete = waveExists && waveManager.getCurrentWave().isComplete();
        boolean noEnemies = enemies.size == 0;
        
        // Debug log
        if (isLastWave && waveComplete && noEnemies) {
            Gdx.app.log("GameScreen", String.format(
                "Win conditions: isFinished=%b, isLastWave=%b, waveExists=%b, waveComplete=%b, noEnemies=%b (enemies=%d)",
                isFinished, isLastWave, waveExists, waveComplete, noEnemies, enemies.size
            ));
        }
        
        return isFinished || (isLastWave && waveComplete && noEnemies);
    }
    private final OrthographicCamera camera;          // Camera theo dõi game
    private TiledMap map;                       // Bản đồ tile
    private OrthogonalTiledMapRenderer renderer;// Renderer cho bản đồ
    private final Array<Enemy> enemies;               // Danh sách quái vật
    private float CAMERA_SPEED = 500f;          // Tốc độ di chuyển camera
    private final Array<Array<Vector2>> paths;        // Danh sách các đường đi của quái
    private WaveManager waveManager;            // Quản lý các đợt tấn công
    private final Viewport viewport;                  // Quản lý khung nhìn
    private final Vector3 lastTouch;                  // Vị trí chạm cuối cùng
    private final float mapWidth;                     // Chiều rộng bản đồ
    private final float mapHeight;                    // Chiều cao bản đồ
    private final ShapeRenderer shapeRenderer;  // Vẽ debug
    private final Array<TowerMenu> towerMenus;        // Danh sách menu xây dựng tháp
    private final TileSelector tileSelector;         // Hiển thị ô được chọn
    private final Array<Tower> towers;               // Danh sách tháp đã xây
    private TowerUpgradeMenu upgradeMenu;           // Menu nâng cấp tháp
    private final Array<Obstacle> obstacles;         // Danh sách obstacle
    private final Vector2 lastMousePos;              // Vị trí chuột cuối cùng
    private final int tileWidth;                     // Chiều rộng của một ô tile
    private final int tileHeight;                    // Chiều cao của một ô tile
    private final MapType currentMap;                // Map hiện tại đang chơi

    // Các giới hạn di chuyển cho camera
    private float minCameraX;                   // Giới hạn trái
    private float maxCameraX;                   // Giới hạn phải
    private float minCameraY;                   // Giới hạn dưới
    private float maxCameraY;                   // Giới hạn trên

    // Constructor khởi tạo màn hình game
    public GameScreen(final Main game, MapType mapType) {
        this.game = game;
        this.currentMap = mapType;

        // Bật chế độ debug log
        Gdx.app.setLogLevel(Logger.DEBUG);

        // Khởi tạo các mảng và công cụ cần thiết
        enemies = new Array<>();                // Mảng chứa quái vật
        paths = new Array<>();                  // Mảng chứa đường đi
        towers = new Array<>();                 // Mảng chứa tháp
        obstacles = new Array<>();              // Mảng chứa obstacle
        shapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();  // Công cụ vẽ debug
        lastTouch = new Vector3();              // Vector lưu vị trí chạm
        lastMousePos = new Vector2();           // Vector lưu vị trí chuột

        // Tạo và khởi tạo game với cấu hình
        NumberRenderer.initialize();            // Khởi tạo renderer cho số
        StarRating.initialize();                // Khởi tạo hệ thống sao
        GameControls.initialize();              // Khởi tạo nút điều khiển
        PauseScreen.initialize();               // Khởi tạo màn hình pause
        GameSoundManager.initialize();          // Khởi tạo game sound manager
        MapConfig config = MapConfigFactory.createConfig(mapType);   // Tạo cấu hình map và wave
        initializeGame(config);                 // Khởi tạo game với cấu hình
        
        // Khởi tạo GameStats
        GameStats.initialize();
        GameStats.startGame();

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
        upgradeMenu = new TowerUpgradeMenu(game.font);

        // Thiết lập camera và viewport
        float screenWidth = graphics.getWidth();    // Chiều rộng màn hình
        float screenHeight = graphics.getHeight();  // Chiều cao màn hình
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

            // Tạo đường đi trực tiếp từ các điểm trên polyline
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

    // Sinh quái mới
    private void spawnEnemy() {
        if (paths.size > 0) {
            // Lấy thông tin quái tiếp theo từ wave hiện tại
            Wave currentWave = waveManager.getCurrentWave();
            Object[] nextEnemy = currentWave.getNextEnemy();

            if (nextEnemy != null) {
                int pathIndex = (int)nextEnemy[0];
                Enemy.Type type = (Enemy.Type)nextEnemy[1];
                float health = (float)nextEnemy[2];
                float speed = (float)nextEnemy[3];

                Array<Vector2> path = paths.get(pathIndex);
                if (path.size > 0) {
                    // Lấy điểm bắt đầu của đường đi
                    Vector2 startPoint = path.first();

                    // Tạo quái mới tại điểm bắt đầu với thông số từ wave
                    Enemy enemy = new Enemy(startPoint.x, startPoint.y, type, health, speed);
                    enemy.setPath(path);  // Thiết lập đường đi cho quái
                    enemies.add(enemy);   // Thêm quái vào danh sách
                }
            }
        }
    }

    // Cập nhật trạng thái và vẽ tất cả quái
    private void updateEnemies(float delta) {
        Wave currentWave = waveManager.getCurrentWave();

        // Duyệt ngược danh sách quái để có thể xóa an toàn
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.update(delta);     // Cập nhật trạng thái
            enemy.render(game.batch);  // Vẽ quái

            // Kiểm tra trạng thái của enemy
            if (enemy.isAlive()) {
                // Chỉ kiểm tra hasReachedEnd() cho enemy còn sống
                if (enemy.hasReachedEnd()) {
                    PlayerHealth.takeDamage(enemy.getType());  // Gây sát thương cho người chơi
                    enemies.removeIndex(i);  // Xóa khỏi danh sách
                    enemy.dispose();         // Giải phóng tài nguyên
                }
            } else {
                // Enemy đã chết, kiểm tra xem có thể xóa khỏi danh sách không
                if (enemy.canBeRemoved()) {
                    currentWave.onEnemyKilled();
                    enemies.removeIndex(i);  // Xóa khỏi danh sách
                    enemy.dispose();         // Giải phóng tài nguyên
                }
                // Nếu chưa thể xóa, enemy sẽ tiếp tục chạy death animation
            }
        }

        // Log thông tin về số lượng quái
        // if (currentWave != null) {
        //     Gdx.app.log("GameScreen", String.format("Quái trên màn hình: %d (Còn sống: %d, Đã chết: %d, Tổng wave: %d)",
        //         enemies.size, aliveCount, currentWave.getEnemiesKilled(), currentWave.getTotalEnemies()));
        // }
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

        // Kiểm tra điều kiện Game Over
        if (PlayerHealth.isGameOver()) {
            game.setScreen(new GameOverScreen(game, currentMap));
            dispose();
            return;
        }

        waveManager.update(delta);
        Wave currentWave = waveManager.getCurrentWave();

        if (currentWave != null) {
            // Kiểm tra và sinh quái mới
            if (waveManager.shouldSpawnEnemy(delta)) {
                spawnEnemy();
            }

            // Kiểm tra hoàn thành wave
//            Gdx.app.log("GameScreen", "enemies size: " + enemies.size + " wave complete: " + currentWave.isComplete());

            if (currentWave.isComplete()) {
                if (enemies.size == 0) {
                    // Không còn quái nào trên màn hình
                    if (!waveManager.isWaitingForNextWave()) {
                        if (waveManager.getCurrentWaveIndex() == waveManager.getTotalWaves() - 1) {
                            // Đã hoàn thành tất cả các wave
                            StarRating.resetAnimation();
                            waveManager.startWaitingForNextWave();
                            Gdx.app.log("GameScreen", "All waves completed! Waiting for game completion...");
                        } else {
                            // Còn wave tiếp theo
                            waveManager.startWaitingForNextWave();
                            Gdx.app.log("GameScreen", "Wave cleared! Waiting 5 seconds before next wave...");
                        }
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

        // Tải obstacles từ map nếu chưa có
        if (obstacles.size == 0) {
            MapLayer obstacleLayer = map.getLayers().get("Obstacle Layer");
            if (obstacleLayer != null) {
                for (MapObject object : obstacleLayer.getObjects()) {
                    if (object instanceof TiledMapTileMapObject) {
                        TiledMapTileMapObject tileObject = (TiledMapTileMapObject) object;
                        if (tileObject.isVisible() && tileObject.getTile() != null) {
                            // Xác định loại obstacle dựa trên tile ID
                            int tileId = tileObject.getTile().getId() - 5; // ID trong Tiled bắt đầu từ 1
                            Gdx.app.debug("GameScreen", "Loading obstacle with tileId: " + tileId);
                            Obstacle.Type type;

                            // Map tile ID sang loại obstacle tương ứng
                            switch (tileId) {
                                case 206:
                                    type = Obstacle.Type.BUSH_CLUSTER;
                                    break;
                                case 207:
                                    type = Obstacle.Type.BUSH_SMALL;
                                    break;
                                case 208:
                                    type = Obstacle.Type.BUSH_MEDIUM;
                                    break;
                                case 210:
                                    type = Obstacle.Type.BUSH_LARGE;
                                    break;
                                case 211:
                                    type = Obstacle.Type.ROCK_SMALL;
                                    break;
                                case 212:
                                    type = Obstacle.Type.ROCK_HUGE;
                                    break;
                                case 213:
                                    type = Obstacle.Type.ROCK_LARGE;
                                    break;
                                case 28:
                                    type = Obstacle.Type.ROCK_01;
                                    break;
                                case 29:
                                    type = Obstacle.Type.ROCK_02;
                                    break;
                                case 30:
                                    type = Obstacle.Type.ROCK_03;
                                    break;
                                case 31:
                                    type = Obstacle.Type.ROCK_04;
                                    break;
                                case 32:
                                    type = Obstacle.Type.ROCK_05;
                                    break;
                                case 33:
                                    type = Obstacle.Type.TENT;
                                    break;
                                case 34:
                                    type = Obstacle.Type.TREA_SURE;
                                    break;
                                case 9:
                                    type = Obstacle.Type.TREE_LARGE;
                                    break;
                                case 35:
                                    type = Obstacle.Type.TREE_MEDIUM;
                                    break;
                                case 36:
                                    type = Obstacle.Type.TREE_SMALL;
                                    break;
                                case 37:
                                    type = Obstacle.Type.TREE_STUMP_SHORT;
                                    break;
                                case 38:
                                    type = Obstacle.Type.TREE_STUMP_TALL;
                                    break;
                                case 39:
                                    type = Obstacle.Type.WATCH_TOWER_SHORT;
                                    break;
                                case 40:
                                    type = Obstacle.Type.WATCH_TOWER_TALL;
                                    break;
                                case 41:
                                    type = Obstacle.Type.WELL;
                                    break;
                                case 10:
                                    type = Obstacle.Type.WIND_MILL;
                                    break;
                                case 11:
                                    type = Obstacle.Type.WOODEN_BARREL;
                                    break;
                                case 12:
                                    type = Obstacle.Type.WOODEN_CART;
                                    break;
                                default:
                                    // Nếu không khớp với ID nào, sử dụng thuộc tính type từ tile (nếu có)
                                    type = Obstacle.Type.BUSH_SMALL; // Mặc định
                                    if (tileObject.getProperties().containsKey("type")) {
                                        String typeStr = tileObject.getProperties().get("type", String.class);
                                        try {
                                            type = Obstacle.Type.valueOf(typeStr.toUpperCase());
                                        } catch (IllegalArgumentException e) {
                                            Gdx.app.error("GameScreen", "Invalid obstacle type: " + typeStr);
                                        }
                                    }
                                    break;
                            }

                            // Gdx.app.log("GameScreen", String.format("Loading obstacle with tileId %d as type %s",
                            //     tileId, type.name()));

                            // Tạo obstacle mới
                            Obstacle obstacle = new Obstacle(
                                tileObject.getX(),
                                tileObject.getY(),
                                tileObject.getScaleX() * tileWidth,
                                tileObject.getScaleY() * tileHeight,
                                type
                            );
                            obstacles.add(obstacle);
                            // Gdx.app.log("GameScreen", String.format("Added %s obstacle at (%.1f,%.1f)",
                            //     type, tileObject.getX(), tileObject.getY()));
                        }
                    }
                }
            }
        }

        // Vẽ và cập nhật obstacles
        for (int i = obstacles.size - 1; i >= 0; i--) {
            Obstacle obstacle = obstacles.get(i);
            if (obstacle.isDestroyed()) {
                obstacles.removeIndex(i);
                // Gdx.app.log("GameScreen", String.format("Removed destroyed obstacle at (%.1f,%.1f)",
                //     obstacle.getX(), obstacle.getY()));
            } else {
                obstacle.render(game.batch);
            }
        }

        // Vẽ enemies
        updateEnemies(delta);

        // Vẽ towers
        for (int i = 0; i < towers.size; i++) {
            Tower tower = towers.get(i);
            tower.update(delta, enemies);
            tower.render(game.batch);
        }

        // Vẽ UI elements
        if (tileSelector != null) {
            tileSelector.render(game.batch);
        }
        for (int i = 0; i < towerMenus.size; i++) {
            TowerMenu menu = towerMenus.get(i);
            if (menu.isVisible()) {
                menu.render(game.batch);
            }
        }
        if (upgradeMenu != null && upgradeMenu.isVisible()) {
            upgradeMenu.render(game.batch);
        }

        game.batch.end();

        // Bỏ qua vẽ debug info để không hiển thị đường đi

        // 5. Vẽ UI overlay (wave info và currency)
        game.batch.begin();
        // Sử dụng projection matrix mặc định cho UI
        game.batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, graphics.getWidth(), graphics.getHeight()));

        // Vẽ UI game
        if (currentWave != null && !isGameCompleted()) {
            // Vẽ thông tin wave - to gấp đôi
            String waveInfo = String.format("Wave %d/%d - Enemies: %d",
                waveManager.getCurrentWaveIndex() + 1,
                waveManager.getTotalWaves(),
                enemies.size);

            float x = 10;
            float y = graphics.getHeight() - 10;
            
            // Tăng kích thước font gấp đôi
            game.font.getData().setScale(2.0f);

            // Vẽ text với màu đen làm viền (dày hơn cho font to)
            game.font.setColor(0, 0, 0, 1);
            game.font.draw(game.batch, waveInfo, x - 2, y - 2);
            game.font.draw(game.batch, waveInfo, x + 2, y - 2);
            game.font.draw(game.batch, waveInfo, x - 2, y + 2);
            game.font.draw(game.batch, waveInfo, x + 2, y + 2);

            // Vẽ text chính với màu trắng
            game.font.setColor(1, 1, 1, 1);
            game.font.draw(game.batch, waveInfo, x, y);
            
            // Reset font scale
            game.font.getData().setScale(1.0f);

            // Vẽ thông tin tiền tệ - to gấp đôi, cùng hàng với các nút điều khiển
            float coinSize = 60; // 30 * 2
            float coinX = graphics.getWidth() - 300; // Đặt ở góc phải trên
            float coinY = graphics.getHeight() - 30; // Cùng hàng với nút pause/speed

            // Vẽ icon coin
            game.batch.setColor(1, 1, 1, 1);  // Màu trắng cho icon
            game.batch.draw(Currency.getCoinTexture(), coinX, coinY - coinSize/2, coinSize, coinSize);

            // Vẽ số tiền bằng texture, cùng kích thước với coin
            NumberRenderer.drawNumber(game.batch, Currency.getMoney(), coinX + coinSize - 10, coinY - coinSize/2, coinSize);

            // Vẽ thanh máu người chơi
            PlayerHealth.render(game.batch, game.font, graphics.getWidth());

            // Vẽ nút điều khiển
            GameControls.render(game.batch, game.font);

            // Vẽ màn hình pause nếu game đang pause
            if (GameControls.isPaused()) {
                PauseScreen.render(game, game.font);
            }

            // Vẽ thông báo Wave
            int srcFunc = game.batch.getBlendSrcFunc();
            int dstFunc = game.batch.getBlendDstFunc();
            game.batch.enableBlending();
            game.batch.setBlendFunction(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            waveManager.render(game.batch);
            game.batch.setBlendFunction(srcFunc, dstFunc);
        }

        game.batch.end();

        // Chuyển sang màn hình thắng nếu đã hoàn thành tất cả wave
        if (isGameCompleted()) {
            // Cập nhật thống kê trước khi chuyển màn hình
            GameStats.updatePlayTime();
            // Sử dụng tổng số quái đã tiêu diệt làm tổng (vì đã hoàn thành game)
            int totalEnemies = GameStats.getEnemiesKilled();
            GameStats.setTotalEnemies(totalEnemies);

            // Mở khóa map tiếp theo trước khi chuyển màn hình (chỉ tính một lần)
            if (!starsCalculated) {
                int stars = StarRating.calculateStars(PlayerHealth.getCurrentHealth(), PlayerHealth.getMaxHealth());
                MapProgress.getInstance().updateMapStars(currentMap, stars);
                starsCalculated = true;
                Gdx.app.log("GameScreen", String.format("Map completed with %d stars! Next map unlocked.", stars));
            }

            // Chuyển sang màn hình thắng
            game.setScreen(new GameWinScreen(game, currentMap));
            dispose();
            return;
        }

        // 5. Vẽ debug info nếu không trong trạng thái pause
        if (!GameControls.isPaused()) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            // Vẽ debug info cho enemies
            for (Enemy enemy : enemies) {
                enemy.renderDebug(shapeRenderer);
            }

            // Vẽ debug info cho obstacles
            for (Obstacle obstacle : obstacles) {
                obstacle.renderDebug(shapeRenderer);
            }

            shapeRenderer.end();
        }

    }

    // Kiểm tra xem một ô có thuộc Ground Layer và không bị che bởi Obstacle Layer không
    private boolean isValidGroundTile(int tileX, int tileY) {
        // Dùng một TAG duy nhất để dễ lọc log
        final String TAG = "isValidGroundTile";


        MapLayer groundMapLayer = map.getLayers().get("Ground Layer");
        if (groundMapLayer == null) return false;
        if (!(groundMapLayer instanceof TiledMapTileLayer)) return false;
        TiledMapTileLayer groundLayer = (TiledMapTileLayer) groundMapLayer;

        Cell groundCell = groundLayer.getCell(tileX, tileY);
        if (groundCell == null) return false;

        for (Obstacle obstacle : obstacles) {
            if (!obstacle.isDestroyed()) {
                // Chuyển đổi tọa độ pixel của obstacle thành tọa độ tile
                int obstacleTileX = (int) (obstacle.getX() / tileWidth);
                int obstacleTileY = (int) (obstacle.getY() / tileHeight);

                // So sánh tọa độ tile
                if (tileX == obstacleTileX && tileY == obstacleTileY) return false;
            }
        }
        return true;
    }

    // Xử lý sự kiện chạm màn hình
    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        lastTouch.set(x, y, 0);  // Lưu vị trí chạm

        // Kiểm tra click vào nút điều khiển
        int controlResult = GameControls.handleClick(x, graphics.getHeight() - y);
        if (controlResult > 0) {
            return true;
        }

        // Kiểm tra click trong màn hình pause
        if (GameControls.isPaused()) {
            int pauseResult = PauseScreen.checkClick(x, graphics.getHeight() - y);
            if (pauseResult == 1) {
                // Resume game
                GameControls.setPaused(false);
                GameSoundManager.resumeBackgroundMusic(); // Tiếp tục nhạc nền
                return true;
            } else if (pauseResult == 2) {
                // Return to menu
                game.setScreen(new MenuScreen(game));
                dispose();
                return true;
            }
            return true; // Chặn click khi đang pause
        }

        // Chuyển đổi tọa độ màn hình thành tọa độ thế giới
        Vector3 worldCoords = camera.unproject(new Vector3(x, y, 0));

        // Kiểm tra click vào menu nâng cấp tháp
        if (upgradeMenu != null && upgradeMenu.isVisible()) {
            int action = upgradeMenu.checkClick(worldCoords.x, worldCoords.y);
            if (action != -1) {
                for (Tower tower : towers) {
                    if (tower.isShowingRange()) {
                        if (action == 0) { // Nâng cấp
                            int upgradeCost = tower.getUpgradeCost();
                            if (Currency.getMoney() >= upgradeCost) {
                                Currency.spendMoney(upgradeCost);
                                tower.upgrade();
                                
                                // Phát âm thanh nâng cấp thành công
                                GameSoundManager.playBuildSound();
                            }
                        } else if (action == 1) { // Bán
                            Currency.addMoney(tower.getSellValue());
                            towers.removeValue(tower, true);
                            tower.dispose();
                        }
                        tower.showRange(false);
                        upgradeMenu.hide();
                        break;
                    }
                }
                return true;
            }
            // Click ra ngoài menu
            for (Tower tower : towers) {
                tower.showRange(false);
            }
            upgradeMenu.hide();
            return true;
        }

        // Nếu có menu xây dựng tháp đang hiển thị, kiểm tra xem có click vào menu không
        if (towerMenus.size > 0) {
            for (TowerMenu menu : towerMenus) {
                if (menu.isVisible()) {
                    int selectedOption = menu.checkClick(worldCoords.x, worldCoords.y);
                    if (selectedOption != -1) {
                        // Click vào một trong các tháp
                        // Lấy loại tháp từ menu được chọn
                        TowerType towerType = menu.getTowerType();

                        // Lấy vị trí tile đã chọn từ TileSelector
                        float tileX = tileSelector.getSelectedTileX();
                        float tileY = tileSelector.getSelectedTileY();

                        // Kiểm tra xem có đủ tiền không
                        int cost = Currency.getCost(towerType);
                        if (Currency.canAfford(towerType)) {
                            // Tạo tháp mới với vị trí tile đã chọn
                            Tower tower = new Tower(towerType, tileX, tileY, tileWidth);
                            towers.add(tower);
                            Currency.spendMoney(cost);
                            
                            // Phát âm thanh xây tháp thành công
                            GameSoundManager.playBuildSound();
                        }

                        hideAllMenus();
                        tileSelector.hide();
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

        // Bỏ target tất cả quái và obstacle trước
        for (int i = 0; i < enemies.size; i++) {
            Enemy enemy = enemies.get(i);
            enemy.setTargeted(false);
        }
        for (int i = 0; i < obstacles.size; i++) {
            Obstacle obstacle = obstacles.get(i);
            if (obstacle.isTargeted()) {
                Gdx.app.log("GameScreen", "Removing target from obstacle at position: " + obstacle.getX() + "," + obstacle.getY());
            }
            obstacle.setTargeted(false);
        }

        // Kiểm tra xem có click vào obstacle không
        for (int i = 0; i < obstacles.size; i++) {
            Obstacle obstacle = obstacles.get(i);
            if (!obstacle.isDestroyed()) {
                float dx = obstacle.getX() + obstacle.getWidth()/2 - worldCoords.x;
                float dy = obstacle.getY() + obstacle.getHeight()/2 - worldCoords.y;
                float distSqr = dx * dx + dy * dy;
                if (distSqr < 30 * 30) { // Bán kính click cho obstacle
                    obstacle.setTargeted(true);
                    Gdx.app.log("GameScreen", String.format("%s targeted at (%.1f,%.1f), distance: %.1f",
                        obstacle.getType(), obstacle.getX(), obstacle.getY(), Math.sqrt(distSqr)));

                    // Kiểm tra và điều khiển các tháp để tấn công obstacle
                    boolean hasInRangeTower = false;
                    int inRangeTowers = 0;
                    int totalTowers = towers.size;

                    for (int j = 0; j < towers.size; j++) {
                        Tower tower = towers.get(j);
                        float towerDx = tower.getPosition().x + tower.getTileSize()/2 - (obstacle.getX() + obstacle.getWidth()/2);
                        float towerDy = tower.getPosition().y + tower.getTileSize()/2 - (obstacle.getY() + obstacle.getHeight()/2);
                        float towerDistSqr = towerDx * towerDx + towerDy * towerDy;

                        if (towerDistSqr <= tower.getRange() * tower.getRange()) {
                            tower.showRange(true);
                            tower.setManualObstacleTarget(obstacle);
                            hasInRangeTower = true;
                            inRangeTowers++;
                        } else {
                            tower.showRange(false);
                            tower.setManualObstacleTarget(null);
                        }
                    }

                    Gdx.app.log("GameScreen", String.format("Total towers in range of obstacle: %d/%d",
                        inRangeTowers, totalTowers));
                    return hasInRangeTower;
                }
            }
        }

        // Kiểm tra xem có click vào quái không
        Enemy targetedEnemy = null;
        for (int i = 0; i < enemies.size; i++) {
            Enemy enemy = enemies.get(i);
            if (enemy.isAlive()) {
                float dx = enemy.getX() - worldCoords.x;
                float dy = enemy.getY() - worldCoords.y;
                float distSqr = dx * dx + dy * dy;
                if (distSqr < 25 * 25) { // Bán kính click cho enemy
                    enemy.setTargeted(true);
                    targetedEnemy = enemy;
                    Gdx.app.log("GameScreen", String.format("Enemy targeted at (%.1f,%.1f), distance: %.1f",
                        enemy.getX(), enemy.getY(), Math.sqrt(distSqr)));
                    break;
                }
            }
        }

        // Nếu có quái được chọn, kiểm tra và điều khiển các tháp
        if (targetedEnemy != null) {
            boolean hasInRangeTower = false;
            int inRangeTowers = 0;
            int totalTowers = towers.size;

            // Kiểm tra từng tháp
            for (int i = 0; i < towers.size; i++) {
                Tower tower = towers.get(i);
                if (tower.isInRange(targetedEnemy)) {
                    tower.setManualTarget(targetedEnemy);
                    tower.showRange(true); // Hiển thị vùng tầm bắn
                    hasInRangeTower = true;
                    inRangeTowers++;
                } else {
                    tower.showRange(false); // Ẩn vùng tầm bắn nếu không trong tầm
                }
            }

            Gdx.app.log("GameScreen", String.format("Total towers in range: %d/%d", inRangeTowers, totalTowers));
            return hasInRangeTower; // Trả về true nếu có ít nhất một tháp trong tầm
        }

        // Kiểm tra xem có click vào tháp không
        for (int i = 0; i < towers.size; i++) {
            Tower tower = towers.get(i);
            Vector2 towerPos = tower.getPosition();
            float towerSize = tower.getTileSize();
            if (worldCoords.x >= towerPos.x && worldCoords.x < towerPos.x + towerSize &&
                worldCoords.y >= towerPos.y && worldCoords.y < towerPos.y + towerSize) {
                // Click vào tháp, hiển thị vùng tầm bắn và menu nâng cấp
                tower.showRange(true);

                // Hiển thị nút nâng cấp bên phải và nút bán bên trái tháp
                float centerX = towerPos.x + towerSize/2;
                float centerY = towerPos.y + towerSize/2;
                upgradeMenu.show(centerX, centerY, tower);

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
                    TowerMenu menu = new TowerMenu(game.font);
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
        float worldDeltaX = -deltaX * (camera.viewportWidth / graphics.getWidth()) * panSpeed;
        float worldDeltaY = deltaY * (camera.viewportHeight / graphics.getHeight()) * panSpeed;

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
        float worldWidth = width * (mapWidth / graphics.getWidth());
        float worldHeight = height * (mapHeight / graphics.getHeight());

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
    public void show() {
        // Dừng nhạc menu
        MusicManager.getInstance().stopMusic();
        
        // Phát nhạc nền game
        GameSoundManager.playBackgroundMusic();
    }

    @Override
    public void hide() {
        // Tạm dừng nhạc nền khi ẩn màn hình
        GameSoundManager.pauseBackgroundMusic();
    }

    @Override
    public void pause() {
        // Tạm dừng nhạc nền khi pause game
        GameSoundManager.pauseBackgroundMusic();
    }

    @Override
    public void resume() {
        // Tiếp tục nhạc nền khi resume game
        GameSoundManager.resumeBackgroundMusic();
    }

    // Giải phóng tài nguyên
    @Override
    public void dispose() {
        // Dừng nhạc game
        GameSoundManager.stopBackgroundMusic();
        
        if (map != null) map.dispose();                 // Giải phóng bản đồ
        if (renderer != null) renderer.dispose();       // Giải phóng renderer
        if (shapeRenderer != null) shapeRenderer.dispose();  // Giải phóng shape renderer
        if (waveManager != null) waveManager.dispose(); // Giải phóng wave manager
        if (upgradeMenu != null) upgradeMenu.dispose(); // Giải phóng menu nâng cấp
        PlayerHealth.dispose();                         // Giải phóng tài nguyên thanh máu
        NumberRenderer.dispose();                       // Giải phóng texture số
        StarRating.dispose();                          // Giải phóng texture sao
        GameControls.dispose();                        // Giải phóng texture nút điều khiển
        PauseScreen.dispose();                         // Giải phóng texture màn hình pause
        GameSoundManager.dispose();                    // Giải phóng âm thanh game
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

    public float getCAMERA_SPEED() {
        return CAMERA_SPEED;
    }

    public void setCAMERA_SPEED(float CAMERA_SPEED) {
        this.CAMERA_SPEED = CAMERA_SPEED;
    }
}
