package tracker.controllers;

import tracker.model.*;

import java.util.List;

public interface TaskManager {

    // получение списка всех задач (пункт 2.1)
    List<Task> getAllTasks();      // оставил пока для тестирования программы
    List<Task> getTasks();
    List<Task> getEpics();
    List<Task> getSubtasks();
    List<Task> getEpicSubtasks(Epic epic); // (пункт 3.1)

    // удаление всех задач из менеджера (пункт 2.2)
    void clearAll();
    void clearTasks();
    void clearEpics();
    void clearSubtasks();

    // получение задачи по идентификатору (пункт 2.3)
    Task getTask(Integer id);

    // добавляем или обновляем задачу/подзадачу/эпик в менеджер (пункты 2.4 и 2.5)
    void addTask(Task o, Integer id);

    // удаление задачи по идентификатору (пункт 2.6)
    void deleteTask(Integer id);

    // вывод истории просмотров задач
    void printHistory();
}