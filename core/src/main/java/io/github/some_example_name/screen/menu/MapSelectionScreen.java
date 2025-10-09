package io.github.some_example_name.screen.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.audio.Music;

import io.github.some_example_name.screen.game.GameScreen;
import io.github.some_example_name.Main;
import io.github.some_example_name.config.map.MapType;
import io.github.some_example_name.config.map.MapInfo;
import io.github.some_example_name.config.map.MapProgress;
import io.github.some_example_name.screen.ui.ScrollingBackground;
import io.github.some_example_name.screen.ui.MenuButton;
import io.github.some_example_name.screen.ui.SoundManager;
import io.github.some_example_name.screen.ui.MusicManager;

public class MapSelectionScreen implements Screen {
    private final Main game;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final BitmapFont titleFont;
    private final BitmapFont mapNameFont;
    private final BitmapFont backButtonFont;
    private final Texture buttonTexture;
    private final Texture lockTexture;
    private final Texture starTexture;
    private final MenuButton backButton;
    private final ScrollingBackground background;
    private final Array<MapInfo> maps;
    private final Array<Texture> mapPreviews;
    private final SoundManager soundManager;
    private final MusicManager musicManager;

    private static final int MAPS_PER_ROW = 3; // 2 hàng x 3 cột = 6 map
    private static final float MAP_PREVIEW_SIZE = 250; // To hơn
    private static final float MAP_SPACING = 80; // Tăng khoảng cách giữa các map
    private static final float STAR_SIZE = 40; // To hơn
    private static final float LOCK_SIZE = 50; // To hơn
    private static final float BORDER_WIDTH = 6; // Độ dày viền 3D
    private static final float BACK_BUTTON_SIZE = 400; // Nút back to gấp 4 lần (100 -> 400)

    public MapSelectionScreen(final Main game) {
        this.game = game;
        this.batch = game.batch;
        this.font = game.font;
        this.soundManager = SoundManager.getInstance();
        this.musicManager = MusicManager.getInstance();
        
        // Khởi tạo font cho tiêu đề
        FreeTypeFontGenerator titleGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/menu.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter titleParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        titleParameter.size = 80;
        titleParameter.color = Color.WHITE;
        titleParameter.borderWidth = 3;
        titleParameter.borderColor = Color.BLACK;
        this.titleFont = titleGenerator.generateFont(titleParameter);
        titleGenerator.dispose();
        
        // Khởi tạo font cho tên map
        FreeTypeFontGenerator mapNameGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/menu.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter mapNameParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        mapNameParameter.size = 50;
        mapNameParameter.color = Color.WHITE;
        mapNameParameter.borderWidth = 2;
        mapNameParameter.borderColor = Color.BLACK;
        this.mapNameFont = mapNameGenerator.generateFont(mapNameParameter);
        mapNameGenerator.dispose();
        
        // Khởi tạo font cho nút back (nhỏ hơn titleFont)
        FreeTypeFontGenerator backButtonGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/menu.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter backButtonParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        backButtonParameter.size = 60; // Nhỏ hơn titleFont (80)
        backButtonParameter.color = Color.WHITE;
        backButtonParameter.borderWidth = 2;
        backButtonParameter.borderColor = Color.BLACK;
        this.backButtonFont = backButtonGenerator.generateFont(backButtonParameter);
        backButtonGenerator.dispose();
        this.buttonTexture = new Texture(Gdx.files.internal("Menu/btn_up.png"));
        this.lockTexture = new Texture(Gdx.files.internal("Menu/lock.png"));
        this.starTexture = new Texture(Gdx.files.internal("Menu/star.png"));

        // Khởi tạo scrolling background với file bg.jpg
        background = new ScrollingBackground("Menu/bg.jpg");

        // Khởi tạo danh sách map
        maps = new Array<>();
        mapPreviews = new Array<>();

        // Thêm thông tin các map từ MapProgress
        MapProgress progress = MapProgress.getInstance();
        maps.add(new MapInfo(MapType.MAP1, "Forest", "Menu/maplogo.png",
            progress.isMapUnlocked(MapType.MAP1), progress.getMapStars(MapType.MAP1)));
        maps.add(new MapInfo(MapType.MAP2, "Desert", "Menu/maplogo.png",
            progress.isMapUnlocked(MapType.MAP2), progress.getMapStars(MapType.MAP2)));
        maps.add(new MapInfo(MapType.MAP3, "Snow", "Menu/maplogo.png",
            progress.isMapUnlocked(MapType.MAP3), progress.getMapStars(MapType.MAP3)));
        maps.add(new MapInfo(MapType.MAP4, "Volcano", "Menu/maplogo.png",
            progress.isMapUnlocked(MapType.MAP4), progress.getMapStars(MapType.MAP4)));
        maps.add(new MapInfo(MapType.MAP5, "Castle", "Menu/maplogo.png",
            progress.isMapUnlocked(MapType.MAP5), progress.getMapStars(MapType.MAP5)));
        maps.add(new MapInfo(MapType.MAP6, "Cave", "Menu/maplogo.png",
            progress.isMapUnlocked(MapType.MAP6), progress.getMapStars(MapType.MAP6)));
        maps.add(new MapInfo(MapType.MAP7, "Sky", "Menu/maplogo.png",
            progress.isMapUnlocked(MapType.MAP7), progress.getMapStars(MapType.MAP7)));
        maps.add(new MapInfo(MapType.MAP8, "Space", "Menu/maplogo.png",
            progress.isMapUnlocked(MapType.MAP8), progress.getMapStars(MapType.MAP8)));

        // Load textures cho map previews
        for (MapInfo map : maps) {
            try {
                if (Gdx.files.internal(map.getPreviewImage()).exists()) {
                    mapPreviews.add(new Texture(Gdx.files.internal(map.getPreviewImage())));
                } else {
                    // Sử dụng texture mặc định nếu không tìm thấy file
                    mapPreviews.add(new Texture(Gdx.files.internal("Menu/background1.png")));
                }
            } catch (Exception e) {
                Gdx.app.error("MapSelectionScreen", "Error loading texture: " + map.getPreviewImage());
                // Sử dụng texture mặc định trong trường hợp lỗi
                mapPreviews.add(new Texture(Gdx.files.internal("Menu/background1.png")));
            }
        }

        // Tạo nút back ở góc trái dưới với kích thước to gấp 4 lần
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        float btnWidth = Math.min(screenW * 0.3f, BACK_BUTTON_SIZE);
        float btnHeight = Math.min(screenH * 0.15f, BACK_BUTTON_SIZE * 0.4f);

        backButton = new MenuButton(buttonTexture,
            20, 20, // Góc trái dưới
            btnWidth, btnHeight, "BACK");
            
        // Khởi tạo nhạc nền
        musicManager.playMusic();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Cập nhật scrolling background và nút back
        background.update(delta);
        backButton.update();

        // Xử lý click
        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.graphics.getHeight() - Gdx.input.getY();
            
            // Kiểm tra click nút back trước
            float backX = 20;
            float backY = 20;
            float backW = Math.min(Gdx.graphics.getWidth() * 0.3f, BACK_BUTTON_SIZE);
            float backH = Math.min(Gdx.graphics.getHeight() * 0.15f, BACK_BUTTON_SIZE * 0.4f);
            
            if (touchX >= backX && touchX <= backX + backW &&
                touchY >= backY && touchY <= backY + backH) {
                game.setScreen(new MenuScreen(game));
                dispose();
                return;
            }
            
            // Nếu không click nút back thì kiểm tra click map
            checkMapClick(touchX, touchY);
        }

        batch.begin();

        // Vẽ background
        background.render(batch);

        // Vẽ tiêu đề với font to và đẹp
        titleFont.setColor(Color.WHITE);
        String title = "SELECT MAP";
        GlyphLayout titleLayout = new GlyphLayout(titleFont, title);
        titleFont.draw(batch, title,
            (Gdx.graphics.getWidth() - titleLayout.width) / 2,
            Gdx.graphics.getHeight() * 0.9f);

        // Vẽ grid maps
        drawMapGrid();

        // Vẽ nút back với font vừa phải
        backButton.draw(batch, backButtonFont); // Sử dụng backButtonFont nhỏ hơn

        batch.end();
    }

    private void drawMapGrid() {
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        // Tính toán vị trí bắt đầu để căn giữa grid (2 hàng x 3 cột)
        float totalWidth = MAPS_PER_ROW * MAP_PREVIEW_SIZE + (MAPS_PER_ROW - 1) * MAP_SPACING;
        float startX = (screenW - totalWidth) / 2;
        float startY = screenH * 0.7f; // Điều chỉnh vị trí

        for (int i = 0; i < Math.min(maps.size, 6); i++) { // Chỉ hiển thị 6 map đầu
            int row = i / MAPS_PER_ROW;
            int col = i % MAPS_PER_ROW;

            float x = startX + col * (MAP_PREVIEW_SIZE + MAP_SPACING);
            float y = startY - row * (MAP_PREVIEW_SIZE + MAP_SPACING + 80); // Thêm khoảng cách cho tên map

            MapInfo map = maps.get(i);
            Texture preview = mapPreviews.get(i);

            // Vẽ nền đen cho map chưa mở khóa
            if (!map.isUnlocked()) {
                batch.setColor(0.2f, 0.2f, 0.2f, 0.8f); // Màu đen mờ
                batch.draw(preview, x, y - MAP_PREVIEW_SIZE, MAP_PREVIEW_SIZE, MAP_PREVIEW_SIZE);
                batch.setColor(1, 1, 1, 1); // Reset màu
            } else {
                // Vẽ preview map bình thường
                batch.draw(preview, x, y - MAP_PREVIEW_SIZE, MAP_PREVIEW_SIZE, MAP_PREVIEW_SIZE);
            }

            // Vẽ viền 3D cho map
            draw3DBorder(x, y - MAP_PREVIEW_SIZE, MAP_PREVIEW_SIZE, map.isUnlocked());

            // Vẽ tên map với font to và đẹp
            mapNameFont.setColor(map.isUnlocked() ? Color.WHITE : Color.GRAY);
            String mapName = map.getName().toUpperCase();
            GlyphLayout nameLayout = new GlyphLayout(mapNameFont, mapName);
            mapNameFont.draw(batch, mapName,
                x + (MAP_PREVIEW_SIZE - nameLayout.width) / 2,
                y - MAP_PREVIEW_SIZE - 20);

            // Vẽ stars hoặc lock tùy trạng thái
            if (map.isUnlocked()) {
                if (map.getStars() > 0) {
                    // Vẽ số sao ở trên cùng của map để nhìn rõ ràng
                    float starsWidth = map.getStars() * STAR_SIZE;
                    float starX = x + (MAP_PREVIEW_SIZE - starsWidth) / 2;
                    float starY = y + 20; // Cao hơn một chút

                    for (int star = 0; star < map.getStars(); star++) {
                        // Hiệu ứng sáng cho sao
                        batch.setColor(1, 1, 0.8f, 1);
                        batch.draw(starTexture,
                            starX + star * STAR_SIZE,
                            starY,
                            STAR_SIZE, STAR_SIZE);
                    }
                    batch.setColor(1, 1, 1, 1); // Reset màu
                }
            } else {
                // Vẽ biểu tượng khóa ở giữa map
                batch.setColor(0.8f, 0.8f, 0.8f, 1);
                batch.draw(lockTexture,
                    x + (MAP_PREVIEW_SIZE - LOCK_SIZE) / 2,
                    y - MAP_PREVIEW_SIZE/2 - LOCK_SIZE/2,
                    LOCK_SIZE, LOCK_SIZE);
                batch.setColor(1, 1, 1, 1); // Reset màu
            }
        }
    }

    // Vẽ viền 3D cho map
    private void draw3DBorder(float x, float y, float size, boolean isUnlocked) {
        Color borderColor = isUnlocked ? Color.GOLD : Color.GRAY;
        
        // Vẽ viền ngoài (sáng)
        batch.setColor(borderColor.r * 1.2f, borderColor.g * 1.2f, borderColor.b * 1.2f, 1);
        // Viền trên
        batch.draw(buttonTexture, x - BORDER_WIDTH, y + size, size + 2*BORDER_WIDTH, BORDER_WIDTH);
        // Viền trái
        batch.draw(buttonTexture, x - BORDER_WIDTH, y, BORDER_WIDTH, size);
        
        // Vẽ viền trong (tối)
        batch.setColor(borderColor.r * 0.6f, borderColor.g * 0.6f, borderColor.b * 0.6f, 1);
        // Viền dưới
        batch.draw(buttonTexture, x, y - BORDER_WIDTH, size, BORDER_WIDTH);
        // Viền phải
        batch.draw(buttonTexture, x + size, y, BORDER_WIDTH, size);
        
        batch.setColor(1, 1, 1, 1); // Reset màu
    }

    private void checkMapClick(float touchX, float touchY) {
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        float totalWidth = MAPS_PER_ROW * MAP_PREVIEW_SIZE + (MAPS_PER_ROW - 1) * MAP_SPACING;
        float startX = (screenW - totalWidth) / 2;
        float startY = screenH * 0.7f;

        for (int i = 0; i < Math.min(maps.size, 6); i++) { // Chỉ kiểm tra 6 map đầu
            int row = i / MAPS_PER_ROW;
            int col = i % MAPS_PER_ROW;

            float x = startX + col * (MAP_PREVIEW_SIZE + MAP_SPACING);
            float y = startY - row * (MAP_PREVIEW_SIZE + MAP_SPACING + 80);

            if (touchX >= x && touchX <= x + MAP_PREVIEW_SIZE &&
                touchY >= y - MAP_PREVIEW_SIZE && touchY <= y) {

                MapInfo map = maps.get(i);
                if (map.isUnlocked()) {
                    game.setScreen(new GameScreen(game, map.getType()));
                    dispose();
                    return;
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        background.resize(width, height);
    }

    @Override
    public void show() {
        // Refresh dữ liệu map khi quay về từ game
        refreshMapData();
        musicManager.playMusic();
    }
    
    private void refreshMapData() {
        // Cập nhật lại trạng thái mở khóa và sao của các map
        MapProgress progress = MapProgress.getInstance();
        for (int i = 0; i < maps.size; i++) {
            MapInfo map = maps.get(i);
            MapType[] mapTypes = {MapType.MAP1, MapType.MAP2, MapType.MAP3, MapType.MAP4, MapType.MAP5, MapType.MAP6, MapType.MAP7, MapType.MAP8};
            if (i < mapTypes.length) {
                boolean unlocked = progress.isMapUnlocked(mapTypes[i]);
                int stars = progress.getMapStars(mapTypes[i]);
                if (unlocked) {
                    map.unlock();
                }
                map.setStars(stars);
            }
        }
    }

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        buttonTexture.dispose();
        lockTexture.dispose();
        starTexture.dispose();
        background.dispose();
        titleFont.dispose();
        mapNameFont.dispose();
        backButtonFont.dispose();
        // Không dispose musicManager vì nó được dùng chung
        for (Texture preview : mapPreviews) {
            preview.dispose();
        }
    }
}
