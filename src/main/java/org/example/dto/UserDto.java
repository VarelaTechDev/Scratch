package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// The DTOs for user registration and login.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String username;
}
