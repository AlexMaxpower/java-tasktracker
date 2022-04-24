package tracker.controllers;

import com.google.gson.reflect.TypeToken;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import java.util.ArrayList;
import java.util.List;

import static tracker.util.Converter.*;

public class HTTPTaskManager extends FileBackedTasksManager {
    private KVTaskClient kvtaskClient;
    public String apiKey;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        kvtaskClient.setApiKey(apiKey);
    }

    public HTTPTaskManager(String uriString,  List<Task> taskList, List<Integer> historyIdTasks,
                           KVTaskClient kvtaskClient) {
       super(uriString, taskList, historyIdTasks);
       this.kvtaskClient = kvtaskClient;
       this.apiKey = kvtaskClient.getApiKey();
    }

    public static HTTPTaskManager loadFromKVServer(String uriString) {

        KVTaskClient kvtaskClient = new KVTaskClient(uriString);

        System.out.println("Читаем данные с KV-сервера");

        List<Integer> historyIdTasks = new ArrayList<>();

        String jsonTasks = kvtaskClient.load("tasks");
        List<Task> taskList = new ArrayList<>();

        if (jsonTasks != null) {
            taskList = gson.fromJson(jsonTasks, new TypeToken<ArrayList<Task>>(){}.getType());
        }

        String jsonEpics = kvtaskClient.load("epics");
        List<Epic> epicList = new ArrayList<>();

        if (jsonEpics != null) {
            epicList = gsonWithSubtaskAdapter.fromJson(jsonEpics, new TypeToken<ArrayList<Epic>>(){}.getType());
        }

        List<Subtask> subtaskList = new ArrayList<>();
        String jsonSubtasks = kvtaskClient.load("subtasks");

        if (jsonSubtasks != null) {
            subtaskList = gsonWithEpicAdapter.fromJson(jsonSubtasks, new TypeToken<ArrayList<Subtask>>(){}.getType());
        }

        taskList.addAll(epicList);
        taskList.addAll(subtaskList);

        String jsonHistory = kvtaskClient.load("history");

        if (jsonHistory != null) {
            historyIdTasks = gson.fromJson(jsonHistory, new TypeToken<ArrayList<Integer>>(){}.getType());
        }

        return new HTTPTaskManager(uriString, taskList, historyIdTasks, kvtaskClient);
    }

    @Override
    protected void save() {

        // сохраняем простые задачи
        if (!getTasks().isEmpty()) {
            kvtaskClient.put("tasks", gson.toJson(getTasks()));
        }

        // сохраняем эпики
        if (!getEpics().isEmpty()) {
            kvtaskClient.put("epics", gsonWithSubtaskAdapter.toJson(getEpics()));
        }

        // сохраняем подзадачи
        if (!getSubtasks().isEmpty()) {
            kvtaskClient.put("subtasks", gsonWithEpicAdapter.toJson(getSubtasks()));
        }

        // сохраняем историю
        List<Integer> historyListIds = new ArrayList<>();
        for (Task task : history()) {
            historyListIds.add(task.getTaskId());
        }
        kvtaskClient.put("history", gson.toJson(historyListIds));
    }
}