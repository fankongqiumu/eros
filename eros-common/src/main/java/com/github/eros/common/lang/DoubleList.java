package com.github.eros.common.lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * LRU使用
 * @param <K>
 * @param <V>
 */
public class DoubleList<K, V>{
    private Node<K, V> head;
    private Node<K, V> tail;
    private int size;

    // 在链表头部添加节点 node，时间 O(1)
    public Node<K, V> addFirst(Node<K, V> node) {
        if (null == head) {
            tail = node;
        } else {
            node.next = head;
            head.prev = node;
        }
        head = node;
        size++;
        return head;
    }

    public Node<K, V> getFirst() {
        return head;
    }

    // 删除链表中的 node 节点（x 一定存在）
    // 由于是双链表且给的是目标 Node 节点，时间 O(1)
    public void remove(Node<K, V> node) {
        if (null == node) {
            return;
        }
        Node<K, V> prev = node.prev;
        Node<K, V> next = node.next;
        if (null == prev) {
            // prev == null 说明第head节点
            // 删除头结点 需要改变头结点指针
            head = next;
        } else {
            prev.next = next;
        }
        if (null == next) {
            // 说明是tail节点
            tail = prev;
        } else {
            next.prev = next;
        }
        size--;
    }

    // 删除链表中最后一个节点，并返回该节点，时间 O(1)
    public Node<K, V> removeLast() {
        if (null == tail) {
            return null;
        }
        Node<K, V> tem = tail;
        Node<K, V> prev = tail.prev;
        prev.next = null;
        tail = prev;
        size--;
        return tem;
    }

    public Node<K, V> getLast() {
        return tail;
    }

    // 返回链表长度，时间 O(1)
    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return size <= 0;
    }

    public boolean isNotEmpty() {
        return size > 0;
    }

    public static <K, V> List<V> transferToList(DoubleList<K, V> doubleList){
        if (null != doubleList && doubleList.isNotEmpty()){
            List<V> list = new ArrayList<>(doubleList.getSize());
            Node<K, V> first = doubleList.getFirst();
            Node<K, V> tem = first;
            list.add(tem.getData());
            while (null != tem){
                list.add(tem.getData());
                tem = tem.next;
            }
            return list;
        }
        return Collections.emptyList();
    }
}
