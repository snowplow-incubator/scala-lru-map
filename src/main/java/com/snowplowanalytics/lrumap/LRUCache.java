package com.snowplowanalytics.lrumap;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * LRUCache: Class to cache objects based on LRU (Least Recently Used) cache eviction strategy,
 * wherein if the cache size has reached the maximum allocated capacity, the
 * least recently used objects in the cache will be evicted.
 *
 * @author sunil
 * @see "https://dzone.com/articles/java-based-simple-cache-lru-eviction"
 */
public final class LRUCache<K, V> {

    /**
     * A doubly-linked-list implementation to save objects into the hashmap
     * as key-value pari.
     *
     * @author sunil
     */
    private static class Node<K, V> {
        private V value;
        private K key;
        private Node<K, V> next, prev;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;

        }

        @Override
        public String toString() {
            return value.toString();
        }
    }

    /**
     * The maximum number of elements that can be cached, should be set during instantiation
     * time.
     */
    private final int maxCapacity;

    /**
     * Use {@linkplain ConcurrentHashMap} here to maintain the cache of objects.
     * Also this offers thread safe access of the cache.
     */
    private ConcurrentHashMap<K, Node<K, V>> map;

    /**
     * A key-value representation of the cache object identified by a cache key.
     * This is actually a doubly-linked list which maintains the most recently accessed
     * objects (read/write) at the tail-end and the least read objects at the head.
     */
    private Node<K, V> head, tail;

    public LRUCache(int maxCapacity) {
        this(16, maxCapacity);
    }

    public LRUCache(int initialCapacity, int maxCapacity) {
        this.maxCapacity = maxCapacity;
        if (initialCapacity > maxCapacity)
            initialCapacity = maxCapacity;
        map = new ConcurrentHashMap<>(initialCapacity);
    }

    /**
     * Removes a node from the head position doubly-linked list.
     */
    private void removeNode(Node<K, V> node) {
        if (node == null)
            return;

        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
    }

    /**
     * Offers a node to the tail-end of the doubly-linked list because
     * it was recently read or written.
     */
    private void offerNode(Node<K, V> node) {
        if (node == null)
            return;
        if (head == null) {
            head = tail = node;
        } else {
            tail.next = node;
            node.prev = tail;
            node.next = null;
            tail = node;
        }
    }

    /**
     * Adds a new object to the cache. If the cache size has reached it's capacity,
     * then the least recently accessed object will be evicted.
     */
    public void put(K key, V value) {
        if (map.contains(key)) {
            Node<K, V> node = map.get(key);
            node.value = value;
            removeNode(node);
            offerNode(node);
        } else {
            if (map.size() == maxCapacity) {
                map.remove(head.key);
                removeNode(head);
            }

            Node<K, V> node = new Node<K, V>(key, value);
            offerNode(node);
            map.put(key, node);
        }
    }

    /**
     * Fetches an object from the cache (could be null if no such mapping exists).
     * If the object is found in the cache, then it will be moved to the tail-end of the
     * doubly-linked list to indicate that it was recently accessed.
     */
    public V get(K key) {
        Node<K, V> node = map.get(key);
        removeNode(node);
        offerNode(node);
        return node != null ? node.value : null;
    }
}

