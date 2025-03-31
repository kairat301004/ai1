package kg.megalab.ga2;
import java.util.*;
import java.util.stream.Collectors;

public class GeneticAlgorithmPSL {
    private static final int N = 35;                  // Длина последовательности
    private static final int POPULATION_SIZE = 70;   // Размер популяции
    private static final int TARGET_PSL = 4;          // Максимально допустимый PSL
    private static final int K = 3;                   // Сколько последовательностей найти
    private static final double CROSSOVER_RATE = 0.8; // Вероятность скрещивания
    private static final double MUTATION_RATE = 0.2;  // Вероятность мутации
    private static final int MAX_GENERATIONS = 1000;  // Макс. число поколений
    private static final int ELITISM_COUNT = 5;       // Число элитных особей

    public static void main(String[] args) {
        List<String> population = initializePopulation();
        Set<String> bestSequences = new LinkedHashSet<>(); // Для хранения уникальных решений

        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
            // 1. Оценка популяции
            Map<String, Integer> pslMap = new HashMap<>();
            for (String seq : population) {
                pslMap.put(seq, calculatePSL(seq));
            }

            // 2. Сохранение лучших последовательностей
            pslMap.entrySet().stream()
                    .filter(e -> e.getValue() <= TARGET_PSL)
                    .sorted(Map.Entry.comparingByValue())
                    .limit(K)
                    .forEach(e -> bestSequences.add(e.getKey()));

            if (bestSequences.size() >= K) break; // Достаточно решений

            // 3. Эволюция
            population = evolvePopulation(population, pslMap);

            // 4. Логирование
            int bestPSL = pslMap.values().stream().min(Integer::compare).orElse(-1);
            System.out.printf("Generation %d: Best PSL = %d, Found = %d/%d%n",
                    generation + 1, bestPSL, bestSequences.size(), K);
        }

        // Вывод результатов
        if (bestSequences.isEmpty()) {
            System.out.println("Не удалось найти последовательности с PSL ≤ " + TARGET_PSL);
        } else {
            System.out.println("\nНайденные последовательности с PSL ≤ " + TARGET_PSL + ":");
            bestSequences.forEach(seq -> System.out.println(seq + " (PSL = " + calculatePSL(seq) + ")"));
        }
    }

    // Инициализация случайной популяции
    private static List<String> initializePopulation() {
        Random random = new Random();
        return random.ints(POPULATION_SIZE, 0, 2)
                .mapToObj(i -> Integer.toBinaryString(i))
                .map(s -> String.format("%" + N + "s", s).replace(' ', '0'))
                .collect(Collectors.toList());
    }

    // Эволюция популяции
    private static List<String> evolvePopulation(List<String> population, Map<String, Integer> pslMap) {
        List<String> newPopulation = new ArrayList<>();

        // 1. Элитизм: сохраняем лучшие особи
        population.stream()
                .sorted(Comparator.comparingInt(pslMap::get))
                .limit(ELITISM_COUNT)
                .forEach(newPopulation::add);

        // 2. Селекция и кроссовер
        double totalFitness = population.stream()
                .mapToDouble(seq -> 1.0 / (1 + pslMap.get(seq)))
                .sum();

        Random random = new Random();
        while (newPopulation.size() < POPULATION_SIZE) {
            String parent1 = selectParent(population, pslMap, totalFitness);
            String parent2 = selectParent(population, pslMap, totalFitness);

            if (random.nextDouble() < CROSSOVER_RATE) {
                String[] children = crossover(parent1, parent2);
                newPopulation.add(mutate(children[0]));
                newPopulation.add(mutate(children[1]));
            } else {
                newPopulation.add(mutate(parent1));
                newPopulation.add(mutate(parent2));
            }
        }

        return newPopulation;
    }

    // Селекция рулеткой
    private static String selectParent(List<String> population, Map<String, Integer> pslMap, double totalFitness) {
        double randomValue = Math.random() * totalFitness;
        double cumulativeFitness = 0.0;

        for (String seq : population) {
            cumulativeFitness += 1.0 / (1 + pslMap.get(seq));
            if (cumulativeFitness >= randomValue) {
                return seq;
            }
        }
        return population.get(population.size() - 1); // fallback
    }

    // Одноточечный кроссовер
    private static String[] crossover(String parent1, String parent2) {
        int crossoverPoint = new Random().nextInt(N - 1) + 1;
        String child1 = parent1.substring(0, crossoverPoint) + parent2.substring(crossoverPoint);
        String child2 = parent2.substring(0, crossoverPoint) + parent1.substring(crossoverPoint);
        return new String[]{child1, child2};
    }

    // Точечная мутация
    private static String mutate(String sequence) {
        if (Math.random() >= MUTATION_RATE) return sequence;

        char[] chars = sequence.toCharArray();
        int mutationPoint = new Random().nextInt(N);
        chars[mutationPoint] = (chars[mutationPoint] == '0') ? '1' : '0';
        return new String(chars);
    }

    // Расчёт PSL (пиковый уровень боковых лепестков)
    private static int calculatePSL(String sequence) {
        int[] seq = new int[N];
        for (int i = 0; i < N; i++) {
            seq[i] = (sequence.charAt(i) == '1') ? 1 : -1;
        }

        int maxPSL = 0;
        for (int shift = 1; shift < N; shift++) {
            int correlation = 0;
            for (int i = 0; i < N - shift; i++) {
                correlation += seq[i] * seq[i + shift];
            }
            maxPSL = Math.max(maxPSL, Math.abs(correlation));
        }
        return maxPSL;
    }
}