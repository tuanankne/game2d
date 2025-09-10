package io.github.some_example_name;

// Nhập các thư viện cần thiết từ libGDX
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;

// Lớp chính của game, kế thừa từ lớp Game của libGDX
public class Main extends Game {
    // SpriteBatch dùng để vẽ các texture lên màn hình
    public SpriteBatch batch;
    // Font để vẽ text
    public BitmapFont font;
    // Texture để lưu trữ hình ảnh
    public Texture image;

    // Phương thức này được gọi khi game được khởi tạo
    @Override
    public void create() {
        // Khởi tạo SpriteBatch để vẽ
        batch = new SpriteBatch();
        // Khởi tạo font mặc định
        font = new BitmapFont();
        // Tải hình ảnh từ tệp libgdx.png
        image = new Texture("libgdx.png");
        // Thiết lập màn hình menu là màn hình đầu tiên
        this.setScreen(new MenuScreen(this));
    }

    // Phương thức để bắt đầu game, chuyển từ menu sang màn hình chơi game
    public void startGame() {
        // Chuyển sang màn hình chơi game
        this.setScreen(new GameScreen(this));
    }
}