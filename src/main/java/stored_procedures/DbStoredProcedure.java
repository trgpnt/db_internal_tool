package stored_procedures;

import java.util.Objects;

public class DbStoredProcedure {
    private static final String SEP = "SEP";
    private String routineName;
    private String routineType;

    private DbStoredProcedure(String routineName, String routineType) {
        this.routineName = routineName;
        this.routineType = routineType;
    }

    public static DbStoredProcedure craftFromString(String inp) {
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
        return new DbStoredProcedure(splitted[0], splitted[1]);
    }

    public static void main(String[] args) {
        System.out.println(craftFromString("\"rs_update_dt_eff_end_trig_valid_chk\",\"FUNCTION\""));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DbStoredProcedure that = (DbStoredProcedure) o;
        return Objects.equals(routineName, that.routineName) && Objects.equals(routineType, that.routineType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routineName, routineType);
    }

    @Override
    public String toString() {
        return "\nDbStoredProcedure : " +
                "\n\t- routineName= '" + routineName + '\'' +
                "\n\t- routineType= '" + routineType + '\'';
    }

}
