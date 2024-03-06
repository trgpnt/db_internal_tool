package indexes;

import utils.CSVUtil;
import utils.StringUtils;
import utils.TrieRepository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DbIndexDataBuilder {

    public static final String CREATE_KEYWORD = "CREATE";
    private static final String UAT_FILE_PATH = "C:\\Users\\NPham\\Downloads\\outputSchema.csv";
    private static final String LOCAL_FILE_PATH = "C:\\Users\\NPham\\Desktop\\local_indexes.csv";
    private static final TrieRepository repository = TrieRepository.go();
    private static final String UAT = "UAT";
    private static final String LOCAL = "LOCAL";
    private static final List<DbIndex> uat_converted = new ArrayList<>();
    private static final List<DbIndex> local_converted = new ArrayList<>();
    private static int unsyncedCounter = 0;

    static {
        buildIndexList(UAT_FILE_PATH, UAT);
        buildIndexList(LOCAL_FILE_PATH, LOCAL);
    }

    public static List<DbIndex> getUatData() {
        return uat_converted;
    }

    public static List<DbIndex> getLocalData() {
        return local_converted;
    }


    private static void buildIndexList(String filePath, String listType) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int idx = 0;
            while ((line = br.readLine()) != null) {
                if (StringUtils.isEmpty(line) || idx == 0) {
                    idx++;
                    continue;
                }
                DbIndex crafted = DbIndex.craftFromRaws(CSVUtil.csvToList(line, CREATE_KEYWORD));
                idx++;
                if (Objects.isNull(crafted) || crafted.getIndexDef().isUnique()) {
                    continue;
                }
                if (UAT.equalsIgnoreCase(listType)) {
                    uat_converted.add(crafted);
                } else if (LOCAL.equalsIgnoreCase(listType)) {
                    local_converted.add(crafted);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println();
    }


}
