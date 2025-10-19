package com.example.ExpenseTrackerAPI.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Authentication_User")
@Getter
@Setter
@NoArgsConstructor
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String username;

    private String password;

    private  String email;

    @OneToOne
    private AppUser appUser;

    public AuthUser(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
}
