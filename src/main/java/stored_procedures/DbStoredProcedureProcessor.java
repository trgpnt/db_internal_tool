package stored_procedures;


import enum_.LOG_DIRECTION;

public class DbStoredProcedureProcessor {
    private static final DbStoredProcedureMainLogic logic = DbStoredProcedureMainLogic.go();

    public static void main(String[] args) {
        processLocalToUat();
        processUATToLocal();
    }

    private static void processLocalToUat() {
        logic.reset()
                .withThis(DbStoredProcedureDataBuilder.getLocal())
                .withThat(DbStoredProcedureDataBuilder.getUat())
                .setDirection(LOG_DIRECTION.LOCAL_TO_UAT)
                .toFile("sps_only_local_has")
                .processComplement();
    }

    private static void processUATToLocal() {
        logic.reset()
                .withThis(DbStoredProcedureDataBuilder.getUat())
                .withThat(DbStoredProcedureDataBuilder.getLocal())
                .setDirection(LOG_DIRECTION.UAT_TO_LOCAL)
                .toFile("sps_only_uat_has")
                .processComplement();
    }

}
