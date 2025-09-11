package io.github.some_example_name.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.some_example_name.Main;

public class thuchanh119 implements Screen {
    final Main game;
    private SpriteBatch batch;
    private Texture background;
    private float bgX;
    private float bgSpeed = 100f; // tốc độ di chuyển nền (pixel/giây)
    private OrthographicCamera camera;

    // Region nền
    private TextureRegion regionNormal;
    private TextureRegion regionFlipped;
    private int segmentIndex = 0; // đếm số đoạn đã trượt qua để lật xen kẽ

    // Tàu
    private Texture shipTexture;
    private float shipX;
    private float shipY;
    private float shipWidth;
    private float shipHeight;
    private float shipSpeed = 350f; // pixel/giây
    
    // Xử lý va chạm và hiệu ứng
    private Texture explosionTexture;
    private boolean isExploding = false;
    private float explosionTimer = 0;
    private final float EXPLOSION_DURATION = 2f; // Thời gian hiển thị hiệu ứng nổ (2 giây)

    public thuchanh119(final Main game) {
        this.game = game;
        batch = new SpriteBatch();
        background = new Texture(Gdx.files.internal("Menu/background5.jpg"));
        bgX = 0f;

        // Region thường và lật ngang để vẽ xen kẽ
        regionNormal = new TextureRegion(background);
        regionFlipped = new TextureRegion(background);
        regionFlipped.flip(true, false);

        // Thiết lập camera theo kích thước màn hình hiện tại
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, screenWidth, screenHeight);

        // Khởi tạo tàu và scale theo 30% chiều rộng màn hình, giữ tỉ lệ gốc
        shipTexture = new Texture(Gdx.files.internal("Menu/tau.png"));
        explosionTexture = new Texture(Gdx.files.internal("Menu/Chay.png"));
        float shipTexW = shipTexture.getWidth();
        float shipTexH = shipTexture.getHeight();
        shipWidth = screenWidth * 0.30f;
        shipHeight = shipWidth * (shipTexH / shipTexW);
        shipX = (screenWidth - shipWidth) * 0.5f;
        shipY = (screenHeight - shipHeight) * 0.3f; // đặt thấp hơn trung tâm một chút
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Đảm bảo batch vẽ theo hệ toạ độ pixel của màn hình
        batch.setProjectionMatrix(camera.combined);

        // Kích thước màn hình
        float screenWidth = Gdx.graphics.getWidth();

        // Tính kích thước tile: rộng = 130% chiều rộng màn hình; cao theo đúng tỉ lệ ảnh
        float texW = background.getWidth();
        float texH = background.getHeight();
        float tileWidth = screenWidth * 1.3f;
        float scaledHeight = tileWidth * (texH / texW);

        // Cập nhật vị trí cuộn nền theo kích thước đã scale
        bgX += bgSpeed * delta;
        if (bgX >= tileWidth) {
            bgX -= tileWidth;
            segmentIndex++; // sang đoạn mới -> đổi trạng thái lật
        }

        // Xử lý cảm ứng để di chuyển tàu theo hướng chạm tương đối so với tàu
        if (Gdx.input.isTouched()) {
            Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
            camera.unproject(touch);
            float shipCenterX = shipX + shipWidth * 0.5f;
            float shipCenterY = shipY + shipHeight * 0.5f;
            float dx = touch.x - shipCenterX;
            float dy = touch.y - shipCenterY;

            if (Math.abs(dx) > Math.abs(dy)) {
                // Di chuyển theo trục ngang
                shipX += Math.signum(dx) * shipSpeed * delta;
            } else {
                // Di chuyển theo trục dọc
                shipY += Math.signum(dy) * shipSpeed * delta;
            }

            // Kiểm tra va chạm với biên màn hình
            boolean collision = false;
            if (shipX < 0 || shipX > screenWidth - shipWidth ||
                shipY < 0 || shipY > camera.viewportHeight - shipHeight) {
                collision = true;
            }

            // Nếu có va chạm và chưa đang trong trạng thái nổ
            if (collision && !isExploding) {
                isExploding = true;
                explosionTimer = 0;
            }

            // Giới hạn trong màn hình
            if (shipX < 0) shipX = 0;
            if (shipY < 0) shipY = 0;
            if (shipX > screenWidth - shipWidth) shipX = screenWidth - shipWidth;
            if (shipY > camera.viewportHeight - shipHeight) shipY = camera.viewportHeight - shipHeight;
        }

        float localX = bgX; // vị trí cuộn trong một tile

        batch.begin();
        // Chọn region theo đoạn chẵn/lẻ: chẵn = thường, lẻ = lật ngang
        TextureRegion firstRegion = (segmentIndex % 2 == 0) ? regionNormal : regionFlipped;
        TextureRegion secondRegion = (segmentIndex % 2 == 0) ? regionFlipped : regionNormal;

        // Vẽ 2 lần để đảm bảo nền liền mạch khi cuộn, giữ đúng tỉ lệ ảnh
        batch.draw(firstRegion, -localX, 0, tileWidth, scaledHeight);
        batch.draw(secondRegion, tileWidth - localX, 0, tileWidth, scaledHeight);

        // Xử lý hiệu ứng nổ và vẽ tàu
        if (isExploding) {
            explosionTimer += delta;
            
            // Vẽ hiệu ứng nổ thay vì tàu
            batch.draw(explosionTexture, shipX, shipY, shipWidth, shipHeight);
            
            // Sau 2 giây
            if (explosionTimer >= EXPLOSION_DURATION) {
                isExploding = false;
                // Đặt lại tàu vào giữa màn hình
                shipX = (screenWidth - shipWidth) * 0.5f;
                shipY = (camera.viewportHeight - shipHeight) * 0.3f;
            }
        } else {
            // Vẽ tàu bình thường
            batch.draw(shipTexture, shipX, shipY, shipWidth, shipHeight);
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // Cập nhật camera khi kích thước màn hình đổi để luôn vẽ full chiều rộng
        if (camera != null) {
            camera.setToOrtho(false, width, height);
            camera.update();
        }

        // Cập nhật kích thước tàu theo kích thước màn hình mới, giữ tỉ lệ
        if (shipTexture != null) {
            float shipTexW = shipTexture.getWidth();
            float shipTexH = shipTexture.getHeight();
            shipWidth = width * 0.30f;
            shipHeight = shipWidth * (shipTexH / shipTexW);
            // Đảm bảo tàu vẫn trong biên
            if (shipX > width - shipWidth) shipX = Math.max(0, width - shipWidth);
            if (shipY > height - shipHeight) shipY = Math.max(0, height - shipHeight);
        }
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        if (shipTexture != null) shipTexture.dispose();
        if (explosionTexture != null) explosionTexture.dispose();
    }
}



