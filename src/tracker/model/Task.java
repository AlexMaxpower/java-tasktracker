package tracker.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

public class Task {

    private String name;
    private String description;
    private Status status;
    private int taskId;

    private Optional<LocalDateTime> startTime;
    private Duration duration;

    public Task(String name, String description, Status status) {
        this.name = name;
        this.description = description;
        this.status = status;
        startTime = startTime.empty();
        duration = Duration.ZERO;
    }

    public Task(String name, String description, Status status, LocalDateTime startTime, Duration duration) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.startTime = Optional.of(startTime);
        this.duration = duration;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getTaskId() {
        return taskId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Optional<LocalDateTime> getStartTime() {
        return startTime;
    }

    public void setStartTime(Optional<LocalDateTime> startTime) {
        this.startTime = startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Optional<LocalDateTime> getEndTime() {
        if (startTime.isPresent()) {
        return Optional.of(startTime.get().plus(duration));
        }
        else return Optional.empty();
      //  else return Optional.of(LocalDateTime.now());
    }

    @Override
    public String toString() {
        String endTimeStr = "empty";
        String startTimeStr = "empty";;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy-HH:mm");

        if (startTime.isPresent()) {
            startTimeStr = startTime.get().format(formatter);
        }

        if (getEndTime().isPresent()) {
            endTimeStr = getEndTime().get().format(formatter);
        }

        return name +
                ": " + description +
                " (" + status +
                ", ID=" + taskId +
                ", start=" + startTimeStr +
                ", duration=" + duration.toMinutes() + "m" +
                ", end=" + endTimeStr +
                ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return taskId == task.taskId && name.equals(task.name) && description.equals(task.description)
                && status.equals(task.status) && startTime.equals(task.startTime)
                && duration.equals(task.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, status, taskId, startTime, duration);
    }
}
