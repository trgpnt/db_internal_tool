package indexes;

import org.apache.commons.collections4.CollectionUtils;
import utils.StringUtils;

import java.util.*;

public class DbIndexDef {
    private static final String USING = "using";
    private static final Map<String, DbIndex.INDEX_TYPE> stringToIdxType = new HashMap<>();
    private static final Set<String> PG_KEYWORDS = new HashSet<>();

    static {
        stringToIdxType.put("btree", DbIndex.INDEX_TYPE.BTREE);
        stringToIdxType.put("hash", DbIndex.INDEX_TYPE.HASH);
        stringToIdxType.put("unknown", DbIndex.INDEX_TYPE.UNKNOWN);
        //continue

        PG_KEYWORDS.add("INCLUDE");
        PG_KEYWORDS.add("NULLS");
        PG_KEYWORDS.add("WITH");
        PG_KEYWORDS.add("ASC");
        PG_KEYWORDS.add("DESC");
        PG_KEYWORDS.add("FIRST");
        PG_KEYWORDS.add("LAST");
        PG_KEYWORDS.add("TABLESPACE");
        PG_KEYWORDS.add("WHERE");

    }

    private boolean isUnique;
    private DbIndex.INDEX_TYPE type;
    private List<String> columns;
    private String definition;

    private DbIndexDef(String definition, boolean isUnique, DbIndex.INDEX_TYPE type, List<String> columns) {
        this.definition = definition;
        this.isUnique = isUnique;
        this.type = type;
        this.columns = columns;
    }

    public static DbIndexDef craftFromStrings(String inp) {
        if (StringUtils.isEmpty(inp) || !inp.contains("(")) {
            return null;
        }
        final int idxOfParenthesis = inp.indexOf('(');
        String rawColumns = StringUtils.stripDoubleEndedNonAlphaNumeric(inp.substring(idxOfParenthesis, inp.length() - 1));
        LinkedList<String> strippedColumns = new LinkedList<>();
        List.of(rawColumns.split(","))
                .parallelStream()
                .forEachOrdered(each -> {
                    strippedColumns.add(stripWithPGKeys(StringUtils.stripDoubleEndedNonAlphaNumeric(each.toLowerCase())));
                });
        Collections.sort(strippedColumns);
        return craft(StringUtils.stripDoubleEndedSpaces(inp), evalIsUnique(inp), evalIndexType(inp), strippedColumns);
    }

    private static String stripWithPGKeys(String x) {
        StringBuffer res = new StringBuffer(x);
        PG_KEYWORDS.parallelStream().forEach(each -> {
            if (res.toString().contains(each)) {
                res.toString().replace(each, "");
            }
        });
        return res.toString();
    }


    private static boolean evalIsUnique(String inp) {
        return inp.contains("unique") || inp.contains("UNIQUE");
    }

    private static DbIndex.INDEX_TYPE evalIndexType(String inp) {
        int lowercasedFound = inp.indexOf(USING);
        int uppercasedFound = inp.indexOf(USING.toUpperCase());
        int finalIdx = Math.max(lowercasedFound, uppercasedFound);
        if (finalIdx == -1) {
            return DbIndex.INDEX_TYPE.UNKNOWN;
        }
        String toScan = inp.substring(finalIdx + USING.length(), inp.indexOf('('));
        String cleansed = StringUtils.stripDoubleEndedNonAlphaNumeric(toScan);
        if (stringToIdxType.containsKey(cleansed.toLowerCase())) {
            return stringToIdxType.get(cleansed);
        }
        return stringToIdxType.get("unknown");
    }

    public static DbIndexDef craft(String definition, boolean isUnique, DbIndex.INDEX_TYPE type, List<String> columns) {
        if (DbIndex.INDEX_TYPE.UNKNOWN.equals(type) || CollectionUtils.isEmpty(columns)) {
            throw new RuntimeException(String.format("Bad values at %s %s %s", isUnique, type, columns));
        }
        return new DbIndexDef(definition, isUnique, type, columns);
    }

    public String getDefinition() {
        return definition;
    }

    public boolean isUnique() {
        return isUnique;
    }

    public DbIndex.INDEX_TYPE getType() {
        return type;
    }

    public List<String> getColumns() {
        return columns;
    }

    @Override
    public String toString() {
//        return forPrintingOnlyFlow();
        return "";
    }

    private String forPrintingOnlyFlow() {
        return "\n\t\t- is_unique = " + isUnique +
                "\n\t\t- type = " + type +
                "\n\t\t- columns = " + columns +
                "\n\t\t- definition = '" + definition + '\'';
    }

    private String forGrouping() {
        String printInternalMsg = isUnique ? " - internally created by Postgre" : "";
        return "\n\t- is_unique = " + isUnique + printInternalMsg +
                "\n\t- definition = " + definition +
                "\n\t- columns = " + printCols();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DbIndexDef that = (DbIndexDef) o;
        return isUnique == that.isUnique && type == that.type && Objects.equals(columns, that.columns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isUnique, type, columns);
    }

    private String printCols() {
        StringBuilder sb = new StringBuilder();
        if (columns.size() >= 2) {
            sb.append('[');
        }
        boolean isFirst = true;
        for (String each : columns) {
            if (!isFirst) {
                sb.append(", ");
            }
            sb.append(each);
            isFirst = false;
        }
        if (sb.toString().contains("[")) {
            sb.append(']');
        }
        return sb.toString();
    }

    public DbIndexDef addColumn(String colName) {
        if (Objects.isNull(this.columns)) {
            this.columns = new ArrayList<>();
        }
        if (this.columns.contains(columns)) {
            this.columns.add(colName);
        }
        return this;
    }

}
