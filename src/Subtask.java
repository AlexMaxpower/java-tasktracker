public class Subtask extends Task {

    private int epicId;

    public Subtask(String name, String description, String status, int epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {

        return  "  - " + super.getName() +
                ": " + super.getDescription() +
                " (" + super.getStatus() +
                ", ID=" + super.getTaskId() +
                ", epicID=" + epicId +
                ")";
    }
}
