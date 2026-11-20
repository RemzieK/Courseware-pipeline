package bg.tu.unicloud.security;

import bg.tu.unicloud.exception.AccessForbiddenException;
import bg.tu.unicloud.model.Role;
import bg.tu.unicloud.model.User;
import bg.tu.unicloud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserClaimService {

    private final UserRepository userRepository;

    /**
     * Maps and validates claims from SSO/OIDC or JWT token, synchronizing the user in the database.
     */
    @Transactional
    public AuthenticatedUser mapAndSynchronizeUser(Map<String, Object> claims) {
        String sub = extractString(claims, "sub");
        if (sub == null || sub.isBlank()) {
            throw new AccessForbiddenException("Липсва задължително твърдение 'sub' в токена.");
        }

        String email = extractString(claims, "email");
        if (email == null || email.isBlank()) {
            throw new AccessForbiddenException("Липсва задължително твърдение 'email' в токена.");
        }

        String fullName = extractString(claims, "name");
        if (fullName == null || fullName.isBlank()) {
            fullName = email;
        }

        Role role = resolveAndValidateRole(claims.get("role"));

        List<Long> courses = extractCourses(claims.get("courses"));

        // Synchronize with database
        User user = findOrCreateUser(sub, email, fullName, role);

        if (!user.isActive()) {
            throw new AccessForbiddenException("Потребителският акаунт е деактивиран.");
        }

        return AuthenticatedUser.builder()
                .id(user.getId())
                .externalSubject(user.getExternalSubject())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .courses(courses)
                .build();
    }

    public Role resolveAndValidateRole(Object roleClaim) {
        if (roleClaim == null) {
            throw new AccessForbiddenException("Липсва твърдение за роля (role). Достъпът се отказва.");
        }

        String roleStr = roleClaim.toString().trim().toUpperCase();
        try {
            Role role = Role.valueOf(roleStr);
            // Strict enforcement: ensure only the 4 supported roles exist
            if (role != Role.STUDENT && role != Role.TEACHER && role != Role.ADMIN && role != Role.SERVICE) {
                throw new AccessForbiddenException("Неподдържана роля в системата: " + roleStr);
            }
            return role;
        } catch (IllegalArgumentException e) {
            log.warn("Rejected unsupported role: {}", roleStr);
            throw new AccessForbiddenException("Неподдържана или невалидна потребителска роля: " + roleStr);
        }
    }

    private User findOrCreateUser(String sub, String email, String fullName, Role role) {
        Optional<User> bySubject = userRepository.findByExternalSubject(sub);
        if (bySubject.isPresent()) {
            User existing = bySubject.get();
            boolean updated = false;
            if (!email.equalsIgnoreCase(existing.getEmail())) {
                existing.setEmail(email);
                updated = true;
            }
            if (!fullName.equals(existing.getFullName())) {
                existing.setFullName(fullName);
                updated = true;
            }
            if (existing.getRole() != role) {
                existing.setRole(role);
                updated = true;
            }
            return updated ? userRepository.save(existing) : existing;
        }

        Optional<User> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            User existing = byEmail.get();
            existing.setExternalSubject(sub);
            existing.setFullName(fullName);
            existing.setRole(role);
            return userRepository.save(existing);
        }

        User newUser = User.builder()
                .externalSubject(sub)
                .email(email)
                .fullName(fullName)
                .role(role)
                .active(true)
                .build();

        return userRepository.save(newUser);
    }

    private String extractString(Map<String, Object> claims, String key) {
        Object val = claims.get(key);
        return val != null ? val.toString().trim() : null;
    }

    @SuppressWarnings("unchecked")
    private List<Long> extractCourses(Object coursesClaim) {
        if (coursesClaim == null) {
            return Collections.emptyList();
        }
        if (coursesClaim instanceof List<?> list) {
            return list.stream()
                    .map(item -> {
                        if (item instanceof Number number) {
                            return number.longValue();
                        }
                        try {
                            return Long.parseLong(item.toString());
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    })
                    .filter(item -> item != null)
                    .toList();
        }
        return Collections.emptyList();
    }
}
