package com.openjob.common.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.lang.NonNull;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Data
@Entity
@Table(name="admin")
public class Admin {
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @Column(columnDefinition = "CHAR(32)")
    @Id
    protected String id;
    @Column(nullable = false, unique = true)
    @Size(min=3, max = 20)
    private String username;
    @Column(nullable = false)
    @Size(min=8, max = 32)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    @Column(nullable = false, columnDefinition = "default true")
    private Boolean isActive;
    @Column(nullable = false)
    @Size(max = 20)
    private String firstName;
    @Column(nullable = false)
    @Size(max = 20)
    private String lastName;


}
