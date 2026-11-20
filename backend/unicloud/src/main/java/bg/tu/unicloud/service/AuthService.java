package bg.tu.unicloud.service;

import bg.tu.unicloud.dto.AuthTokenRequest;
import bg.tu.unicloud.dto.AuthTokenResponse;
import bg.tu.unicloud.security.AuthenticatedUser;
import bg.tu.unicloud.security.JwtTokenService;
import bg.tu.unicloud.security.UserClaimService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenService jwtTokenService;
    private final UserClaimService userClaimService;

    public AuthTokenResponse issueToken(AuthTokenRequest request) {
        String username = request.getUsername() != null ? request.getUsername().trim() : "test-user";
        String role = request.getRole() != null ? request.getRole().trim().toUpperCase() : null;
        String email = request.getEmail() != null ? request.getEmail().trim() : null;
        String fullName = request.getFullName() != null ? request.getFullName().trim() : null;
        String sub = request.getSub() != null ? request.getSub().trim() : null;
        List<Long> courses = request.getCourses();

        // Preset profiles support
        if ("student1".equalsIgnoreCase(username)) {
            role = role != null ? role : "STUDENT";
            email = email != null ? email : "student1@tu-sofia.bg";
            fullName = fullName != null ? fullName : "Студент Първи";
            sub = sub != null ? sub : "usr-student-1";
            courses = courses != null ? courses : List.of(101L, 205L);
        } else if ("student2".equalsIgnoreCase(username)) {
            role = role != null ? role : "STUDENT";
            email = email != null ? email : "student2@tu-sofia.bg";
            fullName = fullName != null ? fullName : "Студент Втори";
            sub = sub != null ? sub : "usr-student-2";
            courses = courses != null ? courses : List.of(101L);
        } else if ("teacher1".equalsIgnoreCase(username)) {
            role = role != null ? role : "TEACHER";
            email = email != null ? email : "teacher1@tu-sofia.bg";
            fullName = fullName != null ? fullName : "Доц. Иван Петров";
            sub = sub != null ? sub : "usr-teacher-1";
            courses = courses != null ? courses : List.of(101L);
        } else if ("teacher2".equalsIgnoreCase(username)) {
            role = role != null ? role : "TEACHER";
            email = email != null ? email : "teacher2@tu-sofia.bg";
            fullName = fullName != null ? fullName : "Проф. Георги Димитров";
            sub = sub != null ? sub : "usr-teacher-2";
            courses = courses != null ? courses : List.of(205L);
        } else if ("admin1".equalsIgnoreCase(username)) {
            role = role != null ? role : "ADMIN";
            email = email != null ? email : "admin1@tu-sofia.bg";
            fullName = fullName != null ? fullName : "Системен Администратор";
            sub = sub != null ? sub : "usr-admin-1";
            courses = courses != null ? courses : List.of();
        } else if ("service-judge".equalsIgnoreCase(username)) {
            role = role != null ? role : "SERVICE";
            email = email != null ? email : "judge@autojudge.tu-sofia.bg";
            fullName = fullName != null ? fullName : "AutoJudge Integration Service";
            sub = sub != null ? sub : "usr-service-judge";
            courses = courses != null ? courses : List.of();
        } else {
            // Generic fallback
            if (role == null) role = "STUDENT";
            if (email == null) email = username + "@tu-sofia.bg";
            if (fullName == null) fullName = username;
            if (sub == null) sub = "usr-" + username;
            if (courses == null) courses = List.of();
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", sub);
        claims.put("email", email);
        claims.put("name", fullName);
        claims.put("role", role);
        claims.put("courses", courses);

        // Map and synchronize user in DB (also validates the role!)
        AuthenticatedUser user = userClaimService.mapAndSynchronizeUser(claims);

        long durationSeconds = 1800; // 30 minutes
        String token = jwtTokenService.createToken(
                user.getExternalSubject(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                courses,
                durationSeconds
        );

        return AuthTokenResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(durationSeconds)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .expiresAt(Instant.now().plusSeconds(durationSeconds))
                .build();
    }
}
