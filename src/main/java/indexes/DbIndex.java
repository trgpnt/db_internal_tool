package indexes;

import org.apache.commons.collections4.CollectionUtils;
import utils.StringUtils;

import java.util.List;
import java.util.Objects;

import static indexes.DbIndexDataBuilder.CREATE_KEYWORD;

public class DbIndex {
    private String tableName;
    private String indexName;
    private DbIndexDef dbIndexDef;

    private DbIndex(String tableName, String indexName, DbIndexDef dbIndexDef) {
        this.tableName = tableName;
        this.indexName = indexName;
        this.dbIndexDef = dbIndexDef;
    }


    public static DbIndex craftFromRaws(List<String> strings) {
        if (CollectionUtils.isEmpty(strings)) {
            return null;
        }
        final String tableName = StringUtils.cleanseDefaultCharValue(strings.get(1));
        final String indexName = StringUtils.cleanseDefaultCharValue(strings.get(2));
        final String indexDef = StringUtils.cleanseDefaultCharValue(strings.get(strings.size() - 1));
        if (indexName.contains(CREATE_KEYWORD) || !indexDef.contains(CREATE_KEYWORD)) {
            throw new RuntimeException("double check at : " + strings);
        }
        return new DbIndex(tableName, indexName, DbIndexDef.craftFromStrings(indexDef));
    }


    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public DbIndexDef getIndexDef() {
        return dbIndexDef;
    }

    public void setIndexDef(DbIndexDef dbIndexDef) {
        this.dbIndexDef = dbIndexDef;
    }


    @Override
    public String toString() {
        return "\n  " + indexName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DbIndex dbIndex = (DbIndex) o;
        return Objects.equals(tableName, dbIndex.tableName) && Objects.equals(dbIndexDef, dbIndex.dbIndexDef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableName, dbIndexDef);
    }

    static enum INDEX_TYPE {
        BTREE,
        UNKNOWN,
        HASH,
        BRIN,
        GIN,
        GiST,
        SP_GiST
    }
}
