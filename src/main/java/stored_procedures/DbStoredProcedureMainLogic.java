package stored_procedures;

import enum_.LOG_DIRECTION;
import org.apache.commons.collections4.CollectionUtils;
import utils.FileUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DbStoredProcedureMainLogic {
    private static final DbStoredProcedureMainLogic INSTANCE = new DbStoredProcedureMainLogic();
    private static Map<String, LinkedList<DbStoredProcedure>> onlyLocalHas = new HashMap<>();
    private static Map<String, LinkedList<DbStoredProcedure>> onlyUatHas = new HashMap<>();
    private LOG_DIRECTION logDirection;
    private String outFileName;

    private List<DbStoredProcedure> A = new ArrayList<>();
    private List<DbStoredProcedure> B = new ArrayList<>();

    public static DbStoredProcedureMainLogic go() {
        return INSTANCE;
    }

    public DbStoredProcedureMainLogic reset() {
        A = new ArrayList<>();
        B = new ArrayList<>();
        onlyLocalHas = new HashMap<>();
        onlyUatHas = new HashMap<>();
        logDirection = null;
        outFileName = "";
        return this;
    }

    public DbStoredProcedureMainLogic withThis(List<DbStoredProcedure> A) {
        this.A = A;
        return this;
    }

    public DbStoredProcedureMainLogic withThat(List<DbStoredProcedure> B) {
        this.B = B;
        return this;
    }

    public DbStoredProcedureMainLogic toFile(String fileName) {
        this.outFileName = fileName;
        return this;
    }

    public DbStoredProcedureMainLogic setDirection(LOG_DIRECTION logDirection) {
        this.logDirection = logDirection;
        return this;
    }

    public void processComplement() {
        if (CollectionUtils.isEmpty(A) || CollectionUtils.isEmpty(B)) {
            throw new RuntimeException();
        }
        Map<String, LinkedList<DbStoredProcedure>> currMap = new ConcurrentHashMap<>(onlyUatHas);
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

        Set<DbStoredProcedure> bKeys = Collections.synchronizedSet(new HashSet<>(B));

        Map<String, LinkedList<DbStoredProcedure>> finalCurrMap = currMap;
        A.parallelStream().forEach(each -> {
            if (!bKeys.contains(each)) {
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
        FileUtil.writeToFile(outFileName, sb.toString());
    }
}
