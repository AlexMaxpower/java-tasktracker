package tracker.controllers;
import java.util.*;

import tracker.exceptions.AddTaskException;
import tracker.model.*;
import tracker.util.Managers;

public class InMemoryTaskManager implements TaskManager{
    protected Map<Integer, Task> tasks;
    protected Map<Integer, Subtask> subtasks;
    protected Map<Integer, Epic> epics;
    protected Set<Task> treeSetTasks;
    private int currentId;   // текущий идентификационный номер задачи

    HistoryManager historyManager;

    public InMemoryTaskManager() {
        currentId = 0;
        tasks = new HashMap<>();
        subtasks = new HashMap<>();
        epics = new HashMap<>();
        historyManager = Managers.getDefaultHistory();

        Comparator<Task> startTimeComparator = new Comparator<>() {
            @Override
            public int compare(Task o1, Task o2) {
               if ((o1.getStartTime().isEmpty()) && (o2.getStartTime().isEmpty())) {
                  return 1;
                }
                if ((o1.getStartTime().isEmpty()) && (o2.getStartTime().isPresent())) {
                    return 1;
                }
                if ((o1.getStartTime().isPresent()) && (o2.getStartTime().isEmpty())) {
                    return -1;
                }
                if ((o1.getStartTime().isPresent()) && (o2.getStartTime().isPresent())) {
                    return o1.getStartTime().get().isAfter(o2.getStartTime().get()) ? 1 : -1;
                }
                return 0;
            }
        };

        treeSetTasks = new TreeSet<Task>(startTimeComparator);
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
            for (Task subtask : epicSubtasks) {
                System.out.println(subtask);
            }
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
        clearTasks();
        clearEpics();  // после удаления всех эпиков, подзадач тоже не будет
        treeSetTasks.clear();
    }

    // удаление всех простых задач из менеджера
    @Override
    public void clearTasks() {
        for (Integer id : tasks.keySet()) {
            historyManager.remove(id);
            treeSetTasks.remove(tasks.get(id));
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
        // удаляем подзадачи из эпиков и множества задач
        for (Subtask subtask : subtasks.values()) {
            subtask.getEpic().deleteSubtask(subtask);
            treeSetTasks.remove(subtask);
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

    // получение задачи по идентификатору без обновления истории
    @Override
    public Task getTaskWithoutHistory(Integer id) {
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
    @Override
    public int addTask(Task o, Integer id) {

        boolean newTask = false;
        if (id == null) {
            id = 0;
        }
        if (id == 0) {
            id = incCurrentId();   // увеличиваем id на единицу, если задача новая
            newTask = true;
        }
        o.setTaskId(id);           // присваиваем задаче id

        //проверяем добавляемую задачу на пересечение во времени
        Optional<Task> intersectionTask = getPrioritizedTasks().stream()
                .filter(existTask -> doesTaskIntersect(existTask, o))
                .findFirst();

        if (intersectionTask.isPresent()) {
            if (intersectionTask.get().getTaskId() != id) {
                throw new AddTaskException("Пересечение по времени с существующей задачей ID="
                        + intersectionTask.get().getTaskId());
            }
        }

        if (o.getClass() == Task.class) {
            if (!newTask) {
                if (treeSetTasks.contains(tasks.get(id))) {
                    treeSetTasks.remove(tasks.get(id));
                }
            }
            tasks.put(id,o);
            treeSetTasks.add(o);
        }

        else if (o.getClass() == Subtask.class) {
            if (!newTask) {
                if (treeSetTasks.contains(subtasks.get(id))) {
                    treeSetTasks.remove(subtasks.get(id));
                }
            }
            subtasks.put(id, (Subtask) o);
            treeSetTasks.add(o);
            if (!newTask) {
                ((Subtask) o).getEpic().deleteSubtask(((Subtask) o));
            }
            ((Subtask) o).getEpic().addSubtaskEpic(((Subtask) o));
        }

        else if (o.getClass() == Epic.class) {
            epics.put(id, (Epic) o);
        }
        return id;
    }

    // удаление задачи по идентификатору (пункт 2.6)
    @Override
    public void deleteTask(Integer id) {
        if (tasks.containsKey(id)) {
            if (treeSetTasks.contains(tasks.get(id))) {
                treeSetTasks.remove(tasks.get(id));
            }
            tasks.remove(id);
            historyManager.remove(id);
        }
        else if (subtasks.containsKey(id)) {      // ищем id в подзадачах и если есть, то удаляем также из эпика
            if (treeSetTasks.contains(subtasks.get(id))) {
                treeSetTasks.remove(subtasks.get(id));
            }
            Epic epic = subtasks.get(id).getEpic();
            epic.deleteSubtask(subtasks.get(id));
            subtasks.remove(id);
            historyManager.remove(id);
        }
        else if (epics.containsKey(id)) {         // ищем id в эпиках и если есть, то удаляем также подзадачи
            for (Subtask subtask: epics.get(id).getSubtasksEpic()) {
                if (treeSetTasks.contains(subtask)) {
                    treeSetTasks.remove(subtask);
                }
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
        List<Task> actualHistoryList = new ArrayList<>();
        List<Task> historyList = historyManager.getHistory();
        if (historyList != null) {
            int i = 0; // вспомогательная переменная для вывода стрелочки
            for (Task historyObject : historyList) {
                if (i != 0) {
                    System.out.print(" -> "); // печатаем стрелочку, если не первый элемент списка
                }
                i++;
                System.out.print(historyObject.getTaskId());
                actualHistoryList.add(getTaskWithoutHistory(historyObject.getTaskId()));
            }
            System.out.println("");
        }
        return actualHistoryList;
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<Task> (treeSetTasks);
    }

    private boolean doesTaskIntersect(Task task1, Task task2){

        if (task1.getStartTime().isEmpty() || task2.getStartTime().isEmpty()) {
            return false;
        }

        if (task1.getTaskId() == task2.getTaskId()) {  // если это обновление задачи, то не сравнивать
           // Обновление задачи - Задачи не пересекаются
            return false;
        }

        if ((task1.getStartTime().get().isAfter(task2.getEndTime().get())
            || (task1.getEndTime().get().isBefore(task2.getStartTime().get())))) {
            // Задачи не пересекаются
            return false;
        }
        // Задачи пересекаются
        return true;
    }
}