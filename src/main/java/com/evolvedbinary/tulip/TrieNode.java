package com.evolvedbinary.tulip;

import java.util.HashMap;
import java.util.Map;

class TrieNode {
    Map<Byte, TrieNode> children = new HashMap<>();
    boolean isFunction = false;
    boolean isAxis = false;
}