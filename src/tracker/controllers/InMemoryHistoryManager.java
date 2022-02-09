package tracker.controllers;

import tracker.model.Task;
import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private List<Task> historyTasks; //список просмотренных объектов
    private static final int MAX_VIEWS = 10;  // максимальное количество хранимых в истории задач

    public InMemoryHistoryManager() {
        historyTasks = new ArrayList<>();
    }

    @Override
    public void add(Task task) {
        if (historyTasks.size() >= MAX_VIEWS) {
            historyTasks.remove(0);
        }
        historyTasks.add(task);
        // System.out.println("Добавляем " + task.getTaskId() + " в историю");
    }

    @Override
    public List<Task> getHistory(){
        return historyTasks;
    }

    @Override
    public void removeTaskFromHistoryById(Integer id) {
        for (int i = 0; i < historyTasks.size(); i++) {
            if (historyTasks.get(i).getTaskId() == id) {
            //  System.out.println("Удаляем " + id + " из истории");
                historyTasks.remove(i);
                i--;
            }
        }
    }

    @Override
    public void clearHistory() {
        historyTasks.clear();
    }

}
