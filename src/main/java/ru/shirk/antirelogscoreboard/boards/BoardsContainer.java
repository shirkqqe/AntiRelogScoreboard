package ru.shirk.antirelogscoreboard.boards;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class BoardsContainer<K, V> extends HashMap<K, V> {
    public @NonNull Collection<V> cloneValues() {
        return new ArrayList<>(this.values());
    }
}
