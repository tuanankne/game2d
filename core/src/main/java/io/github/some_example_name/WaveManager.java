package io.github.some_example_name;

// Import các thư viện cần thiết từ libGDX
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.Interpolation;

// Lớp quản lý các đợt tấn công (wave) của quái
public class WaveManager {
    private Array<Wave> waves;                   // Danh sách các wave
    private int currentWaveIndex;                // Chỉ số wave hiện tại
    private float timeBetweenWaves;              // Thời gian giữa các wave
    private float waveTimer;                     // Bộ đếm thời gian wave
    private BitmapFont messageFont;              // Font chữ cho thông báo wave
    private boolean showingWaveMessage;          // Trạng thái hiển thị thông báo
    private float messageTimer;                  // Thời gian hiển thị còn lại
    private static final float MESSAGE_DURATION = 2f;  // Thời gian hiển thị thông báo (giây)
    private GlyphLayout glyphLayout;             // Đối tượng đo kích thước text
    private float messageScale;                  // Tỷ lệ co giãn của thông báo
    private Color messageColor;                  // Màu sắc thông báo
    private static final float MAX_SCALE = 2.5f; // Tỷ lệ co giãn tối đa

    // Constructor khởi tạo wave manager
    public WaveManager(float timeBetweenWaves) {
        this.waves = new Array<>();                // Khởi tạo danh sách wave
        this.currentWaveIndex = 0;                 // Bắt đầu từ wave đầu tiên
        this.timeBetweenWaves = timeBetweenWaves; // Thiết lập thời gian giữa các wave
        this.waveTimer = timeBetweenWaves;        // Khởi tạo bộ đếm thời gian

        // Tạo font chữ đẹp cho thông báo
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/menu.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 96;  // Kích thước font lớn
        parameter.borderWidth = 5;  // Viền chữ
        parameter.borderColor = Color.BLACK;  // Màu viền
        parameter.color = Color.WHITE;  // Màu chữ
        this.messageFont = generator.generateFont(parameter);
        generator.dispose();

        // Khởi tạo các thành phần khác
        this.glyphLayout = new GlyphLayout();
        this.messageColor = new Color(Color.WHITE);
        this.messageScale = 2f;
        this.showingWaveMessage = false;
        this.messageTimer = 0;
    }

    // Thêm một wave mới vào danh sách
    public void addWave(Wave wave) {
        waves.add(wave);  // Thêm wave vào danh sách
        // Ghi log thông tin wave mới
        Gdx.app.log("WaveManager", "Added wave " + waves.size + " with " + wave.getTotalEnemies() + " enemies");
    }

    // Cập nhật trạng thái wave manager
    public void update(float delta) {
        if (showingWaveMessage) {
            // Đang hiển thị thông báo, giảm thời gian hiển thị
            messageTimer -= delta;

            // Cập nhật hiệu ứng co giãn và màu sắc
            float progress = messageTimer / MESSAGE_DURATION;
            // Sử dụng Interpolation để tạo hiệu ứng mượt mà
            messageScale = Interpolation.bounceOut.apply(1f, MAX_SCALE, progress);

            // Thay đổi màu sắc theo thời gian
            float alpha = Interpolation.fade.apply(progress);
            messageColor.set(1, 1, 1, alpha);

            if (messageTimer <= 0) {
                showingWaveMessage = false;  // Hết thời gian hiển thị
            }
        } else if (waveTimer > 0) {
            // Đang đợi wave tiếp theo
            waveTimer -= delta;
            if (waveTimer <= 0 && currentWaveIndex < waves.size) {
                // Hết thời gian đợi, bắt đầu wave mới
                showWaveMessage();
                Gdx.app.log("WaveManager", "Wave timer expired, starting wave " + (currentWaveIndex + 1));
            }
        }

        // Ghi log trạng thái wave hiện tại
        if (currentWaveIndex < waves.size) {
            Wave currentWave = waves.get(currentWaveIndex);
            if (currentWave.isComplete()) {
                Gdx.app.log("WaveManager", "Current wave status: Complete, Timer: " + waveTimer);
            }
        }
    }

    // Hiển thị thông báo wave mới
    private void showWaveMessage() {
        showingWaveMessage = true;                 // Bật hiển thị thông báo
        messageTimer = MESSAGE_DURATION;           // Đặt thời gian hiển thị
        messageScale = MAX_SCALE;                 // Bắt đầu với kích thước lớn nhất
        messageColor.set(1, 1, 1, 1);            // Đặt màu trắng với độ trong suốt 100%
        Gdx.app.log("WaveManager", "Starting Wave " + (currentWaveIndex + 1));
    }

    // Vẽ thông báo wave lên màn hình
    public void render(SpriteBatch batch) {
        if (showingWaveMessage && currentWaveIndex < waves.size) {
            // Tạo thông báo với hiệu ứng
            String message = "WAVE " + (currentWaveIndex + 1);

            // Thiết lập màu và scale cho font
            messageFont.setColor(messageColor);
            messageFont.getData().setScale(messageScale);

            // Tính toán kích thước và vị trí text
            glyphLayout.setText(messageFont, message);
            float x = (Gdx.graphics.getWidth() - glyphLayout.width) / 2;   // Căn giữa ngang
            float y = (Gdx.graphics.getHeight() + glyphLayout.height) / 2;  // Căn giữa dọc

            // Vẽ thông báo với hiệu ứng shadow
            messageFont.setColor(0, 0, 0, messageColor.a * 0.5f);  // Shadow màu đen
            messageFont.draw(batch, message, x + 2, y - 2);        // Vẽ shadow

            // Vẽ thông báo chính
            messageFont.setColor(messageColor);
            messageFont.draw(batch, message, x, y);

            // Reset scale font về mặc định
            messageFont.getData().setScale(1);
        }
    }

    // Lấy wave hiện tại
    public Wave getCurrentWave() {
        return currentWaveIndex < waves.size ? waves.get(currentWaveIndex) : null;
    }

    // Kiểm tra xem có nên sinh quái mới không
    public boolean shouldSpawnEnemy(float delta) {
        Wave currentWave = getCurrentWave();
        // Chỉ sinh quái khi: có wave hiện tại, không đang hiển thị thông báo,
        // hết thời gian đợi và wave cho phép sinh quái
        return currentWave != null && !showingWaveMessage && waveTimer <= 0 && currentWave.shouldSpawnEnemy(delta);
    }

    // Xử lý khi wave hiện tại hoàn thành
    public void waveCompleted() {
        // Ghi log hoàn thành wave
        Gdx.app.log("WaveManager", "Wave " + (currentWaveIndex + 1) + " completed");
        currentWaveIndex++;  // Chuyển sang wave tiếp theo

        if (currentWaveIndex < waves.size) {
            // Còn wave tiếp theo, đặt lại thời gian đợi
            waveTimer = timeBetweenWaves;
            Gdx.app.log("WaveManager", "Next wave starts in " + timeBetweenWaves + " seconds");
        } else {
            // Đã hoàn thành tất cả wave
            Gdx.app.log("WaveManager", "All waves completed!");
        }
    }

    // Kiểm tra xem đã hoàn thành tất cả wave chưa
    public boolean isFinished() {
        return currentWaveIndex >= waves.size;
    }

    // Giải phóng tài nguyên
    public void dispose() {
        if (messageFont != null) {
            messageFont.dispose();  // Giải phóng font chữ
        }
    }
}
