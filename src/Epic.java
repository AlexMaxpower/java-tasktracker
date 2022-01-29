import java.util.ArrayList;

public class Epic extends Task {

    private ArrayList<Integer> subtaskIds;

    public Epic(String name, String description, String status) {
        super(name, description, status);
        subtaskIds = new ArrayList<>();
    }

    @Override
    public String toString() {
        String result = super.getName() +
                ": " + super.getDescription() +
                " (" + super.getStatus() +
                ", id=" + super.getTaskId() +
                ", subIds=" + subtaskIds +
                ")";
        return result.toUpperCase();
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void deleteSubtaskId(Integer id) {
        subtaskIds.remove(id);
    }
}
