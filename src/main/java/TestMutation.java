import java.util.Arrays;
import java.util.List;

import static utils.MutationUtils.mutate;

public class TestMutation {

    public static void main(String[] args) {
        List<String> input = Arrays.asList("1", "2", "3");
        String[][] rs = mutate(input, String.class);
        for (String[] arr : rs) {
            for (String s : arr) {
                System.out.print(s + " ");
            }
            System.out.println();
        }
    }
}
