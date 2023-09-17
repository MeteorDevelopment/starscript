package org.meteordev.starscript.utils;

import java.util.Arrays;

public class Stack<T> {
    private Object[] items = new Object[8];
    private int size;

    public void clear() {
        // Set all elements to null, starting from the last used index
        for (int i = size - 1; i >= 0; i--) {
            items[i] = null;
        }
        size = 0;
    }

    public void push(T item) {
        if (size >= items.length) {
            int newCapacity = items.length * 2; // increase capacity by doubling it
            items = Arrays.copyOf(items, newCapacity);
        }

        items[size++] = item;
    }

    public T pop() {
        if (size <= 0) {
            throw new IllegalStateException("Stack is empty.");
        }

        @SuppressWarnings("unchecked")
        T item = (T) items[--size];
        items[size] = null; // avoid memory leak by nullifying reference
        return item;
    }

    public T peek() {
        if (size <= 0) {
            throw new IllegalStateException("Stack is empty.");
        }
        @SuppressWarnings("unchecked")
        T item = (T) items[size - 1];
        return item;
    }

    public T peek(int offset) {
        if (size <= offset) {
            throw new IllegalArgumentException("Offset is greater than or equal to the stack size.");
        }
        @SuppressWarnings("unchecked")
        T item = (T) items[size - 1 - offset];
        return item;
    }
}
