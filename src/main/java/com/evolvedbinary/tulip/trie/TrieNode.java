package com.evolvedbinary.tulip.trie;

import java.util.HashMap;
import java.util.Map;

public class TrieNode {
    Map<Byte, TrieNode> children = new HashMap<>();
    public boolean isKeyword = false;
    public boolean isFunction = false;
    public boolean isAxis = false;
}