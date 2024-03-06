package utils;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CSVUtil {

    private static final char DEFAULT = '\u0000';

    public static List<String> csvToList(String inp, String splitter) {
        if (StringUtils.isEmpty(inp)) {
            return Collections.emptyList();
        }
        int idx = inp.indexOf(splitter);
        if (idx == -1) {
            throw new RuntimeException("idx is -1 at " + inp);
        }
        String priorPart = StringUtils.stripDoubleEndedNonAlphaNumeric(inp.substring(0, idx));
        List<String> res = new ArrayList<>(Arrays.asList(priorPart.replace('\"', DEFAULT).split(",")));
        if (CollectionUtils.isEmpty(res)) {
            throw new RuntimeException("list is empty/null at " + inp);
        }
        String lastPart = StringUtils.cleanseDefaultCharValue(inp.substring(idx));
        if (StringUtils.isEmpty(lastPart)) {
            throw new RuntimeException("bad string value at " + inp);
        }
        res.add(lastPart.replace('\"', DEFAULT));
        return res;
    }

}
