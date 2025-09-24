package io.github.some_example_name;

public enum MapType {
    MAP1("map1/map1.tmx", "path"),
    MAP2("map2/map2.tmx", "path"),
    MAP3("map3/map3.tmx", "path"),
    MAP4("map4/map4.tmx", "path"),
    MAP5("map5/map5.tmx", "path"),
    MAP6("map6/map6.tmx", "path"),
    MAP7("map7/map7.tmx", "path"),
    MAP8("map8/map8.tmx", "path");

    private final String mapPath;
    private final String pathLayerName;

    MapType(String mapPath, String pathLayerName) {
        this.mapPath = mapPath;
        this.pathLayerName = pathLayerName;
    }

    public String getMapPath() {
        return mapPath;
    }

    public String getPathLayerName() {
        return pathLayerName;
    }
}