package BaiTapLon;

import java.time.LocalDate;

public class Task {
    private int id;
    private String title;
    private String description;
    private String assigneeId;
    private String creatorUsername;
    private LocalDate deadline;
    private String status;

    public Task(int id, String title, String description, String assigneeId, String creatorUsername, LocalDate deadline, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.assigneeId = assigneeId;
        this.creatorUsername = creatorUsername;
        this.deadline = deadline;
        this.status = status;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAssigneeId() { return assigneeId; }
    public String getCreatorUsername() { return creatorUsername; }
    public LocalDate getDeadline() { return deadline; }
    public String getStatus() { return status; }
}