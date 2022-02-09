package tracker;

import tracker.controllers.TaskManager;
import tracker.model.Epic;
import tracker.model.Status;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.util.Managers;

public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = Managers.getDefault();

        // Далее код только для тестирования работы программы

        // Создаем две задачи
        Task firstTask = new Task("Сходить в магазин", "Купить хлеб, молоко, сыр", Status.NEW);
        Task secondTask = new Task("Полить цветы", "Полить цветы в гостиной и спальне", Status.NEW);

        // Добавляем задачи в менеджер
        taskManager.addTask(firstTask,0);
        taskManager.addTask(secondTask,0);

        // Создаем эпик и две подзадачи
        Epic firstEpic = new Epic("Съездить на дачу",
                "В выходные необходимо заехать на дачу на автомобиле", Status.NEW);
        taskManager.addTask(firstEpic,0);

        Subtask firstSubtask = new Subtask("Заправить автомобиль",
                "Съездить на АЗС",Status.NEW, firstEpic);
        taskManager.addTask(firstSubtask,0);

        Subtask secondSubtask = new Subtask("Погрузить вещи в машину",
                "Обязательно взять ключи от дачи",Status.NEW, firstEpic);
        taskManager.addTask(secondSubtask,0);

        // Создаем второй эпик и одну подзадачу
        Epic secondEpic = new Epic("Сделать презентацию",
                "Необходимо подготовить презентацию по новому проекту", Status.NEW);
        taskManager.addTask(secondEpic,0);

        Subtask thirdSubtask = new Subtask("Скачать шаблон презентации",
          "Взять стандартный шаблон",Status.NEW, secondEpic);
        taskManager.addTask(thirdSubtask,0);

        // меняем статусы у простой задачи и подзадачи первого эпика
        System.out.println("Меняем статусы у простой задачи и подзадачи первого эпика");

        firstTask.setStatus(Status.IN_PROGRESS);
        taskManager.addTask(firstTask, firstTask.getTaskId());
        secondSubtask.setStatus(Status.IN_PROGRESS);
        taskManager.addTask(secondSubtask, secondSubtask.getTaskId());

        // меняем статус у подзадачи второго эпика
        System.out.println("Меняем статус у подзадачи второго эпика");
        thirdSubtask.setStatus(Status.DONE);
        taskManager.addTask(thirdSubtask, thirdSubtask.getTaskId());
        taskManager.printAllTasks();

        // получение задачи по id
        System.out.println("Получаем задачу по ID");
        Task o = taskManager.getTask(secondTask.getTaskId());
        System.out.println(o);

        // печатаем историю
        taskManager.printHistory();

        // получение задачи по id
        System.out.println("Получаем задачу по ID");
        o = taskManager.getTask(secondTask.getTaskId());
        System.out.println(o);

        // получение задачи по id
        System.out.println("Получаем задачу по ID");
        o = taskManager.getTask(secondTask.getTaskId());
        System.out.println(o);

        // получение задачи по id
        System.out.println("Получаем задачу по ID");
        o = taskManager.getTask(secondTask.getTaskId());
        System.out.println(o);

        // печатаем историю
        taskManager.printHistory();

        // удаление задачи по id
        System.out.println("Удаляем задачу по ID");
        taskManager.deleteTask(secondTask.getTaskId());
        taskManager.printHistory();
        taskManager.printAllTasks();
        taskManager.printHistory();

        // удаление подзадачи по id
        System.out.println("Удаляем подзадачу по ID");
        taskManager.deleteTask(secondSubtask.getTaskId());
        taskManager.printHistory();
        taskManager.printAllTasks();
        taskManager.printHistory();

        // возвращаем обратно
        System.out.println("Добавляем подзадачу обратно");
        taskManager.addTask(secondSubtask,0);
        taskManager.printAllTasks();

        System.out.println("Меняем статусы у подзадач первого эпика");
        firstSubtask.setStatus(Status.DONE);
        taskManager.addTask(firstSubtask, firstSubtask.getTaskId());
        secondSubtask.setStatus(Status.DONE);
        taskManager.addTask(secondSubtask, secondSubtask.getTaskId());
        taskManager.printAllTasks();

        // удаляем эпик по id
        System.out.println("Удаляем эпик по ID");
        taskManager.deleteTask(firstEpic.getTaskId());
        taskManager.printHistory();
        taskManager.printAllTasks();

        // удаляем все задачи
        System.out.println("Удаление всех задач...");
        taskManager.clearAll();
        taskManager.printAllTasks();
        taskManager.printHistory();
    }
}