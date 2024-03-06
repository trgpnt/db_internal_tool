package utils;

public class DummyUtil {
    public static void main(String[] args) {
        String inp1 = "\"wc_update_trig_insert_to_wfctc\",FUNCTION\"\n" +
                "\"users_delete_trig_update_to_userarchive\",FUNCTION\"\n" +
                "\"users_insert_trig_insert_to_userarchive\",FUNCTION\"\n" +
                "\"users_update_name_first_trig_update_to_userarchive\",FUNCTION\"\n" +
                "\"users_update_name_last_trig_update_to_userarchive\",FUNCTION\"\n" +
                "\"wc_insert_trig_insert_to_wfctc\",FUNCTION\"\n" +
                "\"pd_insert_trig_insert_to_pda\",FUNCTION\"\n" +
                "\"pd_update_trig_insert_to_pda\",FUNCTION\"\n" +
                "\"pdis_insert_trig_insert_to_pda\",FUNCTION\"\n" +
                "\"rs_insert_trig_valid_chk\",FUNCTION\"\n" +
                "\"rs_update_dt_eff_beg_trig_valid_chk\",FUNCTION\"\n" +
                "\"rs_update_dt_eff_end_trig_valid_chk\",FUNCTION\"\n" +
                "\"ul_delete_trig_insert_to_ulh\",FUNCTION\"\n" +
                "\"ul_update_trig_insert_to_ulh\",FUNCTION\"";
        String inp2 = "\"rs_update_dt_eff_end_trig_valid_chk\",\"FUNCTION\"\n" +
                "\"ul_delete_trig_insert_to_ulh\",\"FUNCTION\"\n" +
                "\"ul_update_trig_insert_to_ulh\",\"FUNCTION\"\n" +
                "\"users_delete_trig_update_to_userarchive\",\"FUNCTION\"\n" +
                "\"users_insert_trig_insert_to_userarchive\",\"FUNCTION\"\n" +
                "\"users_update_name_first_trig_update_to_userarchive\",\"FUNCTION\"\n" +
                "\"users_update_name_last_trig_update_to_userarchive\",\"FUNCTION\"\n" +
                "\"wc_insert_trig_insert_to_wfctc\",\"FUNCTION\"\n" +
                "\"wc_update_trig_insert_to_wfctc\",\"FUNCTION\"\n" +
                "\"pdis_insert_trig_insert_to_pda\",\"FUNCTION\"\n" +
                "\"pd_insert_trig_insert_to_pda\",\"FUNCTION\"\n" +
                "\"pd_update_trig_insert_to_pda\",\"FUNCTION\"\n" +
                "\"rs_insert_trig_valid_chk\",\"FUNCTION\"\n" +
                "\"rs_update_dt_eff_beg_trig_valid_chk\",\"FUNCTION\"";
        System.out.println(StringUtils.isAnagram(inp1, inp2));
    }
}
