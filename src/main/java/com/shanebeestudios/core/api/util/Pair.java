package com.shanebeestudios.core.api.util;

public class Pair<O,T> {

    private final O first;
    private final T second;

    public Pair(O first, T second) {
        this.first = first;
        this.second = second;

    }

    public O getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }
}
