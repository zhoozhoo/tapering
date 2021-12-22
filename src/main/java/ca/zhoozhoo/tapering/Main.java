package ca.zhoozhoo.tapering;

import static java.lang.String.format;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat.Builder;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.paukov.combinatorics3.Generator;
import org.paukov.combinatorics3.IGenerator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Main {

	private static final String[] HEADERS = { "Week", "Current Dosage", "Next Dosage", "Percentage",
			"100", "50",
			"25", "12.5", "5" };

	private static final IGenerator<List<Float>> PERMUTATIONS = Generator
			.permutation(100f, 50f, 25f, 20f, 12.5f, 10f, 5f)
			.simple();

	private static final float STARTING_DOSE = 175;

	private static final float MIN_PERCENTRAGE = 3;

	private static final float MAX_PERCENTAGE = 4;

	public static void main(String[] args) {
		ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
		Configurator.reconfigure(builder.setStatusLevel(Level.INFO)
				.setConfigurationName("DefaultLogger")
				.add(builder.newAppender("Console", "CONSOLE")
						.addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT)
						.add(builder.newLayout("PatternLayout")
								.addAttribute("pattern", "%d %p %c [%t] %m%n")))
				.add(builder.newRootLogger(Level.INFO).add(builder.newAppenderRef("Console"))).build());

		var main = new Main();
		int week = 1;
		var currentDosage = STARTING_DOSE;
		var pillCountsWeeks = new ArrayList<PillCounts>();

		while (currentDosage > 0) {
			var pillCounts = main.nextDosage(week++, currentDosage, MIN_PERCENTRAGE, MAX_PERCENTAGE);
			currentDosage = pillCounts.getNextDosage();
			pillCountsWeeks.add(pillCounts);
		}

		try {
			main.createCSVFile(pillCountsWeeks);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private PillCounts nextDosage(int week, float currentDosage, float minPercentage, float maxPercentage) {
		var pillCounts = findPillCounts(week, currentDosage, minPercentage);
		if (pillCounts.getPercentage() > maxPercentage) {
			pillCounts = findPillCounts(week, currentDosage, minPercentage - 1);
		}

		if (pillCounts.getNextDosage() > 0) {
			System.out.println(pillCounts);
		}

		return pillCounts;
	}

	private PillCounts findPillCounts(int week, float currentDosage, float percentage) {
		float lowestPercentage = 101;
		int[] lowestCounts = null;
		float bestDosage = 0;
		List<Float> bestPermutaion = null;

		log.debug(format("Current dosage: %.2f", currentDosage));
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

	public void createCSVFile(List<PillCounts> weeks) throws IOException {
		FileWriter out = new FileWriter("dosages.csv");

		try (CSVPrinter printer = new CSVPrinter(out, Builder.create()
				.setDelimiter(',')
				.setQuote('"')
				.setRecordSeparator("\r\n")
				.setIgnoreEmptyLines(true)
				.setAllowDuplicateHeaderNames(true)
				.setHeader(HEADERS)
				.build())) {
			weeks.forEach(pillCounts -> {
				try {
					printer.printRecord(pillCounts.getWeek(), pillCounts.getCurrentDosage(), pillCounts.getNextDosage(),
							format("%.2f", pillCounts.getPercentage()),
							pillCounts.getPillCounts()[0].getCount() > 0 ? pillCounts.getPillCounts()[0].getCount()
									: "",
							pillCounts.getPillCounts()[1].getCount() > 0 ? pillCounts.getPillCounts()[1].getCount()
									: "",
							pillCounts.getPillCounts()[2].getCount() > 0 ? pillCounts.getPillCounts()[2].getCount()
									: "",
							pillCounts.getPillCounts()[3].getCount() > 0 ? pillCounts.getPillCounts()[3].getCount()
									: "",
							pillCounts.getPillCounts()[4].getCount() > 0 ? pillCounts.getPillCounts()[4].getCount()
									: "");
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

		PillCount[] pillCounts;

		public PillCounts(int week, float currentDosage, float newDosage, float percentage, Float[] pillSizes,
				int[] counts) {
			this.week = week;
			this.currentDosage = currentDosage;
			this.nextDosage = newDosage;
			this.percentage = percentage;
			List<PillCount> pillCountsList = new ArrayList<>();
			int count5 = 0;

			for (int i = 0; i < pillSizes.length; i++) {
				if (pillSizes[i] == 5f) {
					pillCountsList.add(new PillCount(pillSizes[i], counts[i] + count5));
				} else if (pillSizes[i] == 10f) {
					count5 += counts[i] * 2;
				} else if (pillSizes[i] == 20f) {
					count5 += counts[i] * 4;
				} else {
					pillCountsList.add(new PillCount(pillSizes[i], counts[i]));
				}
			}

			pillCountsList.sort((d1, d2) -> Float.compare(d2.getPillSize(), d1.getPillSize()));
			pillCounts = pillCountsList.toArray(new PillCount[pillCountsList.size()]);

			for (int i = pillCounts.length - 1; i > 0; i--) {
				for (int j = 0; j < i; j++) {
					var dose = pillCounts[i].pillSize * pillCounts[i].count;
					if ((dose > pillCounts[j].pillSize) && (pillCounts[j].pillSize % pillCounts[i].pillSize == 0)) {
						pillCounts[j].count += (int) (dose / pillCounts[j].pillSize);
						pillCounts[i].count = (int) ((dose % pillCounts[j].pillSize) / (pillCounts[i].pillSize));
					}
				}
			}
		}

		@Override
		public String toString() {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(format("Week %2d | %5.1f --> %5.1f (%6.2f%%) | ", week, currentDosage, nextDosage,
					percentage));
			nextDosage = 0;
			for (int i = 0; i < pillCounts.length; i++) {
				nextDosage += pillCounts[i].getPillSize() * pillCounts[i].getCount();
				if (i > 0) {
					stringBuilder.append(" + ");
				}
				if (pillCounts[i].getCount() == 0) {
					stringBuilder.append("      ");
				} else {
					if (pillCounts[i].getPillSize() == (long) pillCounts[i].getPillSize()) {
						stringBuilder.append(format("%4.0fx%d", pillCounts[i].getPillSize(), pillCounts[i].getCount()));
					} else {
						stringBuilder.append(format("%4.1fx%d", pillCounts[i].getPillSize(), pillCounts[i].getCount()));
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