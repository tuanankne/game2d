package io.github.some_example_name;

// Import các thư viện cần thiết từ libGDX
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector3;
import io.github.some_example_name.ui.MenuScreen;


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
    private float animationTime = 0;             // Thời gian hiệu ứng sao
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
        NumberRenderer.initialize();            // Khởi tạo renderer cho số
        StarRating.initialize();                // Khởi tạo hệ thống sao
        GameControls.initialize();              // Khởi tạo nút điều khiển
        PauseScreen.initialize();               // Khởi tạo màn hình pause
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

                            // Tạo danh sách điểm kiểm soát cho đường cong
                            Array<Vector2> controlPoints = new Array<>();
                            for (int i = 0; i < vertices.length; i += 2) {
                                controlPoints.add(new Vector2(vertices[i], vertices[i + 1]));
                            }

                            // Tạo đường đi mới với các điểm chi tiết hơn
                            Array<Vector2> path = generateSmoothPath(controlPoints);
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

    // Tạo đường đi với góc bo tròn
    private Array<Vector2> generateSmoothPath(Array<Vector2> controlPoints) {
        Array<Vector2> smoothPath = new Array<>();
        int segments = 4; // Số điểm cho mỗi đoạn cong
        float baseCornerRadius = 40f; // Bán kính cơ bản nhỏ hơn

        // Thêm điểm đầu tiên
        smoothPath.add(controlPoints.first());

        // Xử lý từng đoạn
        for (int i = 1; i < controlPoints.size - 1; i++) {
            Vector2 prev = controlPoints.get(i - 1);
            Vector2 current = controlPoints.get(i);
            Vector2 next = controlPoints.get(i + 1);

            // Tính vector hướng và độ dài của hai đoạn
            Vector2 dir1 = new Vector2(current.x - prev.x, current.y - prev.y);
            Vector2 dir2 = new Vector2(next.x - current.x, next.y - current.y);
            float len1 = dir1.len();
            float len2 = dir2.len();
            dir1.nor();
            dir2.nor();

            // Tính góc giữa hai đoạn
            float angle = Math.abs(dir1.angle(dir2));
            
            // Chỉ làm cong nếu góc đủ lớn
            if (angle > 30) { // Giảm ngưỡng góc xuống để xử lý cả góc nhọn
                // Điều chỉnh bán kính dựa trên góc và độ dài đoạn
                float cornerRadius = Math.min(
                    baseCornerRadius,
                    Math.min(len1, len2) * 0.25f // Giảm tỷ lệ xuống để tránh cong quá rộng
                );

                // Điều chỉnh bán kính theo góc
                if (angle > 120) { // Góc tù
                    cornerRadius *= 0.7f; // Giảm bán kính cho góc tù
                } else if (angle < 60) { // Góc nhọn
                    cornerRadius *= 0.5f; // Giảm mạnh bán kính cho góc nhọn
                }

                // Tính điểm bắt đầu và kết thúc của đoạn cong
                Vector2 cornerStart = new Vector2(
                    current.x - dir1.x * cornerRadius,
                    current.y - dir1.y * cornerRadius
                );
                Vector2 cornerEnd = new Vector2(
                    current.x + dir2.x * cornerRadius,
                    current.y + dir2.y * cornerRadius
                );

                // Thêm điểm từ điểm trước đến điểm bắt đầu cong
                if (smoothPath.size > 0) {
                    Vector2 lastPoint = smoothPath.get(smoothPath.size - 1);
                    if (!lastPoint.epsilonEquals(cornerStart, 1f)) {
                        smoothPath.add(cornerStart);
                    }
                }

                // Điều chỉnh hệ số điểm kiểm soát dựa trên góc
                float controlScale = 0.8f; // Hệ số cố định để giảm độ cong
            
            // Tạo đoạn cong với đường cong Bézier bậc ba
            for (int j = 1; j <= segments; j++) {
                float t = j / (float)segments;
                float t2 = t * t;
                float t3 = t2 * t;
                float mt = 1 - t;
                float mt2 = mt * mt;
                float mt3 = mt2 * mt;
                
                // Điểm kiểm soát với độ cong thích ứng
                Vector2 control1 = new Vector2(
                    current.x - dir1.x * cornerRadius * controlScale,
                    current.y - dir1.y * cornerRadius * controlScale
                );
                Vector2 control2 = new Vector2(
                    current.x + dir2.x * cornerRadius * controlScale,
                    current.y + dir2.y * cornerRadius * controlScale
                );
                
                // Công thức Bézier bậc ba với trọng số thích ứng
                float x = mt3 * cornerStart.x + 
                         3 * mt2 * t * control1.x + 
                         3 * mt * t2 * control2.x + 
                         t3 * cornerEnd.x;
                float y = mt3 * cornerStart.y + 
                         3 * mt2 * t * control1.y + 
                         3 * mt * t2 * control2.y + 
                         t3 * cornerEnd.y;
                    smoothPath.add(new Vector2(x, y));
                }

                // Thêm điểm kết thúc cong
                smoothPath.add(cornerEnd);
            } else {
                // Nếu góc quá nhỏ, giữ nguyên điểm
                smoothPath.add(current);
            }
        }

        // Thêm điểm cuối cùng
        smoothPath.add(controlPoints.get(controlPoints.size - 1));

        return smoothPath;
    }

    // Tạo cấu hình cho map và các wave
    private MapConfig createMapConfig() {
        // Khởi tạo tiền tệ và máu người chơi
        Currency.initialize(500);
        PlayerHealth.initialize();

        // Tạo cấu hình map với đường dẫn, tên layer path và thời gian giữa các wave
        MapConfig config = new MapConfig("map1/map1.tmx", "path", 5f);

        // Wave 1: Quái thường với số lượng vừa phải
        WaveConfig wave1 = new WaveConfig(1.5f);  // 1.5 giây giữa mỗi lần sinh quái
        wave1.addEnemy(Enemy.Type.NORMAL, 8, 100, 100);      // 8 quái thường
        config.addWaveConfig(wave1);

        // Wave 2: Kết hợp quái thường và quái nhanh
        WaveConfig wave2 = new WaveConfig(1.2f);  // 1.2 giây giữa mỗi lần sinh quái
        wave2.addEnemy(Enemy.Type.NORMAL, 6, 120, 100);      // 6 quái thường
        wave2.addEnemy(Enemy.Type.FAST, 5, 80, 150);        // 5 quái nhanh
        config.addWaveConfig(wave2);

        // Wave 3: Thêm quái tank
        WaveConfig wave3 = new WaveConfig(1.0f);  // 1.0 giây giữa mỗi lần sinh quái
        wave3.addEnemy(Enemy.Type.NORMAL, 5, 140, 100);      // 5 quái thường
        wave3.addEnemy(Enemy.Type.FAST, 4, 100, 150);        // 4 quái nhanh
        wave3.addEnemy(Enemy.Type.TANK, 3, 200, 50);        // 3 quái tank
        config.addWaveConfig(wave3);

        // Wave 4: Đợt tấn công lớn
        WaveConfig wave4 = new WaveConfig(0.8f);  // 0.8 giây giữa mỗi lần sinh quái
        wave4.addEnemy(Enemy.Type.NORMAL, 8, 160, 100);      // 8 quái thường
        wave4.addEnemy(Enemy.Type.FAST, 6, 120, 150);        // 6 quái nhanh
        wave4.addEnemy(Enemy.Type.TANK, 4, 250, 50);        // 4 quái tank
        config.addWaveConfig(wave4);

        // Wave 5: Boss wave
        WaveConfig wave5 = new WaveConfig(0.5f);  // 0.5 giây giữa mỗi lần sinh quái
        wave5.addEnemy(Enemy.Type.NORMAL, 10, 180, 100);     // 10 quái thường
        wave5.addEnemy(Enemy.Type.FAST, 8, 140, 150);        // 8 quái nhanh
        wave5.addEnemy(Enemy.Type.TANK, 6, 300, 50);        // 6 quái tank
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
    private void spawnEnemy(Enemy.Type type) {
        if (paths.size > 0) {
            // Chọn ngẫu nhiên một đường đi cho quái
            int pathIndex = MathUtils.random(paths.size - 1);
            Array<Vector2> path = paths.get(pathIndex);

            if (path.size > 0) {
                // Lấy điểm bắt đầu của đường đi
                Vector2 startPoint = path.first();
                // Lấy thông số của quái từ wave hiện tại
                float health = waveManager.getCurrentWave().getNextEnemyHealth();
                float speed = waveManager.getCurrentWave().getNextEnemySpeed();
                
                // Tạo quái mới tại điểm bắt đầu với thông số từ wave
                Enemy enemy = new Enemy(startPoint.x, startPoint.y, type, health, speed);
                enemy.setPath(path);  // Thiết lập đường đi cho quái
                enemies.add(enemy);   // Thêm quái vào danh sách

            }
        }
    }

    // Cập nhật trạng thái và vẽ tất cả quái
    private void updateEnemies(float delta) {
        Wave currentWave = waveManager.getCurrentWave();
        int aliveCount = 0;
        int deadCount = 0;

        // Duyệt ngược danh sách quái để có thể xóa an toàn
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.update(delta);     // Cập nhật trạng thái
            enemy.render(game.batch);  // Vẽ quái

            // Kiểm tra trạng thái của enemy
            if (enemy.isAlive()) {
                aliveCount++;
                // Chỉ kiểm tra hasReachedEnd() cho enemy còn sống
                if (enemy.hasReachedEnd()) {
                    PlayerHealth.takeDamage(enemy.getType());  // Gây sát thương cho người chơi
                    enemies.removeIndex(i);  // Xóa khỏi danh sách
                    enemy.dispose();         // Giải phóng tài nguyên
                }
            } else {
                deadCount++;
                currentWave.onEnemyKilled();
                enemies.removeIndex(i);  // Xóa khỏi danh sách
                enemy.dispose();         // Giải phóng tài nguyên
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
        animationTime += delta * GameControls.getGameSpeed();
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
                Enemy.Type type = currentWave.getNextEnemy();
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
                        if (waveManager.getCurrentWaveIndex() == waveManager.getTotalWaves() - 1) {
                            // Đã hoàn thành tất cả các wave
                            StarRating.resetAnimation();
                            waveManager.startWaitingForNextWave();
                            int stars = StarRating.calculateStars(PlayerHealth.getCurrentHealth(), PlayerHealth.getMaxHealth());
                            Gdx.app.log("GameScreen", String.format("Map completed with %d stars!", stars));
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

        // Bỏ qua vẽ debug info để không hiển thị đường đi

        // 5. Vẽ UI overlay (wave info và currency)
        game.batch.begin();
        // Sử dụng projection matrix mặc định cho UI
        game.batch.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        
        if (currentWave != null) {
            // Vẽ thông tin wave
            String waveInfo = String.format("Wave %d/%d - Enemies: %d",
                waveManager.getCurrentWaveIndex() + 1,
                waveManager.getTotalWaves(),
                enemies.size);
            
            float x = 10;
            float y = Gdx.graphics.getHeight() - 10;
            
            // Vẽ text với màu đen làm viền
            game.font.setColor(0, 0, 0, 1);
            game.font.draw(game.batch, waveInfo, x - 1, y - 1);
            game.font.draw(game.batch, waveInfo, x + 1, y - 1);
            game.font.draw(game.batch, waveInfo, x - 1, y + 1);
            game.font.draw(game.batch, waveInfo, x + 1, y + 1);
            
            // Vẽ text chính với màu trắng
            game.font.setColor(1, 1, 1, 1);
            game.font.draw(game.batch, waveInfo, x, y);

            // Vẽ thông tin tiền tệ
            float coinSize = 30;
            float coinX = x + 300;
            float coinY = y;
            
            // Vẽ icon coin
            game.batch.setColor(1, 1, 1, 1);  // Màu trắng cho icon
            game.batch.draw(Currency.getCoinTexture(), coinX, coinY - coinSize/2, coinSize, coinSize);
            
            // Vẽ số tiền bằng texture, cùng kích thước với coin
            NumberRenderer.drawNumber(game.batch, Currency.getMoney(), coinX + coinSize - 5, coinY - coinSize/2, coinSize);

            // Vẽ thanh máu người chơi
            PlayerHealth.render(game.batch, game.font, Gdx.graphics.getWidth());

            // Vẽ nút điều khiển
            GameControls.render(game.batch, game.font);

            // Vẽ màn hình pause nếu game đang pause
            if (GameControls.isPaused()) {
                PauseScreen.render(game, game.font);
            }

            // Vẽ thông báo Wave (luôn vẽ cuối cùng để hiển thị trên cùng)
            // Lưu lại blend function hiện tại
            int srcFunc = game.batch.getBlendSrcFunc();
            int dstFunc = game.batch.getBlendDstFunc();
            
            // Thiết lập blend function để text hiển thị rõ hơn
            game.batch.enableBlending();
            game.batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
            
            waveManager.render(game.batch);
        
        // Hiển thị số sao nếu đã hoàn thành tất cả wave
        if (waveManager.isFinished() || 
            (waveManager.getCurrentWaveIndex() == waveManager.getTotalWaves() - 1 && 
             waveManager.getCurrentWave() != null && 
             waveManager.getCurrentWave().isComplete() && 
             enemies.size == 0)) {
            
            int stars = StarRating.calculateStars(PlayerHealth.getCurrentHealth(), PlayerHealth.getMaxHealth());
            float starSize = 80; // Kích thước sao lớn hơn
            float centerX = Gdx.graphics.getWidth() / 2;
            float centerY = Gdx.graphics.getHeight() / 2;
            
            // Kết thúc SpriteBatch hiện tại
            game.batch.end();
            
            // Vẽ nút xác nhận với ShapeRenderer
            float buttonWidth = 200;
            float buttonHeight = 60;
            float buttonX = centerX - buttonWidth/2;
            float buttonY = centerY - starSize - 40;

            // Kiểm tra xem chuột có hover trên nút không
            boolean isHovered = Gdx.input.getX() >= buttonX && 
                              Gdx.input.getX() <= buttonX + buttonWidth &&
                              Gdx.graphics.getHeight() - Gdx.input.getY() >= buttonY && 
                              Gdx.graphics.getHeight() - Gdx.input.getY() <= buttonY + buttonHeight;

            // Vẽ nền nút với màu khác khi hover
            Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
            Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);
            
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            if (isHovered) {
                shapeRenderer.setColor(0.4f, 0.4f, 0.4f, 0.9f); // Màu xám đậm khi hover
            } else {
                shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.8f); // Màu xám nhạt bình thường
            }
            shapeRenderer.rect(buttonX, buttonY, buttonWidth, buttonHeight);
            shapeRenderer.end();

            Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

            // Bắt đầu SpriteBatch mới để vẽ sao và text
            game.batch.begin();
            
            // Vẽ sao với hiệu ứng
            StarRating.render(game.batch, stars, centerX, centerY, starSize, true);

            // Vẽ text "Continue" trên nút
            game.font.setColor(Color.WHITE);
            String buttonText = "Continue";
            GlyphLayout glyphLayout = new GlyphLayout(game.font, buttonText);
            float textWidth = glyphLayout.width;
            float textHeight = glyphLayout.height;
            game.font.draw(game.batch, buttonText, 
                         buttonX + (buttonWidth - textWidth)/2, 
                         buttonY + (buttonHeight + textHeight)/2);

            // Kiểm tra click vào nút
            if (isHovered && Gdx.input.justTouched()) {
                dispose(); // Giải phóng tài nguyên trước
                game.setScreen(new MenuScreen(game)); // Chuyển về màn hình menu chính
            }
        }
        
            // Khôi phục blend function
            game.batch.setBlendFunction(srcFunc, dstFunc);
        }
        game.batch.end();

        // 5. Vẽ debug info nếu không trong trạng thái pause
        if (!GameControls.isPaused()) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line);
            for (Enemy enemy : enemies) {
                enemy.renderDebug(shapeRenderer);
            }
            shapeRenderer.end();
        }

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
        
        // Kiểm tra click vào nút điều khiển
        int controlResult = GameControls.handleClick(x, Gdx.graphics.getHeight() - y);
        if (controlResult > 0) {
            return true;
        }

        // Kiểm tra click trong màn hình pause
        if (GameControls.isPaused()) {
            int pauseResult = PauseScreen.checkClick(x, Gdx.graphics.getHeight() - y);
            if (pauseResult == 1) {
                // Resume game
                GameControls.setPaused(false);
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
                                // Lấy loại tháp từ menu được chọn
                                Tower.Type towerType = menu.getTowerType();

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
                                }

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
        PlayerHealth.dispose();                         // Giải phóng tài nguyên thanh máu
        NumberRenderer.dispose();                       // Giải phóng texture số
        StarRating.dispose();                          // Giải phóng texture sao
        GameControls.dispose();                        // Giải phóng texture nút điều khiển
        PauseScreen.dispose();                         // Giải phóng texture màn hình pause
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
