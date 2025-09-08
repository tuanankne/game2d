package io.github.some_example_name;

public enum EnemyType {
    NORMAL("map1/towerDefense_tile271.png", 100, 100, 1.0f), // speed, health, scale
    FAST("map1/towerDefense_tile272.png", 150, 80, 0.8f),    // quái nhanh nhỏ hơn
    TANK("map1/towerDefense_tile273.png", 70, 200, 1.5f);    // quái tank to hơn

    private final String texturePath;
    private final float speed;
    private final float health;
    private final float scale;

    EnemyType(String texturePath, float speed, float health, float scale) {
        this.texturePath = texturePath;
        this.speed = speed;
        this.health = health;
        this.scale = scale;
    }

    public String getTexturePath() { return texturePath; }
    public float getSpeed() { return speed; }
    public float getHealth() { return health; }
    public float getScale() { return scale; }
}
