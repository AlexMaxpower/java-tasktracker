package tracker;

import tracker.controllers.FileBackedTasksManager;
import tracker.controllers.TaskManager;
import tracker.model.Epic;
import tracker.model.Status;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.util.Managers;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

import static tracker.model.Status.NEW;

public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = Managers.getDefault();
        //FileBackedTasksManager taskManager = FileBackedTasksManager.loadFromFile(new File("src/tracker/resources/tasksdata.csv"));

        // Далее код только для тестирования работы программы

        Task o;

        // Создаем две задачи
        Task firstTask = new Task("Сходить в магазин", "Купить хлеб и молоко", Status.NEW);
        Task secondTask = new Task("Полить цветы", "Полить цветы в гостиной и спальне", Status.NEW);

        // Добавляем задачи в менеджер
        taskManager.addTask(firstTask,0);
        taskManager.addTask(secondTask,0);

        LocalDateTime startTime = LocalDateTime.of(2022,04,01,1,20);
        Duration duration = Duration.ofMinutes(20);




        // Создаем эпик и три подзадачи
        Epic firstEpic = new Epic("Съездить на дачу",
                "В выходные необходимо заехать на дачу на автомобиле", Status.NEW);
        taskManager.addTask(firstEpic,0);

        Subtask firstSubtask = new Subtask("Заправить автомобиль",
                "Съездить на АЗС", NEW, firstEpic,startTime, duration );
        taskManager.addTask(firstSubtask,0);

        Subtask secondSubtask = new Subtask("Погрузить вещи в машину",
                "Забрать все ненужное :)", NEW, firstEpic);
        taskManager.addTask(secondSubtask,0);

        Subtask thirdSubtask = new Subtask("Взять ключи от дачи",
                "Обязательно взять ключи от дачи", NEW, firstEpic, startTime.plusDays(2).plusHours(4),
                Duration.ofMinutes(30));
        System.out.println("!" + thirdSubtask.toString());
        taskManager.addTask(thirdSubtask,0);

        // Создаем второй эпик без подзадач
        Epic secondEpic = new Epic("Сделать презентацию",
                "Необходимо подготовить презентацию по новому проекту", Status.NEW);
        taskManager.addTask(secondEpic,0);

        // Запрашиваем задачи, эпики и подзадачи в разном порядке и выводим историю
        // получение задачи по id

        System.out.println("Получаем вторую подзадачу по ID");
        o = taskManager.getTask(secondSubtask.getTaskId());
        System.out.println(o);

        System.out.println("Получаем первую задачу по ID");
        o = taskManager.getTask(firstTask.getTaskId());
        System.out.println(o);
        taskManager.history();

        System.out.println("Получаем вторую задачу по ID");
        o = taskManager.getTask(secondTask.getTaskId());
        System.out.println(o);
        taskManager.history();

        System.out.println("Еще раз получаем первую задачу по ID");
        o = taskManager.getTask(firstTask.getTaskId());
        System.out.println(o);
        taskManager.history();

        System.out.println("Получаем первый эпик по ID");
        o = taskManager.getTask(firstEpic.getTaskId());
        System.out.println(o);
        taskManager.history();

        System.out.println("Получаем второй эпик по ID");
        o = taskManager.getTask(secondEpic.getTaskId());
        System.out.println(o);
        taskManager.history();

     //   FileBackedTasksManager.main(null);

        taskManager.getAllTasks();
        taskManager.getPrioritizedTasks();
        // удаляем вторую задачу по id
        System.out.println("Удаляем вторую задачу по ID");
        taskManager.deleteTask(secondTask.getTaskId());
        taskManager.history();
 /*
        // удаляем первый эпик по id
        System.out.println("Удаляем первый эпик по ID");
        taskManager.deleteTask(firstEpic.getTaskId());
        taskManager.history();



        // удаляем все задачи, подзадачи и эпики
        System.out.println("Удаляем все задачи, подзадачи и эпики");
        taskManager.clearAll();
        taskManager.history();

*/

        /*
        // меняем статусы у простой задачи и подзадачи первого эпика
        System.out.println("Меняем статусы у простой задачи и подзадачи первого эпика");

        firstTask.setStatus(Status.IN_PROGRESS);
        taskManager.addTask(firstTask, firstTask.getTaskId());
        secondSubtask.setStatus(Status.IN_PROGRESS);
        taskManager.addTask(secondSubtask, secondSubtask.getTaskId());

        // получение задачи по id
        System.out.println("Получаем задачу по ID");
        o = taskManager.getTask(secondSubtask.getTaskId());
        System.out.println(o);

        // печатаем историю
        taskManager.history();

        System.out.println("Получаем задачу по ID");
        o = taskManager.getTask(firstTask.getTaskId());
        System.out.println(o);

        // печатаем историю
        taskManager.history();

        // меняем статус у подзадачи второго эпика
        System.out.println("Меняем статус у подзадачи второго эпика");
        System.out.println(thirdSubtask);
        thirdSubtask.setStatus(Status.DONE);
        taskManager.addTask(thirdSubtask, thirdSubtask.getTaskId());
        System.out.println(thirdSubtask);
        List<Task> allTasks = taskManager.getAllTasks();
        System.out.println("Печатаем полученный список всех задач");
        System.out.println(allTasks);

        // получение задачи по id
        System.out.println("Получаем задачу по ID");
        o = taskManager.getTask(secondTask.getTaskId());
        System.out.println(o);

        // печатаем историю
        taskManager.history();

        // получение задачи по id
        System.out.println("Получаем задачу по ID");
        o = taskManager.getTask(firstTask.getTaskId());
        System.out.println(o);

        // печатаем историю
        taskManager.history();

        // получение задачи по id
        System.out.println("Получаем задачу по ID");
        o = taskManager.getTask(secondTask.getTaskId());
        System.out.println(o);

        // печатаем историю
        taskManager.history();

        // получение задачи по id
        System.out.println("Получаем задачу по ID");
        o = taskManager.getTask(secondTask.getTaskId());
        System.out.println(o);

        // печатаем историю
        taskManager.history();

        // удаление задачи по id
        System.out.println("Удаляем задачу по ID");
        taskManager.deleteTask(secondTask.getTaskId());
        taskManager.history();
        taskManager.getAllTasks();
        taskManager.history();

        // удаление подзадачи по id
        System.out.println("Удаляем подзадачу по ID");
        taskManager.deleteTask(secondSubtask.getTaskId());
        taskManager.history();
        taskManager.getAllTasks();
        taskManager.history();

        // возвращаем обратно
        System.out.println("Добавляем подзадачу обратно");
        taskManager.addTask(secondSubtask,0);
        taskManager.getAllTasks();

        System.out.println("Меняем статусы у подзадач первого эпика");
        firstSubtask.setStatus(Status.DONE);
        taskManager.addTask(firstSubtask, firstSubtask.getTaskId());
        secondSubtask.setStatus(Status.DONE);
        taskManager.addTask(secondSubtask, secondSubtask.getTaskId());
        taskManager.getAllTasks();

        // удаляем эпик по id
        System.out.println("Удаляем эпик по ID");
        taskManager.deleteTask(firstEpic.getTaskId());
        taskManager.history();
        taskManager.getAllTasks();

        // удаляем все задачи
        System.out.println("Удаление всех задач...");
        System.out.println("Удаление всех простых задач...");
        taskManager.clearTasks();
        taskManager.history();
     //   taskManager.clearAll();
        taskManager.getAllTasks();
        taskManager.history();
        System.out.println("Удаление всех подзадач...");
        taskManager.clearSubtasks();
        taskManager.history();
        taskManager.getAllTasks();
        taskManager.history();
        System.out.println("Удаление всех эпиков...");
        taskManager.clearEpics();
        taskManager.history();
        taskManager.getAllTasks();


         */


        // Создаем еще задачи
        Task thirdTask = new Task("Сходить в гараж", "Забрать инструменты", Status.NEW,
                startTime.minusDays(2).plusHours(5),
                Duration.ofMinutes(40));
        Task fourthTask = new Task("Убрать квартиру", "Ковер в детской", Status.NEW);
        Task fifthTask = new Task("Покрасить забор", "Краска на балконе", Status.NEW);
        Task sixthTask = new Task("Купить принтер", "Уточнить наличие в магазине", Status.NEW,
                startTime.plusDays(6).plusHours(1),
                Duration.ofMinutes(120));
        Task seventhTask = new Task("Сдать домашку", "Отправить работу на проверку", Status.NEW);

        taskManager.addTask(thirdTask,0);
        taskManager.addTask(fourthTask,0);
        taskManager.addTask(fifthTask,0);
        taskManager.addTask(sixthTask,0);
        taskManager.addTask(seventhTask,0);

        thirdSubtask = new Subtask("Скачать шаблон презентации",
          "Взять стандартный шаблон",Status.NEW, secondEpic);
        taskManager.addTask(thirdSubtask,0);

         taskManager.getPrioritizedTasks();


    }
}