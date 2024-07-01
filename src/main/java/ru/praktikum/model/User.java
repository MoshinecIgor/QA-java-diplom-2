package ru.praktikum.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    private String email;
    @Setter
    private String name;
    private String password;

    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

}
