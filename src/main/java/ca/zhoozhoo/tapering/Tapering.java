package ca.zhoozhoo.tapering;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import org.paukov.combinatorics3.Generator;
import org.paukov.combinatorics3.IGenerator;

import lombok.AllArgsConstructor;
import lombok.Data;

public class Tapering {

    private static final IGenerator<List<Float>> PERMUTATIONS = Generator
            .permutation(100f, 50f, 25f, 20f, 12.5f, 10f, 5f)
            .simple();
    private static final float STARTING_DOSE = 175;
    private static final float MIN_PERCENTRAGE = 3;
    private static final float MAX_PERCENTAGE = 4;

    public static void main(String[] args) {

        int week = 1;
        float currentDosage = STARTING_DOSE;
        var tapering = new Tapering();

        System.out.println(format("Starting dose: %.1f", currentDosage));

        while (currentDosage > 0) {
            currentDosage = tapering.nextDosage(week, currentDosage, MIN_PERCENTRAGE, MAX_PERCENTAGE);
            week++;
        }
    }

    float nextDosage(int week, float currentDosage, float minPercentage, float maxPercentage) {
        return currentDosage;
    }

    float next(int week, float currentDosage, float minPercentage, float maxPercentage) {
        var dosages = findBestPermutation(week, currentDosage, minPercentage);
        if (dosages.getNewDosage() > maxPercentage) {
            dosages = findBestPermutation(week, currentDosage, minPercentage - 1);
        }

        System.out.println(dosages);

        return dosages.getNewDosage();
    }

    private Dosages findBestPermutation(int week, float currentDosage, float percentage) {
        float next = currentDosage * (1 - percentage / 100);
        float actualDosage = 0;
        float winnerPercentage = Float.MAX_VALUE;
        int[] winnerCounts = null;
        float winnerDosage = 0;
        List<Float> winnerPermutaion = null;

        // System.out.println(format("Current dosage: %.2f, target dosage: %.2f ",
        // current, next));
        for (var permutation : PERMUTATIONS) {
            actualDosage = 0;
            float n = next;
            int[] dosages = new int[permutation.size()];

            for (int i = 0; i < permutation.size(); i++) {
                dosages[i] = (int) (n / permutation.get(i));
                // System.out.print(format("%.1f / %.1f = %s |", n, permutation.get(i),
                // dosages[i]));
                actualDosage += ((float) dosages[i]) * permutation.get(i);
                n %= permutation.get(i);
            }

            float actualPercentage = (1f - (actualDosage / currentDosage)) * 100f;
            if (actualPercentage < winnerPercentage) {
                winnerPercentage = actualPercentage;
                winnerPermutaion = permutation;
                winnerCounts = dosages;
                winnerDosage = actualDosage;
            }
            // System.out.println(format("Permutation: %s, counts: %s, actula dosage: %.2f,
            // actual percentage: %.2f",
            // permutation, Arrays.toString(dosages), actualDosage, actualPercentage));
        }

        return new Dosages(week, currentDosage, winnerDosage,
                winnerPermutaion.toArray(new Float[winnerPermutaion.size()]),
                winnerCounts);
    }

    @Data
    @AllArgsConstructor
    public class Dosages {

        private int week;

        private float currentDosage;

        private float newDosage;

        private Dosage[] dosages;

        public Dosages(int week, float currentDosage, float newDosage, Float[] pillSizes,
                int[] counts) {
            this.week = week;
            this.currentDosage = currentDosage;
            this.newDosage = newDosage;
            List<Dosage> dosages = new ArrayList<>();
            int count5 = 0;
            for (int i = 0; i < pillSizes.length; i++) {
                if (pillSizes[i] == 5f) {
                    dosages.add(new Dosage(pillSizes[i], counts[i] + count5));
                } else if (pillSizes[i] == 10f) {
                    count5 += counts[i] * 2;
                } else if (pillSizes[i] == 20f) {
                    count5 += counts[i] * 4;
                } else {
                    dosages.add(new Dosage(pillSizes[i], counts[i]));
                }
            }
            dosages.sort((d1, d2) -> Float.compare(d2.getPillSize(), d1.getPillSize()));
            this.dosages = dosages.toArray(new Dosage[dosages.size()]);
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(format("Week %2d: %5.1f --> %5.1f (%4.2f%%) | ", week, currentDosage, newDosage,
                    currentDosage * (1 - newDosage / 100)));
            for (int i = 0; i < dosages.length; i++) {
                if (i > 0) {
                    stringBuilder.append(" + ");
                }
                if (dosages[i].getCount() == 0) {
                    stringBuilder.append("      ");
                } else {
                    if (dosages[i].getPillSize() == (long) dosages[i].getPillSize()) {
                        stringBuilder.append(format("%4.0fx%d", dosages[i].getPillSize(), dosages[i].getCount()));
                    } else {
                        stringBuilder.append(format("%4.1fx%d", dosages[i].getPillSize(), dosages[i].getCount()));
                    }
                }
            }

            return stringBuilder.toString();
        }

        @Data
        @AllArgsConstructor
        public class Dosage {

            private float pillSize;

            private int count;
        }
    }
}
