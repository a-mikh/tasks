package com.anton.tasks.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String username;

    protected UserEntity() {
    }

    public UserEntity(String userName) {
        this.username = userName;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

}
