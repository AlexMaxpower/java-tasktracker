package tracker.model;

import java.util.ArrayList;

public class Epic extends Task {

    private ArrayList<Subtask> subtasksEpic;

    public Epic(String name, String description, String status) {
        super(name, description, status);
        subtasksEpic = new ArrayList<>();
    }

    @Override
    public String toString() {
        ArrayList<Integer> subtaskIds  = new ArrayList<>();
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

    public ArrayList<Subtask> getSubtasksEpic() {
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
        String epicStatus;   // текущий статус эпика
        String firstSubtaskStatus;  // статус первой подзадачи в эпике

        epicStatus = getStatus();

        if (getSubtasksEpic().isEmpty()) {
            epicStatus = "NEW";    // эпик новый, если не содержит подзадач
        }
        else {
            firstSubtaskStatus = subtasksEpic.get(0).getStatus();
            for (Subtask subtask : getSubtasksEpic()) {
                String statusSubtask = subtask.getStatus();
                // если статус текущей подзадачи "IN_PROGRESS" или статус не соответствует первой подзадаче эпика,
                // то статус эпика "IN_PROGRESS"
                if (statusSubtask.equals("IN_PROGRESS") || !statusSubtask.equals(firstSubtaskStatus)) {
                    epicStatus = "IN_PROGRESS";
                    break;
                }
                epicStatus = firstSubtaskStatus;
            }
        }
        setStatus(epicStatus);
    }
}