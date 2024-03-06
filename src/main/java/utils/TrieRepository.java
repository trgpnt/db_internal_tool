package utils;

import java.util.List;

public class TrieRepository {
    private static final Trie trie = Trie.current();
    private static final TrieRepository INSTANCE = new TrieRepository();
    private String mode;

    private TrieRepository() {
    }

    public static TrieRepository go() {
        return INSTANCE;
    }

    public TrieRepository printTrie() {
        this.trie.printTrie();
        return this;
    }

    public String getMostCommonPrefix() {
        return trie.getMostCommonPrefix();
    }

    public TrieRepository resetTrie() {
        this.trie.reset();
        return this;
    }

    public boolean containsData() {
        return trie.containsData();
    }

    public TrieRepository with(String inp, boolean isOriginal) {
        if (isOriginal) {
            trie.addWord(inp);
        } else {
            trie.addWord(StringUtils.makeNonAlphaStringsFrom(inp, Boolean.TRUE, null));
        }
        return this;
    }

    public TrieRepository withList(List<String> inp) {
        trie.addWord(inp);
        return this;
    }

    public TrieRepository with(String inp) {
        trie.addWord(inp);
        return this;
    }

    public int search(String inp, boolean exact) {
        if (StringUtils.isEmpty(inp)) {
            return -1;
        }
        return trie.searchWord(inp, exact);
    }

    public int search(String inp) {
        if (StringUtils.isEmpty(inp)) {
            return -1;
        }
        return trie.searchWord(inp);
    }
}

