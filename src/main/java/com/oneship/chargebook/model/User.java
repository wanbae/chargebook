package com.oneship.chargebook.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "users") // 테이블 이름을 명시적으로 변경
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String username;
    private String password;
    private String role;

    @OneToMany(mappedBy = "user")
    private Set<ChargeData> chargeData;
}
