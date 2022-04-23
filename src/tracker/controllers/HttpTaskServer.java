package tracker.controllers;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import tracker.model.Epic;
import tracker.model.Status;
import tracker.model.Subtask;
import tracker.model.Task;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static tracker.model.Status.NEW;

public class HttpTaskServer {

    private static final int PORT = 8080;
    private static HttpServer httpServer;

    public HttpTaskServer(TaskManager taskManager) {

        try {
            httpServer = HttpServer.create();
            // конфигурирование и запуск сервера
            httpServer.bind(new InetSocketAddress(PORT), 0);
            httpServer.createContext("/tasks", new TasksHandler(taskManager));
            httpServer.start();
            System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
        } catch (IOException e) {
            System.out.println("Не удалось запустить HTTP-сервер на " + PORT + " порту!");
        }
    }

    public void stop(){
        httpServer.stop(0);
    }
}

class TasksHandler implements HttpHandler {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private Gson gsonWithEpicAdapter = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(Epic.class, new EpicAdapter())
            .create();

    private Gson gsonWithSubtaskAdapter = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(Subtask.class, new SubtaskAdapter())
            .create();

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    private TaskManager taskManager;

    public TasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String response = "";
        int responseCode = 200;
        // извлеките метод из запроса
        String method = httpExchange.getRequestMethod();
        boolean validRequest = false;
        switch (method) {
            case "GET":
                System.out.println("Вызван метод GET");
                String path = httpExchange.getRequestURI().getPath();
                String query = httpExchange.getRequestURI().getQuery();
                String[] param = path.split("/");

                // эндпоинт GET http://localhost:8080/tasks/
                if ((param.length == 2) && (param[1].equals("tasks"))) {
                    response = gsonWithEpicAdapter.toJson(taskManager.getPrioritizedTasks());
                    validRequest = true;
                }

                // эндпоинты GET http://localhost:8080/tasks/task/ и http://localhost:8080/tasks/task/?id=
                if ((param.length == 3) && (param[1].equals("tasks")) && (param[2].equals("task"))) {
                    if (query == null) {
                        response = gson.toJson(taskManager.getTasks());
                        validRequest = true;
                    } else {
                        if (query.startsWith("id=")) {
                            try {
                                int id = Integer.parseInt(query.replace("id=", ""));
                                Task task = taskManager.getTask(id);
                                if (task != null) {
                                    System.out.println(task);
                                    if (task.getClass() == Epic.class) {
                                        response = gsonWithSubtaskAdapter.toJson(task);
                                    } else {
                                        response = gsonWithEpicAdapter.toJson(task);
                                    }
                                    validRequest = true;
                                }
                            } catch (NumberFormatException e) {
                            }
                        }
                    }
                }

                // эндпоинт GET http://localhost:8080/tasks/subtask/
                if ((param.length == 3) && (param[1].equals("tasks")) && (param[2].equals("subtask"))
                        && (query == null)) {
                    response = gsonWithEpicAdapter.toJson(taskManager.getSubtasks());
                    validRequest = true;
                }

                // эндпоинт GET http://localhost:8080/tasks/epic/
                if ((param.length == 3) && (param[1].equals("tasks")) && (param[2].equals("epic"))
                        && (query == null)) {
                    response = gsonWithSubtaskAdapter.toJson(taskManager.getEpics());
                    validRequest = true;
                }

                // эндпоинт GET http://localhost:8080/tasks/subtask/epic/?id=
                if ((param.length == 4) && (param[1].equals("tasks")) && (param[2].equals("subtask"))
                        && (param[3].equals("epic")) && (query.startsWith("id="))) {
                    try {
                        int id = Integer.parseInt(query.replace("id=", ""));
                        Task task = taskManager.getTask(id);
                        if (task != null) {
                            if (task.getClass() == Epic.class) {
                                response = gsonWithEpicAdapter.toJson(((Epic) task).getSubtasksEpic());
                                validRequest = true;
                            }
                        }
                    } catch (NumberFormatException e) {
                    }
                }

                // эндпоинт GET http://localhost:8080/tasks/history/
                if ((param.length == 3) && (param[1].equals("tasks")) && (param[2].equals("history"))) {
                    if (!taskManager.history().isEmpty()) {
                        for (Task task : taskManager.history()) {
                            if (task.getClass() == Epic.class) {
                                response += gsonWithSubtaskAdapter.toJson(task);
                            }
                            if (task.getClass() == Subtask.class) {
                                response += gsonWithEpicAdapter.toJson(task);
                            }
                            if (task.getClass() == Task.class) {
                                response += gson.toJson(task);
                            }
                            response += ",\n";
                        }
                        response = response.substring(0, response.lastIndexOf(",\n"));
                        response = "[\n" + response + "\n]";
                        validRequest = true;
                    } else {
                        response = "[]";
                        validRequest = true;
                    }
                }

                if (!validRequest) {
                    response = "Некорректные параметры запроса!";
                    responseCode = 404;
                }
                break;

            case "POST":
                System.out.println("Вызван метод POST");
                InputStream inputStream = httpExchange.getRequestBody();
                String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
                path = httpExchange.getRequestURI().getPath();
                query = httpExchange.getRequestURI().getQuery();
                param = path.split("/");
                if ((param.length == 3) && (param[1].equals("tasks")) && (param[2].equals("task")
                     && (query == null))) {
                    JsonElement jsonElement = JsonParser.parseString(body);
                    if (jsonElement.isJsonObject()) {
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        // если это подзадача, то в JSON есть поле epic
                        if (jsonObject.has("epic")) {
                            Subtask subtask = gsonWithEpicAdapter.fromJson(body, Subtask.class);
                            // добавляем вместо эпика-пустышки настоящий эпик
                            Epic epic = (Epic) taskManager.getTaskWithoutHistory(subtask.getEpic().getTaskId());
                            Subtask newSubtask;
                            if (subtask.getStartTime().isEmpty()) {
                                newSubtask = new Subtask(subtask.getName(), subtask.getDescription(),
                                        subtask.getStatus(), epic);
                            } else {
                                newSubtask = new Subtask(subtask.getName(), subtask.getDescription(),
                                        subtask.getStatus(), epic, subtask.getStartTime().get(), subtask.getDuration());
                            }
                            taskManager.addTask(newSubtask, subtask.getTaskId());
                            // если это эпик, то в JSON есть поле subtasksEpic
                        } else if (jsonObject.has("subtasksEpic")) {
                            Epic epic = gsonWithSubtaskAdapter.fromJson(body, Epic.class);
                            for (int i = 0; i < epic.getSubtasksEpic().size(); i++) {
                                int subtaskId = epic.getSubtasksEpic().get(0).getTaskId();
                                epic.deleteSubtask(epic.getSubtasksEpic().get(0));
                                epic.addSubtaskEpic((Subtask) taskManager.getTaskWithoutHistory(subtaskId));
                            }
                            Optional<LocalDateTime> startTime = epic.getStartTime();
                            Duration duration = epic.getDuration();
                            epic.setStartTime(Optional.empty());
                            epic.setDuration(Duration.ZERO);
                            int newId = taskManager.addTask(epic, epic.getTaskId());
                            taskManager.getTaskWithoutHistory(newId).setStartTime(startTime);
                            taskManager.getTaskWithoutHistory(newId).setDuration(duration);
                        } else {
                            // если простая задача
                            Task task = gson.fromJson(body, Task.class);
                            taskManager.addTask(task, task.getTaskId());
                        }
                        validRequest = true;
                    }
                }
                if (!validRequest) {
                    response = "Некорректные параметры запроса!";
                    responseCode = 404;
                }
                break;

            case "DELETE":
                System.out.println("Вызван метод DELETE");
                path = httpExchange.getRequestURI().getPath();
                query = httpExchange.getRequestURI().getQuery();
                param = path.split("/");

                // эндпоинт DELETE http://localhost:8080/tasks/task/
                if ((param.length == 3) && (param[1].equals("tasks")) && (param[2].equals("task"))) {
                    if (query == null) {
                        taskManager.clearAll();
                        response = "Все задачи удалены";
                        validRequest = true;
                    } else {
                        if (query.startsWith("id=")) {
                            try {
                                int id = Integer.parseInt(query.replace("id=", ""));
                                Task task = taskManager.getTask(id);
                                if (task != null) {
                                    taskManager.deleteTask(id);
                                    response = "Задача удалена";
                                    validRequest = true;
                                }
                            } catch (NumberFormatException e) {
                            }
                        }
                    }
                }
                if (!validRequest) {
                    response = "Некорректные параметры запроса!";
                    responseCode = 404;
                }
                break;

            default:
                response = "Некорректный метод!";
                responseCode = 404;
        }

        httpExchange.sendResponseHeaders(responseCode, 0);
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}

class EpicAdapter extends TypeAdapter<Epic> {

    @Override
    public void write(final JsonWriter jsonWriter, final Epic epic) throws IOException {
        jsonWriter.value(epic.getTaskId());
    }

    @Override
    public Epic read(final JsonReader jsonReader) throws IOException {
        int epicId = Integer.parseInt(jsonReader.nextString());
        Epic epic = new Epic("", "", NEW); // пустышка для передачи EpicId
        epic.setTaskId(epicId);
        return epic;
    }
}

class SubtaskAdapter extends TypeAdapter<Subtask> {

    @Override
    public void write(final JsonWriter jsonWriter, final Subtask subtask) throws IOException {
        // приводим Subtask к необходимому формату
        jsonWriter.value(subtask.getTaskId());
    }

    @Override
    public Subtask read(final JsonReader jsonReader) throws IOException {
        int subtaskId = Integer.parseInt(jsonReader.nextString());
        Subtask subtask = new Subtask("", "", NEW,
                new Epic("", "", Status.NEW)); // пустышка для передачи SubtaskId
        subtask.setTaskId(subtaskId);
        return subtask;
    }
}