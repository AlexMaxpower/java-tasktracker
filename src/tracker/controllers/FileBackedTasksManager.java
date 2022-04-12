package tracker.controllers;

import tracker.model.*;
import tracker.exceptions.ManagerSaveException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static tracker.model.Status.NEW;
import static tracker.model.TypeTask.TASK;

public class FileBackedTasksManager extends InMemoryTaskManager {

    private String filename;

    public FileBackedTasksManager(String filename, List<Task> taskList, List<Integer> historyIdTasks) {

        this.filename = filename;
        Integer maxId = 0;

        for (Task task : taskList) {
            if (task.getClass() == Task.class) {
                super.tasks.put(task.getTaskId(), task);
                super.treeSetTasks.add(task);
            } else if (task.getClass() == Subtask.class) {
                // добавляем вместо эпика-пустышки настоящий эпик
                Epic epic = super.epics.get(((Subtask) task).getEpic().getTaskId());
                Task subtask;
                if (task.getStartTime().isEmpty()) {
                    subtask = new Subtask(task.getName(), task.getDescription(),
                        task.getStatus(), epic);
                } else {
                    subtask = new Subtask(task.getName(), task.getDescription(),
                            task.getStatus(), epic, task.getStartTime().get(), task.getDuration());
                }
                subtask.setTaskId(task.getTaskId());
                super.subtasks.put(task.getTaskId(), (Subtask) subtask);
                super.treeSetTasks.add(subtask);
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

        strBuilder.append("id,type,name,status,description,epic,date-time,duration\n");
        for (Task task : resultAllTasks) {
                strBuilder.append(toString(task)+"\n");
            }
        }

        strBuilder.append("\n");
        strBuilder.append(toString(historyManager));

        try (FileWriter fileWriter = new FileWriter(filename, StandardCharsets.UTF_8)) {
            fileWriter.write(strBuilder.toString());

        } catch (IOException e) {
                throw new ManagerSaveException("Произошла ошибка во время записи файла!");
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
            strBuilder.append(Integer.toString(((Subtask) task).getEpic().getTaskId()) + ",");
        }

        if ((task.getStartTime().isPresent()) && (task.getClass() != Epic.class)) {
            strBuilder.append(task.getStartTime().get().format( DateTimeFormatter.ofPattern("dd.MM.yyyy-HH:mm"))
                    + ",");
            strBuilder.append(task.getDuration().toMinutes() + ",");
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

                task = fromString(line);
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

    private static Task fromString(String value) throws IllegalArgumentException {
        String[] split = value.split(",");
        Status status;

        switch (split[3]) {
            case "NEW":
                status = NEW;
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
                if (split.length > 5) {
                    LocalDateTime taskTime = LocalDateTime.parse(split[5],
                            DateTimeFormatter.ofPattern("dd.MM.yyyy-HH:mm"));
                    task = new Task(split[2], split[4], status, taskTime,
                            Duration.ofMinutes(Long.parseLong(split[6].trim())));
                } else {
                    task = new Task(split[2], split[4], status);
                }
                task.setTaskId(taskId);
                break;

            case "EPIC":
                task = new Epic(split[2], split[4], status);
                task.setTaskId(taskId);
                break;

            case "SUBTASK":
                int epicId = Integer.parseInt(split[5].trim());
                Epic epic = new Epic("","", NEW); // пустышка для передачи EpicId
                epic.setTaskId(epicId);
                if (split.length > 6) {
                    LocalDateTime taskTime = LocalDateTime.parse(split[6],
                            DateTimeFormatter.ofPattern("dd.MM.yyyy-HH:mm"));
                    task = new Subtask(split[2], split[4], status, epic, taskTime,
                            Duration.ofMinutes(Long.parseLong(split[7].trim())));
                } else {
                    task = new Subtask(split[2], split[4], status, epic);
                }
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
    public int addTask(Task o, Integer id) {
        int taskId = super.addTask(o, id);
        save();
        return taskId;
    }

    @Override
    public void deleteTask(Integer id) {
        super.deleteTask(id);
        save();
    }
}
