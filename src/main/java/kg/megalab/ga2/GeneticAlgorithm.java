package kg.megalab.ga2;

import java.util.*;

public class GeneticAlgorithm {

    // Константы для настройки генетического алгоритма
    private static final int POPULATION_SIZE = 50; // Размер популяции
    private static final int CHROMOSOME_LENGTH = 20; // Длина хромосомы (бинарная строка)
    private static final double CROSSOVER_RATE = 0.85; // Вероятность кроссовера
    private static final double MUTATION_RATE = 0.1; // Вероятность мутации
    private static final int MAX_GENERATIONS = 100; // Максимальное количество поколений
    private static final double MIN_X = -5; // Минимальное значение x для декодирования
    private static final double MAX_X = 5; // Максимальное значение x для декодирования

    public static void main(String[] args) {
        // Инициализация начальной популяции
        List<String> population = initializePopulation();

        // Эволюция популяции в течение MAX_GENERATIONS поколений
        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
            population = evolvePopulation(population);
        }

        // Нахождение лучшей хромосомы в популяции
        String bestChromosome = findBest(population);
        // Декодирование лучшей хромосомы в десятичное число
        double bestX = decodeGrayToDecimal(bestChromosome);
        // Вычисление значения целевой функции для лучшего x
        double bestY = objectiveFunction(bestX);
        System.out.println("Лучшее решение: x = " + bestX + ", y = " + bestY);

        // Второе задание: поиск бинарной последовательности с заданным PSL
        findBinarySequence();
    }

    // Инициализация начальной популяции случайными хромосомами
    private static List<String> initializePopulation() {
        List<String> population = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            StringBuilder chromosome = new StringBuilder();
            for (int j = 0; j < CHROMOSOME_LENGTH; j++) {
                chromosome.append(random.nextInt(2)); // Генерация случайного бита (0 или 1)
            }
            population.add(chromosome.toString());
        }
        return population;
    }

    // Эволюция популяции: отбор, кроссовер и мутация
    private static List<String> evolvePopulation(List<String> population) {
        List<String> newPopulation = new ArrayList<>();
        // Сортировка популяции по значению целевой функции (лучшие особи в начале списка)
        population.sort(Comparator.comparingDouble(c -> objectiveFunction(decodeGrayToDecimal(c))));

        for (int i = 0; i < POPULATION_SIZE / 2; i++) {
            String parent1 = population.get(i);
            String parent2 = population.get(i + 1);
            if (Math.random() < CROSSOVER_RATE) {
                // Кроссовер между двумя родителями
                String[] offspring = crossover(parent1, parent2);
                // Мутация потомков и добавление их в новую популяцию
                newPopulation.add(mutate(offspring[0]));
                newPopulation.add(mutate(offspring[1]));
            } else {
                // Если кроссовер не произошел, добавляем родителей в новую популяцию
                newPopulation.add(parent1);
                newPopulation.add(parent2);
            }
        }
        return newPopulation;
    }

    // Кроссовер: создание двух потомков из двух родителей
    private static String[] crossover(String parent1, String parent2) {
        int point = new Random().nextInt(CHROMOSOME_LENGTH); // Случайная точка раздела
        String offspring1 = parent1.substring(0, point) + parent2.substring(point);
        String offspring2 = parent2.substring(0, point) + parent1.substring(point);
        return new String[]{offspring1, offspring2};
    }

    // Мутация: случайное изменение битов в хромосоме
    private static String mutate(String chromosome) {
        StringBuilder mutated = new StringBuilder(chromosome);
        for (int i = 0; i < CHROMOSOME_LENGTH; i++) {
            if (Math.random() < MUTATION_RATE) {
                // Инвертирование бита с вероятностью MUTATION_RATE
                mutated.setCharAt(i, chromosome.charAt(i) == '0' ? '1' : '0');
            }
        }
        return mutated.toString();
    }

    // Нахождение лучшей хромосомы в популяции
    private static String findBest(List<String> population) {
        return Collections.min(population, Comparator.comparingDouble(c -> objectiveFunction(decodeGrayToDecimal(c))));
    }

    // Целевая функция, которую нужно оптимизировать
    private static double objectiveFunction(double x) {
        return 0.8 * x + 1.4 * Math.cos(1.8 * x * x) * Math.exp(0.4 * x);
    }

    // Декодирование Gray-кода в десятичное число
    private static double decodeGrayToDecimal(String grayCode) {
        int binary = Integer.parseInt(grayToBinary(grayCode), 2); // Преобразование Gray-кода в бинарный
        return MIN_X + ((MAX_X - MIN_X) * binary) / (Math.pow(2, CHROMOSOME_LENGTH) - 1); // Масштабирование в диапазон [MIN_X, MAX_X]
    }

    // Преобразование Gray-кода в бинарный код
    private static String grayToBinary(String gray) {
        StringBuilder binary = new StringBuilder();
        binary.append(gray.charAt(0));
        for (int i = 1; i < gray.length(); i++) {
            binary.append(gray.charAt(i) ^ binary.charAt(i - 1)); // XOR для преобразования Gray в бинарный
        }
        return binary.toString();
    }

    // Второе задание: Поиск бинарной последовательности с заданным PSL
    private static void findBinarySequence() {
        final int N = 35, P = 70, PSL = 3, K = 3; // Параметры задачи
        final double CROSSOVER_RATE = 0.8, MUTATION_RATE = 0.2; // Параметры генетического алгоритма
        List<String> population = new ArrayList<>();
        Random random = new Random();

        // Инициализация начальной популяции случайными последовательностями
        for (int i = 0; i < P; i++) {
            StringBuilder sequence = new StringBuilder();
            for (int j = 0; j < N; j++) {
                sequence.append(random.nextInt(2));
            }
            population.add(sequence.toString());
        }

        // Эволюция популяции в течение MAX_GENERATIONS поколений
        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
            population.sort(Comparator.comparingInt(GeneticAlgorithm::autocorrelation)); // Сортировка по автокорреляции
            List<String> newPopulation = new ArrayList<>();

            for (int i = 0; i < P / 2; i++) {
                String parent1 = population.get(i);
                String parent2 = population.get(i + 1);
                if (Math.random() < CROSSOVER_RATE) {
                    // Кроссовер и мутация
                    String[] offspring = crossover(parent1, parent2);
                    newPopulation.add(mutate(offspring[0]));
                    newPopulation.add(mutate(offspring[1]));
                } else {
                    // Добавление родителей в новую популяцию
                    newPopulation.add(parent1);
                    newPopulation.add(parent2);
                }
            }
            population = newPopulation;
        }

        System.out.println("_______________________________________________________");
        System.out.println("\nНахождение двоичных последовательностей с заданным PSL");

        // Вывод K лучших последовательностей
        for (int i = 0; i < K; i++) {
            System.out.println("Лучшее решение " + (i + 1) + ": " + population.get(i));
        }
    }

    // Вычисление автокорреляции для последовательности
    private static int autocorrelation(String sequence) {
        int maxSideLobe = 0;
        for (int shift = 1; shift < sequence.length(); shift++) {
            int sum = 0;
            for (int i = 0; i < sequence.length() - shift; i++) {
                sum += (sequence.charAt(i) == sequence.charAt(i + shift)) ? 1 : -1; // Сравнение битов
            }
            maxSideLobe = Math.max(maxSideLobe, Math.abs(sum)); // Максимальное значение бокового лепестка
        }
        return maxSideLobe;
    }
}