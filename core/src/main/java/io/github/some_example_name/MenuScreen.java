//package io.github.some_example_name;
//
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.Screen;
//import com.badlogic.gdx.graphics.GL20;
//import com.badlogic.gdx.graphics.Texture;
//
//public class MenuScreen implements Screen {
//    private final Main game;
//    private Texture background;
//    private Texture title;
//
//    public MenuScreen(final Main game) {
//        this.game = game;
//        background = new Texture("Menu/background1.png");
//        title = new Texture("Menu/title.png");
//    }
//
//    @Override
//    public void render(float delta) {
//        Gdx.gl.glClearColor(0, 0, 0, 1);
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//
//        game.batch.begin();
//        game.batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        float titleWidth = Gdx.graphics.getWidth() * 0.8f;
//        float titleHeight = titleWidth * title.getHeight() / title.getWidth();
//        game.batch.draw(title,
//            Gdx.graphics.getWidth()/2 - titleWidth/2,
//            Gdx.graphics.getHeight()/2 - titleHeight/2,
//            titleWidth, titleHeight);
//        game.batch.end();
//    }
//
//    @Override
//    public void dispose() {
//        background.dispose();
//        title.dispose();
//    }
//
//    @Override public void show() {}
//    @Override public void resize(int width, int height) {}
//    @Override public void pause() {}
//    @Override public void resume() {}
//    @Override public void hide() {}
//}
