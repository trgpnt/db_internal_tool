package triggers;


import enum_.LOG_DIRECTION;
import org.apache.commons.collections4.CollectionUtils;
import utils.FileUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DbTriggerMainLogic {
    private static final DbTriggerMainLogic INSTANCE = new DbTriggerMainLogic();
    private static Map<String, LinkedList<DbTrigger>> onlyLocalHas = new HashMap<>();
    private static Map<String, LinkedList<DbTrigger>> onlyUatHas = new HashMap<>();
    private LOG_DIRECTION logDirection;
    private String outFileName;

    private List<DbTrigger> A = new ArrayList<>();
    private List<DbTrigger> B = new ArrayList<>();

    public static DbTriggerMainLogic go() {
        return INSTANCE;
    }

    public DbTriggerMainLogic reset() {
        A = new ArrayList<>();
        B = new ArrayList<>();
        onlyLocalHas = new HashMap<>();
        onlyUatHas = new HashMap<>();
        logDirection = null;
        outFileName = "";
        return this;
    }

    public DbTriggerMainLogic withThis(List<DbTrigger> A) {
        this.A = A;
        return this;
    }

    public DbTriggerMainLogic withThat(List<DbTrigger> B) {
        this.B = B;
        return this;
    }

    public DbTriggerMainLogic toFile(String fileName) {
        this.outFileName = fileName;
        return this;
    }

    public DbTriggerMainLogic setDirection(LOG_DIRECTION logDirection) {
        this.logDirection = logDirection;
        return this;
    }

    public void processComplement() {
        if (CollectionUtils.isEmpty(A) || CollectionUtils.isEmpty(B)) {
            throw new RuntimeException();
        }
        Map<String, LinkedList<DbTrigger>> currMap = new ConcurrentHashMap<>(onlyUatHas);
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

        Set<DbTrigger> bKeys = Collections.synchronizedSet(new HashSet<>(B));

        Map<String, LinkedList<DbTrigger>> finalCurrMap = currMap;
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
