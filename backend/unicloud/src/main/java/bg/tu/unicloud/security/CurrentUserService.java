package bg.tu.unicloud.security;

import bg.tu.unicloud.exception.AccessForbiddenException;
import bg.tu.unicloud.model.Role;
import bg.tu.unicloud.model.User;
import bg.tu.unicloud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public Optional<AuthenticatedUser> getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public AuthenticatedUser requireAuthenticatedUser() {
        return getAuthenticatedUser()
                .orElseThrow(() -> new AccessForbiddenException("Изисква се автентикиран потребител."));
    }

    public User requireCurrentUserEntity() {
        AuthenticatedUser authUser = requireAuthenticatedUser();
        return userRepository.findById(authUser.getId())
                .orElseThrow(() -> new AccessForbiddenException("Потребителят не е намерен в базата данни."));
    }

    public boolean isAdmin() {
        return getAuthenticatedUser()
                .map(u -> u.getRole() == Role.ADMIN)
                .orElse(false);
    }
}
