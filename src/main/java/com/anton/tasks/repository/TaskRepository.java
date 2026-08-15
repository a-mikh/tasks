package com.anton.tasks.repository;

import com.anton.tasks.model.TaskEntity;
import com.anton.tasks.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    List<TaskEntity> findByStatus(TaskStatus status);

    List<TaskEntity> findByAssignedUser_Username(String assignee);

    List<TaskEntity> findByStatusAndAssignedUser_Username(TaskStatus status, String assignee);
}
