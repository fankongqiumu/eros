package com.github.eros.common.lang;

public class Node<K, V> {
    private final K key;
    private final V data;
    public Node next, prev;
    private final long lastModified;

    public Node(K key, V data, long lastModified) {
        this.key = key;
        this.data = data;
        this.lastModified = lastModified;
    }

    public K getKey() {
        return key;
    }

    public V getData() {
        return data;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public Node getPrev() {
        return prev;
    }

    public void setPrev(Node prev) {
        this.prev = prev;
    }

    public long getLastModified() {
        return lastModified;
    }

    public boolean hasExpire(Long lastModified){
        return this.lastModified < lastModified;
    }
}
