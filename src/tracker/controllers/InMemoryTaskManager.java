package tracker.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tracker.model.*;
import tracker.util.Managers;

public class InMemoryTaskManager implements TaskManager{
    private Map<Integer, Task> tasks;
    private Map<Integer, Subtask> subtasks;
    private Map<Integer, Epic> epics;
    private int currentId;   // текущий идентификационный номер задачи

    HistoryManager historyManager;

    public InMemoryTaskManager() {
        currentId = 0;
        tasks = new HashMap<>();
        subtasks = new HashMap<>();
        epics = new HashMap<>();
        historyManager = Managers.getDefaultHistory();
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


    // получение списка всех задач (вывод в терминал для отладки)
    @Override
    public List<Task> getAllTasks() {
        List<Task> resultAllTasks = new ArrayList<>();
        System.out.println("");
        System.out.println("Задачи:");
        System.out.println("----");

        for (Task task : tasks.values()) {
            resultAllTasks.add(task);
            System.out.println(task);
        }

        System.out.println("");
        System.out.println("Эпики с подзадачами:");
        System.out.println("----");

        for (Epic epic : epics.values()) {
            resultAllTasks.add(epic);
            System.out.println(epic);
            List<Task> epicSubtasks = getEpicSubtasks(epic);
            resultAllTasks.addAll(epicSubtasks);
        }

        System.out.println("");
        return resultAllTasks;
    }

    // получение списка всех простых задач из менеджера
    @Override
    public List<Task> getTasks() {
        List<Task> resultTasks = new ArrayList<>();
        for (Task task : tasks.values()) {
            resultTasks.add(task);
        }
        return resultTasks;
    }

    // получение списка всех эпиков из менеджера
    @Override
    public List<Task> getEpics() {
        List<Task> resultEpics = new ArrayList<>();
        for (Task epic : epics.values()) {
            resultEpics.add(epic);
        }
        return resultEpics;
    }

    // получение списка всех подзадач из менеджера
    @Override
    public List<Task> getSubtasks() {
        List<Task> resultSubtasks = new ArrayList<>();
        for (Task subtask : subtasks.values()) {
            resultSubtasks.add(subtask);
        }
        return resultSubtasks;
    }

    // получение списка подзадач определеннного эпика (пункт 3.1)
    @Override
    public List<Task> getEpicSubtasks(Epic epic) {
        List<Task> resultSubtasks = new ArrayList<>();
        for (Subtask subtask : epic.getSubtasksEpic()) {
            resultSubtasks.add(subtask);
        }
        return resultSubtasks;
    }

    // удаление всех задач из менеджера (пункт 2.2)
    @Override
    public void clearAll() {
        currentId = 0;
        tasks.clear();
        subtasks.clear();
        epics.clear();
    }

    // удаление всех простых задач из менеджера
    @Override
    public void clearTasks() {
        for (Integer id : tasks.keySet()) {
            historyManager.remove(id);
        }
        tasks.clear();
    }

    // удаление всех подзадач из менеджера
    @Override
    public void clearSubtasks() {
        // удаляем просмотры подзадач из истории
        for (Integer id : subtasks.keySet()) {
            historyManager.remove(id);
        }
        // удаляем подзадачи из эпиков
        for (Subtask subtask : subtasks.values()) {
            subtask.getEpic().deleteSubtask(subtask);
        }
        subtasks.clear();
    }

    // удаление всех эпиков из менеджера
    @Override
    public void clearEpics() {
        // сначала удаляем все подзадачи, так как без эпиков подзадачи не существуют
        clearSubtasks();
        for (Integer id : epics.keySet()) {
            historyManager.remove(id);
        }
        epics.clear();
    }

    // получение задачи по идентификатору (пункт 2.3)
    @Override
    public Task getTask(Integer id) {
        if (tasks.containsKey(id)) {
            historyManager.add(tasks.get(id));
            return tasks.get(id);
        }
        else if (subtasks.containsKey(id)) {
            historyManager.add(subtasks.get(id));
            return subtasks.get(id);
        }
        else if (epics.containsKey(id)) {
            historyManager.add(epics.get(id));
            return epics.get(id);
        }
        return null;
    }

    // добавляем или обновляем задачу/подзадачу/эпик в менеджер (пункты 2.4 и 2.5)
    @Override
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
    @Override
    public void deleteTask(Integer id) {
        if (tasks.containsKey(id)) {
            tasks.remove(id);
            historyManager.remove(id);
        }
        else if (subtasks.containsKey(id)) {      // ищем id в подзадачах и если есть, то удаляем также из эпика
            Epic epic = subtasks.get(id).getEpic();
            epic.deleteSubtask(subtasks.get(id));
            subtasks.remove(id);
            historyManager.remove(id);
        }
        else if (epics.containsKey(id)) {         // ищем id в эпиках и если есть, то удаляем также подзадачи
            for (Subtask subtask: epics.get(id).getSubtasksEpic()) {
                subtasks.remove(subtask);
                historyManager.remove(subtask.getTaskId());
            }
            epics.remove(id);
            historyManager.remove(id);
        }
    }

    @Override
    public List<Task> history() {
        System.out.print("Печатаем историю: ");
        List<Task> historyList = new ArrayList<>();
        historyList = historyManager.getHistory();
        if (historyList != null) {
            int i = 0; // вспомогательная переменная для вывода стрелочки
            for (Task historyObject : historyList) {
                if (i != 0) {
                    System.out.print(" -> "); // печатаем стрелочку, если не первый элемент списка
                }
                i++;
                System.out.print(historyObject.getTaskId());
            }
            System.out.println("");
        }
        return historyList;
    }
}