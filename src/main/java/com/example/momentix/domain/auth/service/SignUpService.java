package com.example.momentix.domain.auth.service;

import com.example.momentix.domain.auth.dto.request.SignUpRequest;
import com.example.momentix.domain.auth.repository.SignInRepository;
import com.example.momentix.domain.users.entity.Users;
import com.example.momentix.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import com.example.momentix.domain.common.exception.auth.AuthErrorException;

import static com.example.momentix.domain.common.exception.auth.AuthErrorCode.*;

@Service
@RequiredArgsConstructor
public class SignUpService {
    private static final String HOST_PREFIX = "momentixHost";
    private final UserRepository userRepository;
    private final SignInRepository signInRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long signUpUser(String email, SignUpRequest req) {
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new AuthErrorException(BAD_REQUEST);
        }
        if (signInRepository.existsByUsername(email)) {
            throw new AuthErrorException(CONFLICT);
        }

        Users users = Users.createConsumer(email, req, passwordEncoder);

        userRepository.save(users);
        return users.getUserId();
    }

    @Transactional
    public Map<String, String> signUpHost(SignUpRequest signUpRequest) {

        if (userRepository.existsByBusinessNumber(signUpRequest.getBusinessNumber())) {
            throw new AuthErrorException(CONFLICT);
        }

        String username = nextHostUsername(); // ex) momentixHost0001!
        String rawPassword = username;        // 아이디와 비밀번호 동일

        // User + SignIn 객체 생성 및 연관관계 연결
        Users users = Users.createHost(signUpRequest.getBusinessNumber(), username, rawPassword, passwordEncoder);
        userRepository.save(users);

        Map<String, String> creds = new HashMap<>();
        creds.put("username", username);
        creds.put("password", rawPassword);

        return creds;
    }

    private String nextHostUsername() {
        for (int width : new int[]{4, 5, 6}) {
            int max = (int) Math.pow(10, width) - 1;
            for (int i = 1; i <= max; i++) {
                String candidate = HOST_PREFIX + String.format("%0" + width + "d", i) + "!";
                if (!signInRepository.existsByUsername(candidate)) {
                    return candidate;
                }
            }
        }
        throw new AuthErrorException(HOST_ID_NOT_AVAILABLE);
    }
}
