package com.anton.tasks.repository;

import com.anton.tasks.model.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

}
