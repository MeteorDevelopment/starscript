package org.meteordev.starscript.utils;

public class Stack<T> {
    @SuppressWarnings("unchecked")
    private T[] items = (T[]) new Object[8];
    private int size;

    public void clear() {
        for (int i = 0; i < size; i++) items[i] = null;
        size = 0;
    }

    @SuppressWarnings("unchecked")
    public void push(T item) {
        if (size >= items.length) {
            T[] newItems = (T[]) new Object[items.length * 2];
            System.arraycopy(items, 0, newItems, 0, items.length);
            items = newItems;
        }

        items[size++] = item;
    }

    public T pop() {
        T item = items[--size];
        items[size] = null;
        return item;
    }

    public T peek() {
        return items[size - 1];
    }

    public T peek(int offset) {
        return items[size - 1 - offset];
    }
}
