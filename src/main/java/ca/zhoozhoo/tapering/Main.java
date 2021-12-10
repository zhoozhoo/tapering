package ca.zhoozhoo.tapering;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

public class Main {
	static int week = 1;
	static final List<Float> DOSAGES = List.of(100f, 50f, 25f, 12.5f, 5f);
	static List<List<Float>> combinations = new ArrayList<>();;

	public static void main(String[] args) {
		float current = 175;
		System.out.println(format("Starting with %.1f: ", current));

		createCombinations(DOSAGES, new ArrayList<Float>());

		do {
			current = next(current, 3);
			week++;
		} while (week < 51);
	}

	static float next(float current, float percentage) {
		float next = current * (1 - percentage / 100);
		float actualDosage = 0;
		float winnerPercentage = Float.MAX_VALUE;
		int[] winnerDosages = new int[DOSAGES.size()];
		List<Float> winnerCombination = null;

		for (var combination : combinations) {
			actualDosage = 0;
			float n = next;
			int[] dosages = new int[combination.size()];

			for (int i = 0; i < combination.size(); i++) {
				dosages[i] = (int) (n / combination.get(i));
				actualDosage += ((float) dosages[i]) * combination.get(i);
				n %= combination.get(i);
			}

			float actualPercentage = (1f - (actualDosage / current)) * 100f;
			if (actualPercentage < winnerPercentage) {
				winnerPercentage = actualPercentage;
				winnerCombination = combination;
				winnerDosages = dosages;
			}
		}

		if (winnerPercentage > percentage) {
			next = current * (1 - (percentage - 1) / 100);
			actualDosage = 0;

			for (var combination : combinations) {
				actualDosage = 0;
				float n = next;
				int[] dosages = new int[combination.size()];

				for (int i = 0; i < combination.size(); i++) {
					dosages[i] = (int) (n / combination.get(i));
					actualDosage += ((float) dosages[i]) * combination.get(i);
					n %= combination.get(i);
				}

				float actualPercentage = (1f - (actualDosage / current)) * 100f;
				if (actualPercentage < winnerPercentage) {
					winnerPercentage = actualPercentage;
					winnerCombination = combination;
					winnerDosages = dosages;
				}
			}
		}

		System.out.print(format("Week %d: ", week));
		float winnerDosage = 0;
		for (int i = 0; i < winnerCombination.size(); i++) {
			winnerDosage += ((float) winnerDosages[i]) * winnerCombination.get(i);
			if (i > 0) {
				System.out.print(" + ");
			}
			if (winnerCombination.get(i) == winnerCombination.get(i).longValue()) {
				System.out.print(format("%.0fx%d", winnerCombination.get(i), winnerDosages[i]));
			} else {
				System.out.print(format("%.1fx%d", winnerCombination.get(i), winnerDosages[i]));
			}
		}
		System.out.println(format(" = %.1f (%.2f%%)", winnerDosage, winnerPercentage));

		return winnerDosage;
	}

	static void createCombinations(List<Float> str, List<Float> ans) {
		if (str.size() == 0) {
			combinations.add(ans);
			return;
		}

		for (int i = 0; i < str.size(); i++) {
			Float ch = str.get(i);
			var str1 = new ArrayList<Float>(str);
			str1.remove(i);

			var ans1 = new ArrayList<Float>(ans);
			ans1.add(ch);

			createCombinations(str1, ans1);
		}
	}

	public class Dosages {
		float dosage = 0;
		float percentage;
		int[] dosages;
		List<Float> combination;

	}
}