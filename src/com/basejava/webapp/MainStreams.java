package com.basejava.webapp;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;

import java.util.Arrays;
import java.util.List;

public class MainStreams {
    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 3, 2, 3};
        System.out.println("minValue = " + minValue(arr));

        List<Integer> list = List.of(1, 2, 3, 3, 2, 3);
        System.out.println("OddOrEven = " + addOrEven(list));
    }

    public static int minValue(int[] nums) {
        return Arrays.stream(nums)
                .distinct()
                .sorted()
                .reduce(0, (acc, d) -> acc * 10 + d);
    }

    public static List<Integer> addOrEven(List<Integer> nums) {
        return nums.stream()
                .collect(collectingAndThen(
                        groupingBy(n -> n % 2),
                        map -> {
                            List<Integer> odds = map.getOrDefault(1, List.of());
                            int oddCount = odds.size();
                            int keyToKeep = (oddCount % 2 == 0) ? 1 : 0;
                            return map.getOrDefault(keyToKeep, List.of());
                        }
                ));
    }
}
