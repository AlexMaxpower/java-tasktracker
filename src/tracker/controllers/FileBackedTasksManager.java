package tracker.controllers;

import tracker.model.*;
import tracker.exceptions.ManagerSaveException;
import tracker.util.Managers;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static tracker.model.TypeTask.TASK;

public class FileBackedTasksManager extends InMemoryTaskManager implements TaskManager {

    private String filename;

    public FileBackedTasksManager(String filename, List<Task> taskList, List<Integer> historyIdTasks) {

        this.filename = filename;
        Integer maxId = 0;

        for (Task task : taskList) {
            if (task.getClass() == Task.class) {
                super.tasks.put(task.getTaskId(), task);
            } else if (task.getClass() == Subtask.class) {

                // добавляем вместо эпика-пустышки настоящий эпик
                Task subtask = new Subtask(task.getName(), task.getDescription(),
                        task.getStatus(), super.epics.get(((Subtask) task).getEpic().getTaskId()));
                subtask.setTaskId(task.getTaskId());
                super.subtasks.put(task.getTaskId(), (Subtask) subtask);
                // добавляем сабтаск в эпик
                super.epics.get(((Subtask) task).getEpic().getTaskId()).addSubtaskEpic((Subtask) subtask);

            } else if (task.getClass() == Epic.class) {
                super.epics.put(task.getTaskId(), (Epic) task);
            }
            if (maxId < task.getTaskId()) {
                maxId = task.getTaskId();
            }
            super.setCurrentId(maxId);

        }

        for (Integer id : historyIdTasks) {
               historyManager.add(super.getTask(id));
        }
    }

    public static void main(String[] args) {

        FileBackedTasksManager testManager = loadFromFile(new File("src/tracker/resources/tasksdata.csv"));

        Task firstTask = new Task("Купить клавиатуру", "Обязательно", Status.NEW);
        testManager.addTask(firstTask,0);

        Task secondTask = new Task("Полить цветы", "Полить цветы в гостиной и спальне", Status.NEW);

        testManager.addTask(secondTask,0);

        // Создаем эпик и три подзадачи
        Epic firstEpic = new Epic("Съездить на дачу",
                "В выходные необходимо заехать на дачу на автомобиле", Status.NEW);
        testManager.addTask(firstEpic,0);

        Subtask firstSubtask = new Subtask("Заправить автомобиль",
                "Съездить на АЗС",Status.NEW, firstEpic);
        testManager.addTask(firstSubtask,0);

        Subtask secondSubtask = new Subtask("Погрузить вещи в машину",
                "Забрать все ненужное :)",Status.NEW, firstEpic);
        testManager.addTask(secondSubtask,0);

        Subtask thirdSubtask = new Subtask("Взять ключи от дачи",
                "Обязательно взять ключи от дачи",Status.NEW, firstEpic);
        testManager.addTask(thirdSubtask,0);

        Task o;

        System.out.println("Получаем вторую подзадачу по ID");
        o = testManager.getTask(secondSubtask.getTaskId());
        System.out.println(o);

        System.out.println("Получаем первую задачу по ID");
        o = testManager.getTask(firstTask.getTaskId());
        System.out.println(o);
        testManager.history();

        System.out.println("Получаем вторую задачу по ID");
        o = testManager.getTask(secondTask.getTaskId());
        System.out.println(o);
        testManager.history();

        System.out.println("\nСравнение данных в двух менеджерах");
        System.out.println("-------------");
        System.out.println("\nПервый менеджер:");
        testManager.getAllTasks();
        testManager.history();

        System.out.println("\nВторой менеджер:");
        FileBackedTasksManager testManagerNew = loadFromFile(new File("src/tracker/resources/tasksdata.csv"));

        testManagerNew.getAllTasks();
        testManagerNew.history();
    }

    private void save() {
        StringBuilder strBuilder = new StringBuilder();
        List<Task> resultAllTasks = new ArrayList<>();

        if (getTasks() != null) {
            for (Task task : getTasks()) {
                resultAllTasks.add(task);
            }
        }

        if (getEpics() != null) {
            for (Task epic : getEpics()) {
                resultAllTasks.add(epic);
                List<Task> epicSubtasks = getEpicSubtasks((Epic) epic);
                resultAllTasks.addAll(epicSubtasks);
            }

        strBuilder.append("id,type,name,status,description,epic\n");
        for (Task task : resultAllTasks) {
                strBuilder.append(toString(task)+"\n");
            }
        }

        strBuilder.append("\n");
        strBuilder.append(toString(historyManager));

        try (FileWriter fileWriter = new FileWriter(filename, StandardCharsets.UTF_8)) {
            fileWriter.write(strBuilder.toString());

        } catch (IOException e) {
            try {
                throw new ManagerSaveException("Произошла ошибка во время записи файла!");
            } catch (ManagerSaveException ex) {
                ex.printStackTrace();
            }
        }
         //   System.out.println(strBuilder.toString());
    }

    private String toString(Task task) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(Integer.toString(task.getTaskId()) + ",");

        if (task.getClass() == Task.class) {
            strBuilder.append(TASK.toString() + ",");
        }

        else if (task.getClass() == Epic.class) {
            strBuilder.append(TypeTask.EPIC.toString() + ",");
        }

        else if (task.getClass() == Subtask.class) {
            strBuilder.append(TypeTask.SUBTASK.toString() + ",");
        }

        strBuilder.append(task.getName() + ",");
        strBuilder.append(task.getStatus().toString() + ",");
        strBuilder.append(task.getDescription() + ",");

        if (task.getClass() == Subtask.class) {
            strBuilder.append(Integer.toString(((Subtask) task).getEpic().getTaskId()));
        }

        return strBuilder.toString();
    }

    private static String toString(HistoryManager manager) {
        List<Task> historyList;
        historyList = manager.getHistory();

        StringBuilder strBuilder = new StringBuilder();

        for (Task task : historyList) {
            strBuilder.append(task.getTaskId() + ",");
        }
        if ((strBuilder.length() != 0) && (historyList.size() != 0)) {
            strBuilder.deleteCharAt(strBuilder.length()-1);
            strBuilder.append("\n");
        }
        return strBuilder.toString();
    }

    public static FileBackedTasksManager loadFromFile(File file) {

        System.out.println("Читаем данные из файла " + file.getPath());
        List<Task> taskList = new ArrayList<>();
        List<Integer> historyIdTasks = new ArrayList<>();
        Task task;

        try (BufferedReader fileReader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {

            // чтение задач
            String line = fileReader.readLine();
            while (line != null) {
                line = fileReader.readLine();
                if (line.equals("")) {
                    break;
                }

                task = FromString(line);
                taskList.add(task);
            }

            // чтение истории
            line = fileReader.readLine();

            if (line != null) {
                historyIdTasks = historyFromString(line);
            }
            } catch(FileNotFoundException e){
                System.out.println("Файл " + file.getPath() + " не найден!");
            } catch(IOException e){
                e.printStackTrace();
            }

            return new FileBackedTasksManager(file.getPath(), taskList, historyIdTasks);
    }

    private static Task FromString(String value) throws IllegalArgumentException {
        String[] split = value.split(",");
        Status status;

        switch (split[3]) {
            case "NEW":
                status = Status.NEW;
                break;
            case "IN_PROGRESS":
                status = Status.IN_PROGRESS;
                break;
            case "DONE":
                status = Status.DONE;
                break;
            default:
                throw new IllegalArgumentException("Ошибка статуса задачи в файле данных!");
        }

        Task task;
        int taskId = Integer.parseInt(split[0]);

        switch (split[1]) {
            case "TASK":
                task = new Task(split[2], split[4], status);
                task.setTaskId(taskId);
                break;
            case "EPIC":
                task = new Epic(split[2], split[4], status);
                task.setTaskId(taskId);
                break;
            case "SUBTASK":
                int epicId = Integer.parseInt(split[5].trim());
                Epic epic = new Epic("","",Status.NEW); // пустышка для передачи EpicId
                epic.setTaskId(epicId);
                task = new Subtask(split[2], split[4], status, epic);
                task.setTaskId(taskId);
                break;
            default:
                throw new IllegalArgumentException("Ошибка типа задачи в файле данных!");
        }
        return task;
    }

    static List<Integer> historyFromString(String value) {
        String[] split = value.split(",");
        List<Integer> historyIds = new ArrayList<>();
        for (int i=0; i < split.length; i++) {
            historyIds.add(Integer.parseInt(split[i]));
        }
        return historyIds;
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public Task getTask(Integer id) {
        Task task = super.getTask(id);
        save();
        return task;
    }

    @Override
    public void addTask(Task o, Integer id) {
        super.addTask(o, id);
        save();
    }

    @Override
    public void deleteTask(Integer id) {
        super.deleteTask(id);
        save();
    }
}
