package com.campusconnect.userservice.config;

import com.campusconnect.userservice.entity.Role;
import com.campusconnect.userservice.entity.User;
import com.campusconnect.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;
    private static final String DEFAULT_PROFILE_IMAGE = "https://i.imgur.com/example.png";

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // 3. Load the OIDC user from Google
        OidcUser oidcUser = super.loadUser(userRequest);
        Map<String, Object> attributes = oidcUser.getAttributes();

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = attributes.get("sub").toString();
        String email = attributes.get("email").toString();
        String name = attributes.get("name").toString();
        String profileImageUrl = attributes.get("picture").toString();

        Optional<User> userOptional = userRepository.findByProviderAndProviderId(provider, providerId);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.setName(name);
            user.setProfileImageUrl(profileImageUrl);
        } else {
            Optional<User> localUser = userRepository.findByEmail(email);
            if (localUser.isPresent()) {
                throw new OAuth2AuthenticationException("An account with this email already exists. Please log in with your password.");
            }
            user = User.builder()
                    .name(name)
                    .email(email)
                    .department("Not Set")
                    .profileImageUrl(profileImageUrl)
                    .provider(provider)
                    .providerId(providerId)
                    .role(Role.USER)
                    .isVerified(true)
                    .password(UUID.randomUUID().toString())
                    .build();
        }

        userRepository.save(user);

        return oidcUser;
    }
}