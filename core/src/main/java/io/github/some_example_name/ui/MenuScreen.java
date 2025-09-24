package io.github.some_example_name.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import io.github.some_example_name.Main;

// Lớp quản lý màn hình menu chính của game
public class MenuScreen implements Screen {
    final Main game;                          // Tham chiếu đến game chính
    private SpriteBatch batch;                // Dùng để vẽ các texture
    private BitmapFont font;                  // Font chữ chung
    private AnimatedBackground background;     // Thay đổi từ Texture sang AnimatedBackground
    private Texture buttonTexture;            // Texture cho nút
    private Texture logoTexture;              // Texture cho logo
    private Music menuMusic;                  // Nhạc nền menu

    // Các button
    private MenuButton playButton;            // Nút bắt đầu chơi
    private MenuButton settingsButton;        // Nút cài đặt

    // Vị trí logo
    private float logoX, logoY, logoWidth, logoHeight;

    // Constructor khởi tạo màn hình menu
    public MenuScreen(final Main game) {
        this.game = game;
        batch = new SpriteBatch();                // Khởi tạo SpriteBatch để vẽ

        // Load custom font từ file
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/menu.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 90; // Kích thước font
        parameter.color = Color.WHITE; // Màu chữ
        parameter.borderWidth = 2; // Độ dày viền
        parameter.borderColor = Color.BLACK; // Màu viền
        font = generator.generateFont(parameter);
        generator.dispose(); // Giải phóng generator sau khi dùng xong

        // Khởi tạo animated background với các frame
        String[] backgroundFrames = new String[] {
            "Menu/background1.png",
            "Menu/background2.png",
            "Menu/background3.png",
            "Menu/background4.png",
            // Thêm các frame khác tại đây
        };
        background = new AnimatedBackground(backgroundFrames, 0.2f); // 0.1f = 10 frame/giây

        // Tải các texture khác
        buttonTexture = new Texture(Gdx.files.internal("Menu/btn_up.png"));
        if (Gdx.files.internal("Menu/title.png").exists()) {
            logoTexture = new Texture(Gdx.files.internal("Menu/title.png"));
        }

        // Khởi tạo các nút (chỉ cần tạo một lần)
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        playButton = new MenuButton(buttonTexture, 0, 0, 100, 100, "Play");
        settingsButton = new MenuButton(buttonTexture, 0, 0, 100, 100, "Settings");

        // Cập nhật layout ban đầu
        updateLayout(screenW, screenH);

        // Tải và thiết lập nhạc nền cho menu
        if (Gdx.files.internal("Music/Menu.mp3").exists()) {
            menuMusic = Gdx.audio.newMusic(Gdx.files.internal("Music/Menu.mp3"));
            menuMusic.setLooping(true);     // Lặp lại liên tục
            menuMusic.setVolume(0.5f);       // Âm lượng
            menuMusic.play();               // Bắt đầu phát nhạc
        }
    }

    // Phương thức để tính toán và cập nhật vị trí, kích thước các thành phần
    private void updateLayout(float width, float height) {
        // Tính toán kích thước cho các nút
        float btnWidth = Math.min(width * 0.9f, 900);
        float btnHeight = Math.min(height * 0.4f, 900);

        if (playButton != null) {
            playButton.setSize(btnWidth, btnHeight);
            playButton.setPosition((width - btnWidth) / 2, height * 0.25f);
        }

        if (settingsButton != null) {
            settingsButton.setSize(btnWidth, btnHeight);
            settingsButton.setPosition((width - btnWidth) / 2, height * 0.05f);
        }

        // Cập nhật vị trí và kích thước logo
        logoWidth = Math.min(width * 0.8f, 800);
        logoHeight = Math.min(height * 0.4f, 400);
        logoX = (width - logoWidth) / 2;
        logoY = height * 0.6f;
    }

    // Được gọi khi màn hình menu được hiển thị
    @Override
    public void show() {
        if (menuMusic != null) {
            menuMusic.play();  // Phát nhạc nền
        }
    }

    // Được gọi mỗi frame để vẽ màn hình
    @Override
    public void render(float delta) {
        // Xóa màn hình với màu đen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Cập nhật animation background
        background.update(delta);

        // Cập nhật trạng thái các nút
        playButton.update();
        settingsButton.update();

        // Xử lý sự kiện click cho các nút
        if (playButton.isClicked()) {
            game.startGame();  // Chuyển sang màn hình chơi game
        }
        if (settingsButton.isClicked()) {
//            game.setScreen(new thuchanh119(game)); // Chuyển sang màn hình thuchanh119
        }

        // Bắt đầu vẽ các thành phần lên màn hình
        batch.begin();

        // Vẽ background động
        background.render(batch);

        // Vẽ logo nếu có
        if (logoTexture != null) {
            batch.draw(logoTexture, logoX, logoY, logoWidth, logoHeight);
        }

        // Vẽ các nút
        playButton.draw(batch, font);
        settingsButton.draw(batch, font);

        batch.end();
    }

    // Được gọi khi kích thước màn hình thay đổi
    @Override
    public void resize(int width, int height) {
        updateLayout(width, height);  // Tự động chuyển đổi từ int sang float
        background.resize(width, height);
    }

    // Được gọi khi game tạm dừng
    @Override
    public void pause() {}

    // Được gọi khi game tiếp tục sau khi tạm dừng
    @Override
    public void resume() {}

    // Được gọi khi màn hình menu bị ẩn
    @Override
    public void hide() {
        if (menuMusic != null) {
            menuMusic.pause();  // Tạm dừng nhạc nền
        }
    }

    // Được gọi khi cần giải phóng tài nguyên
    @Override
    public void dispose() {
        batch.dispose();                    // Giải phóng SpriteBatch
        font.dispose();                    // Giải phóng font chữ
        background.dispose();               // Giải phóng background
        buttonTexture.dispose();            // Giải phóng texture nút
        if (logoTexture != null) {
            logoTexture.dispose();          // Giải phóng texture logo
        }
        if (menuMusic != null) {
            menuMusic.stop();
            menuMusic.dispose();            // Giải phóng nhạc nền
        }
    }
}
