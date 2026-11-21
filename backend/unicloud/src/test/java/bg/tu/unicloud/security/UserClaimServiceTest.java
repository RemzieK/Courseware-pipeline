package bg.tu.unicloud.security;

import bg.tu.unicloud.exception.AccessForbiddenException;
import bg.tu.unicloud.model.Role;
import bg.tu.unicloud.model.User;
import bg.tu.unicloud.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserClaimServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserClaimService userClaimService;

    @BeforeEach
    void setUp() {
        userClaimService = new UserClaimService(userRepository);
    }

    @Test
    @DisplayName("Successfully maps claims for STUDENT")
    void testMapStudentClaims() {
        Map<String, Object> claims = Map.of(
                "sub", "usr-student-1",
                "email", "student1@tu-sofia.bg",
                "name", "Студент Тест",
                "role", "STUDENT",
                "courses", List.of(101L, 205L)
        );

        User user = User.builder()
                .id(UUID.randomUUID())
                .externalSubject("usr-student-1")
                .email("student1@tu-sofia.bg")
                .fullName("Студент Тест")
                .role(Role.STUDENT)
                .active(true)
                .build();

        when(userRepository.findByExternalSubject("usr-student-1")).thenReturn(Optional.of(user));

        AuthenticatedUser result = userClaimService.mapAndSynchronizeUser(claims);

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(Role.STUDENT);
        assertThat(result.getEmail()).isEqualTo("student1@tu-sofia.bg");
        assertThat(result.getCourses()).containsExactly(101L, 205L);
    }

    @Test
    @DisplayName("Rejects missing role claim")
    void testMissingRole() {
        Map<String, Object> claims = Map.of(
                "sub", "usr-1",
                "email", "user@tu-sofia.bg"
        );

        assertThatThrownBy(() -> userClaimService.mapAndSynchronizeUser(claims))
                .isInstanceOf(AccessForbiddenException.class)
                .hasMessageContaining("роля");
    }

    @Test
    @DisplayName("Rejects unsupported roles DEAN and INSPECTOR")
    void testUnsupportedRoles() {
        Map<String, Object> deanClaims = Map.of(
                "sub", "usr-dean-1",
                "email", "dean@tu-sofia.bg",
                "role", "DEAN"
        );

        assertThatThrownBy(() -> userClaimService.mapAndSynchronizeUser(deanClaims))
                .isInstanceOf(AccessForbiddenException.class)
                .hasMessageContaining("Неподдържана или невалидна потребителска роля: DEAN");

        Map<String, Object> inspectorClaims = Map.of(
                "sub", "usr-insp-1",
                "email", "inspector@tu-sofia.bg",
                "role", "INSPECTOR"
        );

        assertThatThrownBy(() -> userClaimService.mapAndSynchronizeUser(inspectorClaims))
                .isInstanceOf(AccessForbiddenException.class)
                .hasMessageContaining("Неподдържана или невалидна потребителска роля: INSPECTOR");
    }

    @Test
    @DisplayName("Rejects missing sub claim")
    void testMissingSub() {
        Map<String, Object> claims = Map.of(
                "email", "user@tu-sofia.bg",
                "role", "STUDENT"
        );

        assertThatThrownBy(() -> userClaimService.mapAndSynchronizeUser(claims))
                .isInstanceOf(AccessForbiddenException.class)
                .hasMessageContaining("sub");
    }

    @Test
    @DisplayName("Rejects inactive user account")
    void testInactiveUser() {
        Map<String, Object> claims = Map.of(
                "sub", "usr-inactive",
                "email", "inactive@tu-sofia.bg",
                "role", "STUDENT"
        );

        User inactiveUser = User.builder()
                .id(UUID.randomUUID())
                .externalSubject("usr-inactive")
                .email("inactive@tu-sofia.bg")
                .fullName("Inactive User")
                .role(Role.STUDENT)
                .active(false)
                .build();

        when(userRepository.findByExternalSubject("usr-inactive")).thenReturn(Optional.of(inactiveUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> userClaimService.mapAndSynchronizeUser(claims))
                .isInstanceOf(AccessForbiddenException.class)
                .hasMessageContaining("деактивиран");
    }
}
