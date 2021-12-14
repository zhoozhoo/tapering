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
	static final String[] HEADERS = { "Week", "Current Dosage", "New Dosage", "Percentage",
			"100", "50",
			"25", "12.5", "5" };

	static List<List<Float>> combinations = new ArrayList<>();
	static final IGenerator<List<Float>> PERMUTATIONS = Generator.permutation(100f, 50f, 25f, 20f, 12.5f, 10f, 5f)
			.simple();

	private static final float STARTING_DOSE = 175;

	private static final float MIN_PERCENTRAGE = 3;

	private static final float MAX_PERCENTAGE = 4;

	public static void main(String[] args) {
		var main = new Main();
		int week = 1;
		float currentDosage = STARTING_DOSE;

		while (currentDosage > 0) {
			currentDosage = main.nextDosage(week, currentDosage, MIN_PERCENTRAGE);
			week++;
		}
	}

	private float nextDosage(int week, float current, float percentage) {
		var dosages = findBestPermutation(week, current, percentage);
		if (dosages.getPercentage() > (percentage + 1)) {
			dosages = findBestPermutation(week, current, percentage - 1);
		}

		System.out.println(dosages);

		return dosages.getNextDosage();
	}

	private PillCounts findBestPermutation(int week, float currentDosage, float percentage) {
		float lowestPercentage = 101;
		int[] lowestCounts = null;
		float bestDosage = 0;
		List<Float> bestPermutaion = null;

		// System.out.println(format("Current dosage: %.2f, target dosage: %.2f ",
		// current, next));
		for (var permutation : PERMUTATIONS) {
			float actualDosage = 0;
			float targetDosage = currentDosage * (1 - percentage / 100);
			int[] counts = new int[permutation.size()];

			for (int i = 0; i < permutation.size(); i++) {
				counts[i] = (int) (targetDosage / permutation.get(i));
				// System.out.print(format("%.1f / %.1f = %s |", n, permutation.get(i),
				// dosages[i]));
				actualDosage += ((float) counts[i]) * permutation.get(i);
				targetDosage %= permutation.get(i);
			}

			if ((1f - (actualDosage / currentDosage)) * 100f < lowestPercentage) {
				lowestPercentage = (1f - (actualDosage / currentDosage)) * 100f;
				bestPermutaion = permutation;
				lowestCounts = counts;
				bestDosage = actualDosage;
			}
			// System.out.println(format("Permutation: %s, counts: %s, actula dosage: %.2f,
			// actual percentage: %.2f",
			// permutation, Arrays.toString(dosages), actualDosage, actualPercentage));
		}

		return new PillCounts(week, currentDosage, bestDosage, lowestPercentage,
				bestPermutaion.toArray(new Float[bestPermutaion.size()]), lowestCounts);
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
	public class PillCounts {

		int week;

		float currentDosage;

		float nextDosage;

		float percentage;

		PillCount[] dosages;

		public PillCounts(int week, float currentDosage, float newDosage, float percentage, Float[] pillSizes,
				int[] counts) {
			this.week = week;
			this.currentDosage = currentDosage;
			this.nextDosage = newDosage;
			this.percentage = percentage;
			List<PillCount> dosages = new ArrayList<>();
			int count5 = 0;

			for (int i = 0; i < pillSizes.length; i++) {
				if (pillSizes[i] == 5f) {
					dosages.add(new PillCount(pillSizes[i], counts[i] + count5));
				} else if (pillSizes[i] == 10f) {
					count5 += counts[i] * 2;
				} else if (pillSizes[i] == 20f) {
					count5 += counts[i] * 4;
				} else {
					dosages.add(new PillCount(pillSizes[i], counts[i]));
				}
			}

			dosages.sort((d1, d2) -> Float.compare(d2.getPillSize(), d1.getPillSize()));
			this.dosages = dosages.toArray(new PillCount[dosages.size()]);

			if (this.dosages[4].pillSize * this.dosages[4].count > 50) {
				this.dosages[1].count += (int) ((this.dosages[4].pillSize * this.dosages[4].count) / 50);
				this.dosages[4].count = (int) (((this.dosages[4].pillSize * this.dosages[4].count) % 50)
						/ (this.dosages[4].pillSize));
			}

			if (this.dosages[4].pillSize * this.dosages[4].count > 25) {
				this.dosages[2].count += (int) ((this.dosages[4].pillSize * this.dosages[4].count) / 25);
				this.dosages[4].count = (int) (((this.dosages[4].pillSize * this.dosages[4].count) % 25)
						/ (this.dosages[4].pillSize));
			}

		}

		@Override
		public String toString() {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(format("Week %2d: %5.1f --> %5.1f (%6.2f%%) | ", week, currentDosage, nextDosage,
					percentage));
			nextDosage = 0;
			for (int i = 0; i < dosages.length; i++) {
				nextDosage += dosages[i].getPillSize() * dosages[i].getCount();
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
		@RequiredArgsConstructor
		public class PillCount {

			float pillSize;

			int count;
		}
	}
}