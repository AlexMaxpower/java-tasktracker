import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import static org.junit.jupiter.api.Assertions.*;
import tracker.controllers.HistoryManager;
import tracker.controllers.InMemoryTaskManager;
import tracker.controllers.TaskManager;
import tracker.exceptions.AddTaskException;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.util.Managers;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static tracker.model.Status.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    T taskManager;

    Task task = new Task("task1", "descriptionOfTask1", NEW);

    public TaskManagerTest(T taskManager) {
        this.taskManager = taskManager;
    }


    @Test
    // тест на возврат статуса NEW у эпика при пустом списке подзадач
    public void shouldReturnEpicStatusAsNewWhenEmptyListOfSubtask(){
        Task epic = new Epic("epic1", "descriptionOfEpic1", DONE);
        final int epicId = taskManager.addTask(epic, 0);
        assertEquals(NEW, taskManager.getTask(epicId).getStatus(), "У эпика должен быть статус NEW");
    }

    @Test
    // тест на возврат статуса NEW у эпика, если у всех подзадач статус NEW
    public void shouldReturnEpicStatusAsNewWhenAllSubtasksInStatusNew(){
        Task epic = new Epic("epic1", "descriptionOfEpic1", DONE);  // DONE - специально
        int epicId = taskManager.addTask(epic, 0);
        Task subtask1 = new Subtask("subtask1","descriptionOfSubtask1",NEW,(Epic) epic);
        taskManager.addTask(subtask1, 0);
        Task subtask2 = new Subtask("subtask2","descriptionOfSubtask2",NEW,(Epic) epic);
        taskManager.addTask(subtask2, 0);
        Task subtask3 = new Subtask("subtask3","descriptionOfSubtask3",NEW,(Epic) epic);
        taskManager.addTask(subtask3, 0);
        assertEquals(NEW, taskManager.getTask(epicId).getStatus(), "У эпика должен быть статус NEW");
    }

    @Test
    // тест на возврат статуса DONE у эпика, если у всех подзадач статус DONE
    public void shouldReturnEpicStatusAsDoneWhenAllSubtasksInStatusDone(){
        Task epic = new Epic("epic1", "descriptionOfEpic1", NEW);
        int epicId = taskManager.addTask(epic, 0);
        Task subtask1 = new Subtask("subtask1","descriptionOfSubtask1",DONE,(Epic) epic);
        taskManager.addTask(subtask1, 0);
        Task subtask2 = new Subtask("subtask2","descriptionOfSubtask2",DONE,(Epic) epic);
        taskManager.addTask(subtask2, 0);
        Task subtask3 = new Subtask("subtask3","descriptionOfSubtask3",DONE,(Epic) epic);
        taskManager.addTask(subtask3, 0);
        assertEquals(DONE, taskManager.getTask(epicId).getStatus(), "У эпика должен быть статус DONE");
    }

    @Test
    // тест на возврат статуса IN_PROGRESS у эпика, если у подзадач статусы DONE и NEW
    public void shouldReturnEpicStatusAsInProgressWhenSubtasksInStatusDoneOrNew(){
        Task epic = new Epic("epic1", "descriptionOfEpic1", NEW);
        int epicId = taskManager.addTask(epic, 0);
        Task subtask1 = new Subtask("subtask1","descriptionOfSubtask1",DONE,(Epic) epic);
        taskManager.addTask(subtask1, 0);
        Task subtask2 = new Subtask("subtask2","descriptionOfSubtask2",NEW,(Epic) epic);
        taskManager.addTask(subtask2, 0);
        Task subtask3 = new Subtask("subtask3","descriptionOfSubtask3",DONE,(Epic) epic);
        taskManager.addTask(subtask3, 0);
        assertEquals(IN_PROGRESS, taskManager.getTask(epicId).getStatus(),
                "У эпика должен быть статус IN_PROGRESS");
    }

    @Test
    // тест на возврат статуса IN_PROGRESS у эпика, если всех подзадач статусы IN_PROGRESS
    public void shouldReturnEpicStatusAsInProgressWhenAllSubtasksInStatusInProgress(){
        Task epic = new Epic("epic1", "descriptionOfEpic1", NEW);
        int epicId = taskManager.addTask(epic, 0);
        Task subtask1 = new Subtask("subtask1","descriptionOfSubtask1",IN_PROGRESS,(Epic) epic);
        taskManager.addTask(subtask1, 0);
        Task subtask2 = new Subtask("subtask2","descriptionOfSubtask2",IN_PROGRESS,(Epic) epic);
        taskManager.addTask(subtask2, 0);
        Task subtask3 = new Subtask("subtask3","descriptionOfSubtask3",IN_PROGRESS,(Epic) epic);
        taskManager.addTask(subtask3, 0);
        assertEquals(IN_PROGRESS, taskManager.getTask(epicId).getStatus(),
                "У эпика должен быть статус IN_PROGRESS");
    }

    @Test
    // тест на возврат статуса IN_PROGRESS у эпика, если у подзадач разные статусы
    public void shouldReturnEpicStatusAsInProgressWhenSubtasksInDifferentStatus(){
        Task epic = new Epic("epic1", "descriptionOfEpic1", NEW);
        int epicId = taskManager.addTask(epic, 0);
        Task subtask1 = new Subtask("subtask1","descriptionOfSubtask1",NEW,(Epic) epic);
        taskManager.addTask(subtask1, 0);
        Task subtask2 = new Subtask("subtask2","descriptionOfSubtask2",IN_PROGRESS,(Epic) epic);
        taskManager.addTask(subtask2, 0);
        Task subtask3 = new Subtask("subtask3","descriptionOfSubtask3",DONE,(Epic) epic);
        taskManager.addTask(subtask3, 0);
        assertEquals(IN_PROGRESS, taskManager.getTask(epicId).getStatus(),
                "У эпика должен быть статус IN_PROGRESS");
    }

    @Test
    // тест getAllTasks() при нормальном режиме работы
    public void getAllTasksShouldReturnListOfTasks(){
        taskManager.clearAll();
        Task task1 = new Task("task1", "descriptionOfTask1", NEW);
        taskManager.addTask(task1, 0);
        Task task2 = new Task("task2", "descriptionOfTask2", DONE);
        taskManager.addTask(task2, 0);
        Task epic1 = new Epic("epic1", "descriptionOfEpic1", NEW);
        taskManager.addTask(epic1, 0);
        LocalDateTime startTime = LocalDateTime.of(2022,04,01,1,20);
        Duration duration = Duration.ofMinutes(20);
        Task subtask1 = new Subtask("subtask1","descriptionOfSubtask1",NEW, (Epic) epic1,
                startTime, duration);
        taskManager.addTask(subtask1, 0);
        Task subtask2 = new Subtask("subtask2","descriptionOfSubtask2",IN_PROGRESS,(Epic) epic1);
        taskManager.addTask(subtask2, 0);
        List<Task> list = new ArrayList<>(taskManager.getAllTasks());
        assertEquals(5, list.size(),"В списке должно быть 5 объектов");
        Subtask subTaskExp = (Subtask) list.get(list.indexOf(subtask1));
        assertEquals(epic1, subTaskExp.getEpic(),"В сабтаске должен быть эпик");
        assertEquals(IN_PROGRESS, list.get(list.indexOf(epic1)).getStatus(),
                "Статус у эпика должен быть IN_PROGRESS");
    }

    @Test
    // тест getAllTasks() при пустом списке задач в менеджере
    public void getAllTasksShouldReturnEmptyListTasksWhenNoTasks(){
        taskManager.clearAll();
        List<Task> list = new ArrayList<>(taskManager.getAllTasks());
        assertTrue(list.isEmpty(),"Список задач должен быть пустым");
    }

    @Test
    // тест getTasks() при нормальном режиме работы
    public void getTasksShouldReturnListOfTasksOnly(){
        taskManager.clearAll();
        Task task1 = new Task("task1", "descriptionOfTask1", NEW);
        taskManager.addTask(task1, 0);
        Task task2 = new Task("task2", "descriptionOfTask2", DONE);
        taskManager.addTask(task2, 0);
        Task epic1 = new Epic("epic1", "descriptionOfEpic1", NEW);
        taskManager.addTask(epic1, 0);
        Task subtask1 = new Subtask("subtask1","descriptionOfSubtask1",NEW,(Epic) epic1);
        taskManager.addTask(subtask1, 0);
        Task subtask2 = new Subtask("subtask2","descriptionOfSubtask2",IN_PROGRESS,(Epic) epic1);
        taskManager.addTask(subtask2, 0);
        List<Task> list = new ArrayList<>(taskManager.getTasks());
        assertEquals(2, list.size(),"В списке должно быть 2 задачи");
        assertEquals(Task.class, list.get(0).getClass(),
                "Первый объект должен быть таском");
        assertEquals(Task.class, list.get(1).getClass(),
                "Второй объект должен быть таском");
    }

    @Test
    // тест getTasks() при пустом списке задач в менеджере
    public void getTasksShouldReturnEmptyListTasksWhenNoTasks(){
        taskManager.clearAll();
        Task epic1 = new Epic("epic1", "descriptionOfEpic1", NEW);
        taskManager.addTask(epic1, 0);
        Task subtask1 = new Subtask("subtask1","descriptionOfSubtask1",NEW,(Epic) epic1);
        taskManager.addTask(subtask1, 0);
        Task subtask2 = new Subtask("subtask2","descriptionOfSubtask2",IN_PROGRESS,(Epic) epic1);
        taskManager.addTask(subtask2, 0);
        List<Task> list = new ArrayList<>(taskManager.getTasks());
        assertTrue(list.isEmpty(),"Список задач должен быть пустым");
    }

    @Test
    // тест getEpics() при нормальном режиме работы
    public void getEpicsShouldReturnListOfEpicsOnly(){
        taskManager.clearAll();
        Task task1 = new Task("task1", "descriptionOfTask1", NEW);
        taskManager.addTask(task1, 0);
        Task task2 = new Task("task2", "descriptionOfTask2", DONE);
        taskManager.addTask(task2, 0);
        Task epic1 = new Epic("epic1", "descriptionOfEpic1", NEW);
        taskManager.addTask(epic1, 0);
        Task subtask1 = new Subtask("subtask1","descriptionOfSubtask1",NEW,(Epic) epic1);
        taskManager.addTask(subtask1, 0);
        Task subtask2 = new Subtask("subtask2","descriptionOfSubtask2",IN_PROGRESS,(Epic) epic1);
        taskManager.addTask(subtask2, 0);
        List<Task> list = new ArrayList<>(taskManager.getEpics());
        assertEquals(1, list.size(),"В списке должен быть 1 эпик");
        assertEquals(Epic.class, list.get(0).getClass(),
                "Первый объект должен быть эпиком");
    }

    @Test
    // тест getEpics() при пустом списке эпиков
    public void getEpicsShouldReturnEmptyListEpicsWhenNoEpics(){
        taskManager.clearAll();
        Task task1 = new Task("task1", "descriptionOfTask1", NEW);
        taskManager.addTask(task1, 0);
        Task task2 = new Task("task2", "descriptionOfTask2", DONE);
        taskManager.addTask(task2, 0);
        List<Task> list = new ArrayList<>(taskManager.getEpics());
        assertTrue(list.isEmpty(),"Список задач должен быть пустым");
    }

    @Test
    // тест getSubtasks() при нормальном режиме работы
    public void getSubtasksShouldReturnListOfSubtasksOnly(){
        taskManager.clearAll();
        Task task1 = new Task("task1", "descriptionOfTask1", NEW);
        taskManager.addTask(task1, 0);
        Task epic1 = new Epic("epic1", "descriptionOfEpic1", NEW);
        taskManager.addTask(epic1, 0);
        Task subtask1 = new Subtask("subtask1","descriptionOfSubtask1",NEW,(Epic) epic1);
        taskManager.addTask(subtask1, 0);
        Task subtask2 = new Subtask("subtask2","descriptionOfSubtask2",IN_PROGRESS,(Epic) epic1);
        taskManager.addTask(subtask2, 0);
        List<Task> list = new ArrayList<>(taskManager.getSubtasks());
        assertEquals(2, list.size(),"В списке должно быть 2 подзадачи");
        Subtask subTaskExp1 = (Subtask) list.get(list.indexOf(subtask1));
        assertEquals(epic1, subTaskExp1.getEpic(),"В сабтаске1 должен быть эпик");
        assertEquals(Subtask.class, list.get(0).getClass(),
                "Первый объект должен быть сабтаском");
        assertEquals(Subtask.class, list.get(1).getClass(),
                "Второй объект должен быть сабтаском");
    }

    @Test
    // тест getSubtasks() при пустом списке подзадач
    public void getSubtasksShouldReturnEmptyListSubtasksWhenNoSubtasks(){
        taskManager.clearAll();
        Task task1 = new Task("task1", "descriptionOfTask1", NEW);
        taskManager.addTask(task1, 0);
        Task epic1 = new Epic("epic1", "descriptionOfEpic1", NEW);
        taskManager.addTask(epic1, 0);
        List<Task> list = new ArrayList<>(taskManager.getSubtasks());
        assertTrue(list.isEmpty(),"Список подзадач должен быть пустым");
    }

    @Test
    // тест getEpicSubtasks() при нормальном режиме работы
    public void getEpicSubtasksShouldReturnListOfEpicSubtasksOnly(){
        taskManager.clearAll();
        Task epic1 = new Epic("epic1", "descriptionOfEpic1", NEW);
        taskManager.addTask(epic1, 0);
        LocalDateTime startTime = LocalDateTime.of(2022,04,01,1,20);
        Duration duration = Duration.ofMinutes(20);
        Task subtask1 = new Subtask("subtask1","descriptionOfSubtask1",NEW, (Epic) epic1,
                startTime, duration);
        taskManager.addTask(subtask1, 0);
        Task subtask2 = new Subtask("subtask2","descriptionOfSubtask2",IN_PROGRESS,(Epic) epic1);
        taskManager.addTask(subtask2, 0);
        List<Task> list = new ArrayList<>(taskManager.getEpicSubtasks((Epic) epic1));
        assertEquals(2, list.size(),"В списке должно быть 2 подзадачи");
        Subtask subTaskExp1 = (Subtask) list.get(0);
        assertEquals(epic1, subTaskExp1.getEpic(),"В сабтаске1 должен быть эпик1");
        Subtask subTaskExp2 = (Subtask) list.get(1);
        assertEquals(epic1, subTaskExp2.getEpic(),"В сабтаске2 должен быть эпик1");
        assertEquals(Optional.of(LocalDateTime.of(2022,04,01,1,40)),
                taskManager.getEpics().get(0).getEndTime(),
                "Время завершения эпика должно быть 2022-04-01 1:40");
    }

    @Test
    // тест getEpicSubtasks() при пустом списке подзадач у эпика
    public void getEpicSubtasksShouldReturnEmptyListSubtasksWhenEpicDoNotHaveSubtasks(){
        taskManager.clearAll();
        Task epic1 = new Epic("epic1", "descriptionOfEpic1", NEW);
        taskManager.addTask(epic1, 0);
        Task subtask1 = new Subtask("subtask1","descriptionOfSubtask1",NEW,(Epic) epic1);
        taskManager.addTask(subtask1, 0);
        Task subtask2 = new Subtask("subtask2","descriptionOfSubtask2",IN_PROGRESS,(Epic) epic1);
        taskManager.addTask(subtask2, 0);
        Task epic2 = new Epic("epic2", "descriptionOfEpic2", NEW);
        taskManager.addTask(epic2, 0);
        List<Task> list = new ArrayList<>(taskManager.getEpicSubtasks((Epic) epic2));
        assertTrue(list.isEmpty(),"Список подзадач должен быть пустым");
    }

    @Test
    // тест clearAll() - удаление всех простых задач, подзадач и эпиков
    public void clearAllShouldDeleteAllTasksInAllManagers(){
        taskManager.clearAll();
        Task task1 = new Task("task1", "descriptionOfTask1", NEW);
        taskManager.getTask(taskManager.addTask(task1, 0));
        Task task2 = new Task("task2", "descriptionOfTask2", DONE);
        taskManager.getTask(taskManager.addTask(task2, 0));
        Task epic = new Epic("epic1", "descriptionOfEpic1", NEW);
        taskManager.getTask(taskManager.addTask(epic, 0));
        Task subtask1 = new Subtask("subtask1","descriptionOfSubtask1",NEW,(Epic) epic);
        taskManager.getTask(taskManager.addTask(subtask1, 0));
        Task subtask2 = new Subtask("subtask2","descriptionOfSubtask2",IN_PROGRESS,(Epic) epic);
        taskManager.getTask(taskManager.addTask(subtask2, 0));
        taskManager.clearAll();
        List<Task> list = new ArrayList<>(taskManager.getAllTasks());
        assertTrue(list.isEmpty(),"Список задач в менеджере должен быть пустым");
        list = new ArrayList<>(taskManager.history());
        assertTrue(list.isEmpty(),"Список задач в истории должен быть пустым");
    }

    @Test
    // тест clearTasks() - удаление всех простых задач
    public void clearTasksShouldDeleteTasksInAllManagers(){
        taskManager.clearAll();
        Task task1 = new Task("task1", "descriptionOfTask1", NEW);
        taskManager.getTask(taskManager.addTask(task1, 0)); // добавляем задачу и заполняем историю
        Task task2 = new Task("task2", "descriptionOfTask2", DONE);
        taskManager.getTask(taskManager.addTask(task2, 0));
        taskManager.clearTasks();
        List<Task> list = new ArrayList<>(taskManager.getTasks());
        assertTrue(list.isEmpty(),"Список задач в менеджере должен быть пустым");
        list = new ArrayList<>(taskManager.history());
        assertTrue(list.isEmpty(),"Список задач в истории должен быть пустым");
    }

    @Test
    // тест clearSubtasks() - удаление всех подзадач из менеджера
    public void clearSubtasksShouldDeleteSubtasksInAllManagersAndEpics(){
        taskManager.clearAll();
        Task epic = new Epic("epic1", "descriptionOfEpic1", NEW);
        taskManager.getTask(taskManager.addTask(epic, 0)); // добавляем задачу и заполняем историю
        Task subtask1 = new Subtask("subtask1","descriptionOfSubtask1",NEW,(Epic) epic);
        taskManager.getTask(taskManager.addTask(subtask1, 0));
        Task subtask2 = new Subtask("subtask2","descriptionOfSubtask2",IN_PROGRESS,(Epic) epic);
        taskManager.getTask(taskManager.addTask(subtask2, 0));
        taskManager.clearSubtasks();
        List<Task> list = new ArrayList<>(taskManager.getSubtasks());
        assertTrue(list.isEmpty(),"Список подзадач в менеджере должен быть пустым");
        assertEquals(1, taskManager.history().size(), "В истории должен остаться только эпик");
        list = new ArrayList<>(taskManager.getEpicSubtasks((Epic) epic));
        assertTrue(list.isEmpty(),"У эпика не должно быть подзадач");
    }

    @Test
    // тест clearEpics() - удаление всех эпиков и подзадач из менеджера
    public void clearEpicsShouldDeleteEpicsAndSubtasksInAllManagers(){
        taskManager.clearAll();
        Task task1 = new Task("task1", "descriptionOfTask1", NEW);
        taskManager.getTask(taskManager.addTask(task1, 0)); // добавляем задачу и заполняем историю
        Task task2 = new Task("task2", "descriptionOfTask2", DONE);
        taskManager.getTask(taskManager.addTask(task2, 0));
        Task epic = new Epic("epic1", "descriptionOfEpic1", NEW);
        taskManager.getTask(taskManager.addTask(epic, 0)); // добавляем задачу и заполняем историю
        Task subtask1 = new Subtask("subtask1","descriptionOfSubtask1",NEW,(Epic) epic);
        taskManager.getTask(taskManager.addTask(subtask1, 0));
        Task subtask2 = new Subtask("subtask2","descriptionOfSubtask2",IN_PROGRESS,(Epic) epic);
        taskManager.getTask(taskManager.addTask(subtask2, 0));
        taskManager.clearEpics();
        List<Task> list = new ArrayList<>(taskManager.getEpics());
        assertTrue(list.isEmpty(),"Список эпиков в менеджере должен быть пустым");
        list = new ArrayList<>(taskManager.getSubtasks());
        assertTrue(list.isEmpty(),"Список подзадач в менеджере должен быть пустым");
        assertEquals(2, taskManager.history().size(), "В истории должны остаться 2 задачи");
    }

    @Test
    // тест getTask() на получение задачи при нормальной работе
    public void getTaskShouldReturnTask(){
        taskManager.clearAll();
        LocalDateTime startTime = LocalDateTime.of(2022,04,01,1,20);
        Duration duration = Duration.ofMinutes(20);
        Task task1 = new Task("Test addNewTask", "Test addNewTask description", NEW,
                startTime, duration);
        final int taskId = taskManager.addTask(task1,0);
        final Task savedTask = taskManager.getTask(taskId);
        assertEquals(task1, savedTask, "Задачи должны совпадать");
        assertEquals(task1, taskManager.history().get(0), "В истории должна быть задача");
    }

    @Test
    // тест getTask() на получение задачи при недопустимом Id
    public void getTaskShouldReturnNullWhenIdNotCorrect(){
        taskManager.clearAll();
        LocalDateTime startTime = LocalDateTime.of(2022,04,01,1,20);
        Duration duration = Duration.ofMinutes(20);
        Task task1 = new Task("Test addNewTask", "Test addNewTask description", NEW,
                startTime, duration);
        taskManager.addTask(task1,0);
        assertNull(taskManager.getTask(100), "getTask() должен вернуть null");
        assertTrue(taskManager.history().isEmpty(), "История должна быть пустой");
    }

    @Test
    // тест getTask() на получение задачи при пустом Id
    public void getTaskShouldReturnNullWhenIdNull(){
        taskManager.clearAll();
        LocalDateTime startTime = LocalDateTime.of(2022,04,01,1,20);
        Duration duration = Duration.ofMinutes(20);
        Task task1 = new Task("Test addNewTask", "Test addNewTask description", NEW,
                startTime, duration);
        taskManager.addTask(task1,0);
        assertNull(taskManager.getTask(null), "getTask() должен вернуть null");
        assertTrue(taskManager.history().isEmpty(), "История должна быть пустой");
    }

    @Test
    // тест getTask() на получение задачи при отсутствии задач
    public void getTaskShouldReturnNullWhenManagerEmpty(){
        taskManager.clearAll();
        assertNull(taskManager.getTask(1), "getTask() должен вернуть null");
        assertTrue(taskManager.history().isEmpty(), "История должна быть пустой");
    }


    @Test
    // тест addTask() на добавление задачи без времени и продолжительности
    public void shouldAddTaskWithoutTimeAndDuration(){
        taskManager.clearAll();
        Task task1 = new Task("Test addNewTask", "Test addNewTask description", NEW);
        final int taskId = taskManager.addTask(task1,0);
        final Task savedTask = taskManager.getTask(taskId);
        assertNotNull(savedTask, "Задача должна быть сохранена");
        assertEquals(task1, savedTask, "Задачи должны совпадать");
        final List<Task> tasks = taskManager.getTasks();
        assertNotNull(tasks, "Задачи должны возвращаться");
        assertEquals(1, tasks.size(), "Должна быть одна задача");
        assertEquals(task1, tasks.get(0), "Задачи должны совпадать");
    }

    @Test
    // тест addTask() на добавление задачи с пустым Id
    public void shouldAddTaskWithNullId(){
        taskManager.clearAll();
        Task task1 = new Task("Test addNewTask", "Test addNewTask description", NEW);
        final int taskId = taskManager.addTask(task1,null);
        final Task savedTask = taskManager.getTask(taskId);
        assertNotNull(savedTask, "Задача должна быть сохранена");
        assertEquals(task1, savedTask, "Задачи должны совпадать");
        final List<Task> tasks = taskManager.getTasks();
        assertNotNull(tasks, "Задачи должны возвращаться");
        assertEquals(1, tasks.size(), "Должна быть одна задача");
        assertEquals(task1, tasks.get(0), "Задачи должны совпадать");
    }


    @Test
    // тест addTask() на добавление задачи со временем и продолжительностью
    public void shouldAddTaskWithTimeAndDuration(){
        taskManager.clearAll();
        LocalDateTime startTime = LocalDateTime.of(2022,04,01,1,20);
        Duration duration = Duration.ofMinutes(20);
        Task task1 = new Task("Test addNewTask", "Test addNewTask description", NEW,
                startTime, duration);
        final int taskId = taskManager.addTask(task1,0);
        final Task savedTask = taskManager.getTask(taskId);
        assertNotNull(savedTask, "Задача должна быть сохранена");
        assertEquals(task1, savedTask, "Задачи должны совпадать");
        final List<Task> tasks = taskManager.getTasks();
        assertNotNull(tasks, "Задачи должны возвращаться");
        assertEquals(1, tasks.size(), "Должна быть одна задача");
        assertEquals(task1, tasks.get(0), "Задачи должны совпадать");
    }

    @Test
    // тест addTask() на корректное редактирование задачи
    public void addTaskShouldCorrectEditTask(){
        taskManager.clearAll();
        LocalDateTime startTime = LocalDateTime.of(2022,04,01,1,20);
        Duration duration = Duration.ofMinutes(20);
        Task task = new Task("Test addNewTask", "Test addNewTask description", NEW,
                startTime, duration);
        final int taskId = taskManager.addTask(task,0);
        Task taskEdit = new Task("Test addNewTask", "Test EditTask description", NEW,
                startTime, duration);
        final int taskEditId = taskManager.addTask(taskEdit,taskId);
        final Task savedTask = taskManager.getTask(taskEditId);
        assertNotNull(savedTask, "Задача должна быть сохранена");
        assertEquals(taskEdit, savedTask, "Сохраненная задача и отредактированная должны совпадать");
        assertEquals(taskId, taskEditId, "Id задач должны совпадать");
        assertNotEquals(task, taskEdit, "Начальная задача и отредактированная не должны совпадать");
    }

    @Test
    // тест deleteTask() на корректное удаление задачи
    public void deleteTaskShouldDeleteTaskInManagerAndHistory(){
        taskManager.clearAll();
        Task task1 = new Task("task1", "descriptionOfTask1", NEW);
        taskManager.getTask(taskManager.addTask(task1, 0)); // добавляем задачу и заполняем историю
        Task task2 = new Task("task2", "descriptionOfTask2", DONE);
        taskManager.getTask(taskManager.addTask(task2, 0));
        taskManager.deleteTask(task1.getTaskId());
        assertEquals(1, taskManager.getAllTasks().size(), "должна остаться одна задача");
        taskManager.deleteTask(task2.getTaskId());
        assertTrue(taskManager.history().isEmpty(), "История должна быть пустой");
        assertTrue(taskManager.getAllTasks().isEmpty(), "Список задач должен быть пустым");
    }

    @Test
    // тест deleteTask() на корректное удаление подзадачи из менеджера, истории и эпика
    public void deleteTaskShouldDeleteSubtaskInManagerAndHistoryAndEpic(){
        taskManager.clearAll();
        Task epic = new Epic("epic1", "descriptionOfEpic1", NEW);
        taskManager.getTask(taskManager.addTask(epic, 0)); // добавляем задачу и заполняем историю
        Task subtask1 = new Subtask("subtask1","descriptionOfSubtask1",NEW,(Epic) epic);
        taskManager.getTask(taskManager.addTask(subtask1, 0));
        Task subtask2 = new Subtask("subtask2","descriptionOfSubtask2",IN_PROGRESS,(Epic) epic);
        taskManager.getTask(taskManager.addTask(subtask2, 0));
        taskManager.deleteTask(subtask1.getTaskId());
        List<Task> list = new ArrayList<>(taskManager.getSubtasks());
        assertEquals(1, list.size(), "В списке подзадач должна быть одна подзадача");
        assertEquals(2, taskManager.history().size(), "В истории должны остаться 2 задачи");
        list = new ArrayList<>(taskManager.getEpicSubtasks((Epic) epic));
        assertEquals(1, list.size(),"В списке подзадач эпика должна быть 1 подзадача");
    }

    @Test
    // тест add() на добавление задачи в историю
    void addShouldAddHistory() {
        taskManager.clearAll();
        LocalDateTime startTime = LocalDateTime.of(2022,04,01,1,20);
        Duration duration = Duration.ofMinutes(20);
        Task task1 = new Task("Test addNewTask", "Test addNewTask description", NEW,
                startTime, duration);
        HistoryManager historyManager = Managers.getDefaultHistory();
        historyManager.add(task1);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История должна быть заполнена.");
        assertEquals(1, history.size(), "В истории должна быть одна задача");
        assertEquals(task1.getTaskId(), history.get(0).getTaskId(), "ID задачи в истории должна быть равны");
    }

    @Test
    // тест history() на возврат пустого списка, когда история пуста
    void historyShouldReturnEmptyListWhenHistoryEmpty() {
        taskManager.clearAll();
        LocalDateTime startTime = LocalDateTime.of(2022,04,01,1,20);
        Duration duration = Duration.ofMinutes(20);
        Task task1 = new Task("Test addNewTask", "Test addNewTask description", NEW,
                startTime, duration);
        HistoryManager historyManager = Managers.getDefaultHistory();
        final List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой");
    }

    @Test
    // history() должен возвращать список без дублей
    void historyShouldReturnOnlyOneTaskInstance() {
        taskManager.clearAll();
        Task task1 = new Task("task1","descriptionOfTask1",NEW);
        taskManager.getTask(taskManager.addTask(task1, 0));
        taskManager.getTask(0);
        List<Task> list = new ArrayList<>(taskManager.history()); // задействует getHistory
        assertEquals(1, list.size(), "Задачи в истории не должны дублироваться");
    }

    @Test
    // тест на удаление первой задачи в списке из истории
    void removeShouldDeleteFirstTaskInHistory() {
        taskManager.clearAll();
        Task task1 = new Task("task1", "descriptionOfTask1", NEW);
        taskManager.getTask(taskManager.addTask(task1, 0)); // добавляем задачу и заполняем историю
        Task task2 = new Task("task2", "descriptionOfTask2", DONE);
        taskManager.getTask(taskManager.addTask(task2, 0));
        Task epic = new Epic("epic1", "descriptionOfEpic1", NEW);
        taskManager.getTask(taskManager.addTask(epic, 0)); // добавляем задачу и заполняем историю
        Task subtask1 = new Subtask("subtask1","descriptionOfSubtask1",NEW,(Epic) epic);
        taskManager.getTask(taskManager.addTask(subtask1, 0));
        Task subtask2 = new Subtask("subtask2","descriptionOfSubtask2",IN_PROGRESS,(Epic) epic);
        taskManager.getTask(taskManager.addTask(subtask2, 0));
        taskManager.deleteTask(task1.getTaskId());
        List<Task> list = new ArrayList<>(taskManager.history());
        assertEquals(4, list.size(), "В истории должно быть 4 задачи");
        assertFalse(list.contains(task1), "Первая задача должна быть удалена");
    }

    @Test
    // тест на удаление последней задачи из истории
    void removeShouldDeleteLastTaskInHistory() {
        taskManager.clearAll();
        Task task1 = new Task("task1", "descriptionOfTask1", NEW);
        taskManager.getTask(taskManager.addTask(task1, 0)); // добавляем задачу и заполняем историю
        Task task2 = new Task("task2", "descriptionOfTask2", DONE);
        taskManager.getTask(taskManager.addTask(task2, 0));
        Task epic = new Epic("epic1", "descriptionOfEpic1", NEW);
        taskManager.getTask(taskManager.addTask(epic, 0)); // добавляем задачу и заполняем историю
        Task subtask1 = new Subtask("subtask1","descriptionOfSubtask1",NEW,(Epic) epic);
        taskManager.getTask(taskManager.addTask(subtask1, 0));
        Task subtask2 = new Subtask("subtask2","descriptionOfSubtask2",IN_PROGRESS,(Epic) epic);
        taskManager.getTask(taskManager.addTask(subtask2, 0));
        taskManager.deleteTask(subtask2.getTaskId());
        List<Task> list = new ArrayList<>(taskManager.history());
        assertEquals(4, list.size(), "В истории должно быть 4 задачи");
        assertFalse(list.contains(subtask2), "Последняя задача должна быть удалена");
    }

    @Test
    // тест на удаление задачи в середине списка из истории
    void removeShouldDeleteMiddleTaskInHistory() {
        taskManager.clearAll();
        Task task1 = new Task("task1", "descriptionOfTask1", NEW);
        taskManager.getTask(taskManager.addTask(task1, 0)); // добавляем задачу и заполняем историю
        Task epic = new Epic("epic1", "descriptionOfEpic1", NEW);
        taskManager.getTask(taskManager.addTask(epic, 0)); // добавляем задачу и заполняем историю
        Task task2 = new Task("task2", "descriptionOfTask2", DONE);
        taskManager.getTask(taskManager.addTask(task2, 0));
        Task subtask1 = new Subtask("subtask1","descriptionOfSubtask1",NEW,(Epic) epic);
        taskManager.getTask(taskManager.addTask(subtask1, 0));
        Task subtask2 = new Subtask("subtask2","descriptionOfSubtask2",IN_PROGRESS,(Epic) epic);
        taskManager.getTask(taskManager.addTask(subtask2, 0));
        taskManager.deleteTask(task2.getTaskId());
        List<Task> list = new ArrayList<>(taskManager.history());
        assertEquals(4, list.size(), "В истории должно быть 4 задачи");
        assertFalse(list.contains(task2), "Средняя задача должна быть удалена");
    }

    @Test
    // получение InMemoryTaskManager в качестве "по-умолчанию"
    void getDefaultShouldReturnInMemoryTaskManager() {
        TaskManager taskManager = Managers.getDefault();
        assertEquals(InMemoryTaskManager.class, taskManager.getClass(),
                "Должен возвращать объект класса InMemoryTask");
    }

    @Test
    // тест переопределения hashCode для одинаковых задач
    void hashCodeShouldReturnEqualHashForEqualTask() {
        LocalDateTime startTime = LocalDateTime.of(2022,04,01,1,20);
        Duration duration = Duration.ofMinutes(20);
        Task task1 = new Task("Test addNewTask", "Test addNewTask description", NEW,
                startTime, duration);
        task1.setTaskId(1);
        Task task2 = new Task("Test addNewTask", "Test addNewTask description", NEW,
                startTime, duration);
        task2.setTaskId(1);
        assertEquals(task1.hashCode(), task2.hashCode(), "Должно быть одинаковое значение hashcode");
    }

    @Test
    // тест переопределения hashCode для разных задач
    void hashCodeShouldReturnDifferentHashForDifferentTask() {
        LocalDateTime startTime = LocalDateTime.of(2022,04,01,1,20);
        Duration duration = Duration.ofMinutes(20);
        Task task1 = new Task("Test addNewTask", "Test addNewTask description", NEW,
                startTime, duration);
        task1.setTaskId(1);
        Task task2 = new Task("Test addNewTask", "Test addNewTask description", DONE,
                startTime, duration);
        task2.setTaskId(1);
        assertNotEquals(task1.hashCode(), task2.hashCode(), "Должно быть разное значение hashcode");
    }


    // тест на получение исключения при добавлении двух пересекающихся задач
    @Test
    void shouldThrowAddTaskExceptionWhenTasksInterseсtion() {
        taskManager.clearAll();
        LocalDateTime startTime = LocalDateTime.of(2022,04,01,1,20);
        Duration duration = Duration.ofMinutes(20);
        Task task1 = new Task("Test addNewTask", "Test addNewTask description", NEW,
                startTime, duration);
        int id1 = taskManager.addTask(task1, 0);
        Task task2 = new Task("Test addNewTask", "Test addNewTask description", NEW,
                startTime.minusMinutes(10), duration);

        final AddTaskException exception = assertThrows(
                AddTaskException.class,
                // создание и переопределение экземпляра класса Executable
                new Executable() {
                    @Override
                    public void execute() {
                        taskManager.addTask(task2, 0);
                    }
                });
        assertEquals("Пересечение по времени с существующей задачей ID="
                + id1, exception.getMessage(),"Выбрасывает исключение при добавлении пересекающихся задач");
    }
}

