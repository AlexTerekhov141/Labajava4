
/*
 * Main
 *
 * Version 1.0
 *
 * May2024
 *
 * Practice work for java 4(Elevator system)
 */
import java.util.*;

// Класс, представляющий лифт
class Elevator extends Thread {
    private int currentFloor;
    private final int id;
    private final List<Integer> requests = new LinkedList<>();
    private boolean movingUp = true;


    public Elevator(int id, int startFloor) {
        this.id = id;
        this.currentFloor = startFloor;
    }


    public synchronized void addRequest(int floor) { // Метод для добавления новой заявки на этаж
        if (!requests.contains(floor)) {
            requests.add(floor);
            requests.sort((a, b) -> movingUp ? a - b : b - a);
            notify();
        }
    }


    public int getCurrentFloor() {
        return currentFloor;
    }


    public void moveToFloor(int targetFloor) { // Метод для перемещения лифта на указанный этаж
        while (currentFloor != targetFloor) {
            if (currentFloor < targetFloor) {
                currentFloor++;
                movingUp = true;
            } else {
                currentFloor--;
                movingUp = false;
            }
            System.out.println("Лифт " + id + " на этаже " + currentFloor);
            checkForIntermediateStops(); // Проверяем промежуточные остановки
            try {
                Thread.sleep(1100); // Задержка для симуляции движения лифта
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private synchronized void checkForIntermediateStops() { // Метод для проверки промежуточных остановок
        if (requests.contains(currentFloor)) {
            System.out.println("Лифт " + id + " остановился на попутном этаже " + currentFloor);
            requests.remove((Integer) currentFloor);
        }
    }


    public void run() { // Основной метод потока лифта
        System.out.println("Лифт " + id + " готов к работе на этаже " + currentFloor);
        while (true) { // Бесконечный цикл для обработки заявок
            try {
                int nextFloor;
                synchronized (this) {
                    while (requests.isEmpty()) {
                        wait();
                    }
                    nextFloor = requests.remove(0);
                }
                System.out.println("Лифт " + id + " направляется на этаж " + nextFloor);
                moveToFloor(nextFloor);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}


class RequestGenerator extends Thread { // Класс, генерирующий случайные заявки на вызов лифта
    private final int totalFloors;
    private final Elevator[] elevators;
    private final Random random = new Random();


    public RequestGenerator(int totalFloors, Elevator[] elevators) {
        this.totalFloors = totalFloors;
        this.elevators = elevators;
    }


    public int generateRequest() {
        return random.nextInt(totalFloors) + 1; // Случайный этаж от 1 до totalFloors
    }


    public void run() { // Основной метод потока генератора заявок
        while (true) { // Бесконечный цикл генерации заявок
            int requestedFloor = generateRequest();
            System.out.println("Заявка на вызов лифта на этаж " + requestedFloor);
            assignElevator(requestedFloor);
            try {
                Thread.sleep(1000); // Задержка между заявками
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    void assignElevator(int requestedFloor) { // Метод для назначения лифта для выполнения заявки
        Elevator bestElevator = null;
        int minDistance = Integer.MAX_VALUE;

        // Поиск лифта с минимальным расстоянием до целевого этажа
        for (Elevator elevator : elevators) {
            int distance = Math.abs(elevator.getCurrentFloor() - requestedFloor);
            if (distance < minDistance) {
                minDistance = distance;
                bestElevator = elevator;
            }
        }


        if (bestElevator != null) {
            bestElevator.addRequest(requestedFloor);
        }
    }
}


public class Main {
    public static void main(String[] args) {
        int totalFloors = 9;

        // Создание и запуск двух лифтов
        Elevator elevator1 = new Elevator(1, 4);
        Elevator elevator2 = new Elevator(2, 8);

        elevator1.start();
        elevator2.start();


        Elevator[] elevators = {elevator1, elevator2};
        // Создание и запуск генератора заявок
        RequestGenerator requestGenerator = new RequestGenerator(totalFloors, elevators);
        requestGenerator.start();


        Scanner in = new Scanner(System.in);

        while (true) { // Бесконечный цикл для ввода заявок пользователем
            System.out.println("Введите номер этажа для вызова лифта или 'выход' для завершения: ");
            String input = in.nextLine();
            if (Objects.equals(input, "выход")) {
                break;
            }

            int targetFloor;
            try {
                targetFloor = Integer.parseInt(input);
                if (targetFloor < 1 || targetFloor > totalFloors) {
                    System.out.println("Неверный номер этажа");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("Неверный ввод");
                continue;
            }


            requestGenerator.assignElevator(targetFloor);
        }

        in.close();
    }
}
