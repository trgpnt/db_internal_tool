package indexes;

import enum_.LOG_DIRECTION;

public class DbIndexProcessor {
    public static void main(String[] args) {
        processLocalToUAT();
        processUATToLocal();
        selfProcessLocal();
        selfProcessUAT();
        processGrouping();
    }

    private static void processGrouping() {
        DbIndexMainLogic processGrouping = DbIndexMainLogic.go().withThis(DbIndexDataBuilder.getLocalData())
                .withThat(DbIndexDataBuilder.getUatData())
                .setDirection(LOG_DIRECTION.LOCAL_TO_UAT);

        processGrouping.processGroupingForLocalAndUAT();
    }

    private static void processLocalToUAT() {
        DbIndexMainLogic processComplement = DbIndexMainLogic.go().reset()
                .withThis(DbIndexDataBuilder.getLocalData())
                .withThat(DbIndexDataBuilder.getUatData())
                .setDirection(LOG_DIRECTION.LOCAL_TO_UAT)
                .toFile("only_local_has.txt");

        processComplement.processComplement();
    }

    private static void processUATToLocal() {
        DbIndexMainLogic processComplement = DbIndexMainLogic.go().reset()
                .withThis(DbIndexDataBuilder.getUatData())
                .withThat(DbIndexDataBuilder.getLocalData())
                .setDirection(LOG_DIRECTION.UAT_TO_LOCAL)
                .toFile("only_uat_has.txt");
        processComplement.processComplement();
    }

    private static void selfProcessLocal() {
        DbIndexMainLogic reducer = DbIndexMainLogic.go().reset()
                .withSelfReferential(DbIndexDataBuilder.getLocalData())
                .toFile("self_local.txt");
        reducer.processReduce();
    }

    private static void selfProcessUAT() {
        DbIndexMainLogic reducer = DbIndexMainLogic.go().reset()
                .withSelfReferential(DbIndexDataBuilder.getUatData())
                .toFile("self_uat.txt");

        reducer.processReduce();
    }
}
