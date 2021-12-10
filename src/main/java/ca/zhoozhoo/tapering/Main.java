package ca.zhoozhoo.tapering;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.paukov.combinatorics3.Generator;
import org.paukov.combinatorics3.IGenerator;

public class Main {
	static int week = 1;
	static List<List<Float>> combinations = new ArrayList<>();
	static final IGenerator<List<Float>> PERMUTATIONS = Generator.permutation(100f, 50f, 25f, 12.5f, 5f).simple();

	public static void main(String[] args) {
		float current = 175;
		System.out.println(format("Starting with %.1f: ", current));

		do {
			current = next(current, 3);
			week++;
		} while (week < 2);
	}

	static float next(float current, float percentage) {
		float next = current * (1 - percentage / 100);
		float actualDosage = 0;
		float winnerPercentage = Float.MAX_VALUE;
		int[] winnerDosages = null;
		List<Float> winnerPermutaion = null;

		System.out.println(current);
		for (var permutation : PERMUTATIONS) {
			actualDosage = 0;
			float n = next;
			int[] dosages = new int[permutation.size()];

			for (int i = 0; i < permutation.size(); i++) {
				dosages[i] = (int) (n / permutation.get(i));
				// System.out.print(format("%.1f / %.1f = %s, ", n, permutation.get(i), dosages[i]));
				actualDosage += ((float) dosages[i]) * permutation.get(i);
				n %= permutation.get(i);
			}

			float actualPercentage = (1f - (actualDosage / current)) * 100f;
			if (actualPercentage < winnerPercentage) {
				winnerPercentage = actualPercentage;
				winnerPermutaion = permutation;
				winnerDosages = dosages;
			}
			// System.out.println();
			// System.out.println(
			// 		permutation + " " + Arrays.toString(winnerDosages) + " " + actualDosage + " " + actualPercentage);
		}

		if (winnerPercentage > (percentage + 1)) {
			next = current * (1 - (percentage - 1) / 100);
			actualDosage = 0;

			for (var permutation : PERMUTATIONS) {
				actualDosage = 0;
				float n = next;
				int[] dosages = new int[permutation.size()];

				for (int i = 0; i < permutation.size(); i++) {
					dosages[i] = (int) (n / permutation.get(i));
					// System.out.print(format("%.1f / %.1f = %s, ", n, permutation.get(i), dosages[i]));
					actualDosage += ((float) dosages[i]) * permutation.get(i);
					n %= permutation.get(i);
				}

				float actualPercentage = (1f - (actualDosage / current)) * 100f;
				if (actualPercentage < winnerPercentage) {
					winnerPercentage = actualPercentage;
					winnerPermutaion = permutation;
					winnerDosages = dosages;
				}
				// System.out.println();
				// System.out.println(permutation + " " + Arrays.toString(winnerDosages) + " " + actualDosage + " "
				// 		+ actualPercentage);
			}
		}

		System.out.print(format("Week %d: ", week));
		float winnerDosage = 0;
		for (int i = 0; i < winnerPermutaion.size(); i++) {
			winnerDosage += ((float) winnerDosages[i]) * winnerPermutaion.get(i);
			if (i > 0) {
				System.out.print(" + ");
			}
			if (winnerPermutaion.get(i) == winnerPermutaion.get(i).longValue()) {
				System.out.print(format("%.0fx%d", winnerPermutaion.get(i), winnerDosages[i]));
			} else {
				System.out.print(format("%.1fx%d", winnerPermutaion.get(i), winnerDosages[i]));
			}
		}
		System.out.println(format(" = %.1f (%.2f%%)", winnerDosage, winnerPercentage));

		return winnerDosage;
	}
}