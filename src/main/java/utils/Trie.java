package utils;

import org.apache.commons.collections4.CollectionUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A Trie's implementation
 * works with :
 * letters, digits, limited special chars.
 */

public class Trie {
    private static final Trie INSTANCE = new Trie();
    private static final int BASE_OFFSET = 32;
    private static final int SIZE = 95;
    private final List<String> data = new ArrayList<>();
    private TrieNode root;

    private Trie() {
        root = new TrieNode();
    }

    public static Trie current() {
        return INSTANCE;
    }

    public static void main(String[] args) {
        Trie instance = Trie.current();
        instance.addWord("CREATE (hello_hello)");
        System.out.println(instance.searchWord("ello__hello"));
        System.out.println(instance.searchWord("hello_hello"));
        System.out.println(instance.searchWord("CREATE (hello_hello)ello_helloooo)", true));
        System.out.println(instance.searchWord("CREATE (hello_hello)", true));
        System.out.println(instance.searchWord("CREATE (hello_hello)", false));
    }

    public void reset() {
        root = new TrieNode();
    }

    public void addWord(List<String> stringList) {
        for (String each : stringList) {
            addWord(StringUtils.cleanseDefaultCharValue(each));
        }
    }

    public void addWord(String inp) {
        try {
            TrieNode current = root;
            for (Character each : inp.toCharArray()) {
                int idx = StringUtils.getRelaxedAsciiValueOf(each, BASE_OFFSET);
                if (idx < 0) {
                    continue;
                }
                if (Objects.isNull(current.trieNodes[idx])) {
                    current.trieNodes[idx] = new TrieNode();
                }
                current = current.trieNodes[idx];
            }
            current.count++;
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage());
        }
    }

    public int searchWord(String inp, boolean exact) {
        inp = StringUtils.cleanseDefaultCharValue(inp);
        TrieNode current = root;
        int running = 0;
        for (Character each : inp.toCharArray()) {
            running++;
            int idx = StringUtils.getRelaxedAsciiValueOf(each, BASE_OFFSET);
            if (idx < 0) {
                continue;
            }
            if (Objects.isNull(current.trieNodes[idx])) {
                break;
            }
            current = current.trieNodes[idx];
        }
        if (exact && running != inp.length()) {
            return -1;//exact matches
        }
        return current.count;
    }

    public int searchWord(String inp) {
        TrieNode current = root;
        for (Character each : inp.toCharArray()) {
            int idx = StringUtils.getRelaxedAsciiValueOf(each, BASE_OFFSET);
            if (idx < 0) {
                break;
            }
            if (Objects.isNull(current.trieNodes[idx])) {
                continue;
            }
            current = current.trieNodes[idx];
        }
        return current.count;
    }

    public String getMostCommonPrefix() {
        return mostCommonPrefix(root, new StringBuilder());
    }

    private boolean hasOnlyOneChild(TrieNode[] node) {
        return countNonNullNodes(node) == 2;
    }

    private int countNonNullNodes(TrieNode[] nodes) {
        int counter = 0;
        for (TrieNode each : nodes) {
            if (Objects.isNull(each)) {
                continue;
            }
            counter++;
            if (counter == 2) {
                break;
            }
        }
        return counter;
    }

    private String mostCommonPrefix(TrieNode current, StringBuilder sb) {
        TrieNode[] children = current.trieNodes;
        if (hasOnlyOneChild(children)) {
            return "";
        }
        for (int i = 0, n = children.length; i < n; i++) {
            TrieNode each = children[i];
            if (Objects.isNull(each)) {
                continue;
            }
            sb.append(convertFromAscii(i));
            mostCommonPrefix(each, sb);
        }
        return sb.toString();
    }

    public void printTrie() {
        convergeData(root, new StringBuilder());
        display();
    }

    private void display() {
        for (String each : data) {
            System.out.println(each);
        }
    }

    private void convergeData(TrieNode node, StringBuilder running) {
        TrieNode[] extracted = node.trieNodes;
        for (int i = 0, n = extracted.length; i < n; i++) {
            TrieNode each = extracted[i];
            if (Objects.isNull(each)) {
                continue;
            }
            running.append(convertFromAscii(i));
            convergeData(each, running);
        }
        if (running.length() > 0) {
            data.add(running.toString());
            running.setLength(0);
        }
    }

    private char convertFromAscii(int asciiVal) {
        return (char) (asciiVal + BASE_OFFSET);
    }

    public boolean containsData() {
        return containsData(root, new AtomicBoolean(false));
    }

    public boolean containsData(TrieNode node, AtomicBoolean res) {
        if (res.get()) {
            return res.get();
        }
        if (CollectionUtils.isNotEmpty(Arrays.asList(node.trieNodes))) {
            for (TrieNode each : node.trieNodes) {
                if (Objects.nonNull(each)) {
                    res.set(true);
                    return true;
                }
            }
        }
        for (TrieNode eachNode : node.trieNodes) {
            res.set(containsData(eachNode, res));
        }
        return res.get();
    }


    static class TrieNode {
        TrieNode[] trieNodes;
        int count;

        private TrieNode() {
            count = 0;
            trieNodes = new TrieNode[SIZE];
            for (int i = 0; i < SIZE; i++) {
                trieNodes[i] = null;
            }
        }
    }
}

