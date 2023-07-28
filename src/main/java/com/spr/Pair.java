package com.spr;

public class Pair<T, U> {
    private T first;
    private U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public T getLeft() {
        return first;
    }

    public U getRight() {
        return second;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public void setSecond(U second) {
        this.second = second;
    }

    public static  <T, U> Pair<T, U> of(T first, U second) {
        return new Pair<>(first, second);
    }

}