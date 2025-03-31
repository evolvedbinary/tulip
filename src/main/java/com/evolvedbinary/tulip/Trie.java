package com.evolvedbinary.tulip;

class Trie {
    private final TrieNode root = new TrieNode();


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

    public TrieNode traverse(byte b, TrieNode node) {
        return node.children.get(b);
    }

    public TrieNode getRoot() {
        return root;
    }
}