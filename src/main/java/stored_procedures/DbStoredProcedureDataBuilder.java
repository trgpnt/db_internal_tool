package stored_procedures;


import utils.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DbStoredProcedureDataBuilder {
    private static final String LOCAL_PATH = "C:\\proj\\java\\java-class-decorator\\src\\main\\java\\com\\aggregated\\db2_migration\\stored_procedures\\local_sps.csv";
    private static final String UAT_PATH = "C:\\\\proj\\\\java\\\\java-class-decorator\\\\src\\\\main\\\\java\\\\com\\\\aggregated\\\\db2_migration\\\\stored_procedures\\uat_sps.csv";
    private static final List<DbStoredProcedure> local = new ArrayList<>();
    private static final List<DbStoredProcedure> uat = new ArrayList<>();

    static {
        buildData(LOCAL_PATH, local);
        buildData(UAT_PATH, uat);
    }

    public static List<DbStoredProcedure> getLocal() {
        return local;
    }

    public static List<DbStoredProcedure> getUat() {
        return uat;
    }

    private static void buildData(String filePath, List<DbStoredProcedure> current) {
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
                current.add(DbStoredProcedure.craftFromString(line));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
