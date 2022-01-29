import java.util.ArrayList;
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
            getEpicSubtasks(epic.getTaskId());
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
    public void getTask(int id) {
        if (tasks.containsKey(id)) {
            System.out.println(tasks.get(id));
        }
        else if (subtasks.containsKey(id)) {
            System.out.println(subtasks.get(id));
        }
        else if (epics.containsKey(id)) {
            System.out.println(epics.get(id));
        }
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
            if (newTask) {
                epics.get(((Subtask) o).getEpicId()).addSubtaskId(id);
            }
            // после добавления или обновления подзадачи необходимо уточнить статус эпика
            checkEpicStatus(subtasks.get(id).getEpicId());
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
            Integer epicId = subtasks.get(id).getEpicId();
            epics.get(epicId).deleteSubtaskId(id);
            subtasks.remove(id);
            checkEpicStatus(epicId); // уточняем статус эпика после удаления
        }
        else if (epics.containsKey(id)) {         // ищем id в эпиках и если есть, то удаляем также подзадачи
            for (Integer subtaskId : epics.get(id).getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
            epics.remove(id);
        }
    }

    // получение списка подзадач определеннного эпика (пункт 3.1)
    private void getEpicSubtasks(Integer id) {
        if (epics.containsKey(id)) {
            for (Integer subtaskId : epics.get(id).getSubtaskIds()) {
                System.out.println(subtasks.get(subtaskId));
            }
        }

    }

    // проверка статуса эпика при изменении подзадач
    private void checkEpicStatus(Integer id) {
        String epicStatus;   // текущий статус эпика
        String firstSubtaskStatus;  // статус первой подзадачи в эпике

        epicStatus = epics.get(id).getStatus();
        if (epics.get(id).getSubtaskIds().isEmpty()) {
            epicStatus = "NEW";    // эпик новый, если не содержит подзадач
        }
        else {
            firstSubtaskStatus = subtasks.get(epics.get(id).getSubtaskIds().get(0)).getStatus();
            for (Integer subtaskId : epics.get(id).getSubtaskIds()) {
                String statusSubtask = subtasks.get(subtaskId).getStatus();
                // если статус текущей подзадачи "IN_PROGRESS" или статус не соответствует первой подзадаче эпика,
                // то статус эпика "IN_PROGRESS"
                if (statusSubtask.equals("IN_PROGRESS") || !statusSubtask.equals(firstSubtaskStatus)) {
                    epicStatus = "IN_PROGRESS";
                    break;
                }
                epicStatus = firstSubtaskStatus;
            }
        }
        epics.get(id).setStatus(epicStatus);
    }
}