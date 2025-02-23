package com.example.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity(name = "users")
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fullName;
    private Boolean hasFullName = Boolean.FALSE;

    private String phoneNumber;
    private Boolean hasPhoneNumber = Boolean.FALSE;

    private String employmentActivity;
    private Boolean hasEmploymentActivity = Boolean.FALSE;

    private String chatId;
    private String step;
    private Boolean isAdmin = false;
}
