package tracker.controllers;

import tracker.model.*;

public interface TaskManager {

    // получение списка всех задач (пункт 2.1)
    void printAllTasks();

    // удаление всех задач из менеджера (пункт 2.2)
    void clearAll();

    // получение задачи по идентификатору (пункт 2.3)
    Task getTask(Integer id);

    // добавляем или обновляем задачу/подзадачу/эпик в менеджер (пункты 2.4 и 2.5)
    void addTask(Task o, Integer id);

    // удаление задачи по идентификатору (пункт 2.6)
    void deleteTask(Integer id);

    // получение списка подзадач определеннного эпика (пункт 3.1)
    void getEpicSubtasks(Epic epic);

    // вывод истории просмотров задач
    void printHistory();
}