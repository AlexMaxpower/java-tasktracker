package tracker.controllers;

import tracker.model.*;
import java.util.HashMap;

public class TaskManager {

    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Subtask> subtasks;
    private HashMap<Integer, Epic> epics;
    private int currentId;   // текущий идентификационный номер задачи

    public TaskManager() {
        currentId = 0;
        tasks = new HashMap<>();
        subtasks = new HashMap<>();
        epics = new HashMap<>();
    }

    public void setCurrentId(int currentId) {
        this.currentId = currentId;
    }

    public int getCurrentId() {
        return currentId;
    }

    // увеличиваем текущее значение Id на единицу
    private int incCurrentId() {
        return ++currentId;
    }

    // получение списка всех задач (пункт 2.1)
    public void printAllTasks() {
        System.out.println("");
        System.out.println("Задачи:");
        System.out.println("----");
        for (Task task : tasks.values()) {
            System.out.println(task);
        }
        System.out.println("");
        System.out.println("Эпики с подзадачами:");
        System.out.println("----");
        for (Epic epic : epics.values()) {
            System.out.println(epic);
            getEpicSubtasks(epic);
        }
        System.out.println("");
    }

    // удаление всех задач из менеджера (пункт 2.2)
    public void clearAll() {
        currentId = 0;
        tasks.clear();
        subtasks.clear();
        epics.clear();
    }

    // получение задачи по идентификатору (пункт 2.3)
    public Task getTask(Integer id) {
        if (tasks.containsKey(id)) {
            return tasks.get(id);
        }
        else if (subtasks.containsKey(id)) {
            return subtasks.get(id);
        }
        else if (epics.containsKey(id)) {
            return epics.get(id);
        }
        return null;
    }

    // добавляем или обновляем задачу/подзадачу/эпик в менеджер (пункты 2.4 и 2.5)
    public void addTask(Task o, Integer id) {
        boolean newTask = false;
        if (id == 0) {
            id = incCurrentId();   // увеличиваем id на единицу, если задача новая
            newTask = true;
        }
        o.setTaskId(id);           // присваиваем задаче id

        if (o.getClass() == Task.class) {
            tasks.put(id,o);
        }

        else if (o.getClass() == Subtask.class) {
            subtasks.put(id, (Subtask) o);
            if (!newTask) {
                ((Subtask) o).getEpic().deleteSubtask(((Subtask) o));
            }
            ((Subtask) o).getEpic().addSubtaskEpic(((Subtask) o));
        }

        else if (o.getClass() == Epic.class) {
            epics.put(id, (Epic) o);
        }
    }

    // удаление задачи по идентификатору (пункт 2.6)
    public void deleteTask(Integer id) {
        if (tasks.containsKey(id)) {
            tasks.remove(id);
        }
        else if (subtasks.containsKey(id)) {      // ищем id в подзадачах и если есть, то удаляем также из эпика
            Epic epic = subtasks.get(id).getEpic();
            epic.deleteSubtask(subtasks.get(id));
            subtasks.remove(id);
        }
        else if (epics.containsKey(id)) {         // ищем id в эпиках и если есть, то удаляем также подзадачи
            for (Subtask subtask: epics.get(id).getSubtasksEpic()) {
                subtasks.remove(subtask);
            }
            epics.remove(id);
        }
    }

    // получение списка подзадач определеннного эпика (пункт 3.1)
    public void getEpicSubtasks(Epic epic) {
        for (Subtask subtask : epic.getSubtasksEpic()) {
            System.out.println(subtask);
        }
    }
}