package tracker.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class Subtask extends Task {

    private Epic epic;

    public Subtask(String name, String description, Status status, Epic epic) {
        super(name, description, status);
        this.epic = epic;
    }

    public Subtask(String name, String description, Status status, Epic epic,
                   LocalDateTime startTime, Duration duration) {
        super(name, description, status, startTime, duration);
        this.epic = epic;
    }

    public Epic getEpic() {
        return epic;
    }

    @Override
    public String toString() {

        String endTimeStr = "empty";
        String startTimeStr = "empty";;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy-HH:mm");

        if (getStartTime().isPresent()) {
            startTimeStr = getStartTime().get().format(formatter);
        }

        if (getEndTime().isPresent()) {
            endTimeStr = getEndTime().get().format(formatter);
        }

        return  super.getName() +
                ": " + getDescription() +
                " (" + getStatus() +
                ", ID=" + getTaskId() +
                ", epicID=" + epic.getTaskId() +
                ", start=" + startTimeStr +
                ", duration=" + getDuration().toMinutes() + "m" +
                ", end=" + endTimeStr +
                ")";
    }
}