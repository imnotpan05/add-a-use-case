package team.projectpulse.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import team.projectpulse.user.PeerEvaluationUser;
import team.projectpulse.user.UserRepository;

/** Rejects bearer tokens whose user has since been disabled or removed. */
public class ActiveAccountJwtValidator implements OAuth2TokenValidator<Jwt> {

    private final UserRepository userRepository;

    public ActiveAccountJwtValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        Object rawUserId = jwt.getClaims().get("userId");
        if (!(rawUserId instanceof Number number)
                || number.longValue() < Integer.MIN_VALUE
                || number.longValue() > Integer.MAX_VALUE) {
            return invalidToken();
        }

        Integer userId = number.intValue();
        PeerEvaluationUser user = this.userRepository.findById(userId).orElse(null);
        if (user == null || !user.isEnabled()) {
            return invalidToken();
        }
        return OAuth2TokenValidatorResult.success();
    }

    private OAuth2TokenValidatorResult invalidToken() {
        return OAuth2TokenValidatorResult.failure(
                new OAuth2Error("invalid_token", "The account associated with this token is not active.", null));
    }
}
