package triggers;

import java.util.Objects;

public class DbTrigger {
    private static final String SEP = "SEP";
    private String triggerName;
    private String eventObjTable;
    private String eventManipulation;

    private DbTrigger(String triggerName, String eventObjTable, String eventManipulation) {
        this.triggerName = triggerName;
        this.eventObjTable = eventObjTable;
        this.eventManipulation = eventManipulation;
    }

    public static DbTrigger craftFromString(String inp) {
        int stackSimulated = 0;
        StringBuilder runner = new StringBuilder();
        StringBuilder output = new StringBuilder();
        for (Character each : inp.toCharArray()) {
            if ('\"' == each) {
                if (stackSimulated > 0) {
                    if (output.length() > 0) {
                        output.append(SEP);
                    }
                    output.append(runner);
                    runner.setLength(0);
                    stackSimulated--;
                } else {
                    stackSimulated++;
                }
            } else {
                if (',' != each) {
                    runner.append(each);
                }
            }
        }
        if (runner.length() > 0) {
            if (output.length() > 0) {
                output.append(SEP);
            }
            output.append(runner);
        }
        String[] splitted = output.toString().split(SEP);
        try {
            return new DbTrigger(splitted[0], splitted[1], splitted[2]);
        } catch (RuntimeException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DbTrigger dbTrigger = (DbTrigger) o;
        return Objects.equals(triggerName, dbTrigger.triggerName) && Objects.equals(eventObjTable, dbTrigger.eventObjTable) && Objects.equals(eventManipulation, dbTrigger.eventManipulation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(triggerName, eventObjTable, eventManipulation);
    }

    @Override
    public String toString() {
        return "\nDbTrigger :" +
                "\n\t- triggerName = '" + triggerName + '\'' +
                "\n\t- eventObjTable = '" + eventObjTable + '\'' +
                "\n\t- eventManipulation = '" + eventManipulation + '\'';
    }
}
