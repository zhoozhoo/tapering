package ca.zhoozhoo.tapering;

import static java.lang.String.format;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.paukov.combinatorics3.Generator;

import org.paukov.combinatorics3.IGenerator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

public class Main {
	static final String[] HEADERS = { "Week", "Previous Dosage", "Target New Dosage", "Actual New Dosage", "Percentage",
			"100", "50",
			"25", "12.5", "5" };

	static int week = 1;
	static List<List<Float>> combinations = new ArrayList<>();
	static final IGenerator<List<Float>> PERMUTATIONS = Generator.permutation(100f, 50f, 25f, 20f, 12.5f, 10f, 5f)
			.simple();

	public static void main(String[] args) {
		var main = new Main();
		float current = 175;
		System.out.println(format("Starting with %.1f: ", current));

		while (current > 0) {
			current = main.next(current, 3);
			week++;
		}
  	}

	float next(float current, float percentage) {
		var dosages = findBestPermutation(current, percentage);
		if (dosages.getActualPercentage() > (percentage + 1)) {
			dosages = findBestPermutation(current, percentage - 1);
		}

		System.out.println(dosages);

		return dosages.getActualDosage();
	}

	Dosages findBestPermutation(float current, float percentage) {
		float next = current * (1 - percentage / 100);
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

			float actualPercentage = (1f - (actualDosage / current)) * 100f;
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

		return new Dosages(week, current, percentage, winnerDosage,
				winnerPercentage, winnerPermutaion.toArray(new Float[winnerPermutaion.size()]), winnerCounts);
	}

	void createCSVFile() throws IOException {
		FileWriter out = new FileWriter("dosages.csv");

		try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
				.withHeader(HEADERS))) {
			combinations.forEach(combination -> {
				try {
					printer.printRecord();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
	}

	@Data
	@AllArgsConstructor
	@RequiredArgsConstructor
	public class Dosages {

		int week;
		float currentDosage;
		float targetPercentage;
		float actualDosage;
		float actualPercentage;
		Dosage[] dosages;

		public Dosages(int week, float currentDosage, float targetPercentage, float actualDosage,
				float actualPercentage, Float[] pillSizes, int[] counts) {
			this.week = week;
			this.currentDosage = currentDosage;
			this.targetPercentage = targetPercentage;
			this.actualDosage = actualDosage;
			this.actualPercentage = actualPercentage;
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
			stringBuilder.append(format("Week %2d: %5.1f --> %5.1f | ", week, currentDosage,
					currentDosage * (1 - targetPercentage / 100)));
			actualDosage = 0;
			for (int i = 0; i < dosages.length; i++) {
				actualDosage += dosages[i].getPillSize() * dosages[i].getCount();
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
			stringBuilder.append(format(" = %5.1f (%4.2f%%)", actualDosage, actualPercentage));

			return stringBuilder.toString();
		}

		@Data
		@AllArgsConstructor
		@RequiredArgsConstructor
		public class Dosage {
			float pillSize;
			int count;
		}
	}
}