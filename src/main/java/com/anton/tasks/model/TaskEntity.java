package com.anton.tasks.model;

import com.anton.tasks.exceptions.task.InvalidTaskStatusException;
import jakarta.persistence.*;

@Entity
@Table(name = "tasks")
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;
    @ManyToOne
    @JoinColumn(name = "assigned_user_id")
    private UserEntity assignedUser;

    protected TaskEntity() {}

    public TaskEntity(String title, String description, TaskStatus status, UserEntity assignedUser) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.assignedUser = assignedUser;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public UserEntity getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(UserEntity assignedUser) {
        this.assignedUser = assignedUser;
    }

    public void moveToNextStatus() {
        switch (this.status) {
            case TaskStatus.TODO -> this.status = TaskStatus.IN_PROGRESS;
            case TaskStatus.IN_PROGRESS -> this.status = TaskStatus.DONE;
            case TaskStatus.DONE -> throw new InvalidTaskStatusException("Task is already DONE and cannot advance to the next status.");
        }
    }
}
