package ca.zhoozhoo.tapering;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.lang.String.format;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.paukov.combinatorics3.Generator;

public class App {

	static final String[] HEADERS = { "Week", "100", "50", "25", "12.5", "5" };
	static final List<Float> DOSAGES = List.of(100f, 50f, 25f, 12.5f, 5f);
	static List<List<Float>> combinations = new ArrayList<>();

	Map<String, String> AUTHOR_BOOK_MAP = new HashMap<>() {
		{
			put("Dan Simmons", "Hyperion");
			put("Douglas Adams", "The Hitchhiker's Guide to the Galaxy");
		}
	};

	public static void main(String[] args) {
		Generator.permutation("100", "50", "25", "12.5", "5")
		.simple()
		.stream()
		.forEach(System.out::println);
		
		// try {
		// 	var app = new App();

		// 	app.createCombinations(DOSAGES, new ArrayList<Float>());

		// 	app.createCSVFile(null);
		// } catch (IOException e) {
		// 	e.printStackTrace();
		// }
	}

	public float next(float current, float percentage) {
		float next = current * (1 - percentage / 100);
		float actualDosage = 0;
		float winnerPercentage = Float.MAX_VALUE;
		int[] winnerDosages = new int[DOSAGES.size()];
		List<Float> winnerCombination = null;

		for (var combination : combinations) {
			actualDosage = 0;
			float n = next;
			int[] dosages = new int[DOSAGES.size()];

			for (int i = 0; i < combination.size(); i++) {
				int count = (int) (n / combination.get(i));
				actualDosage += count * combination.get(i);
				dosages[i] = count;
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
				int[] dosages = new int[DOSAGES.size()];

				for (int i = 0; i < combination.size(); i++) {
					int count = (int) (n / combination.get(i));
					actualDosage += count * combination.get(i);
					dosages[i] = count;
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

		// System.out.print(format("Week %d: ", week));
		for (int i = 0; i < winnerCombination.size(); i++) {
			if (i > 0) {
				System.out.print(" + ");
			}
			if (winnerCombination.get(i) == winnerCombination.get(i).longValue()) {
				System.out.print(format("%.0fx%d", winnerCombination.get(i), winnerDosages[i]));
			} else {
				System.out.print(format("%.1fx%d", winnerCombination.get(i), winnerDosages[i]));
			}
		}
		System.out.println(format(" = %.1f (%.2f%%)", actualDosage, winnerPercentage));

		return actualDosage;
	}

	public void createCSVFile(List<Dosage> dosages) throws IOException {
		FileWriter out = new FileWriter("dosages.csv");

		try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
				.withHeader(HEADERS))) {
			AUTHOR_BOOK_MAP.forEach((author, title) -> {
				try {
					printer.printRecord(author, title);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
	}

	public void createCombinations(List<Float> str, List<Float> ans) {
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

	public class Dosage {

		float dosage;

		int count;
	}
}
