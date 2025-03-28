package com.evolvedbinary.tulip;

import java.util.HashMap;
import java.util.Map;



class Trie {
    private final TrieNode root = new TrieNode();


    // Insert a word using its byte hex values
    public void insert(String word, boolean isFunction) {
        TrieNode node = root;
        for (byte b : word.getBytes()) { // Convert to byte array
            node.children.putIfAbsent(b, new TrieNode());
            node = node.children.get(b);
        }
        if(isFunction) {
            node.isFunction = true;
        } else {
            node.isAxis = true;
        }
    }

    // Search for a word using byte array input
    public TrieNode traverse(byte b, TrieNode node) {
        return node.children.get(b);
    }

    public TrieNode getRoot() {
        return root;
    }
}