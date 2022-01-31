package tracker;

import tracker.controllers.TaskManager;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = new TaskManager();

        // Далее код только для тестирования работы программы

        // Создаем две задачи
        Task firstTask = new Task("Сходить в магазин", "Купить хлеб, молоко, сыр", "NEW");
        Task secondTask = new Task("Полить цветы", "Полить цветы в гостиной и спальне", "NEW");

        // Добавляем задачи в менеджер
        taskManager.addTask(firstTask,0);
        taskManager.addTask(secondTask,0);

        // Создаем эпик и две подзадачи
        Epic firstEpic = new Epic("Съездить на дачу",
                "В выходные необходимо заехать на дачу на автомобиле", "NEW");
        taskManager.addTask(firstEpic,0);

        Subtask firstSubtask = new Subtask("Заправить автомобиль",
                "Съездить на АЗС","NEW", firstEpic);
        taskManager.addTask(firstSubtask,0);

        Subtask secondSubtask = new Subtask("Погрузить вещи в машину",
                "Обязательно взять ключи от дачи","NEW", firstEpic);
        taskManager.addTask(secondSubtask,0);

        // Создаем второй эпик и одну подзадачу
        Epic secondEpic = new Epic("Сделать презентацию",
                "Необходимо подготовить презентацию по новому проекту", "NEW");
        taskManager.addTask(secondEpic,0);

        Subtask thirdSubtask = new Subtask("Скачать шаблон презентации",
          "Взять стандартный шаблон","NEW", secondEpic);
        taskManager.addTask(thirdSubtask,0);
        taskManager.printAllTasks();

        // меняем статусы у простой задачи и подзадачи первого эпика
        System.out.println("Меняем статусы у простой задачи и подзадачи первого эпика");

        firstTask.setStatus("IN_PROGRESS");
        taskManager.addTask(firstTask, firstTask.getTaskId());
        secondSubtask.setStatus("IN_PROGRESS");
        taskManager.addTask(secondSubtask, secondSubtask.getTaskId());
        taskManager.printAllTasks();

        // меняем статус у подзадачи второго эпика
        System.out.println("Меняем статус у подзадачи второго эпика");
        thirdSubtask.setStatus("DONE");
        taskManager.addTask(thirdSubtask, thirdSubtask.getTaskId());
        taskManager.printAllTasks();

        // получение задачи по id
        System.out.println("Получаем задачу по ID");
        Task o = taskManager.getTask(secondTask.getTaskId());
        System.out.println(o);

        // удаление подзадачи по id
        System.out.println("Удаляем подзадачу по ID");
        taskManager.deleteTask(secondSubtask.getTaskId());
        taskManager.printAllTasks();

        // возвращаем обратно
        System.out.println("Добавляем подзадачу обратно");
        taskManager.addTask(secondSubtask,0);
        taskManager.printAllTasks();

        System.out.println("Меняем статусы у подзадач первого эпика");
        firstSubtask.setStatus("DONE");
        taskManager.addTask(firstSubtask, firstSubtask.getTaskId());
        secondSubtask.setStatus("DONE");
        taskManager.addTask(secondSubtask, secondSubtask.getTaskId());
        taskManager.printAllTasks();

        // удаляем эпик по id
        System.out.println("Удаляем эпик по ID");
        taskManager.deleteTask(firstEpic.getTaskId());
        taskManager.printAllTasks();

        // удаляем все задачи
        System.out.println("Удаление всех задач...");
        taskManager.clearAll();
        taskManager.printAllTasks();
    }
}