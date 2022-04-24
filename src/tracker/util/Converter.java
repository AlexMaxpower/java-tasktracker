package tracker.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import tracker.model.Epic;
import tracker.model.Status;
import tracker.model.Subtask;

import java.io.IOException;

import static tracker.model.Status.NEW;

public class Converter {

    public static Gson gsonWithEpicAdapter = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(Epic.class, new EpicAdapter())
            .create();

    public static Gson gsonWithSubtaskAdapter = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(Subtask.class, new SubtaskAdapter())
            .create();

    public static Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();
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