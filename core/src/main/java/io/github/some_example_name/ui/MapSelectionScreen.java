package io.github.some_example_name.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Array;
import io.github.some_example_name.Main;
import io.github.some_example_name.MapType;
import io.github.some_example_name.GameScreen;
import io.github.some_example_name.MapInfo;
import io.github.some_example_name.MapProgress;

public class MapSelectionScreen implements Screen {
    private final Main game;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final Texture buttonTexture;
    private final Texture lockTexture;
    private final Texture starTexture;
    private final MenuButton backButton;
    private final AnimatedBackground background;
    private final Array<MapInfo> maps;
    private final Array<Texture> mapPreviews;

    private static final int MAPS_PER_ROW = 4;
    private static final float MAP_PREVIEW_SIZE = 200;
    private static final float MAP_SPACING = 50;
    private static final float STAR_SIZE = 30;

    public MapSelectionScreen(final Main game) {
        this.game = game;
        this.batch = game.batch;
        this.font = game.font;
        this.buttonTexture = new Texture(Gdx.files.internal("Menu/btn_up.png"));
        this.lockTexture = new Texture(Gdx.files.internal("Menu/lock.png"));
        this.starTexture = new Texture(Gdx.files.internal("Menu/star.png"));

        // Khởi tạo background động
        String[] backgroundFrames = new String[] {
            "Menu/background1.png",
            "Menu/background2.png",
            "Menu/background3.png",
            "Menu/background4.png"
        };
        background = new AnimatedBackground(backgroundFrames, 0.2f);

        // Khởi tạo danh sách map
        maps = new Array<>();
        mapPreviews = new Array<>();

        // Thêm thông tin các map từ MapProgress
        MapProgress progress = MapProgress.getInstance();
        maps.add(new MapInfo(MapType.MAP1, "Forest", "Menu/background1.png",
            progress.isMapUnlocked(MapType.MAP1), progress.getMapStars(MapType.MAP1)));
        maps.add(new MapInfo(MapType.MAP2, "Desert", "Menu/background1.png",
            progress.isMapUnlocked(MapType.MAP2), progress.getMapStars(MapType.MAP2)));
        maps.add(new MapInfo(MapType.MAP3, "Snow", "Menu/background1.png",
            progress.isMapUnlocked(MapType.MAP3), progress.getMapStars(MapType.MAP3)));
        maps.add(new MapInfo(MapType.MAP4, "Volcano", "Menu/background1.png",
            progress.isMapUnlocked(MapType.MAP4), progress.getMapStars(MapType.MAP4)));
        maps.add(new MapInfo(MapType.MAP5, "Castle", "Menu/background1.jpg",
            progress.isMapUnlocked(MapType.MAP5), progress.getMapStars(MapType.MAP5)));
        maps.add(new MapInfo(MapType.MAP6, "Cave", "Menu/background1.png",
            progress.isMapUnlocked(MapType.MAP6), progress.getMapStars(MapType.MAP6)));
        maps.add(new MapInfo(MapType.MAP7, "Sky", "Menu/background1.png",
            progress.isMapUnlocked(MapType.MAP7), progress.getMapStars(MapType.MAP7)));
        maps.add(new MapInfo(MapType.MAP8, "Space", "Menu/background1.png",
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

        // Tạo nút back
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        float btnWidth = Math.min(screenW * 0.2f, 200);
        float btnHeight = Math.min(screenH * 0.1f, 100);

        backButton = new MenuButton(buttonTexture,
            (screenW - btnWidth) / 2, screenH * 0.1f,
            btnWidth, btnHeight, "Back");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Cập nhật animation background và nút back
        background.update(delta);
        backButton.update();

        // Xử lý click nút back
        if (backButton.isClicked()) {
            game.setScreen(new MenuScreen(game));
            dispose();
            return;
        }

        // Xử lý click vào map
        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.graphics.getHeight() - Gdx.input.getY();
            checkMapClick(touchX, touchY);
        }

        batch.begin();

        // Vẽ background
        background.render(batch);

        // Vẽ tiêu đề
        font.setColor(Color.WHITE);
        String title = "Select Map";
        GlyphLayout glyphLayout = new GlyphLayout(font, title);
        font.draw(batch, title,
            (Gdx.graphics.getWidth() - glyphLayout.width) / 2,
            Gdx.graphics.getHeight() * 0.9f);

        // Vẽ grid maps
        drawMapGrid();

        // Vẽ nút back
        backButton.draw(batch, font);

        batch.end();
    }

    private void drawMapGrid() {
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        // Tính toán vị trí bắt đầu để căn giữa grid
        float totalWidth = MAPS_PER_ROW * MAP_PREVIEW_SIZE + (MAPS_PER_ROW - 1) * MAP_SPACING;
        float startX = (screenW - totalWidth) / 2;
        float startY = screenH * 0.75f;

        for (int i = 0; i < maps.size; i++) {
            int row = i / MAPS_PER_ROW;
            int col = i % MAPS_PER_ROW;

            float x = startX + col * (MAP_PREVIEW_SIZE + MAP_SPACING);
            float y = startY - row * (MAP_PREVIEW_SIZE + MAP_SPACING);

            MapInfo map = maps.get(i);
            Texture preview = mapPreviews.get(i);

            // Vẽ preview map
            batch.draw(preview, x, y - MAP_PREVIEW_SIZE, MAP_PREVIEW_SIZE, MAP_PREVIEW_SIZE);

            // Vẽ tên map
            GlyphLayout layout = new GlyphLayout(font, map.getName());
            font.draw(batch, map.getName(),
                x + (MAP_PREVIEW_SIZE - layout.width) / 2,
                y - MAP_PREVIEW_SIZE - 10);

            // Vẽ stars hoặc lock tùy trạng thái
            if (map.isUnlocked()) {
                if (map.getStars() > 0) {
                    // Vẽ số sao
                    float starsWidth = map.getStars() * STAR_SIZE;
                    float starX = x + (MAP_PREVIEW_SIZE - starsWidth) / 2;
                    float starY = y - MAP_PREVIEW_SIZE/2;

                    for (int star = 0; star < map.getStars(); star++) {
                        batch.draw(starTexture,
                            starX + star * STAR_SIZE,
                            starY,
                            STAR_SIZE, STAR_SIZE);
                    }
                }
            } else {
                // Vẽ biểu tượng khóa
                batch.draw(lockTexture,
                    x + (MAP_PREVIEW_SIZE - STAR_SIZE) / 2,
                    y - MAP_PREVIEW_SIZE/2 - STAR_SIZE/2,
                    STAR_SIZE, STAR_SIZE);
            }
        }
    }

    private void checkMapClick(float touchX, float touchY) {
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        float totalWidth = MAPS_PER_ROW * MAP_PREVIEW_SIZE + (MAPS_PER_ROW - 1) * MAP_SPACING;
        float startX = (screenW - totalWidth) / 2;
        float startY = screenH * 0.75f;

        for (int i = 0; i < maps.size; i++) {
            int row = i / MAPS_PER_ROW;
            int col = i % MAPS_PER_ROW;

            float x = startX + col * (MAP_PREVIEW_SIZE + MAP_SPACING);
            float y = startY - row * (MAP_PREVIEW_SIZE + MAP_SPACING);

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
    public void show() {}

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
        for (Texture preview : mapPreviews) {
            preview.dispose();
        }
    }
}
