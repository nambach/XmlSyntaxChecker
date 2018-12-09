package utils;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;

public class MutationUtils {

    public static <T> T[][] mutate(List<T> input, Class<T> clazz) {

        int expectedRs = calculatePermutation(input.size());
        T[][] matrix = create2DMatrix(clazz, expectedRs, input.size());

        scatter(matrix, input, 0, expectedRs - 1, 0);
        return matrix;
    }

    private static <T> List<T> cloneList(List<T> list) {
        List<T> newList = new LinkedList<>();
        newList.addAll(list);
        return newList;
    }

    private static int calculatePermutation(int n) {
        int rs = 1;
        for (int i = 1; i <= n; i++) {
            rs *= i;
        }
        return rs;
    }

    private static <T> void scatter(T[][] matrix, List<T> candidates, int from, int to, int pos) {
        int breakPoint = (to - from + 1) / candidates.size();
        int candidateCounter = 0;

        for (int i = from; i <= to; i++) {
            if (i != from && i % breakPoint == 0) {
                candidateCounter++;
            }
            T[] arr = matrix[i];
            arr[pos] = candidates.get(candidateCounter);
        }

        if (candidates.size() == 1) return;

        int rangeFrom = from, rangeTo = rangeFrom + breakPoint - 1;
        for (int i = 0; i < candidates.size(); i++) {
            List<T> newCandidates = cloneList(candidates);
            newCandidates.remove(candidates.get(i));

            scatter(matrix, newCandidates, rangeFrom, rangeTo, pos + 1);

            rangeFrom = rangeTo + 1;
            rangeTo = rangeFrom + breakPoint - 1;
        }
    }

    private static <T> T[][] create2DMatrix(Class<T> clazz, int depth, int width) {
        T[] sampleArr = (T[]) Array.newInstance(clazz, width); //get sample class of 1D array
        T[][] matrix = (T[][]) Array.newInstance(sampleArr.getClass(), depth); //init 2D matrix

        //for each depth-dimension of matrix, init an 1D array
        for (int i = 0; i < depth; i++) {
            T[] arr = (T[]) Array.newInstance(clazz, width);
            matrix[i] = arr;
        }
        return matrix;
    }
}
