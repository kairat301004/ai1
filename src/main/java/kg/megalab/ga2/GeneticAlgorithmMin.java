package kg.megalab.ga2;

import java.util.*;

public class GeneticAlgorithmMin {
    private static final int POPULATION_SIZE = 50;
    private static final int CHROMOSOME_LENGTH = 20;
    private static final double CROSSOVER_RATE = 0.85;
    private static final double MUTATION_RATE = 0.1;
    private static final int MAX_GENERATIONS = 100;
    private static final double MIN_X = -5;
    private static final double MAX_X = 5;

    public static void main(String[] args) {
        List<String> population = initializePopulation();
        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
            population = evolve(population);
        }
        String bestChromosome = getBestChromosome(population);
        double bestX = decodeGray(bestChromosome);
        double bestY = objectiveFunction(bestX);
        System.out.println("Best solution: x = " + bestX + ", y = " + bestY);
    }

    private static List<String> initializePopulation() {
        List<String> population = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            StringBuilder chromosome = new StringBuilder();
            for (int j = 0; j < CHROMOSOME_LENGTH; j++) {
                chromosome.append(random.nextBoolean() ? "1" : "0");
            }
            population.add(chromosome.toString());
        }
        return population;
    }

    private static List<String> evolve(List<String> population) {
        List<String> newPopulation = new ArrayList<>();
        population.sort(Comparator.comparingDouble(c -> objectiveFunction(decodeGray(c))));
        for (int i = 0; i < POPULATION_SIZE / 2; i++) {
            String parent1 = population.get(i);
            String parent2 = population.get(i + 1);
            if (Math.random() < CROSSOVER_RATE) {
                String[] children = crossover(parent1, parent2);
                newPopulation.add(children[0]);
                newPopulation.add(children[1]);
            } else {
                newPopulation.add(parent1);
                newPopulation.add(parent2);
            }
        }
        for (int i = 0; i < newPopulation.size(); i++) {
            if (Math.random() < MUTATION_RATE) {
                newPopulation.set(i, mutate(newPopulation.get(i)));
            }
        }
        return newPopulation;
    }

    private static String[] crossover(String parent1, String parent2) {
        int point = new Random().nextInt(CHROMOSOME_LENGTH);
        String child1 = parent1.substring(0, point) + parent2.substring(point);
        String child2 = parent2.substring(0, point) + parent1.substring(point);
        return new String[]{child1, child2};
    }

    private static String mutate(String chromosome) {
        char[] chars = chromosome.toCharArray();
        int index = new Random().nextInt(CHROMOSOME_LENGTH);
        chars[index] = chars[index] == '0' ? '1' : '0';
        return new String(chars);
    }

    private static double objectiveFunction(double x) {
        return 0.8 * x + 1.4 * Math.cos(1.8 * x * x) * Math.exp(0.4 * x);
    }

    private static double decodeGray(String gray) {
        int binary = Integer.parseInt(gray, 2);
        int result = binary;
        for (int shift = 1; shift < CHROMOSOME_LENGTH; shift <<= 1) {
            result ^= (binary >> shift);
        }
        return MIN_X + (MAX_X - MIN_X) * result / (Math.pow(2, CHROMOSOME_LENGTH) - 1);
    }

    private static String getBestChromosome(List<String> population) {
        return Collections.min(population, Comparator.comparingDouble(c -> objectiveFunction(decodeGray(c))));
    }
}
