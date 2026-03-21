package com.example.momentix.domain.auth.entity;

import com.example.momentix.domain.users.entity.Users;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Table(name = "Signin")
@Entity
@NoArgsConstructor
public class SignIn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long signInId;
    @Column(nullable = false, unique = true)
    private String username; // 로그인 ID(컨슈머는 이메일)
    @Column(nullable = false)
    private String password;
    // Users 하드 삭제 허용
    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", referencedColumnName = "userId", unique = true)
    private Users users;
    //소프트 삭제용
    @Column(nullable = false)
    private boolean isDeleted = false;
    private LocalDateTime withdrawnAt; //언제 탈퇴?

    //캡슐화, new+set을 안해도 되게 만듦
    public static SignIn create(String username, String rawPassword, PasswordEncoder encoder, Users users) {
        SignIn signIn = new SignIn();
        signIn.setUsername(username);
        signIn.setPassword(encoder.encode(rawPassword));
        //Users에도 setSignIn()을 해줘야 양방향 관계 완성됨
        signIn.setUsers(users);
        return signIn;
    }

    public Long getSignInId() {
        return signInId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Users getUser() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }

    // 탈퇴처리
    public void markAsWithdrawn() {
        this.isDeleted = true;
        this.withdrawnAt = LocalDateTime.now();
        this.password = "__DELETED__" + java.util.UUID.randomUUID(); // 로그인 불가화
        this.users = null; // Users FK 끊기
    }
}
