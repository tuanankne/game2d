package io.github.some_example_name;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;

public class Main extends Game {
    public SpriteBatch batch;
    public Texture image;

    @Override
    public void create() {
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
        this.setScreen(new MenuScreen(this));
    }

    public void startGame() {
        this.setScreen(new GameScreen(this));
    }
}
