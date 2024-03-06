package triggers;


import enum_.LOG_DIRECTION;

public class DbTriggerProcessor {
    private static final DbTriggerMainLogic logic = DbTriggerMainLogic.go();

    public static void main(String[] args) {
        processLocalToUat();
        processUATToLocal();
    }

    private static void processLocalToUat() {
        logic.reset()
                .withThis(DbTriggerDataBuilder.getLocal())
                .withThat(DbTriggerDataBuilder.getUat())
                .setDirection(LOG_DIRECTION.LOCAL_TO_UAT)
                .toFile("triggers_only_local_has")
                .processComplement();
    }

    private static void processUATToLocal() {
        logic.reset()
                .withThis(DbTriggerDataBuilder.getUat())
                .withThat(DbTriggerDataBuilder.getLocal())
                .setDirection(LOG_DIRECTION.UAT_TO_LOCAL)
                .toFile("triggers_only_uat_has")
                .processComplement();
    }

}
