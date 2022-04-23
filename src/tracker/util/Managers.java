package tracker.util;

import tracker.controllers.*;

public class Managers {

  /*  public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    } */

    public static TaskManager getDefault() {
        return HTTPTaskManager.loadFromKVServer("http://localhost:8078");
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
