import com.google.gson.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import static org.junit.jupiter.api.Assertions.*;
import tracker.controllers.*;
import tracker.exceptions.AddTaskException;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.util.Managers;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static tracker.model.Status.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    T taskManager;
    static KVServer kvServer;
    HttpTaskServer taskServer;

    public TaskManagerTest(T taskManager) {
        this.taskManager = taskManager;
    }

    @BeforeAll
    public static void beforeAll(){
        try {
            kvServer = new KVServer();
            kvServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @BeforeEach
    public void beforeEach() {
        taskServer = new HttpTaskServer(taskManager);
        taskManager.clearAll();
        Task task1 = new Task("task1", "descriptionOfTask1", NEW);
        taskManager.getTask(taskManager.addTask(task1, 0)); // добавляем задачу и заполняем историю
        Task task2 = new Task("task2", "descriptionOfTask2", DONE);
        taskManager.getTask(taskManager.addTask(task2, 0));
        Task epic = new Epic("epic1", "descriptionOfEpic1", NEW);
        taskManager.getTask(taskManager.addTask(epic, 0)); // добавляем задачу и заполняем историю
        LocalDateTime startTime = LocalDateTime.of(2022,04,01,1,20);
        Duration duration = Duration.ofMinutes(20);
        Task subtask1 = new Subtask("subtask1","descriptionOfSubtask1",NEW, (Epic) epic,
                startTime, duration);
        taskManager.getTask(taskManager.addTask(subtask1, 0));
        Task subtask2 = new Subtask("subtask2","descriptionOfSubtask2",NEW,(Epic) epic);
        taskManager.getTask(taskManager.addTask(subtask2, 0));
    }

    @AfterEach
    public void afterEach(){
        taskServer.stop();
    }

    @AfterAll
    public static void afterAll(){
       kvServer.stop();
    }

    @Test
    // тест на возврат статуса NEW у эпика при пустом списке подзадач
    public void shouldReturnEpicStatusAsNewWhenEmptyListOfSubtask(){
        Task epic = new Epic("epic1", "descriptionOfEpic1", DONE);
        final int epicId = taskManager.addTask(epic, 0);
        taskManager.clearSubtasks();
        assertEquals(NEW, taskManager.getTask(epicId).getStatus(), "У эпика должен быть статус NEW");
    }

    @Test
    // тест на возврат статуса NEW у эпика, если у всех подзадач статус NEW
    public void shouldReturnEpicStatusAsNewWhenAllSubtasksInStatusNew(){
        assertEquals(NEW, taskManager.getEpics().get(0).getStatus(), "У эпика должен быть статус NEW");
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
        List<Task> list = new ArrayList<>(taskManager.getAllTasks());
        assertEquals(5, list.size(),"В списке должно быть 5 объектов");
        Subtask subTaskExp = (Subtask) taskManager.getSubtasks().get(0);
        assertEquals(taskManager.getEpics().get(0), subTaskExp.getEpic(),"В сабтаске должен быть эпик");
        assertEquals(NEW, taskManager.getEpics().get(0).getStatus(),
                "Статус у эпика должен быть NEW");
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
        taskManager.clearTasks();
        List<Task> list = new ArrayList<>(taskManager.getTasks());
        assertTrue(list.isEmpty(),"Список задач должен быть пустым");
    }

    @Test
    // тест getEpics() при нормальном режиме работы
    public void getEpicsShouldReturnListOfEpicsOnly(){
        List<Task> list = new ArrayList<>(taskManager.getEpics());
        assertEquals(1, list.size(),"В списке должен быть 1 эпик");
        assertEquals(Epic.class, list.get(0).getClass(),
                "Первый объект должен быть эпиком");
    }

    @Test
    // тест getEpics() при пустом списке эпиков
    public void getEpicsShouldReturnEmptyListEpicsWhenNoEpics(){
        taskManager.clearEpics();
        List<Task> list = new ArrayList<>(taskManager.getEpics());
        assertTrue(list.isEmpty(),"Список задач должен быть пустым");
    }

    @Test
    // тест getSubtasks() при нормальном режиме работы
    public void getSubtasksShouldReturnListOfSubtasksOnly(){
        List<Task> list = new ArrayList<>(taskManager.getSubtasks());
        assertEquals(2, list.size(),"В списке должно быть 2 подзадачи");
        Subtask subTaskExp1 = (Subtask) list.get(0);
        assertEquals(taskManager.getEpics().get(0), subTaskExp1.getEpic(),"В сабтаске1 должен быть эпик");
        assertEquals(Subtask.class, list.get(0).getClass(),
                "Первый объект должен быть сабтаском");
        assertEquals(Subtask.class, list.get(1).getClass(),
                "Второй объект должен быть сабтаском");
    }

    @Test
    // тест getSubtasks() при пустом списке подзадач
    public void getSubtasksShouldReturnEmptyListSubtasksWhenNoSubtasks(){
        taskManager.clearSubtasks();
        List<Task> list = new ArrayList<>(taskManager.getSubtasks());
        assertTrue(list.isEmpty(),"Список подзадач должен быть пустым");
    }

    @Test
    // тест getEpicSubtasks() при нормальном режиме работы
    public void getEpicSubtasksShouldReturnListOfEpicSubtasksOnly(){
        Epic epic = (Epic) taskManager.getEpics().get(0);
        List<Task> list = new ArrayList<>(taskManager.getEpicSubtasks(epic));
        assertEquals(2, list.size(),"В списке должно быть 2 подзадачи");
        Subtask subTaskExp1 = (Subtask) list.get(0);
        assertEquals(epic, subTaskExp1.getEpic(),"В сабтаске1 должен быть эпик1");
        Subtask subTaskExp2 = (Subtask) list.get(1);
        assertEquals(epic, subTaskExp2.getEpic(),"В сабтаске2 должен быть эпик1");
        assertEquals(Optional.of(LocalDateTime.of(2022,04,01,1,40)),
                taskManager.getEpics().get(0).getEndTime(),
                "Время завершения эпика должно быть 2022-04-01 1:40");
    }

    @Test
    // тест getEpicSubtasks() при пустом списке подзадач у эпика
    public void getEpicSubtasksShouldReturnEmptyListSubtasksWhenEpicDoNotHaveSubtasks(){
        taskManager.clearSubtasks();
        List<Task> list = new ArrayList<>(taskManager.getEpicSubtasks((Epic) taskManager.getEpics().get(0)));
        assertTrue(list.isEmpty(),"Список подзадач должен быть пустым");
    }

    @Test
    // тест clearAll() - удаление всех простых задач, подзадач и эпиков
    public void clearAllShouldDeleteAllTasksInAllManagers(){
        taskManager.clearAll();
        List<Task> list = new ArrayList<>(taskManager.getAllTasks());
        assertTrue(list.isEmpty(),"Список задач в менеджере должен быть пустым");
        list = new ArrayList<>(taskManager.history());
        assertTrue(list.isEmpty(),"Список задач в истории должен быть пустым");
    }

    @Test
    // тест clearTasks() - удаление всех простых задач
    public void clearTasksShouldDeleteTasksInAllManagers(){
        taskManager.clearTasks();
        List<Task> list = new ArrayList<>(taskManager.getTasks());
        assertTrue(list.isEmpty(),"Список задач в менеджере должен быть пустым");
        list = new ArrayList<>(taskManager.history());
        assertEquals(3, list.size(),"В истории должны остаться эпик с 2 подзадачами");
    }

    @Test
    // тест clearSubtasks() - удаление всех подзадач из менеджера
    public void clearSubtasksShouldDeleteSubtasksInAllManagersAndEpics(){
        taskManager.clearSubtasks();
        List<Task> list = new ArrayList<>(taskManager.getSubtasks());
        assertTrue(list.isEmpty(),"Список подзадач в менеджере должен быть пустым");
        assertEquals(3, taskManager.history().size(), "В истории должен остаться эпик и 2 задачи");
        list = new ArrayList<>(taskManager.getEpicSubtasks((Epic) taskManager.getEpics().get(0)));
        assertTrue(list.isEmpty(),"У эпика не должно быть подзадач");
    }

    @Test
    // тест clearEpics() - удаление всех эпиков и подзадач из менеджера
    public void clearEpicsShouldDeleteEpicsAndSubtasksInAllManagers(){
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
        LocalDateTime startTime = LocalDateTime.of(2023,04,01,1,20);
        Duration duration = Duration.ofMinutes(50);
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
        assertNull(taskManager.getTask(null), "getTask() должен вернуть null");
        assertEquals(5,taskManager.history().size(), "В истории должно быть 5 элементов");
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
        List<Task> tasks = taskManager.getTasks();
        taskManager.deleteTask(tasks.get(0).getTaskId());
        assertEquals(1, taskManager.getTasks().size(), "должна остаться одна задача");
        taskManager.deleteTask(tasks.get(1).getTaskId());
        assertTrue(taskManager.getTasks().isEmpty(), "Список задач должен быть пустым");
        assertEquals(3, taskManager.history().size(), "В истории должны остаться эпик и подзадачи");
    }

    @Test
    // тест deleteTask() на корректное удаление подзадачи из менеджера, истории и эпика
    public void deleteTaskShouldDeleteSubtaskInManagerAndHistoryAndEpic(){
        List<Task> list = new ArrayList<>(taskManager.getSubtasks());
        taskManager.deleteTask(list.get(0).getTaskId());
        assertEquals(1, taskManager.getSubtasks().size(), "В списке подзадач должна быть одна подзадача");
        assertEquals(4, taskManager.history().size(), "В истории должны остаться 4 задачи");
        list = new ArrayList<>(taskManager.getEpicSubtasks((Epic) taskManager.getEpics().get(0)));
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
        taskManager.getTask(1);
        List<Task> list = new ArrayList<>(taskManager.history()); // задействует getHistory
        assertEquals(5, list.size(), "Задачи в истории не должны дублироваться");
    }

    @Test
    // тест на удаление первой задачи в списке из истории
    void removeShouldDeleteFirstTaskInHistory() {
        Task task = taskManager.getTask(1);
        taskManager.deleteTask(1);
        List<Task> list = new ArrayList<>(taskManager.history());
        assertEquals(4, list.size(), "В истории должно быть 4 задачи");
        assertFalse(list.contains(task), "Первая задача должна быть удалена");
    }

    @Test
    // тест на удаление последней задачи из истории
    void removeShouldDeleteLastTaskInHistory() {
        Task subtask = taskManager.getTask(5);
        taskManager.deleteTask(5);
        List<Task> list = new ArrayList<>(taskManager.history());
        assertEquals(4, list.size(), "В истории должно быть 4 задачи");
        assertFalse(list.contains(subtask), "Последняя задача должна быть удалена");
    }

    @Test
    // тест на удаление задачи в середине списка из истории
    void removeShouldDeleteMiddleTaskInHistory() {
        Task epic = taskManager.getEpics().get(0);
        taskManager.deleteTask(epic.getTaskId());
        List<Task> list = new ArrayList<>(taskManager.history());
        assertEquals(2, list.size(), "В истории должно быть 2 задачи");
        assertFalse(list.contains(epic), "Средняя задача должна быть удалена");
    }

    @Test
    // получение HTTPTaskManager в качестве "по-умолчанию"
    void getDefaultShouldReturnInMemoryTaskManager() {
        TaskManager taskManager = Managers.getDefault();
        assertEquals(HTTPTaskManager.class, taskManager.getClass(),
                "Должен возвращать объект класса HTTPTaskManager");
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

    // тест getPrioritizedTasks() при пустом списке задач
    @Test
    void getPrioritizedTasksShouldReturnEmptyTaskListWhenNoTaskInManager() {
        taskManager.clearAll();
        assertTrue(taskManager.getPrioritizedTasks().isEmpty(),"Отсортированный список должен быть пустым");
    }

    // тест getPrioritizedTasks() при нормальном режиме работы
    @Test
    void getPrioritizedTasksShouldReturnSortedTaskList() {
        LocalDateTime startTime = LocalDateTime.of(2023,04,01,1,20);
        Duration duration = Duration.ofMinutes(5);
        Task task1 = new Task("Test addNewTask", "Test addNewTask description", NEW,
                startTime, duration);
        taskManager.addTask(task1, 0);
        Task task2 = new Task("Test addNewTask", "Test addNewTask description", NEW,
                startTime.minusMinutes(50), duration);
        taskManager.addTask(task2, 0);
        List<Task> list = new ArrayList<>(taskManager.getPrioritizedTasks());
        assertEquals(6, list.size(),"В списке должно быть 6 задач без эпика");
        assertEquals(4, list.get(0).getTaskId(), "Первым в списке должен быть сабтаск с ID=4");
        assertEquals(task2, list.get(1), "Второй должна быть задача task2");
        assertEquals(task1, list.get(2), "Третьей должна быть задача task1");
        assertEquals(1,list.get(3).getTaskId(), "Четвертой должна быть задача без времени");
        assertEquals(2,list.get(4).getTaskId(), "Пятой должна быть задача без времени");
        assertEquals(5,list.get(5).getTaskId(), "Шестым должен быть сабтаск без времени");
    }

    // проверка работы эндпоинта GET "http://localhost:8080/tasks/task/ при нормальной работе
    @Test
    void getTaskEndpointShouldReturnListOfTasks() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        // вызываем список всех простых задач из менеджера
        URI url = URI.create("http://localhost:8080/tasks/task/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
        Integer firstTaskId = jsonObject.get("taskId").getAsInt();
        String firstTaskName = jsonObject.get("name").getAsString();
        assertEquals(1, firstTaskId, "Id первой задачи должен быть равен 1");
        assertEquals("task1",firstTaskName, "Название первой задачи должно быть task1");
        jsonObject = jsonArray.get(1).getAsJsonObject();
        Integer secondTaskId = jsonObject.get("taskId").getAsInt();
        String secondTaskName = jsonObject.get("name").getAsString();
        assertEquals(2, secondTaskId, "Id второй задачи должен быть равен 2");
        assertEquals("task2", secondTaskName, "Название второй задачи должно быть task2");
        assertEquals(2,jsonArray.size(), "Должно быть получено 2 задачи");
    }

    // проверка работы эндпоинта GET "http://localhost:8080/tasks/task/ при отсутствии простых задач
    @Test
    void getTaskEndpointShouldReturnListOfTasksWhenTaskListEmpty() throws IOException, InterruptedException {
        taskManager.clearAll();
        HttpClient client = HttpClient.newHttpClient();
        // вызываем список всех простых задач из менеджера
        URI url = URI.create("http://localhost:8080/tasks/task/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        assertEquals("[]", response.body(), "Список задач должен быть пустым");
    }

    // проверка работы эндпоинта GET "http://localhost:8080/tasks/task/?id= при нормальной работе
    @Test
    void getTaskByIdEndpointShouldReturnTask() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        // вызываем первую простую задачу из менеджера
        URI url = URI.create("http://localhost:8080/tasks/task/?id=1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        Integer taskId = jsonObject.get("taskId").getAsInt();
        String name = jsonObject.get("name").getAsString();
        assertEquals(1, taskId, "Id задачи должен быть равен 1");
        assertEquals("task1", name, "Название задачи должно быть task1");
    }

    // проверка работы эндпоинта GET "http://localhost:8080/tasks/task/?id= при пустом списке задач или с "плохим" Id
    @Test
    void getTaskByIdEndpointShouldReturn404WhenTaskListIsEmpty() throws IOException, InterruptedException {
        taskManager.clearAll();
        HttpClient client = HttpClient.newHttpClient();
        // вызываем первую простую задачу из менеджера
        URI url = URI.create("http://localhost:8080/tasks/task/?id=1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Код ответа должен быть 404 (не найдено)");
        assertEquals("Некорректные параметры запроса!", response.body(), "Должен быть ответ сервера");
    }

    // проверка работы эндпоинта POST "http://localhost:8080/tasks/task/ при добавлении задачи
    @Test
    void addTaskEndpointShouldAddTaskInManager() throws IOException, InterruptedException {
        // добавляем простую задачу
        Task newTask = new Task("NewTask", "DescriptionNewTask", IN_PROGRESS);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/task/");
        Gson gson = new Gson();
        String json = gson.toJson(newTask);
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код ответа сервера должен быть 200");
        assertEquals(3, taskManager.getTasks().size(), "В менеджере должно быть 3 простых задачи");
        assertEquals("NewTask", taskManager.getTasks().get(taskManager.getTasks().size()-1).getName(),
                "Название задачи должно быть NewTask");
        assertEquals(IN_PROGRESS, taskManager.getTasks().get(taskManager.getTasks().size()-1).getStatus(),
                "Статус задачи должен быть IN_PROGRESS");
    }

    // проверка работы эндпоинта POST "http://localhost:8080/tasks/task/ при обновлении задачи
    @Test
    void addTaskEndpointShouldUpdateTaskInManager() throws IOException, InterruptedException {
        // обновляем статус у простой задачи с NEW на DONE
        Task task = taskManager.getTask(1);
        task.setStatus(DONE);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/task/");
        Gson gson = new Gson();
        String json = gson.toJson(task);
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код ответа сервера должен быть 200");
        assertEquals(2, taskManager.getTasks().size(), "В менеджере должно быть 2 простых задачи");
        assertEquals("task1", taskManager.getTask(task.getTaskId()).getName(),
                "Название задачи должно быть task1");
        assertEquals(DONE, taskManager.getTask(task.getTaskId()).getStatus(),
                "Статус задачи должен быть DONE");
    }

    // проверка работы эндпоинта DELETE "http://localhost:8080/tasks/task/?id= , когда задача есть в менеджере
    @Test
    void deleteTaskEndpointShouldDeleteTaskInManager() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/task/?id=1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код ответа сервера должен быть 200");
        assertEquals(1, taskManager.getTasks().size(), "В менеджере должна быть 1 простая задача");
        assertEquals("task2", taskManager.getTasks().get(0).getName(),
                "Название задачи должно быть task2");
    }

    // проверка работы эндпоинта DELETE "http://localhost:8080/tasks/task/?id= при отсутствии задач
    @Test
    void deleteTaskEndpointShouldReturn404ifTaskListEmpty() throws IOException, InterruptedException {
        taskManager.clearAll();
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/task/?id=1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Код ответа сервера должен быть 404 (не найдено");
        assertEquals("Некорректные параметры запроса!", response.body(),
                "Должен быть ответ сервера");
    }

    // проверка работы эндпоинта DELETE "http://localhost:8080/tasks/task/
    @Test
    void deleteTaskEndpointShouldDeleteAllTaskInManager() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/task/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код ответа сервера должен быть 200");
        assertTrue(taskManager.getTasks().isEmpty(), "В менеджере не должно быть простых задач");
        assertTrue(taskManager.getSubtasks().isEmpty(), "В менеджере не должно быть подзадач");
        assertTrue(taskManager.getEpics().isEmpty(), "В менеджере не должно быть эпиков");
    }

    // проверка работы эндпоинта DELETE "http://localhost:8080/tasks/task/ при отсутствии задач в менеджере
    @Test
    void deleteTaskEndpointShouldDeleteAllTaskInManagerWhenTaskListEmpty() throws IOException, InterruptedException {
        taskManager.clearAll();
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/task/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код ответа сервера должен быть 200");
        assertTrue(taskManager.getTasks().isEmpty(), "В менеджере не должно быть простых задач");
        assertTrue(taskManager.getSubtasks().isEmpty(), "В менеджере не должно быть подзадач");
        assertTrue(taskManager.getEpics().isEmpty(), "В менеджере не должно быть эпиков");
    }

    // проверка работы эндпоинта GET "http://localhost:8080/tasks/subtask/ при нормальной работе
    @Test
    void getSubtaskEndpointShouldReturnListOfSubtasks() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        // вызываем список подзадач из менеджера
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
        Integer firstSubtaskId = jsonObject.get("taskId").getAsInt();
        String firstSubtaskName = jsonObject.get("name").getAsString();
        assertEquals(4, firstSubtaskId, "id первой подзадачи должен быть равен 4");
        assertEquals("subtask1", firstSubtaskName, "Название первой подзадачи должно быть subtask1");
        jsonObject = jsonArray.get(1).getAsJsonObject();
        Integer secondSubtaskId = jsonObject.get("taskId").getAsInt();
        String secondSubtaskName = jsonObject.get("name").getAsString();
        assertEquals(5, secondSubtaskId, "id второй подзадачи должен быть равен 5");
        assertEquals("subtask2", secondSubtaskName, "Название второй подзадачи должно быть subtask2");
        assertEquals(2,jsonArray.size(), "Должно быть получено 2 подзадачи");
    }

    // проверка работы эндпоинта GET "http://localhost:8080/tasks/subtask/ при отсутствии подзадач в менеджере
    @Test
    void getSubtaskEndpointShouldReturnEmptyWhenListOfSubtaskEmpty() throws IOException, InterruptedException {
        taskManager.clearAll();
        HttpClient client = HttpClient.newHttpClient();
        // вызываем список подзадач из менеджера
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        assertEquals("[]", response.body(), "Список подзадач должен быть пустым");
    }

    // проверка работы эндпоинта GET "http://localhost:8080/tasks/epic/ при нормальной работе
    @Test
    void getEpicsEndpointShouldReturnListOfEpics() throws IOException, InterruptedException {
        Epic epicNew = new Epic("epic2", "descriptionEpic2", NEW);
        taskManager.addTask(epicNew,0);
        HttpClient client = HttpClient.newHttpClient();
        // вызываем список эпиков из менеджера
        URI url = URI.create("http://localhost:8080/tasks/epic/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
        Integer firstEpicId = jsonObject.get("taskId").getAsInt();
        String firstEpicName = jsonObject.get("name").getAsString();
        assertEquals(3, firstEpicId, "id первой эпика должен быть равен 3");
        assertEquals("epic1", firstEpicName, "Название первого эпика должно быть epic1");
        jsonObject = jsonArray.get(1).getAsJsonObject();
        Integer secondEpicId = jsonObject.get("taskId").getAsInt();
        String secondEpicName = jsonObject.get("name").getAsString();
        assertEquals(6, secondEpicId, "id второго эпика должен быть равен 6");
        assertEquals("epic2", secondEpicName, "Название второго эпика должно быть epic2");
        assertEquals(2,jsonArray.size(), "Должно быть получено 2 эпика");
    }

    // проверка работы эндпоинта GET "http://localhost:8080/tasks/epic/ при отсутствии подзадач в менеджере
    @Test
    void getEpicsEndpointShouldReturnEmptyWhenListOfEpicEmpty()  throws IOException, InterruptedException {
        taskManager.clearAll();
        HttpClient client = HttpClient.newHttpClient();
        // вызываем список эпиков из менеджера
        URI url = URI.create("http://localhost:8080/tasks/epic/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        assertEquals("[]", response.body(), "Список эпиков должен быть пустым");
    }

    // проверка работы эндпоинта GET "http://localhost:8080/tasks/subtask/epic/?id= при нормальной работе
    @Test
    void getEpicSubtaskEndpointShouldReturnListOfSubtasks() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        // вызываем список подзадач эпика из менеджера
        URI url = URI.create("http://localhost:8080/tasks/subtask/epic/?id=3");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
        Integer firstSubtaskId = jsonObject.get("taskId").getAsInt();
        String firstSubtaskName = jsonObject.get("name").getAsString();
        assertEquals(4, firstSubtaskId, "id первой подзадачи должен быть равен 4");
        assertEquals("subtask1", firstSubtaskName, "Название первой подзадачи должно быть subtask1");
        jsonObject = jsonArray.get(1).getAsJsonObject();
        Integer secondSubtaskId = jsonObject.get("taskId").getAsInt();
        String secondSubtaskName = jsonObject.get("name").getAsString();
        assertEquals(5, secondSubtaskId, "id второй подзадачи должен быть равен 5");
        assertEquals("subtask2", secondSubtaskName, "Название второй подзадачи должно быть subtask2");
        assertEquals(2,jsonArray.size(), "Должно быть получено 2 подзадачи");
    }

    // проверка работы эндпоинта GET "http://localhost:8080/tasks/subtask/epic/?id= при пустом списке подзадач у эпика
    @Test
    void getEpicSubtaskEndpointShouldReturnEmptyListOfSubtasks() throws IOException, InterruptedException {
        Epic epicNew = new Epic("epic2", "descriptionEpic2", NEW);
        int epicId = taskManager.addTask(epicNew,0);
        HttpClient client = HttpClient.newHttpClient();
        // вызываем список подзадач эпика из менеджера
        URI url = URI.create("http://localhost:8080/tasks/subtask/epic/?id=" + epicId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        assertEquals("[]", response.body(), "Список эпиков должен быть пустым");
    }

    // проверка работы эндпоинта GET "http://localhost:8080/tasks/history при нормальной работе
    @Test
    void getHistoryEndpointShouldReturnHistoryList() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        // вызываем список задач истории из менеджера
        URI url = URI.create("http://localhost:8080/tasks/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
        Integer firstTaskId = jsonObject.get("taskId").getAsInt();
        String firstTaskName = jsonObject.get("name").getAsString();
        assertEquals(1, firstTaskId, "id первой задачи в истории должен быть равен 1");
        assertEquals("task1", firstTaskName, "Название первой задачи должно быть task1");
        jsonObject = jsonArray.get(4).getAsJsonObject();
        Integer lastTaskId = jsonObject.get("taskId").getAsInt();
        String lastTaskName = jsonObject.get("name").getAsString();
        assertEquals(5, lastTaskId, "id последней задачи в истории должен быть равен 5");
        assertEquals("subtask2", lastTaskName, "Название последней задачи в истории должно быть subtask2");
        assertEquals(5,jsonArray.size(), "Должно быть получено 5 задач");
    }

    // проверка работы эндпоинта GET "http://localhost:8080/tasks/history при пустой истории
    @Test
    void getHistoryEndpointShouldReturnEmptyHistoryListWhenHistoryEmpty() throws IOException, InterruptedException {
        taskManager.clearAll();
        HttpClient client = HttpClient.newHttpClient();
        // вызываем список задач истории из менеджера
        URI url = URI.create("http://localhost:8080/tasks/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        assertEquals("[]", response.body(), "Список задач в истории должен быть пустым");
    }

    // проверка работы эндпоинта GET "http://localhost:8080/tasks/ при нормальной работе
    @Test
    void getPrioritizedTasksEndpointShouldReturnSortedTaskList() throws IOException, InterruptedException {
        LocalDateTime startTime = LocalDateTime.of(2023, 04, 01, 1, 20);
        Duration duration = Duration.ofMinutes(5);
        Task task1 = new Task("NewTask1", "Test addNewTask1 description", NEW,
                startTime, duration);
        taskManager.addTask(task1, 0);
        Task task2 = new Task("NewTask2", "Test addNewTask2 description", NEW,
                startTime.minusMinutes(50), duration);
        taskManager.addTask(task2, 0);
        HttpClient client = HttpClient.newHttpClient();
        // вызываем список приоритетных задач из менеджера
        URI url = URI.create("http://localhost:8080/tasks/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertEquals(6, jsonArray.size(), "В списке должно быть 6 задач без эпика");
        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
        Integer firstTaskId = jsonObject.get("taskId").getAsInt();
        String firstTaskName = jsonObject.get("name").getAsString();
        assertEquals(4, firstTaskId, "Первым в списке должен быть сабтаск с ID=4");
        assertEquals("subtask1", firstTaskName, "Название первой задачи в списке должно быть subtask1");
        jsonObject = jsonArray.get(1).getAsJsonObject();
        Integer secondTaskId = jsonObject.get("taskId").getAsInt();
        String secondTaskName = jsonObject.get("name").getAsString();
        assertEquals(7, secondTaskId, "id второй задачи в списке должен быть равен 7");
        assertEquals("NewTask2", secondTaskName, "Название второй задачи списка должно быть NewTask2");
        jsonObject = jsonArray.get(2).getAsJsonObject();
        Integer thirdTaskId = jsonObject.get("taskId").getAsInt();
        String thirdTaskName = jsonObject.get("name").getAsString();
        assertEquals(6, thirdTaskId, "id третьей задачи в списке должен быть равен 6");
        assertEquals("NewTask1", thirdTaskName, "Название третьей задачи списка должно быть NewTask1");
    }

    // проверка работы эндпоинта GET "http://localhost:8080/tasks/ при отсутствии задач в менеджере
    @Test
    void getPrioritizedTasksEndpointShouldReturnEmptyTaskList() throws IOException, InterruptedException {
        taskManager.clearAll();
        HttpClient client = HttpClient.newHttpClient();
        // вызываем список приоритетных задач из менеджера
        URI url = URI.create("http://localhost:8080/tasks/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        assertEquals("[]", response.body(), "Список задач должен быть пустым");
    }
}