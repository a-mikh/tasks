package com.anton.tasks.repository;

import com.anton.tasks.model.TaskEntity;
import com.anton.tasks.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    Page<TaskEntity> findByStatus(TaskStatus status, Pageable pageable);

    Page<TaskEntity> findByAssignedUser_Username(String assignee, Pageable pageable);

    Page<TaskEntity> findByStatusAndAssignedUser_Username(
            TaskStatus status,
            String assignee,
            Pageable pageable
    );
}
