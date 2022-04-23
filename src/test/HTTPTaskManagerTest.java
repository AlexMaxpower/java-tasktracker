import tracker.controllers.FileBackedTasksManager;
import tracker.controllers.HTTPTaskManager;

import java.io.File;

public class HTTPTaskManagerTest extends TaskManagerTest<HTTPTaskManager> {

    HTTPTaskManagerTest() {
        super(HTTPTaskManager.loadFromKVServer("http://localhost:8078"));
    }
}