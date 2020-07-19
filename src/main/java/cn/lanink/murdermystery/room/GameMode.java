package cn.lanink.murdermystery.room;

public enum GameMode {
    CLASSIC("classic"), //经典
    INFECTED("infected"); //感染

    private final String name;

    GameMode(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
