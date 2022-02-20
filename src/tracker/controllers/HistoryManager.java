package tracker.controllers;

import tracker.model.Task;
import java.util.List;

public interface HistoryManager {
    void add(Task task);
    void remove(Integer id);
    List<Task> getHistory();
 //   void clearHistory();
}
