package com.pocketempire.events;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GameEventBus {
    private static final GameEventBus INSTANCE = new GameEventBus();
    private final List<Consumer<GameEvent>> subscribers = new ArrayList<>();

    private GameEventBus() {}

    public static GameEventBus getInstance() {
        return INSTANCE;
    }

    public void subscribe(Consumer<GameEvent> subscriber) {
        subscribers.add(subscriber);
    }

    public void publish(GameEvent event) {
        for (var sub : subscribers) {
            sub.accept(event);
        }
    }
}
