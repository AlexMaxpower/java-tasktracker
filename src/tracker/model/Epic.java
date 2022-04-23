package tracker.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Epic extends Task {

    private List<Subtask> subtasksEpic;
    private Optional<LocalDateTime> endTime;

    public Epic(String name, String description, Status status) {
        super(name, description, status);
        endTime = Optional.empty();
        subtasksEpic = new ArrayList<>();
        checkEpicStatus();
    }

    @Override
    public String toString() {
        List<Integer> subtaskIds  = new ArrayList<>();
        for (Subtask subtask : subtasksEpic) {
            subtaskIds.add(subtask.getTaskId());
        }

        String endTimeStr = "empty";
        String startTimeStr = "empty";;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy-HH:mm");

        if (getStartTime().isPresent()) {
            startTimeStr = getStartTime().get().format(formatter);
        }

        if (endTime.isPresent()) {
            endTimeStr = endTime.get().format(formatter);
        }

        String result = getName() +
                ": " + getDescription() +
                " (" + getStatus() +
                ", id=" + getTaskId() +
                ", subIds=" + subtaskIds +
                ", start=" + startTimeStr +
                ", duration=" + getDuration().toMinutes() + "m" +
                ", end=" + endTimeStr +
                ")";
        return result.toUpperCase();
    }

    public List<Subtask> getSubtasksEpic() {
        return subtasksEpic;
    }

    public void addSubtaskEpic(Subtask subtask) {
        subtasksEpic.add(subtask);
        checkEpicStatus();
        checkEpicTime();
    }

    public void deleteSubtask(Subtask subtask) {
        for (int i = 0 ; i < subtasksEpic.size(); i++) {
            if (subtasksEpic.get(i).getTaskId() == subtask.getTaskId()) {
                subtasksEpic.remove(i);
            }
        }
        checkEpicStatus();
        checkEpicTime();
    }

    // проверка статуса эпика при изменении подзадач
    private void checkEpicStatus() {
        Status epicStatus;   // текущий статус эпика
        Status firstSubtaskStatus;  // статус первой подзадачи в эпике

        epicStatus = getStatus();

        if (getSubtasksEpic().isEmpty()) {
            epicStatus = Status.NEW;    // эпик новый, если не содержит подзадач
        }
        else {
            firstSubtaskStatus = subtasksEpic.get(0).getStatus();
            for (Subtask subtask : getSubtasksEpic()) {
                Status statusSubtask = subtask.getStatus();
                // если статус текущей подзадачи "IN_PROGRESS" или статус не соответствует первой подзадаче эпика,
                // то статус эпика "IN_PROGRESS"
                if ((statusSubtask == Status.IN_PROGRESS) || (statusSubtask != firstSubtaskStatus)) {
                    epicStatus = Status.IN_PROGRESS;
                    break;
                }
                epicStatus = firstSubtaskStatus;
            }
        }
        setStatus(epicStatus);
    }

    // проверка времени эпика при изменении подзадач
    private void checkEpicTime() {

        Optional<LocalDateTime> startTimeEpic = Optional.empty();
        Optional<LocalDateTime> endTimeEpic = Optional.empty();
        Duration durationSummary = Duration.ZERO;

        if (!getSubtasksEpic().isEmpty()) {
            startTimeEpic = subtasksEpic.get(0).getStartTime();

            for (Subtask subtask : getSubtasksEpic()) {
                Optional<LocalDateTime> startTimeSubtask = subtask.getStartTime();
                Duration durationSubtask = subtask.getDuration();

                durationSummary = durationSummary.plus(durationSubtask);

                if (startTimeEpic.isEmpty() && startTimeSubtask.isPresent()) {
                    startTimeEpic = startTimeSubtask;
                    endTimeEpic = Optional.of(startTimeSubtask.get().plus(durationSubtask));
                } else {
                    if (startTimeSubtask.isPresent()) {
                        if (startTimeEpic.get().isAfter(startTimeSubtask.get())) {
                            startTimeEpic = startTimeSubtask;
                        }
                        if (endTimeEpic.isEmpty()) {
                            endTimeEpic = Optional.of(startTimeSubtask.get().plus(durationSubtask));
                        } else if (endTimeEpic.get().isBefore(startTimeSubtask.get().plus(durationSubtask))) {
                                endTimeEpic = Optional.of(startTimeSubtask.get().plus(durationSubtask));
                            }

                    }
                }
            }

        }
        setStartTime(startTimeEpic);
        setDuration(durationSummary);
        endTime = endTimeEpic;
    }

    @Override
    public Optional<LocalDateTime> getEndTime() {
        return endTime;
    }
}