package indexes;


import enum_.LOG_DIRECTION;
import org.apache.commons.collections4.CollectionUtils;
import utils.FileUtil;
import utils.NullabilityUtils;
import utils.StringUtils;
import utils.TrieRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reduce and compare complements
 */
public class DbIndexMainLogic {
    private static final DbIndexMainLogic INSTANCE = new DbIndexMainLogic();
    private static final TrieRepository trieProcessor = TrieRepository.go();
    private static Map<String, LinkedList<DbIndex>> onlyLocalHas = new HashMap<>();
    private static Map<String, LinkedList<DbIndex>> onlyUatHas = new HashMap<>();
    private static int unsyncedCounter = 0;
    private LOG_DIRECTION logDirection;
    private List<DbIndex> A = new ArrayList<>();
    private String fileName;
    private List<DbIndex> B = new ArrayList<>();
    private List<DbIndex> self = new ArrayList<>();
    private boolean isInitGrouping = false;
    private Set<String> _sharedTableName;
    private List<DbIndex> strippedLocal = new ArrayList<>();
    private List<DbIndex> strippedUAT = new ArrayList<>();

    private Map<String, LinkedList<DbIndex>> _localTableToIndices = new ConcurrentHashMap<>();
    private Map<String, LinkedList<DbIndex>> _uatTableToIndices = new ConcurrentHashMap<>();

    private List<DbIndex> reduced = new ArrayList<>();

    private DbIndexMainLogic() {
    }

    private DbIndexMainLogic(List<DbIndex> A) {
        this.A = A;
    }

    public static DbIndexMainLogic go() {
        return INSTANCE;
    }

    public static Map<String, LinkedList<DbIndex>> getOnlyLocalHas() {
        return onlyLocalHas;
    }

    public static Map<String, LinkedList<DbIndex>> getOnlyUatHas() {
        return onlyUatHas;
    }

    public DbIndexMainLogic toFile(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public DbIndexMainLogic reset() {
        this.A = new ArrayList<>();
        this.B = new ArrayList<>();
        this.self = new ArrayList<>();
        onlyLocalHas = new HashMap<>();
        onlyUatHas = new HashMap<>();
        return this;
    }

    public DbIndexMainLogic withSelfReferential(List<DbIndex> index) {
        self = Collections.unmodifiableList(index);
        return this;
    }

    public DbIndexMainLogic setDirection(LOG_DIRECTION logDirection) {
        this.logDirection = logDirection;
        return this;
    }

    public LOG_DIRECTION getLogDirection() {
        return logDirection;
    }

    public DbIndexMainLogic withThis(List<DbIndex> index) {
        A = index;
        return this;
    }

    public DbIndexMainLogic withThat(List<DbIndex> index) {
        B = index;
        return this;
    }

    private void selfProcess() {
        /**
         * if identical ref-ed column list and table name
         *    index names share the most common prefix
         */
        StringBuilder sb = new StringBuilder().append("process on self, ith and jth are the record's cardinal number");
        for (int i = 0, n = self.size(); i < n; i++) {
            DbIndex currentA = self.get(i);
            for (int j = i + 1, m = self.size(); j < m; j++) {
                DbIndex currentB = self.get(j);
                if (!isIdentical(currentA, currentB)) {
                    continue;
                }
                sb.append(new StringBuilder()
                        .append(String.format("\n\nFOUND AT\n- Ai = %d, Aj = %d, \n- A's idx name = %s \n- B's idx name = %s \n", i + 1, j + 1, currentA.getIndexName(), currentB.getIndexName()))
                        .append(String.format("- A's def : %s", currentA.getIndexDef().getDefinition()))
                        .append("\n")
                        .append(String.format("- B's def : %s", currentB.getIndexDef().getDefinition())));
            }
        }
        FileUtil.writeToFile(fileName, sb.toString());
    }

    private void initGroupingForLocalAndUAT() {
        if (isInitGrouping) {
            return;
        }
        LinkedList<DbIndex> local;
        LinkedList<DbIndex> uat;
        if (LOG_DIRECTION.LOCAL_TO_UAT.equals(this.logDirection)) {
            local = new LinkedList<>(A);
            uat = new LinkedList<>(B);
        } else {
            uat = new LinkedList<>(A);
            local = new LinkedList<>(B);
        }
        //
        _localTableToIndices = new ConcurrentHashMap<>();
        _uatTableToIndices = new ConcurrentHashMap<>();
        statefulBuildMap(local, _localTableToIndices);
        statefulBuildMap(uat, _uatTableToIndices);
        //
        _sharedTableName = Collections.synchronizedSet(new HashSet<>());
        statefulBuildSharedTables(local, uat, _sharedTableName);
        isInitGrouping = true;
    }

    public void processGroupingForLocalAndUAT() {
        initGroupingForLocalAndUAT();
        String outFile = "shared_log.txt";
        StringBuilder toWrite = new StringBuilder();
        StringBuilder header = new StringBuilder();
        int idx = 0;
        for (String each : _sharedTableName) {
            LinkedList<DbIndex> A;
            LinkedList<DbIndex> B;
            if (this.logDirection.equals(LOG_DIRECTION.LOCAL_TO_UAT)) {
                if (StringUtils.isEmpty(toWrite.toString())) {
                    toWrite.append("---->>> A IS LOCAL, B IS UAT");
                }
                A = _localTableToIndices.get(each);
                B = _uatTableToIndices.get(each);
            } else {
                if (StringUtils.isEmpty(toWrite.toString())) {
                    toWrite.append("---->>> A IS UAT, B IS LOCAL");
                }
                A = _uatTableToIndices.get(each);
                B = _localTableToIndices.get(each);
            }
            if (CollectionUtils.isEqualCollection(A, B)) {
                continue;
            }
            String innerLog = getManualResembleInfo(A, B);
            if (StringUtils.isEmpty(innerLog)) {
                continue;
            }
            idx++;
            if (idx > 1) {
                header.append(String.format("\n\n%s", StringUtils.genCharsByLen(100,'#')));
            }
            header.append(String.format("\n------------ COMPARISON : %d \n", idx));
            header.append(String.format("\n\tFor TABLE NAME = %s\n", each));
            toWrite
                    .append(header)
                    .append(innerLog);

            resetString(header);
        }
        FileUtil.writeToFile(outFile, toWrite.toString());
    }

    private void resetString(StringBuilder header) {
        header.setLength(0);
    }

    /**
     * If
     * index A and B shares the most common prefix
     * ref-ed columns are similar.
     *
     * @param A
     * @param B
     * @return
     */
    private String getManualResembleInfo(LinkedList<DbIndex> A, LinkedList<DbIndex> B) {
        StringBuilder sb = new StringBuilder();
        for (DbIndex a : A) {
            List<String> refColsA = a.getIndexDef().getColumns();
            for (DbIndex b : B) {
                String commonPrefix = getCommonPrefixExistsBtwn(a.getIndexName(), b.getIndexName());
                if (StringUtils.isEmpty(commonPrefix)) {
                    continue;
                }
                List<String> refColsB = b.getIndexDef().getColumns();
                String shared = getSharedAtLeastOneElement(refColsA, refColsB);
                if (StringUtils.isEmpty(shared)) {
                    continue;
                }
                sb.append(String.format("\n\t\t%s", commonPrefix))
                        .append(String.format("\n\tShared referenced columns : %s", shared))
                        .append(String.format("\n\tA's details = %s\n", a.getIndexDef()))
                        .append(String.format("\n\tB's details = %s", b.getIndexDef()));
            }
        }
        return sb.toString();
    }

    /**
     * If shared all then break.
     *
     * @param A
     * @param B
     * @return
     */
    private String getSharedAtLeastOneElement(List<String> A, List<String> B) {
        Collections.sort(A);
        Collections.sort(B);
        if (CollectionUtils.isEqualCollection(A, B)) {
            return "";
        }
        StringBuffer shared = new StringBuffer();
        Set<String> aSet = Collections.synchronizedSet(new HashSet<>());
        A.parallelStream().forEachOrdered(each -> {
            if (!aSet.contains(each)) {
                aSet.add(each);
            }
        });
        StringBuffer finalShared = shared;
        B.parallelStream().forEachOrdered(each -> {
            if (aSet.contains(each)) {
                if (finalShared.length() > 0) {
                    finalShared.append(", ");
                }
                finalShared.append(each);
            }
        });
        if (shared.toString().contains(",")) {
            shared = new StringBuffer("[" + shared + "]");
        }
        return shared.toString();
    }

    private boolean isCommonPrefixInStrings(List<String> total) {
        return StringUtils.isNotEmpty(trieProcessor.go().resetTrie().withList(total).getMostCommonPrefix());
    }

    /**
     * Compares pair
     *
     * @param x
     * @param y
     * @return
     */

    private String getCommonPrefixExistsBtwn(String x, String y) {
        int _x_midIdx = x.length() / 2;
        int _y_midIdx = y.length() / 2;
        trieProcessor.go()
                .resetTrie()
                .withList(Arrays.asList(x, y));

        String possibleCommonPrefix = trieProcessor.getMostCommonPrefix();
        int commonPrefixLen = possibleCommonPrefix.length();
        if (commonPrefixLen >= _x_midIdx && commonPrefixLen >= _y_midIdx) {
            return String.format("\nDetails :\n\tindex A = %s \n\tindex B = %s \n\tcommon prefix = %s\n", x, y, possibleCommonPrefix);
        }
        return "";
    }

    private String toStringFromList(LinkedList<DbIndex> X) {
        StringBuilder sb = new StringBuilder();
        for (DbIndex each : X) {
            String toAdd = each.getIndexDef().toString();
            sb.append(toAdd).append("\n");
        }
        return sb.toString();
    }

    private void statefulBuildSharedTables(LinkedList<DbIndex> A, LinkedList<DbIndex> B, Set<String> toSet) {
        Set<String> aNames = Collections.synchronizedSet(new HashSet<>());
        A.parallelStream().forEachOrdered(each -> {
            if (!aNames.contains(each.getTableName())) {
                aNames.add(each.getTableName());
            }
        });

        B.parallelStream().forEachOrdered(each -> {
            if (aNames.contains(each.getTableName())) {
                toSet.add(each.getTableName());
            }
        });
    }


    private void statefulBuildMap(LinkedList<DbIndex> current, Map<String, LinkedList<DbIndex>> toMap) {
        current.parallelStream().forEachOrdered(each -> {
            String tblName = each.getTableName();
            LinkedList<DbIndex> justFetched = toMap.get(tblName);
            if (Objects.isNull(justFetched)) {
                justFetched = new LinkedList<>();
            }
            justFetched.add(each);
            toMap.put(tblName, justFetched);
        });
    }

    public void processComplement() {
        initGroupingForLocalAndUAT();
        if (CollectionUtils.isEmpty(A) || CollectionUtils.isEmpty(B) || Objects.isNull(logDirection)) {
            throw new RuntimeException("nothing to process");
        }
        Map<String, LinkedList<DbIndex>> currMap = new ConcurrentHashMap<>(onlyUatHas);
        String msgHeader;
        boolean isLocalFirst = true;
        if (LOG_DIRECTION.UAT_TO_LOCAL.equals(this.logDirection)) {
            isLocalFirst = false;
            msgHeader = "--- only uat has\n\n";
            currMap = onlyUatHas;
        } else {
            msgHeader = "--- only local has\n\n";
        }
        currMap.put(msgHeader, new LinkedList<>());
        StringBuffer sb = new StringBuffer()
                .append(isLocalFirst ? "A is local, B is uat" : "A is uat, B is local");

        Set<DbIndex> bKeys = Collections.synchronizedSet(new HashSet<>(B));

        Map<String, LinkedList<DbIndex>> finalCurrMap = currMap;
        A.parallelStream().forEach(each -> {
            if (!bKeys.contains(each) && !StringUtils.containsAny(each.getTableName(), "_0", "_prttn_", "id_cust", "user_def", "svs1ds", "id_prttns", "_o_00", "_idcust_")) {
                finalCurrMap.get(msgHeader).add(each);
            }
        });
        sb.append(msgHeader.toUpperCase());
        sb.append("TOTAL SIZE = ").append(currMap.get(msgHeader).size()).append("\n");
        //process map values
        currMap.values().forEach(dbIndices -> {
            dbIndices.parallelStream().forEach(each -> {
                sb.append(each);
            });
        });
        FileUtil.writeToFile(fileName, sb.toString());
    }

    public void processReduce() {
        if (CollectionUtils.isEmpty(A) || CollectionUtils.isEmpty(B)) {
            selfProcess();
            return;
        }
        /**
         * if identical ref-ed column list and table name
         *    index names share the most common prefix
         */
        StringBuilder sb = new StringBuilder().append("A is local , B is uat, ith and jth are the record indices");
        for (int i = 0, n = A.size(); i < n; i++) {
            DbIndex currentA = A.get(i);
            for (int j = 0, m = B.size(); j < m; j++) {
                DbIndex currentB = B.get(j);
                if (!isIdentical(currentA, currentB)) {

                    continue;
                }
                sb.append(new StringBuilder()
                        .append(String.format("\n\nFOUND AT\nAi = %d, Bj = %d, \nA's idx name = %s \nB's idx name = %s \n", i + 1, j + 1, currentA.getIndexName(), currentB.getIndexName()))
                        .append(String.format("A's def = %s", currentA.getIndexDef().getDefinition()))
                        .append("\n")
                        .append(String.format("B's def = %s", currentB.getIndexDef().getDefinition())));
            }
        }
        FileUtil.writeToFile(fileName, sb.toString());
    }

    //TODO not sure if needs this
    private boolean isMostCommonPrefix(String A, String B) {
        trieProcessor.resetTrie().withList(Arrays.asList(A, B));
        String prefix = trieProcessor.getMostCommonPrefix();
        if (A.contains(prefix) && B.contains(prefix)) {
            return true;
        }
        return false;
    }

    private boolean isIdentical(DbIndex A, DbIndex B) {
        if (!A.getTableName().equalsIgnoreCase(B.getTableName()) || !isIdenticalTableAndCols(A, B)) {
            return false;
        }
        String aDef = A.getIndexDef().getDefinition().toLowerCase();
        String bDef = B.getIndexDef().getDefinition().toLowerCase();
        List<String> aList = StringUtils.splitListByValue(StringUtils.makeNonAlphaStringsFrom(aDef, true, null), "ON");
        List<String> bList = StringUtils.splitListByValue(StringUtils.makeNonAlphaStringsFrom(bDef, true, null), "ON");
        return CollectionUtils.isEqualCollection(aList, bList);
    }


    private boolean isIdenticalTableAndCols(DbIndex A, DbIndex B) {
        if (NullabilityUtils.isAnyNullIn(A, B)) {
            throw new RuntimeException("null object ");
        }
        DbIndexDef aIndexDef = A.getIndexDef();
        DbIndexDef bIndexDef = B.getIndexDef();
        boolean isAUnique = aIndexDef.isUnique();
        boolean isBUnique = bIndexDef.isUnique();
        List<String> aColumns = aIndexDef.getColumns();
        List<String> bColumns = bIndexDef.getColumns();
//        if (isAUnique && isBUnique) {
//            throw new RuntimeException("Identical this much !!! ");
//        }
        return CollectionUtils.isEqualCollection(aColumns, bColumns);
    }

    public List<DbIndex> getReduced() {
        return Collections.unmodifiableList(reduced);
    }

    public List<DbIndex> getA() {
        return Collections.unmodifiableList(A);
    }
}
