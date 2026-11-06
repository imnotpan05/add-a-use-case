package team.projectpulse.security;

import org.junit.jupiter.api.Test;
import team.projectpulse.user.PeerEvaluationUser;
import team.projectpulse.user.UserRepository;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class ActiveAccountJwtValidatorTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final ActiveAccountJwtValidator validator = new ActiveAccountJwtValidator(this.userRepository);

    @Test
    void acceptsTokenForActiveAccount() {
        PeerEvaluationUser user = mock(PeerEvaluationUser.class);
        given(user.isEnabled()).willReturn(true);
        given(this.userRepository.findById(7)).willReturn(Optional.of(user));

        assertThat(this.validator.validate(token(7)).hasErrors()).isFalse();
    }

    @Test
    void rejectsTokenForDisabledAccount() {
        PeerEvaluationUser user = mock(PeerEvaluationUser.class);
        given(user.isEnabled()).willReturn(false);
        given(this.userRepository.findById(7)).willReturn(Optional.of(user));

        assertThat(this.validator.validate(token(7)).hasErrors()).isTrue();
    }

    @Test
    void rejectsTokenWithoutUserId() {
        assertThat(this.validator.validate(token(null)).hasErrors()).isTrue();
    }

    private Jwt token(Integer userId) {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .issuer("self")
                .subject("student")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(7200));
        if (userId != null) builder.claim("userId", userId);
        return builder.build();
    }
}
