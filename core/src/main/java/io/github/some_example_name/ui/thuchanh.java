package io.github.some_example_name.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

import io.github.some_example_name.Main;

/**
 * Màn hình thực hành: nền cuộn liên tục từ phải sang trái, khi hết sẽ lật ngược lại và chạy tiếp.
 * Ảnh nằm trong thư mục assets/thuchanh
 */
public class thuchanh implements Screen {
    private final Main game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    // Âm thanh
    private com.badlogic.gdx.audio.Sound victorySound;

    // Textures nền: dùng 1 ảnh nền, vẽ lặp để cuộn
    private Texture backgroundTexture;

    // Kích thước vẽ ảnh nền để vừa màn hình theo tỷ lệ
    private float drawWidth;
    private float drawHeight;

    // Vị trí x của hai tile nền để cuộn liên tục
    private float bgX1;
    private float bgX2;
    // Mỗi tile có trạng thái lật riêng
    private boolean tileFlipped1 = false; // tile đầu bình thường
    private boolean tileFlipped2 = true;  // tile thứ hai lật ngay từ đầu

    // Tốc độ cuộn (pixel/giây)
    private float scrollSpeed = 150f; // vừa phải

    // Luôn cuộn sang trái
    private static final int SCROLL_LEFT = -1;

    // Tàu người chơi và đạn
    private Texture playerTexture;           // thuchanh/tauta.png
    private Texture playerBulletTexture;     // thuchanh/danta1.png
    private float playerX;
    private float playerY;
    private float playerW;
    private float playerH;
    private float playerSpeedY = 800f;
    private float playerFireCooldown = 1.0f; // giây, bắn tự động mỗi 1s
    private float playerFireTimer = 0f;
    private boolean playerBlinkOn = false;
    private float playerBlinkTimer = 0f; // điều khiển nhấp nháy
    private float playerInvulnTimer = 0f; // miễn thương ngắn sau khi trúng đạn

    // Tàu địch và đạn địch
    private Texture enemy1Texture;           // thuchanh/taudich1.png
    private Texture enemy2Texture;           // thuchanh/taudich2.png
    private Texture enemy3Texture;           // thuchanh/taudich3.png
    private Texture enemyBullet1Texture;     // thuchanh/dandich1.png
    private Texture enemyBullet2Texture;     // thuchanh/dandich2.png
    private Texture explosionTexture;        // thuchanh/Chay.png
    private Texture coinTexture;             // thuchanh/coin.png
    private Texture heartTexture;            // thuchanh/heart.png (fallback mau.png)
    private Texture shieldTexture;           // thuchanh/khien.png
    private Texture danta2Texture;           // thuchanh/danta2.png
    private Texture danta3Texture;           // thuchanh/danta3.png
    private Texture danta4Texture;           // thuchanh/danta4.png
    private Texture playerShieldOverlayTexture; // thuchanh/shield.png
    private Texture gameOverTexture;         // thuchanh/Gameover.jpg
    private Texture replayButtonTexture;     // thuchanh/replay.png
    private Texture dashboardButtonTexture;  // thuchanh/dashboard.png
    private Texture homeButtonTexture;       // thuchanh/home.png
    private Texture congratulationsTexture;  // thuchanh/congratulations.png
    private Texture giantTexture;            // thuchanh/giant.png
    private Texture dangiantTexture;         // thuchanh/dangiant.png

    private static class Bullet {
        float x, y, w, h, vx, vy;
        Texture texture;
        boolean pierce;              // xuyên qua (danta2)
        boolean destroysBullets;     // có phá đạn địch
        boolean destroysEnemies;     // có phá tàu địch
    }

    private static class Enemy {
        int type;             // 1,2,3
        float x, y, w, h;
        float speedX;         // âm (di chuyển trái)
        float shootCooldown;  // thời gian giữa các lần bắn
        float shootTimer;     // tích lũy
    }

    private static class Boss {
        float x, y, w, h;
        float shootTimer;
        int shootCount;       // đếm số lần bắn để đổi vị trí
        int shootPosition;    // 0=trên, 1=dưới, 2=giữa
        int health;
        int maxHealth;
    }

    private final Array<Bullet> playerBullets = new Array<>();
    private final Array<Bullet> enemyBullets = new Array<>();
    private final Array<Enemy> enemies = new Array<>();
    private float enemySpawnTimer = 0f;
    private float nextEnemySpawn = 1.0f; // giây
    private Boss boss = null;

    private static class Explosion {
        float x, y, w, h, timer, duration;
    }
    private final Array<Explosion> explosions = new Array<>();

    private static class Item {
        String type; // "shield", "heart", "danta2", "danta3", "danta4"
        float x, y, w, h, vx;
        Texture texture;
    }
    private final Array<Item> items = new Array<>();

    // HUD
    private int coins = 0;
    private int playerHealth = 1;
    private int playerMaxHealth = 6;

    // Shield & vũ khí
    private boolean shieldActive = false;
    private float shieldTimer = 0f;         // thời lượng khiên (giây)
    private static final float SHIELD_DURATION = 6f;
    private String weapon = "danta1";       // danta1/danta2/danta3/danta4

    // Game state & UI
    private boolean isGameOver = false;
    private boolean isWin = false;
    private float elapsedTime = 0f;          // thời gian chơi
    private float waveTimer = 0f;            // thời gian wave hiện tại
    private static final float WAVE_DURATION = 20f; // 20s cho mỗi wave
    private int currentWave = 1;

    // Nút Game Over
    private float btnReplayX, btnReplayY, btnReplayW, btnReplayH;
    private float btnDashX, btnDashY, btnDashW, btnDashH;
    private float btnHomeX, btnHomeY, btnHomeW, btnHomeH;

    // Wave progress bar
    private float waveBarX, waveBarY, waveBarW, waveBarH;
    private float waveProgress = 0f;

    // Boss health bar
    private float bossHealthBarX, bossHealthBarY, bossHealthBarW, bossHealthBarH;

    public thuchanh(Main game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.camera = new OrthographicCamera();
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        this.viewport = new FitViewport(w, h, camera);
        this.viewport.apply(true);
        camera.position.set(w / 2f, h / 2f, 0);

        // Tải ảnh nền từ assets/thuchanh/background5.jpg hoặc ảnh đầu tiên có sẵn
        String bgPath = Gdx.files.internal("thuchanh/background5.jpg").exists()
                ? "thuchanh/background5.jpg"
                : Gdx.files.internal("thuchanh/Chay.png").exists() ? "thuchanh/Chay.png" : "thuchanh/taudich1.png";
        backgroundTexture = new Texture(Gdx.files.internal(bgPath));

        // Load âm thanh
        try {
            if (Gdx.files.internal("thuchanh/congratulation.mp3").exists()) {
                victorySound = Gdx.audio.newSound(Gdx.files.internal("thuchanh/congratulation.mp3"));
                Gdx.app.log("thuchanh", "Đã tải âm thanh chiến thắng");
            } else {
                Gdx.app.error("thuchanh", "Không tìm thấy file âm thanh: thuchanh/congratulation.mp3");
            }
        } catch (Exception e) {
            Gdx.app.error("thuchanh", "Lỗi khi tải âm thanh: " + e.getMessage());
        }

        // Load textures gameplay
        playerTexture = new Texture(Gdx.files.internal("thuchanh/tauta.png"));
        playerBulletTexture = new Texture(Gdx.files.internal("thuchanh/danta1.png"));
        enemy1Texture = new Texture(Gdx.files.internal("thuchanh/taudich1.png"));
        enemy2Texture = new Texture(Gdx.files.internal("thuchanh/taudich2.png"));
        enemy3Texture = new Texture(Gdx.files.internal("thuchanh/taudich3.png"));
        enemyBullet1Texture = new Texture(Gdx.files.internal("thuchanh/dandich1.png"));
        enemyBullet2Texture = new Texture(Gdx.files.internal("thuchanh/dandich2.png"));
        explosionTexture = new Texture(Gdx.files.internal("thuchanh/Chay.png"));
        coinTexture = new Texture(Gdx.files.internal("thuchanh/coin.png"));
        // ưu tiên heart.png, fallback mau.png
        if (Gdx.files.internal("thuchanh/heart.png").exists()) {
            heartTexture = new Texture(Gdx.files.internal("thuchanh/heart.png"));
        } else {
            heartTexture = new Texture(Gdx.files.internal("thuchanh/mau.png"));
        }
        shieldTexture = new Texture(Gdx.files.internal("thuchanh/khien.png"));
        danta2Texture = new Texture(Gdx.files.internal("thuchanh/danta2.png"));
        danta3Texture = new Texture(Gdx.files.internal("thuchanh/danta3.png"));
        danta4Texture = new Texture(Gdx.files.internal("thuchanh/danta4.png"));
        playerShieldOverlayTexture = Gdx.files.internal("thuchanh/shield.png").exists()
                ? new Texture(Gdx.files.internal("thuchanh/shield.png")) : null;
        gameOverTexture = Gdx.files.internal("thuchanh/Gameover.jpg").exists()
                ? new Texture(Gdx.files.internal("thuchanh/Gameover.jpg")) : null;
        replayButtonTexture = Gdx.files.internal("thuchanh/replay.png").exists()
                ? new Texture(Gdx.files.internal("thuchanh/replay.png")) : null;
        dashboardButtonTexture = Gdx.files.internal("thuchanh/dashboard.png").exists()
                ? new Texture(Gdx.files.internal("thuchanh/dashboard.png")) : null;
        homeButtonTexture = Gdx.files.internal("thuchanh/home.png").exists()
                ? new Texture(Gdx.files.internal("thuchanh/home.png")) : null;
        congratulationsTexture = Gdx.files.internal("thuchanh/congratulations.png").exists()
                ? new Texture(Gdx.files.internal("thuchanh/congratulations.png")) : null;
        giantTexture = Gdx.files.internal("thuchanh/giant.png").exists()
                ? new Texture(Gdx.files.internal("thuchanh/giant.png")) : null;
        dangiantTexture = Gdx.files.internal("thuchanh/dangiant.png").exists()
                ? new Texture(Gdx.files.internal("thuchanh/dangiant.png")) : null;

        // Tính kích thước vẽ để nền luôn phủ kín màn hình, giữ tỉ lệ ảnh
        computeDrawSize(w, h);

        // Đặt hai tile liền nhau theo hướng cuộn ban đầu
        bgX1 = 0f;
        bgX2 = bgX1 + drawWidth;

        // Thiết lập kích thước và vị trí tàu người chơi: xuất hiện gần biên trái, cách vào 1 xíu
        playerW = Math.min(240f, drawWidth * 0.12f); // to thêm 1 xíu
        playerH = playerW * (playerTexture.getHeight() / (float) playerTexture.getWidth());
        playerX = 20f; // cách biên trái 1 xíu
        playerY = (h - playerH) / 2f;
    }

    private void computeDrawSize(float screenW, float screenH) {
        float texW = backgroundTexture.getWidth();
        float texH = backgroundTexture.getHeight();
        float scale = Math.max(screenW / texW, screenH / texH);
        drawWidth = texW * scale;
        drawHeight = texH * scale;
    }

    @Override
    public void show() { }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (isWin) {
            // Màn hình WIN
            camera.update();
            batch.setProjectionMatrix(camera.combined);
            batch.begin();

            // Vẽ congratulations
            if (congratulationsTexture != null) {
                float cw = Math.min(viewport.getWorldWidth() * 0.8f, 800f);
                float ch = cw * (congratulationsTexture.getHeight() / (float) congratulationsTexture.getWidth());
                float cx = (viewport.getWorldWidth() - cw) / 2f;
                float cy = (viewport.getWorldHeight() - ch) / 2f + 100f;
                batch.draw(congratulationsTexture, cx, cy, cw, ch);
            }

            // Vẽ 3 nút như Game Over
            float btnW = 160f, btnH = 160f;
            float baseY = viewport.getWorldHeight() * 0.3f;
            float screenW = viewport.getWorldWidth();
            float gap = 48f;
            float totalW = btnW * 3f + gap * 2f;
            float startX = (screenW - totalW) / 2f;

            btnReplayW = btnDashW = btnHomeW = btnW;
            btnReplayH = btnDashH = btnHomeH = btnH;
            btnReplayX = startX;
            btnDashX = startX + btnW + gap;
            btnHomeX = startX + (btnW + gap) * 2f;
            btnReplayY = btnDashY = btnHomeY = baseY;

            if (replayButtonTexture != null)
                batch.draw(replayButtonTexture, btnReplayX, btnReplayY, btnReplayW, btnReplayH);
            if (dashboardButtonTexture != null)
                batch.draw(dashboardButtonTexture, btnDashX, btnDashY, btnDashW, btnDashH);
            if (homeButtonTexture != null)
                batch.draw(homeButtonTexture, btnHomeX, btnHomeY, btnHomeW, btnHomeH);
            batch.end();

            // Xử lý click
            if (Gdx.input.justTouched()) {
                Vector3 tp = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                Vector3 world = camera.unproject(tp);
                float x = world.x, y = world.y;
                if (inside(x, y, btnReplayX, btnReplayY, btnReplayW, btnReplayH)) {
                    restart();
                } else if (inside(x, y, btnHomeX, btnHomeY, btnHomeW, btnHomeH)) {
                    game.setScreen(new io.github.some_example_name.screen.menu.MenuScreen(game));
                } else if (inside(x, y, btnDashX, btnDashY, btnDashW, btnDashH)) {
                    // Mở màn hình xếp hạng
                    game.setScreen(new io.github.some_example_name.ui.LeaderboardScreen(game, elapsedTime));
                }
            }
            return;
        }

        if (isGameOver) {
            // Vẽ nền hiện tại mờ (tuỳ chọn) và màn Game Over với UI
            camera.update();
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            float gw = Math.min(viewport.getWorldWidth() * 0.8f, 900f);
            float gh = gw * ((gameOverTexture != null ? gameOverTexture.getHeight() : 1080f) /
                    (float) (gameOverTexture != null ? gameOverTexture.getWidth() : 1920f));
            float gx = (viewport.getWorldWidth() - gw) / 2f;
            float gy = (viewport.getWorldHeight() - gh) / 2f + 80f;
            if (gameOverTexture != null) batch.draw(gameOverTexture, gx, gy, gw, gh);

            // Coin lớn bên dưới
            float coinSize = 64f;
            float coinX = viewport.getWorldWidth() / 2f - coinSize - 30f;
            float coinY = gy - coinSize - 20f;
            batch.draw(coinTexture, coinX, coinY, coinSize, coinSize);
            float originalScale = game.font.getData().scaleX;
            game.font.getData().setScale(3f);
            game.font.draw(batch, "x" + coins, coinX + coinSize + 12f, coinY + coinSize * 0.75f);
            game.font.getData().setScale(originalScale);

            // Thời gian chơi dưới coin
            float timeY = coinY - 20f;
            game.font.getData().setScale(2.5f);
            String timeStr = formatTime(elapsedTime);
            game.font.draw(batch, timeStr, viewport.getWorldWidth() / 2f - 60f, timeY);
            game.font.getData().setScale(originalScale);

            // 3 nút: thẳng hàng, căn giữa, khoảng cách đều
            float btnW = 160f, btnH = 160f;
            float baseY = timeY - btnH - 30f;
            float screenW = viewport.getWorldWidth();
            float gap = 48f; // khoảng cách giữa các nút
            float totalW = btnW * 3f + gap * 2f;
            float startX = (screenW - totalW) / 2f;

            btnReplayW = btnDashW = btnHomeW = btnW;
            btnReplayH = btnDashH = btnHomeH = btnH;
            btnReplayX = startX;
            btnDashX = startX + btnW + gap;
            btnHomeX = startX + (btnW + gap) * 2f;
            btnReplayY = btnDashY = btnHomeY = baseY;
            if (replayButtonTexture != null)
                batch.draw(replayButtonTexture, btnReplayX, btnReplayY, btnReplayW, btnReplayH);
            if (dashboardButtonTexture != null)
                batch.draw(dashboardButtonTexture, btnDashX, btnDashY, btnDashW, btnDashH);
            if (homeButtonTexture != null)
                batch.draw(homeButtonTexture, btnHomeX, btnHomeY, btnHomeW, btnHomeH);
            batch.end();

            // Xử lý click trên nút khi Game Over
            if (Gdx.input.justTouched()) {
                Vector3 tp = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                Vector3 world = camera.unproject(tp);
                float x = world.x, y = world.y;
                if (inside(x, y, btnReplayX, btnReplayY, btnReplayW, btnReplayH)) {
                    restart();
                } else if (inside(x, y, btnHomeX, btnHomeY, btnHomeW, btnHomeH)) {
                    // Về menu
                    game.setScreen(new io.github.some_example_name.screen.menu.MenuScreen(game));
                } else if (inside(x, y, btnDashX, btnDashY, btnDashW, btnDashH)) {
                    // Tạm thời: cũng restart hoặc sau này mở dashboard riêng
                    restart();
                }
            }
            return;
        }

        // Thời gian chơi
        elapsedTime += delta;

        // Cập nhật wave timer
        waveTimer += delta;
        waveProgress = waveTimer / WAVE_DURATION;

        // Kiểm tra spawn boss
        if (waveProgress >= 1.0f && boss == null) {
            spawnBoss();
        }

        // Cập nhật vị trí cuộn
        float move = scrollSpeed * delta * SCROLL_LEFT;
        bgX1 += move;
        bgX2 += move;

        // Nếu tile chạy hết bên trái màn hình, đưa sang phải và thiết lập lật theo kiểu xen kẽ
        if (bgX1 + drawWidth <= 0) {
            float rightMost = Math.max(bgX1, bgX2);
            bgX1 = rightMost + drawWidth;
            // luôn xen kẽ so với tile còn lại để ghép mượt
            tileFlipped1 = !tileFlipped2;
        }
        if (bgX2 + drawWidth <= 0) {
            float rightMost = Math.max(bgX1, bgX2);
            bgX2 = rightMost + drawWidth;
            // luôn xen kẽ so với tile còn lại để ghép mượt
            tileFlipped2 = !tileFlipped1;
        }

        // Cập nhật input tàu người chơi: hỗ trợ chạm - dựa vào hướng chạm để biết lên hay xuống
        float moveY = 0f;
        if (Gdx.input.isTouched()) {
            // lấy tọa độ screen touch và chuyển sang world
            Vector3 tp = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            Vector3 world = camera.unproject(tp);
            float centerY = playerY + playerH * 0.5f;
            if (world.y > centerY + 6f) moveY += playerSpeedY * delta; // chạm phía trên -> đi lên
            else if (world.y < centerY - 6f) moveY -= playerSpeedY * delta; // chạm phía dưới -> đi xuống
        } else {
            // fallback bàn phím (tùy chọn)
            if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) moveY += playerSpeedY * delta;
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) moveY -= playerSpeedY * delta;
        }
        playerY = MathUtils.clamp(playerY + moveY, 0f, viewport.getWorldHeight() - playerH);

        // Bắn đạn người chơi theo vũ khí hiện tại
        playerFireTimer += delta;
        if (weapon.equals("danta1")) {
            if (playerFireTimer >= playerFireCooldown) {
                spawnPlayerBulletSingle(playerBulletTexture, 520f);
                playerFireTimer = 0f;
            }
        } else if (weapon.equals("danta2")) {
            if (playerFireTimer >= playerFireCooldown) {
                // danta2: xuyên, phá đạn & tàu, bay hết biên phải
                Bullet b = createBullet(playerX + playerW - 4f, playerY + playerH * 0.5f, danta2Texture, 600f, 0f, playerW * 0.35f);
                b.pierce = true;
                b.destroysBullets = true;
                b.destroysEnemies = true;
                playerBullets.add(b);
                playerFireTimer = 0f;
            }
        } else if (weapon.equals("danta3")) {
            // 2 hàng, 0.2s một loạt
            if (playerFireTimer >= 0.2f) {
                float baseY = playerY + playerH * 0.5f;
                spawnBulletWithOffset(playerX + playerW - 4f, baseY + 22f, danta3Texture, 560f);
                spawnBulletWithOffset(playerX + playerW - 4f, baseY - 22f, danta3Texture, 560f);
                playerFireTimer = 0f;
            }
        } else if (weapon.equals("danta4")) {
            // 3 hàng: giữa ngang, 2 bên lệch 35 độ, 0.2s một loạt
            if (playerFireTimer >= 0.2f) {
                float baseX = playerX + playerW - 4f;
                float baseY = playerY + playerH * 0.5f;
                // giữa
                Bullet mid = createBullet(baseX, baseY, danta4Texture, 560f, 0f, playerW * 0.30f);
                // trên 35 độ
                float speed = 560f;
                float ang = (float)Math.toRadians(35);
                Bullet up = createBullet(baseX, baseY + 26f, danta4Texture, speed * (float)Math.cos(ang), speed * (float)Math.sin(ang), playerW * 0.30f);
                Bullet down = createBullet(baseX, baseY - 26f, danta4Texture, speed * (float)Math.cos(ang), -speed * (float)Math.sin(ang), playerW * 0.30f);
                playerBullets.add(mid);
                playerBullets.add(up);
                playerBullets.add(down);
                playerFireTimer = 0f;
            }
        }

        // Sinh tàu địch ngẫu nhiên: loại 1-2-3, spawn bên phải và di chuyển sang trái (chỉ khi không có boss)
        if (boss == null) {
            enemySpawnTimer += delta;
            if (enemySpawnTimer >= nextEnemySpawn) {
                enemySpawnTimer = 0f;
                nextEnemySpawn = MathUtils.random(0.7f, 1.8f);
                Enemy e = new Enemy();
                e.type = MathUtils.random(1, 3);
                Texture t = e.type == 1 ? enemy1Texture : (e.type == 2 ? enemy2Texture : enemy3Texture);
                e.w = Math.min(160f, drawWidth * 0.09f);
                e.h = e.w * (t.getHeight() / (float) t.getWidth());
                e.x = viewport.getWorldWidth() + MathUtils.random(10f, 60f);
                e.y = MathUtils.random(0f, viewport.getWorldHeight() - e.h);
                e.speedX = -MathUtils.random(120f, 200f); // trái
                e.shootCooldown = 1.5f * 3f; // bắn chậm gấp 3 lần
                e.shootTimer = 0f;
                enemies.add(e);
            }
        }

        // Cập nhật tàu địch và bắn đạn
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            e.x += e.speedX * delta;
            e.shootTimer += delta;
            if (e.shootTimer >= e.shootCooldown) {
                e.shootTimer = 0f;
                Bullet b = new Bullet();
                // Tàu 1 bắn dandich2; tàu 2-3 ngẫu nhiên dandich1 hoặc dandich2
                Texture bt;
                if (e.type == 1) {
                    bt = enemyBullet2Texture;
                } else {
                    bt = MathUtils.randomBoolean() ? enemyBullet1Texture : enemyBullet2Texture;
                }
                b.texture = bt;
                b.w = bt.getWidth();
                b.h = bt.getHeight();
                // tăng kích thước đạn địch lên một chút (~1.35x tàu tỉ lệ)
                float scale = Math.min(1.35f, e.w * 0.32f / b.w);
                b.w *= scale;
                b.h *= scale;
                // Đạn địch bay từ tàu địch sang trái, nhanh hơn tàu địch một xíu
                b.vx = -Math.abs(e.speedX) - 60f;
                b.x = e.x;
                b.y = e.y + e.h * 0.5f - b.h * 0.5f;
                enemyBullets.add(b);
            }
            if (e.x + e.w < -80f) {
                enemies.removeIndex(i);
                // tàu địch đi ra khỏi biên trái -> trừ 1 máu
                if (!shieldActive && playerInvulnTimer <= 0f) {
                    playerHealth = Math.max(0, playerHealth - 1);
                    playerBlinkOn = true;
                    playerBlinkTimer = 0.4f;
                    playerInvulnTimer = 0.5f;
                    if (playerHealth == 0) isGameOver = true;
                }
            }
        }

        // Cập nhật boss
        if (boss != null) {
            boss.shootTimer += delta;
            if (boss.shootTimer >= 0.2f) {
                boss.shootTimer = 0f;
                boss.shootCount++;

                // Đổi vị trí bắn sau mỗi 10 phát
                if (boss.shootCount % 10 == 0) {
                    boss.shootPosition = (boss.shootPosition + 1) % 3;
                }

                // Bắn đạn
                Bullet b = new Bullet();
                b.texture = dangiantTexture;
                b.w = dangiantTexture.getWidth();
                b.h = dangiantTexture.getHeight();
                float scale = Math.min(1f, boss.w * 0.15f / b.w);
                b.w *= scale;
                b.h *= scale;

                // Vị trí bắn theo shootPosition
                float shootY;
                if (boss.shootPosition == 0) { // trên
                    shootY = boss.y + boss.h * 0.8f;
                } else if (boss.shootPosition == 1) { // dưới
                    shootY = boss.y + boss.h * 0.2f;
                } else { // giữa
                    shootY = boss.y + boss.h * 0.5f;
                }

                b.x = boss.x;
                b.y = shootY - b.h * 0.5f;
                b.vx = -400f; // bay sang trái
                b.vy = 0f;
                b.pierce = false;
                b.destroysBullets = false;
                b.destroysEnemies = false;
                enemyBullets.add(b);
            }
        }

        // Cập nhật đạn: người chơi bay phải, địch bay phải (theo yêu cầu)
        for (int i = playerBullets.size - 1; i >= 0; i--) {
            Bullet b = playerBullets.get(i);
            b.x += b.vx * delta;
            b.y += b.vy * delta;
            if (b.x > viewport.getWorldWidth() + 80f) playerBullets.removeIndex(i);
        }
        for (int i = enemyBullets.size - 1; i >= 0; i--) {
            Bullet b = enemyBullets.get(i);
            b.x += b.vx * delta; // bay sang trái
            if (b.x + b.w < -80f) enemyBullets.removeIndex(i);
        }

        // Va chạm: đạn người chơi trúng địch -> nổ, rơi vật phẩm, tăng coin
        for (int i = playerBullets.size - 1; i >= 0; i--) {
            Bullet pb = playerBullets.get(i);
            boolean hit = false;

            // Va chạm với boss
            if (boss != null && rectsOverlap(pb.x, pb.y, pb.w, pb.h, boss.x, boss.y, boss.w, boss.h)) {
                boss.health--;
                if (boss.health <= 0) {
                    // Boss chết -> WIN
                    isWin = true;
                    // Phát âm thanh
                    if (victorySound != null) {
                        long soundId = victorySound.play(1.0f);
                        victorySound.setLooping(soundId, false);
                        Gdx.app.log("thuchanh", "Phát âm thanh chiến thắng");
                    }
                }
                hit = !pb.pierce;
            }

            // Va chạm với tàu địch thường
            for (int j = enemies.size - 1; j >= 0; j--) {
                Enemy e = enemies.get(j);
                if (rectsOverlap(pb.x, pb.y, pb.w, pb.h, e.x, e.y, e.w, e.h)) {
                    // nổ
                    Explosion ex = new Explosion();
                    ex.w = Math.max(e.w, 48f);
                    ex.h = ex.w * (explosionTexture.getHeight() / (float) explosionTexture.getWidth());
                    ex.x = e.x + e.w * 0.5f - ex.w * 0.5f;
                    ex.y = e.y + e.h * 0.5f - ex.h * 0.5f;
                    ex.duration = 0.25f;
                    ex.timer = 0f;
                    explosions.add(ex);

                    // rơi vật phẩm ngẫu nhiên: khien.png, mau/heart, danta2/3/4
                    float dropRoll = MathUtils.random();
                    if (dropRoll < 0.35f) { // giảm tỉ lệ drop xuống 35%
                        Item it = new Item();
                        float pick = MathUtils.random();
                        if (pick < 0.25f) {
                            it.type = "shield";
                            it.texture = shieldTexture;
                        } else if (pick < 0.5f) {
                            it.type = "heart";
                            it.texture = heartTexture;
                        } else if (pick < 0.7f) {
                            it.type = "danta2";
                            it.texture = danta2Texture;
                        } else if (pick < 0.85f) {
                            it.type = "danta3";
                            it.texture = danta3Texture;
                        } else {
                            it.type = "danta4";
                            it.texture = danta4Texture;
                        }
                        it.w = Math.min(48f, e.w * 0.45f) * 3f; // to x3
                        it.h = it.w * (it.texture.getHeight() / (float) it.texture.getWidth());
                        it.x = e.x + e.w * 0.5f - it.w * 0.5f;
                        it.y = e.y + e.h * 0.5f - it.h * 0.5f;
                        // trôi sang trái tốc độ bằng tốc độ đạn địch trung bình
                        it.vx = -(200f + 60f);
                        items.add(it);
                    }

                    // cộng coin
                    coins += 1;
                    enemies.removeIndex(j);
                    // nếu là đạn xuyên (danta2) thì không xóa đạn, còn lại xóa
                    hit = !pb.pierce;
                    break;
                }
            }
            if (hit) playerBullets.removeIndex(i);
        }

        // Nếu đang có khiên: miễn thương đạn & va chạm tàu
        if (shieldActive) {
            shieldTimer -= delta;
            if (shieldTimer <= 0f) shieldActive = false;
        }
        // Va chạm: đạn địch trúng ta -> nhấp nháy và trừ 1 máu (trừ khi có khiên)
        if (playerInvulnTimer > 0f) playerInvulnTimer -= delta;
        for (int i = enemyBullets.size - 1; i >= 0; i--) {
            Bullet eb = enemyBullets.get(i);
            if (rectsOverlap(eb.x, eb.y, eb.w, eb.h, playerX, playerY, playerW, playerH)) {
                enemyBullets.removeIndex(i);
                if (!shieldActive && playerInvulnTimer <= 0f) {
                    playerBlinkOn = true;
                    playerBlinkTimer = 0.6f; // nhấp nháy 0.6s
                    playerInvulnTimer = 0.8f; // miễn thương ngắn
                    playerHealth = Math.max(0, playerHealth - 1);
                    if (playerHealth == 0) isGameOver = true;
                }
            }
        }

        // Đạn người chơi có thể phá đạn địch (danta1/2/3/4 tùy flag)
        for (int i = playerBullets.size - 1; i >= 0; i--) {
            Bullet pb = playerBullets.get(i);
            if (!pb.destroysBullets) continue;
            for (int j = enemyBullets.size - 1; j >= 0; j--) {
                Bullet eb = enemyBullets.get(j);
                if (rectsOverlap(pb.x, pb.y, pb.w, pb.h, eb.x, eb.y, eb.w, eb.h)) {
                    enemyBullets.removeIndex(j);
                    if (!pb.pierce) { // nếu không xuyên thì xóa đạn người chơi
                        playerBullets.removeIndex(i);
                        break;
                    }
                }
            }
        }

        // cập nhật explosion timer
        for (int i = explosions.size - 1; i >= 0; i--) {
            Explosion ex = explosions.get(i);
            ex.timer += delta;
            if (ex.timer >= ex.duration) explosions.removeIndex(i);
        }

        // cập nhật nhấp nháy (ẩn/hiện theo thời gian)
        if (playerBlinkOn) {
            playerBlinkTimer -= delta;
            if (playerBlinkTimer <= 0f) playerBlinkOn = false;
        }

        // cập nhật item rơi
        for (int i = items.size - 1; i >= 0; i--) {
            Item it = items.get(i);
            it.x += it.vx * delta;
            // ăn vật phẩm
            if (rectsOverlap(it.x, it.y, it.w, it.h, playerX, playerY, playerW, playerH)) {
                if (it.type.equals("heart")) {
                    playerHealth = Math.min(playerMaxHealth, playerHealth + 1);
                } else if (it.type.equals("shield")) {
                    shieldActive = true;
                    shieldTimer = SHIELD_DURATION;
                } else if (it.type.equals("danta2")) {
                    weapon = "danta2";
                    playerBulletTexture = danta2Texture;
                } else if (it.type.equals("danta3")) {
                    weapon = "danta3";
                } else if (it.type.equals("danta4")) {
                    weapon = "danta4";
                }
                // các vật phẩm khác có thể thêm logic sau
                items.removeIndex(i);
                continue;
            }
            if (it.x + it.w < -80f) items.removeIndex(i);
        }

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        // Vẽ hai tile để đảm bảo phủ màn hình khi cuộn
        if (!tileFlipped1) {
            batch.draw(backgroundTexture, bgX1, 0f, drawWidth, drawHeight);
        } else {
            batch.draw(backgroundTexture, bgX1 + drawWidth, 0f, -drawWidth, drawHeight);
        }
        if (!tileFlipped2) {
            batch.draw(backgroundTexture, bgX2, 0f, drawWidth, drawHeight);
        } else {
            batch.draw(backgroundTexture, bgX2 + drawWidth, 0f, -drawWidth, drawHeight);
        }

        // Vẽ tàu người chơi (nhấp nháy khi trúng đạn)
        boolean renderPlayer = true;
        if (playerBlinkOn) {
            // ẩn/hiện theo nhịp 8Hz
            float blinkPhase = (float) (Math.floor((playerBlinkTimer * 8f)) % 2);
            renderPlayer = blinkPhase == 0f;
        }
        if (renderPlayer) {
            batch.draw(playerTexture, playerX, playerY, playerW, playerH);
            if (shieldActive) {
                float sw = playerW * 1.2f;
                float sh = playerH * 1.2f;
                batch.draw(playerShieldOverlayTexture,
                        playerX + playerW * 0.5f - sw * 0.5f,
                        playerY + playerH * 0.5f - sh * 0.5f,
                        sw, sh);
            }
        }
        // Vẽ tàu địch
        for (int i = 0; i < enemies.size; i++) {
            Enemy e = enemies.get(i);
            Texture t = e.type == 1 ? enemy1Texture : (e.type == 2 ? enemy2Texture : enemy3Texture);
            batch.draw(t, e.x, e.y, e.w, e.h);
        }

        // Vẽ boss
        if (boss != null && giantTexture != null) {
            batch.draw(giantTexture, boss.x, boss.y, boss.w, boss.h);
        }
        // Vẽ đạn
        for (int i = 0; i < playerBullets.size; i++) {
            Bullet b = playerBullets.get(i);
            batch.draw(b.texture, b.x, b.y, b.w, b.h);
        }
        for (int i = 0; i < enemyBullets.size; i++) {
            Bullet b = enemyBullets.get(i);
            batch.draw(b.texture, b.x, b.y, b.w, b.h);
        }
        // Vẽ explosion
        for (int i = 0; i < explosions.size; i++) {
            Explosion ex = explosions.get(i);
            batch.draw(explosionTexture, ex.x, ex.y, ex.w, ex.h);
        }
        // Vẽ items
        for (int i = 0; i < items.size; i++) {
            Item it = items.get(i);
            batch.draw(it.texture, it.x, it.y, it.w, it.h);
        }
        // HUD: coin và tim ở góc trái, thời gian góc phải
        float hudX = 10f;
        float hudY = viewport.getWorldHeight() - 10f;
        float coinSize = 28f * 3f; // to x3
        float heartSize = 26f * 3f; // to x3
        // coin icon
        batch.draw(coinTexture, hudX, hudY - coinSize, coinSize, coinSize);
        // hiển thị số coin ở dạng x12 (phóng to 3x)
        float heartsX = hudX + coinSize + 12f;
        float originalScale = game.font.getData().scaleX;
        game.font.getData().setScale(3f);
        String coinText = "x" + coins;
        // căn theo chiều dọc gần trung tâm của icon coin
        float textY = hudY - coinSize * 0.25f;
        game.font.draw(batch, coinText, heartsX, textY);
        game.font.getData().setScale(originalScale);
        heartsX += coinSize; // chừa không gian cho text
        // vẽ hearts theo máu hiện tại
        for (int i = 0; i < playerHealth; i++) {
            batch.draw(heartTexture, heartsX + i * (heartSize + 4f), hudY - heartSize, heartSize, heartSize);
        }
        // Thời gian chơi góc phải
        String timeStrHud = formatTime(elapsedTime);
        game.font.getData().setScale(2.5f);
        float timeWidth = game.font.getRegion().getRegionWidth(); // xấp xỉ
        float timeX = viewport.getWorldWidth() - 220f;
        float timeY = hudY - 8f;
        game.font.draw(batch, timeStrHud, timeX, timeY);
        game.font.getData().setScale(originalScale);

        // Vẽ thanh tiến độ wave
        if (boss == null) {
            waveBarW = viewport.getWorldWidth() * 0.6f;
            waveBarH = 20f;
            waveBarX = (viewport.getWorldWidth() - waveBarW) / 2f;
            waveBarY = viewport.getWorldHeight() - 50f;

            // Vẽ nền thanh
            batch.setColor(0.3f, 0.3f, 0.3f, 1f);
            batch.draw(coinTexture, waveBarX, waveBarY, waveBarW, waveBarH);
            batch.setColor(1f, 1f, 1f, 1f);

            // Vẽ tiến độ
            batch.setColor(0f, 1f, 0f, 1f);
            batch.draw(coinTexture, waveBarX, waveBarY, waveBarW * waveProgress, waveBarH);
            batch.setColor(1f, 1f, 1f, 1f);

            // Vẽ text wave
            game.font.getData().setScale(1.5f);
            String waveText = "Wave " + currentWave;
            float waveTextX = waveBarX + waveBarW + 10f;
            float waveTextY = waveBarY + waveBarH / 2f + game.font.getCapHeight() / 2f;
            game.font.draw(batch, waveText, waveTextX, waveTextY);
            game.font.getData().setScale(originalScale);
        }

        // Vẽ thanh máu boss
        if (boss != null) {
            bossHealthBarW = viewport.getWorldWidth() * 0.8f;
            bossHealthBarH = 30f;
            bossHealthBarX = (viewport.getWorldWidth() - bossHealthBarW) / 2f;
            bossHealthBarY = 50f;

            // Vẽ nền thanh máu
            batch.setColor(0.5f, 0f, 0f, 1f);
            batch.draw(coinTexture, bossHealthBarX, bossHealthBarY, bossHealthBarW, bossHealthBarH);
            batch.setColor(1f, 1f, 1f, 1f);

            // Vẽ máu hiện tại
            float healthPercent = (float) boss.health / (float) boss.maxHealth;
            batch.setColor(1f, 0f, 0f, 1f);
            batch.draw(coinTexture, bossHealthBarX, bossHealthBarY, bossHealthBarW * healthPercent, bossHealthBarH);
            batch.setColor(1f, 1f, 1f, 1f);

            // Vẽ text boss
            game.font.getData().setScale(2f);
            String bossText = "BOSS";
            GlyphLayout bossLayout = new GlyphLayout();
            bossLayout.setText(game.font, bossText);
            float bossTextX = bossHealthBarX + bossHealthBarW / 2f - bossLayout.width / 2f;
            float bossTextY = bossHealthBarY + bossHealthBarH + 20f;
            game.font.draw(batch, bossText, bossTextX, bossTextY);
            game.font.getData().setScale(originalScale);
        }

        batch.end();
    }

    private boolean rectsOverlap(float x1, float y1, float w1, float h1,
                                 float x2, float y2, float w2, float h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }

    private boolean inside(float x, float y, float rx, float ry, float rw, float rh) {
        return x >= rx && x <= rx + rw && y >= ry && y <= ry + rh;
    }

    private String formatTime(float seconds) {
        int total = Math.max(0, (int)seconds);
        int m = total / 60;
        int s = total % 60;
        return (m < 10 ? "0"+m : String.valueOf(m)) + ":" + (s < 10 ? "0"+s : String.valueOf(s));
    }

    private void restart() {
        game.setScreen(new thuchanh(game));
    }

    private void spawnBoss() {
        if (giantTexture == null) return;

        boss = new Boss();
        boss.maxHealth = 20;
        boss.health = boss.maxHealth;
        boss.w = viewport.getWorldHeight(); // cao bằng màn hình
        boss.h = boss.w * (giantTexture.getHeight() / (float) giantTexture.getWidth());
        boss.x = viewport.getWorldWidth() - boss.w;
        boss.y = 0f;
        boss.shootTimer = 0f;
        boss.shootCount = 0;
        boss.shootPosition = 0; // bắt đầu từ trên

        // Xóa tất cả tàu địch khi boss xuất hiện
        enemies.clear();
    }

    private void spawnPlayerBulletSingle(Texture texture, float speedX) {
        Bullet b = createBullet(playerX + playerW - 4f, playerY + playerH * 0.5f, texture, speedX, 0f, playerW * 0.35f);
        b.pierce = false;
        b.destroysBullets = true;  // như danta1: phá đạn địch khi va chạm
        b.destroysEnemies = true;
        playerBullets.add(b);
    }

    private void spawnBulletWithOffset(float x, float y, Texture texture, float speedX) {
        Bullet b = createBullet(x, y, texture, speedX, 0f, playerW * 0.30f);
        b.pierce = false;
        b.destroysBullets = true;
        b.destroysEnemies = true;
        playerBullets.add(b);
    }

    private Bullet createBullet(float x, float y, Texture texture, float vx, float vy, float targetWidth) {
        Bullet b = new Bullet();
        b.texture = texture;
        b.w = texture.getWidth();
        b.h = texture.getHeight();
        float scale = Math.min(1f, targetWidth / b.w);
        b.w *= scale;
        b.h *= scale;
        b.x = x;
        b.y = y - b.h * 0.5f;
        b.vx = vx;
        b.vy = vy;
        return b;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        computeDrawSize(width, height);

        // Căn lại vị trí hai tile để tiếp tục mượt mà
        bgX1 = 0f;
        bgX2 = bgX1 + drawWidth;
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() { }

    @Override
    public void dispose() {
        batch.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (victorySound != null) victorySound.dispose();
    }
}

