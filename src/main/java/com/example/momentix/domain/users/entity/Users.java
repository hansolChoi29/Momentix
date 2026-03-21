package com.example.momentix.domain.users.entity;

import com.example.momentix.domain.auth.dto.request.SignUpRequest;
import com.example.momentix.domain.auth.entity.RoleType;
import com.example.momentix.domain.auth.entity.SignIn;
import com.example.momentix.domain.common.entity.TimeStamped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Table(name = "users")
@Getter
@Entity
@NoArgsConstructor
public class Users extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(length = 10)
    private String nickname;

    @Column(length = 11)  // 01012345678
    private String phoneNumber;

    @Column
    private LocalDate birthDate; //LocalDate는 length 의미없음

    @Column(length = 10)  // host만 입력 (사업자번호)
    private String businessNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleType role;   // "USER", "ADMIN", "HOST"

    @Column(length = 12)
    private String slackId;

    @Column(nullable = false)
    private boolean isDeleted = false;

    //CascadeType.PERSIST: Users새로 저장 시 SignIn도 같이 저장
    //CascadeType.MERGE: Users 수정 시 SignIn도 같이 수정
    @OneToOne(mappedBy = "users", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private SignIn signIn;

    // users - auth 식별관계 users.id + auth.id
    // users - auth 비식별관계 users.id <- 이것만

    private String email;

    //Consumer(일반 유저) 회원가입 시 Users + SignIn 객체를 생성
    // 양방향, 1:1 관계 (한 유저당 로그인 계정 하나)
    public static Users createConsumer(String email, SignUpRequest dto, PasswordEncoder encoder) {
        // 1. Users 엔티티 기본 정보 세팅
        Users users = new Users();
        users.setRole(RoleType.CONSUMER);// 일반 유저 역할 부여
        users.setNickname(dto.getNickname());
        users.setPhoneNumber(dto.getPhoneNumber());
        users.setBirthDate(LocalDate.parse(dto.getBirthDate()));
        // 2. SignIn 엔티티 생성 (로그인 계정 정보)
        // SignIn.create() 내부에서 비밀번호 암호화 + 양방향 연관관계(users ↔ signIn) 연결까지 처리
        SignIn signIn = SignIn.create(email, dto.getPassword(), encoder, users);
        // 3. Users에도 signIn 세팅 (양방향 관계 유지)
        users.setSignIn(signIn);

        return users;
    }

    // Host(호스트 회원) 회원가입 시 Users + SignIn 객체를 생성하는 정적 메서드
    public static Users createHost(String businessNumber, String username, String rawPassword,
                                   PasswordEncoder encoder) {
        Users users = new Users();
        users.setRole(RoleType.HOST);
        users.setBusinessNumber(businessNumber);

        SignIn signIn = SignIn.create(username, rawPassword, encoder, users);
        users.setSignIn(signIn);

        return users;
    }

    public String getEmail() {
        return email;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setBusinessNumber(String businessNumber) {
        this.businessNumber = businessNumber;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }

    public void setSignIn(SignIn signIn) {
        this.signIn = signIn;
    }


}
