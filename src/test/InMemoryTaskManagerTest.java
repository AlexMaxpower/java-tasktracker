
import org.junit.jupiter.api.Test;
import tracker.controllers.InMemoryTaskManager;
import tracker.model.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tracker.model.Status.IN_PROGRESS;
import static tracker.model.Status.NEW;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager>{
    public InMemoryTaskManagerTest() {
        super(new InMemoryTaskManager());
    }

    @Test
    // тест на установку и возврат стартового идентификатора задач у менеджера
    public void shouldReturnCurrentIdWhenSetCurrentId(){
        taskManager.setCurrentId(1000);
        assertEquals(1000, taskManager.getCurrentId(), "Текущий ID должен быть равен 1000");
        Task task = new Task("task1", "descriptionOfTask1", NEW);
        int id = taskManager.addTask(task, 0);
        assertEquals(1001, taskManager.getCurrentId(), "ID задачи должен быть 1000+1");
    }


}
