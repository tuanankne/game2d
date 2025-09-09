package io.github.some_example_name;

// Import các thư viện cần thiết từ libGDX
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.audio.Music;

// Lớp quản lý màn hình menu chính của game
public class MenuScreen implements Screen {
    final Main game;                          // Tham chiếu đến game chính
    private SpriteBatch batch;                // Dùng để vẽ các texture
    private BitmapFont font;                  // Font chữ chung
    private BitmapFont buttonFont;            // Font chữ cho nút
    private Stage stage;                      // Sân khấu chứa các UI element
    private TextButton playButton;            // Nút bắt đầu chơi
    private TextButton settingsButton;        // Nút cài đặt
    private Table table;                      // Bảng sắp xếp các UI element
    private Texture background;               // Hình nền
    private java.util.List<Texture> buttonTextures;  // Danh sách texture cho nút
    private Texture titleTexture;             // Texture cho tiêu đề
    private Image titleImage;                 // Ảnh tiêu đề
    private Music menuMusic;                  // Nhạc nền menu

    // Constructor khởi tạo màn hình menu
    public MenuScreen(final Main game) {
        this.game = game;
        batch = new SpriteBatch();                // Khởi tạo SpriteBatch để vẽ
        font = loadUiFont();                      // Tải font chữ chung
        buttonFont = loadButtonFont();            // Tải font chữ cho nút
        background = loadBackground();            // Tải hình nền
        stage = new Stage();                      // Khởi tạo sân khấu UI
        Gdx.input.setInputProcessor(stage);       // Thiết lập xử lý input cho stage

        // Tạo style cho nút: ưu tiên dùng ảnh PNG, nếu không có thì dùng màu đơn
        TextButton.TextButtonStyle buttonStyle = createButtonStyleFromPng(
                "Menu/btn_up.png",      // Ảnh nút bình thường
                "Menu/btn_over.png",    // Ảnh nút khi di chuột qua
                "Menu/btn_down.png"     // Ảnh nút khi nhấn
        );

        // Nếu không có ảnh nút, tạo style với màu đơn
        if (buttonStyle == null) {
            buttonStyle = new TextButton.TextButtonStyle();
            buttonStyle.font = buttonFont != null ? buttonFont : font;
            // Tạo các trạng thái màu cho nút
            TextureRegionDrawable up = singleColor(0.15f, 0.15f, 0.18f, 0.90f);    // Màu bình thường
            TextureRegionDrawable over = singleColor(0.20f, 0.20f, 0.24f, 0.95f);   // Màu hover
            TextureRegionDrawable down = singleColor(0.10f, 0.10f, 0.12f, 1.00f);   // Màu nhấn
            buttonStyle.up = up;
            buttonStyle.over = over;
            buttonStyle.down = down;
        } else {
            buttonStyle.font = buttonFont != null ? buttonFont : font;
        }

        // Tạo các nút với style đã định nghĩa
        playButton = new TextButton("Play", buttonStyle);
        settingsButton = new TextButton("Settings", buttonStyle);

        // Tính toán kích thước nút dựa trên kích thước màn hình
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        // Thiết lập kích thước nút với giới hạn min/max
        float btnWidth = Math.max(Math.min(screenW * 0.49f, 1000f), 500f);    // Chiều rộng nút
        float btnHeight = Math.max(Math.min(screenH * 0.36f, 520f), 280f);    // Chiều cao nút
        // Tăng kích thước chữ trên nút
        playButton.getLabel().setFontScale(1.25f);
        settingsButton.getLabel().setFontScale(1.25f);

        // Thêm xử lý sự kiện click cho nút Play
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.startGame();  // Chuyển sang màn hình chơi game
            }
        });

        // Tải và thiết lập ảnh tiêu đề nếu có
        if (Gdx.files.internal("Menu/title.png").exists()) {
            titleTexture = new Texture(Gdx.files.internal("Menu/title.png"));
            titleTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            titleImage = new Image(new TextureRegionDrawable(new TextureRegion(titleTexture)));
            titleImage.setScaling(Scaling.fit);  // Tự động co giãn để vừa khung
        }

        // Thiết lập bảng layout cho UI
        table = new Table();
        table.setFillParent(true);              // Bảng sẽ lấp đầy màn hình
        table.top();                            // Căn các phần tử từ trên xuống
        table.padTop(screenH * 0.02f).padBottom(screenH * 1.5f);  // Đệm trên dưới

        // Tính toán khoảng cách và kích thước các phần tử
        float minSpacing = btnHeight * 0.05f;   // Khoảng cách tối thiểu giữa các phần tử
        float neededForButtons = btnHeight * 3f + minSpacing * 2f;  // Không gian cần cho các nút
        float titleReservedH = Math.min(Math.max(screenH - neededForButtons, screenH * 0.28f), screenH * 0.40f);

        // Thêm tiêu đề vào bảng
        if (titleImage != null) {
            float titleMaxW = Math.min(screenW * 0.95f, 1600f);
            table.add(titleImage).width(titleMaxW).height(titleReservedH).center().row();
        } else {
            table.add().height(titleReservedH).center().row();  // Dành chỗ cho tiêu đề
        }

        // Thêm các nút và khoảng cách vào bảng
        table.add().height(minSpacing * 0.02f).row();          // Khoảng cách trên nút Play
        table.add(playButton).width(btnWidth).height(btnHeight).center().row();
        table.add().height(minSpacing * 0.002f).row();         // Khoảng cách giữa các nút
        table.add(settingsButton).width(btnWidth).height(btnHeight).center().row();
        stage.addActor(table);  // Thêm bảng vào stage

        // Tải và thiết lập nhạc nền cho menu
        if (Gdx.files.internal("Music/Menu.mp3").exists()) {
            menuMusic = Gdx.audio.newMusic(Gdx.files.internal("Music/Menu.mp3"));
            menuMusic.setLooping(true);     // Lặp lại liên tục
            menuMusic.setVolume(10f);       // Âm lượng
            menuMusic.play();               // Bắt đầu phát nhạc
        }
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
        
        // Vẽ hình nền
        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();
        
        // Cập nhật và vẽ các thành phần UI
        stage.act(delta);  // Cập nhật trạng thái
        stage.draw();      // Vẽ lên màn hình
    }

    // Được gọi khi kích thước màn hình thay đổi
    @Override
    public void resize(int width, int height) {
        // Cập nhật viewport của stage để phù hợp với kích thước mới
        stage.getViewport().update(width, height, true);
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
        if (font != null) font.dispose();   // Giải phóng font chữ
        if (buttonFont != null) buttonFont.dispose();  // Giải phóng font nút
        stage.dispose();                    // Giải phóng stage
        background.dispose();               // Giải phóng hình nền
        if (titleTexture != null) titleTexture.dispose();  // Giải phóng texture tiêu đề
        // Giải phóng các texture của nút
        if (buttonTextures != null) {
            for (Texture t : buttonTextures) t.dispose();
        }
        // Giải phóng nhạc nền
        if (menuMusic != null) {
            menuMusic.stop();
            menuMusic.dispose();
        }
    }

    // Tải hình nền từ các nguồn khác nhau
    private Texture loadBackground() {
        // Thử tải từ thư mục gốc
        if (Gdx.files.internal("background.png").exists()) {
            return new Texture(Gdx.files.internal("background.png"));
        }
        // Thử tải từ thư mục Menu
        if (Gdx.files.internal("Menu/background.jpg").exists()) {
            return new Texture(Gdx.files.internal("Menu/background.jpg"));
        }
        // Sử dụng hình mặc định nếu không có hình nền
        return new Texture(Gdx.files.internal("libgdx.png"));
    }

    // Tải font chữ cho UI chung
    private BitmapFont loadUiFont() {
        try {
            // Thử tải font tùy chỉnh
            if (Gdx.files.internal("fonts/menu.ttf").exists()) {
                FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/menu.ttf"));
                FreeTypeFontParameter p = new FreeTypeFontParameter();
                // Tính kích thước font dựa trên kích thước màn hình
                p.size = Math.round(Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()) * 0.05f);
                p.characters = FreeTypeFontGenerator.DEFAULT_CHARS;
                BitmapFont f = gen.generateFont(p);
                gen.dispose();
                return f;
            }
        } catch (Throwable ignored) {}
        // Sử dụng font mặc định nếu không tải được font tùy chỉnh
        return new BitmapFont();
    }

    // Tải font chữ cho các nút
    private BitmapFont loadButtonFont() {
        try {
            // Thử tải font tùy chỉnh với kích thước lớn hơn
            if (Gdx.files.internal("fonts/menu.ttf").exists()) {
                FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/menu.ttf"));
                FreeTypeFontParameter p = new FreeTypeFontParameter();
                // Kích thước font nút lớn hơn font UI thông thường
                p.size = Math.round(Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()) * 0.065f);
                p.characters = FreeTypeFontGenerator.DEFAULT_CHARS;
                BitmapFont f = gen.generateFont(p);
                gen.dispose();
                return f;
            }
        } catch (Throwable ignored) {}
        return null;  // Trả về null để sử dụng font UI thông thường
    }

    // Tạo style cho nút từ các file ảnh PNG
    private TextButton.TextButtonStyle createButtonStyleFromPng(String upPath, String overPath, String downPath) {
        try {
            // Kiểm tra sự tồn tại của các file ảnh
            boolean upExists = Gdx.files.internal(upPath).exists();
            boolean overExists = Gdx.files.internal(overPath).exists();
            boolean downExists = Gdx.files.internal(downPath).exists();
            if (!upExists && !downExists && !overExists) return null;

            // Tạo style mới và danh sách texture nếu cần
            TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
            if (buttonTextures == null) buttonTextures = new java.util.ArrayList<Texture>();

            // Tải và thiết lập texture cho trạng thái bình thường
            if (upExists) {
                Texture t = new Texture(Gdx.files.internal(upPath));
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                buttonTextures.add(t);
                style.up = new TextureRegionDrawable(new TextureRegion(t));
            }
            // Tải và thiết lập texture cho trạng thái hover
            if (overExists) {
                Texture t = new Texture(Gdx.files.internal(overPath));
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                buttonTextures.add(t);
                style.over = new TextureRegionDrawable(new TextureRegion(t));
            }
            // Tải và thiết lập texture cho trạng thái nhấn
            if (downExists) {
                Texture t = new Texture(Gdx.files.internal(downPath));
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                buttonTextures.add(t);
                style.down = new TextureRegionDrawable(new TextureRegion(t));
            }

            // Thiết lập fallback cho các trạng thái thiếu
            if (style.over == null) style.over = style.down != null ? style.down : style.up;
            if (style.down == null) style.down = style.up;
            return style;
        } catch (Throwable t) {
            return null;  // Trả về null nếu có lỗi
        }
    }

    // Tạo texture đơn sắc với màu và độ trong suốt chỉ định
    private TextureRegionDrawable singleColor(float r, float g, float b, float a) {
        // Tạo pixmap với kích thước nhỏ và định dạng RGBA
        com.badlogic.gdx.graphics.Pixmap pm = new com.badlogic.gdx.graphics.Pixmap(8, 8, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pm.setColor(r, g, b, a);  // Thiết lập màu
        pm.fill();                // Tô toàn bộ pixmap
        Texture tex = new Texture(pm);  // Tạo texture từ pixmap
        pm.dispose();             // Giải phóng pixmap
        return new TextureRegionDrawable(new TextureRegion(tex));  // Trả về drawable
    }
}
