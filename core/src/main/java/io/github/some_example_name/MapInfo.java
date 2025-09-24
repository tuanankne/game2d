package io.github.some_example_name;

public class MapInfo {
    private final MapType type;
    private final String name;
    private final String previewImage;
    private boolean unlocked;
    private int stars;

    public MapInfo(MapType type, String name, String previewImage, boolean unlocked, int stars) {
        this.type = type;
        this.name = name;
        this.previewImage = previewImage;
        this.unlocked = unlocked;
        this.stars = stars;
    }

    public MapType getType() { return type; }
    public String getName() { return name; }
    public String getPreviewImage() { return previewImage; }
    public boolean isUnlocked() { return unlocked; }
    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }
    public void unlock() { this.unlocked = true; }
}
