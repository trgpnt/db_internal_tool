package utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StringUtils {
    private static final Logger LOG = LoggerFactory.getLogger(StringUtils.class);

    private static final String OPEN_PAREN = "(";
    private static final String CLOSE_PAREN = ")";
    private static final String AT = "@";
    private static final String COMMA = ",";
    private static final String DOT = ".";
    private static final String EQUAL = " = ";
    private static final char SEMICOLON = ';';
    private static final String SINGLE_BREAK = "\n";
    private static final Map<String, String> rawImportToResovled = new HashMap<>();

    public static boolean isNotEmpty(String input) {
        return !isEmpty(input);
    }

    public static boolean isEmpty(String input) {
        return Objects.isNull(input) || input.isEmpty();
    }

    public static boolean isNoneBlank(String input) {
        if (isEmpty(input)) {
            return false;
        }
        int l = 0;
        int r = input.length() - 1;
        char[] inpArr = input.toCharArray();
        while (l <= r) {
            char left = inpArr[l];
            char right = inpArr[r];
            if (Character.isLetterOrDigit(left) || Character.isLetterOrDigit(right)) {
                return true;
            }
            l++;
            r--;
        }
        return false;
    }
    public static String genCharsByLen(int len, char x) {
        char[] res = new char[len];
        for (int i = 0; i < len; i++) {
            res[i] = x;
        }
        return new String(res);
    }

    public static String stripDoubleEndedSpaces(String inp) {
        if (StringUtils.isEmpty(inp)) {
            return inp;
        }
        int n = inp.length();
        int i = 0;
        int j = n - 1;
        while (i < j) {
            boolean isProcessible = false;
            if (Character.isSpaceChar(inp.charAt(i))) {
                isProcessible = true;
                i++;
            }
            if (Character.isSpaceChar(inp.charAt(j))) {
                isProcessible = true;
                j--;
            }
            if (!isProcessible) {
                break;
            }
        }
        return inp.substring(i, j + 1);
    }

    public static boolean isBlank(String inp) {
        return !isNoneBlank(inp);
    }

    public static boolean containsAny(String toCheck, String... args) {
        AtomicBoolean res = new AtomicBoolean();
        Arrays.stream(args)
                .parallel()
                .forEach(
                        each -> {
                            if (res.get()) {
                                return;
                            }
                            if (toCheck.contains(each)) {
                                res.set(true);
                            }
                        });
        return res.get();
    }

    public static String resolveReplaces(String orig, String... fromToPairs) {
        final int PAIR_JUMP = 2;
        if (fromToPairs.length % 2 != 0) {
            LOG.error("Not enough data to perform action");
        }
        for (int i = 0, n = fromToPairs.length; i < n; i += PAIR_JUMP) {
            orig = orig.replace(fromToPairs[i], fromToPairs[i + 1]);
        }
        // fishy, ensure single-dotted only.
        return orig.replaceAll("\\.+", ".");
    }

    public static boolean endsWithAny(String toCheck, String... args) {
        for (String each : args) {
            if (toCheck.endsWith(each) || toCheck.endsWith(each + ".java")) {
                return true;
            }
        }
        return false;
    }

    public static String stripComments(String inp) {
        inp = inp.replaceAll("//.*", "");
        Pattern pattern = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(inp);
        inp = matcher.replaceAll("");

        return inp;
    }

    public static String appendIndentableBracketTo(String inp, String bracket, String indentVal) {
        if (inp.isEmpty() || inp.contains(String.valueOf(bracket))) {
            return inp;
        }
        StringBuilder resultBuilder = new StringBuilder(inp);
        if (!bracket.equalsIgnoreCase(
                String.valueOf(resultBuilder.charAt(resultBuilder.length() - 1)))) {
            resultBuilder.append(indentVal).append(bracket);
        }
        return resultBuilder.toString();
    }

    public static String stripUntilDollarSign(String inp) {
        for (int i = 0, n = inp.length(); i < n; i++) {
            if (inp.charAt(i) == '$') {
                return inp.substring(0, i);
            }
        }
        return inp;
    }

    public static String stripUntilClassPath(String inp, Character... toKeep) {
        List<Character> toKeeps = Arrays.asList(toKeep);
        StringBuilder sb = new StringBuilder();
        for (Character c : inp.toCharArray()) {
            if (Character.isLetterOrDigit(c) || toKeeps.contains(c)) {
                sb.append(c);
            }
        }
        return resolveReplaces(sb.toString(), "/", "");
    }

    public static String extractSuffixByMarker(String inp, char marker) {
        int stop = getIdxToExtractSuffixByMarker(inp, marker);
        return new String(inp.toCharArray(), stop, inp.length() - stop);
    }


    public static int getIdxToExtractSuffixByMarker(String inp, char marker) {
        char[] charArr = inp.toCharArray();
        int n = charArr.length;
        int stop = 0;
        for (int i = n - 1; i >= 0; i--) {
            if (marker == inp.charAt(i)) {
                stop = i + 1;
                break;
            }
        }
        return stop;

    }

    public static boolean isAllLowerCase(String inp) {
        for (Character c : inp.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return false;
            }
        }
        return true;
    }

    public static String stripChars(String inp, Set<Character> skipped) {
        StringBuilder sb = new StringBuilder();
        for (Character each : inp.toCharArray()) {
            if (skipped.contains(each)) {
                continue;
            }
            sb.append(each);
        }
        return sb.toString();
    }

    /**
     * Pincer-strip double-ended non alphanumeric chars from string, until meets character / digit
     * from both ends.
     *
     * @param inp
     * @return
     */
    public static String stripDoubleEndedNonAlphaNumeric(String inp) {
        if (StringUtils.isEmpty(inp)) {
            return ""; // avoid NPE
        }
        inp = StringUtils.cleanseDefaultCharValue(inp);
        final int THRESHOLD = 10;
        final long start = System.currentTimeMillis();
        int left = 0, n = inp.length() - 1, right = n;
        while (left < right && left < inp.length() && !Character.isLetterOrDigit(inp.charAt(left))) {
            left++;
        }
        while (left < right && right > 0 && !Character.isLetterOrDigit(inp.charAt(right))) {
            right--;
        }
        // if unchanged.
        if (left >= right || (left == 0 && right == n)) {
            return inp;
        }
        while (true) {
            if (System.currentTimeMillis() - start >= THRESHOLD) {
                break;
            }
            try {
                return inp.substring(left, right + 1);
            } catch (Throwable t) {
                right -= 1;
            }
        }
        return inp;
    }


    public static List<String> processImportRegion(String inp) {
        if (StringUtils.isEmpty(inp)) {
            return Collections.emptyList();
        }
        List<String> processed = new ArrayList<>();
        int n = inp.length();
        char[] charArr = inp.toCharArray();
        int endMarker;
        for (int i = 0; i < n; i++) {
            char curr = charArr[i];
            if (!Character.isLetterOrDigit(curr)) {
                continue;
            }
            endMarker = i + 1;
            while (endMarker < n && SEMICOLON != charArr[endMarker]) {
                endMarker++;
            }
            processed.add(new String(charArr, i, endMarker - i));
            i = endMarker + 1;
        }
        return processed;
    }

    public static String stripValueFromString(String inp, String toStrip) {
        try {
            char[] inpArr = inp.toCharArray();
            int inpLen = inpArr.length;
//            char[] importArr = new char[]{'i', 'm', 'p', 'o', 'r', 't', SPACE};
            char[] stripped = (toStrip + " ").toCharArray();
            int strippedLen = stripped.length;
            int idx = 0;
            int checkerIdx = 0;
            boolean navigated = false;
            //Navigate to the first equal pair.
            while (idx < inpLen && inpArr[idx] != stripped[checkerIdx]) {
                idx++;
                navigated = true;
            }
            //edge case
            if (idx == inpLen - 1) {
                return "";
            }
            //synchronize the import array's index to compare the next pair
            if (navigated) {
                checkerIdx++;
            }
            while (checkerIdx < strippedLen && idx < inpLen) {
                if (stripped[checkerIdx++] != inpArr[idx++]) {
                    break;
                }
            }
            //if checker idx hasn't passed thru all import array then bail
            if (checkerIdx < strippedLen - 1) {
                return "";
            }
            return StringUtils.stripDoubleEndedNonAlphaNumeric(new String(inpArr, idx - 1, inpLen - idx + 1));
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage());
        }
    }

    public static int lastIndexOf(
            String inp, char x, Integer backwardFrom, Integer ordinalIndex, Boolean skipBreaker) {
        if (!inp.contains(String.valueOf(x))) {
            return -1;
        }
        if (Objects.isNull(skipBreaker)) {
            skipBreaker = true;
        }
        if (Objects.isNull(ordinalIndex)) {
            ordinalIndex = 1;
        }
        int n = inp.length() - 1;
        if (Objects.nonNull(backwardFrom)) {
            n = backwardFrom;
        }
        int matches = 0;
        int shrinkingI = n;
        for (int i = n; i >= 0; i--) {
            try {
                if (inp.charAt(i) == x) {
                    matches++;
                    if (ordinalIndex == -1) {
                        shrinkingI = i;
                        continue;
                    }
                    if (matches == ordinalIndex) {
                        return i;
                    }
                }
                if ((skipBreaker) && (inp.charAt(i) == '\r' || inp.charAt(i) == '\n')) {
                    break;
                }
            } catch (Throwable t) {
                i--;
            }
        }
        return shrinkingI;
    }

    public static int countCharsFromEnd(String inp, char x) {
        int i = lastIndexOf(inp, x, null, null, null);
        int count = 0;
        while (i >= 0 && inp.charAt(i) == x) {
            i--;
            count++;
        }
        return count;
    }

    public static String cleanseDefaultCharValue(String inp) {
        StringBuilder sb = new StringBuilder();
        for (Character each : inp.toCharArray()) {
            if (each == '\u0000' || StringUtils.getRelaxedAsciiValueOf(each, 32) < 0) {
                continue;
            }
            sb.append(each);
        }
        return sb.toString();
    }


    public static List<String> makeNonAlphaStringsFrom(String inp, boolean isUnique, Set<Character> toKeep) {
        if (StringUtils.isEmpty(inp)) {
            return new ArrayList<>();
        }
        if (Objects.isNull(toKeep)) {
            toKeep = new HashSet<>();
        }
        List<String> res = new ArrayList<>();
        char[] inpCharArr = inp.toCharArray();
        int l = 0;
        int n = inpCharArr.length;
        while (l < n) {
            while (l < n && !Character.isLetterOrDigit(inpCharArr[l])) {
                l++;
            }
            int r = l;
            while (r < n && (Character.isLetterOrDigit(inpCharArr[r]) || toKeep.contains(inpCharArr[r]))) {
                r++;
            }
            final String toAdd = new String(inpCharArr, l, r - l);
            if (isUnique && !res.contains(toAdd) && StringUtils.isNotEmpty(toAdd)) {
                res.add(toAdd);
            }
            l = r + 1;
        }

        return res;
    }

    /**
     * @param inp
     * @return A spaced-string from list
     */
    public static String toStringFromList(List<String> inp, char connective) {
        if (CollectionUtils.isEmpty(inp)) {
            return "";
        }
        int length = inp.size();
        final int availThreads = Runtime.getRuntime().availableProcessors();
        int possibleThreads = length / availThreads;
        StringBuffer stringBuffer = new StringBuffer();
        if (possibleThreads < 2) {
            boolean isFirst = true;
            for (String each : inp) {
                if (!isFirst) {
                    stringBuffer.append(connective);
                }
                stringBuffer.append(each);
                if (isFirst) {
                    isFirst = false;
                }
            }
        } else {
            List<String> threadSafe = Collections.synchronizedList(inp);
            Thread[] threads = new Thread[availThreads];
            int threadIdx = 0;
            AtomicInteger decreaser = new AtomicInteger(availThreads);
            AtomicInteger increaser = new AtomicInteger(0);
            for (int i = 0; i < threads.length; i++) {
                AtomicInteger idx = new AtomicInteger(increaser.get());
                AtomicInteger atomicLen = new AtomicInteger(length / decreaser.get());
                Runnable runnable = (() -> {
                    AtomicBoolean isFirst = new AtomicBoolean(true);
                    for (; idx.get() < atomicLen.get() - 1; idx.getAndIncrement()) {
                        if (!isFirst.get()) {
                            stringBuffer.append(connective);
                        }
                        stringBuffer.append(threadSafe.get(idx.get()));
                        if (isFirst.get()) {
                            isFirst.set(false);
                        }
                    }
                });
                threads[threadIdx++] = new Thread(runnable);
                increaser.set(atomicLen.get());
                decreaser.getAndDecrement();
            }
            ThreadUtils.executeAndJoinAll(threads);
        }
        return stringBuffer.toString();
    }


    public static String bulkCascadeRemoveSuffixedString(
            String inp, char suffix, Character... patternSplitterTeller) {
        final List<Character> teller = Arrays.asList(patternSplitterTeller);
        StringBuilder partitionCollector = new StringBuilder();
        StringBuilder removed = new StringBuilder();
        for (int i = 0, n = inp.length(); i < n; i++) {
            Character cur = inp.charAt(i);
            if (!teller.contains(cur)) {
                partitionCollector.append(cur);
                continue;
            }
            Character connective = cur;
            if (i == n - 1) {
                if (partitionCollector.length() > 0) {
                    removed.append(cascadeRemoveSuffixedString(partitionCollector.toString(), suffix));
                }
                removed.append(connective);
                break;
            }
            removed.append(cascadeRemoveSuffixedString(partitionCollector.toString(), suffix));
            if (Objects.nonNull(connective)) {
                removed.append(connective);
            }
            partitionCollector.setLength(0);
        }
        String finalSwticher =
                removed.length() == 0 ? partitionCollector.toString() : removed.toString();
        if (finalSwticher.contains(String.valueOf(suffix))) {
            finalSwticher = cascadeRemoveSuffixedString(finalSwticher, suffix);
        }
        return finalSwticher;
    }

    /**
     * Should be used when ony " ONE pattern range " is present. This method will work for only 1
     * self-contained pattern. not work for multiple pattern ranges.
     *
     * <p>A suffixed string is a pattern containing: a word followed by a character. for example,
     * these are suffixed strings: string = java.util.List suffixed strings : java., util.
     * non-suffixed : List
     *
     * @param inp
     * @return a string having its suffixed ones removed.
     * <p>input : java.util.List output : List
     */
    public static String cascadeRemoveSuffixedString(String inp, char suffix) {
        int stop = getIdxToExtractSuffixByMarker(inp, suffix);
        return new String(inp.toCharArray(), stop, inp.length() - stop);
    }

    public static boolean isPrefixedWith(String prefix, String content) {
        int n = content.length();
        StringBuilder revRunner = new StringBuilder();
        for (int i = n - 1; i >= 0; i--) {
            Character cur = content.charAt(i);
            if (Character.isLetter(cur)) {
                revRunner.append(cur);
            }
            if (revRunner.length() == prefix.length()) {
                return revRunner.reverse().toString().equalsIgnoreCase(prefix);
            }
        }
        return false;
    }

    public static int firstIdxOfNonAlphanumeric(String x) {
        for (int i = 0, n = x.length(); i < n; i++) {
            if (Character.isLetterOrDigit(x.charAt(i))) {
                continue;
            }
            return i;
        }
        return -1;
    }

    public static String buildAnnotationPackage(String unresolvedPackage, String annotation) {
        return (" " + unresolvedPackage + DOT + annotation + SEMICOLON).replace(AT, "");
    }

    /**
     * Will stop when reaching the last separator.
     *
     * @param inp
     * @param separator
     * @return
     */
    public static String getLastWord(String inp, String separator) {
        NullabilityUtils.isAllNonEmpty(true, inp, separator);
        StringBuilder rev = new StringBuilder();
        for (int n = inp.length(), i = n - 1; i >= 0; i--) {
            Character cur = inp.charAt(i);
            if (separator.equalsIgnoreCase(String.valueOf(cur))) {
                break;
            }
            rev.append(cur);
        }
        return rev.reverse().toString();
    }

    /**
     * Separated by each dot, ensure no more than 1 word contains >= 1 upper-case characters.
     *
     * @param inp
     * @return
     */
    public static String correctifyImportString(String inp, Character sep) {
        if (StringUtils.isEmpty(inp) || !inp.contains(String.valueOf(sep))) {
            return inp;
        }
        if (!MapUtils.isEmpty(rawImportToResovled) && rawImportToResovled.containsKey(inp)) {
            return rawImportToResovled.get(inp);
        }
        StringBuilder res = new StringBuilder();
        StringBuilder each = new StringBuilder();
        boolean isMetUppercase = false;
        for (int i = 0, n = inp.length(); i < n && !isMetUppercase; i++) {
            Character curr = inp.charAt(i);
            if (curr != sep) {
                each.append(curr);
                continue;
            }
            if (each.length() > 0 && !res.toString().contains(each)) {
                if (res.length() > 0) {
                    res.append(sep);
                }
                res.append(each);
                if (!isAllLowerCase(each.toString())) {
                    isMetUppercase = true;
                }
            }
            each.setLength(0);
        }
        if (each.length() > 0 && !res.toString().contains(each)) {
            res.append(sep).append(each);
        }
        /** Ok time to hack */
        String toPut = "";
        if (inp.charAt(0) == '.' || inp.charAt(inp.length() - 1) == '.') {
            final String rawText = stripDoubleEndedNonAlphaNumeric(inp);
            toPut = "java.util." + rawText;
        }
        rawImportToResovled.put(inp, toPut);
        return rawImportToResovled.get(inp);
    }

    public static boolean bidirectionalContains(String x, String y) {
        return x.contains(y) || y.contains(x);
    }

    public static List<String> splitListByValue(List<String> inp, String from) {
        if (CollectionUtils.isEmpty(inp) || StringUtils.isEmpty(from)) {
            return Collections.emptyList();
        }
        List<String> subbed = new ArrayList<>();
        int n = inp.size();
        int i;
        for (i = 0; i < n; i++) {
            String each = inp.get(i);
            if (from.equalsIgnoreCase(each)) {
                break;
            }
        }
        for (i = i + 1; i < n; i++) {
            subbed.add(inp.get(i));
        }
        return subbed;
    }

    public static int firstIndexOf(String inp, char x, int start, boolean isBackward) {
        if (isBackward) {
            for (int i = start; i >= 0; i--) {
                if (x == inp.charAt(i)) {
                    return i;
                }
            }
        } else {
            for (int i = start, n = inp.length(); i < n; i++) {
                if (x == inp.charAt(i)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Space is handled by the caller.
     *
     * @param key
     * @param value
     * @param operator
     * @return
     */
    public static String formKeyValuePair(String key, String value, String operator) {
        NullabilityUtils.isAllNonEmpty(true, key, value, operator);
        return key + operator + value;
    }

    public static String findPrependablePieceFrom(
            String content, int backwardFrom, Character breakingChar, boolean isSkipSpace) {
        if (Objects.isNull(breakingChar)) {
            breakingChar = '\r';
        }
        StringBuilder rev = new StringBuilder();
        for (int i = backwardFrom; i >= 0; i--) {
            Character c = content.charAt(i);
            if (String.valueOf(content.charAt(i)).equalsIgnoreCase(SINGLE_BREAK)
                    || content.charAt(i) == breakingChar) {
                break;
            }
            if (isSkipSpace && !Character.isLetterOrDigit(c)) {
                continue;
            }
            rev.append(c);
        }
        return rev.reverse().toString();
    }

    /**
     * @param c          must be alphanumeric.
     * @param isRebounce use 0 as index.
     * @return
     */
    public static int asciiValueOf(char c, boolean isRebounce) {
        int asciiVal;
        try {
            if (!isRebounce || !Character.isLetter(c)) {
                return c;
            }
            if (Character.isLowerCase(c)) {
                asciiVal = c - 97;
            } else {
                asciiVal = c - 65;
            }
        } catch (Throwable t) {
            asciiVal = -1;
        }
        return asciiVal;
    }

    public static int getRelaxedAsciiValueOf(char c, int baseOffset) {
        return c - baseOffset;
    }
    public static String extractLastSubStrAfter(char x, String inp, boolean isReversed) {
        try {
            StringBuilder sb = new StringBuilder();
            int n = inp.length();
            for (int i = n - 1; i >= 0; i--) {
                char curr = inp.charAt(i);
                if (x == curr) {
                    break;
                }
                sb.append(curr);
            }
            if (isReversed) {
                return sb.reverse().toString();
            }
            return sb.toString();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public static List<String> reduceToOnlyLastSubStrAfter(
            char x, List<String> input, boolean isModifiable) {
        List<String> res = new ArrayList<>();
        for (int idx = 0, n = input.size(); idx < n; idx++) {
            final String substringed = extractLastSubStrAfter(x, input.get(idx), true);
            if (!isModifiable) {
                res.add(substringed);
            } else {
                input.set(idx, substringed);
            }
        }
        if (CollectionUtils.isNotEmpty(res)) {
            return res;
        }
        return input;
    }


    /**
     * Supports upper/lower-cased alphanumeric letters.
     *
     * @param x
     * @param y
     * @return
     */
    public static boolean isAnagram(String x, String y) {
        /** Set A = {Aa-Zz + 0 -> 9} -> nO of chars = 62 */
        int[] map1 = new int[62];
        int[] map2 = new int[62];
        Arrays.fill(map1, 0);
        Arrays.fill(map2, 0);
        int i = 0;
        int n = x.length();
        int m = y.length();
        for (; i < n && i < m; i++) {
            char c1 = x.charAt(i);
            char c2 = y.charAt(i);
            if (Character.isLetterOrDigit(c1)) {
                map1[asciiValueOf(c1, Boolean.TRUE)]++;
            }
            if (Character.isLetterOrDigit(c2)) {
                map2[asciiValueOf(c2, Boolean.TRUE)]++;
            }
        }

        for (; i < n; i++) {
            char c1 = x.charAt(i);
            if (Character.isLetterOrDigit(c1)) {
                map1[asciiValueOf(c1, Boolean.TRUE)]++;
            }
        }

        for (; i < m; i++) {
            char c2 = y.charAt(i);
            if (Character.isLetterOrDigit(c2)) {
                map2[asciiValueOf(c2, Boolean.TRUE)]++;
            }
        }

        /** Verify */
        for (int r = 0; r < 62; r++) {
            if (map1[r] == map2[r]) {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * We only consume letter / digit character, except given characters. This will apply
     * #stripDoubleEndedNonAlphaNumeric's logic, and also ensure each pair of consecutive characters
     * are only one-spaced separated.
     *
     * @param inp
     * @return
     */
    public static String ensureOneWhitespace(String inp, Set<Character> consumable) {
        if (StringUtils.isEmpty(inp) || StringUtils.isBlank(inp)) {
            return "";
        }
        inp = stripDoubleEndedNonAlphaNumeric(inp);
        if (Objects.isNull(consumable)) {
            consumable = new HashSet<>();
        }
        StringBuilder runner = new StringBuilder();
        boolean isMetSpace = false;
        for (int i = 0, n = inp.length(); i < n; i++) {
            Character c = inp.charAt(i);
            if (Character.isLetterOrDigit(c) || consumable.contains(c)) {
                runner.append(c);
                isMetSpace = false;
                continue;
            }
            if (Character.isSpaceChar(c)) {
                if (i > 0 && !isMetSpace && runner.length() > 0) {
                    runner.append(c);
                    isMetSpace = true;
                }
            }
        }
        return runner.toString();
    }
}

