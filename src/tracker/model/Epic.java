package tracker.model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private List<Subtask> subtasksEpic;

    public Epic(String name, String description, Status status) {
        super(name, description, status);
        subtasksEpic = new ArrayList<>();
    }

    @Override
    public String toString() {
        List<Integer> subtaskIds  = new ArrayList<>();
        for (Subtask subtask : subtasksEpic) {
            subtaskIds.add(subtask.getTaskId());
        }
        String result = super.getName() +
                ": " + super.getDescription() +
                " (" + super.getStatus() +
                ", id=" + super.getTaskId() +
                ", subIds=" + subtaskIds +
                ")";
        return result.toUpperCase();
    }

    public List<Subtask> getSubtasksEpic() {
        return subtasksEpic;
    }

    public void addSubtaskEpic(Subtask subtask) {
        subtasksEpic.add(subtask);
        checkEpicStatus();
    }

    public void deleteSubtask(Subtask subtask) {
        subtasksEpic.remove(subtask);
        checkEpicStatus();
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
}