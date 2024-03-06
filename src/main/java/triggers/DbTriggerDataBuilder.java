package triggers;


import utils.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DbTriggerDataBuilder {
    private static final String LOCAL_PATH = "C:\\proj\\java\\java-class-decorator\\src\\main\\java\\com\\aggregated\\db2_migration\\triggers\\local_triggers.csv";
    private static final String UAT_PATH = "C:\\proj\\java\\java-class-decorator\\src\\main\\java\\com\\aggregated\\db2_migration\\triggers\\uat_triggers.csv";

    private static final List<DbTrigger> local = new ArrayList<>();
    private static final List<DbTrigger> uat = new ArrayList<>();

    static {
        buildData(LOCAL_PATH, local);
        buildData(UAT_PATH, uat);
    }

    public static List<DbTrigger> getLocal() {
        return local;
    }

    public static List<DbTrigger> getUat() {
        return uat;
    }

    private static void buildData(String filePath, List<DbTrigger> current) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int idx = 0;
            while ((line = br.readLine()) != null) {
                if (idx == 0) {
                    idx = 1;
                    continue;
                }
                if (StringUtils.isEmpty(line)) {
                    continue;
                }
                current.add(DbTrigger.craftFromString(line));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
