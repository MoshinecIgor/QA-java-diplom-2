package ru.praktikum.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;

@Getter
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    private String email;
    private String name;
    private String password;

    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
