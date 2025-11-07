package com.campusconnect.userservice.config;
import com.campusconnect.userservice.entity.Role;
import com.campusconnect.userservice.entity.User;
import com.campusconnect.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private static final String DEFAULT_PROFILE_IMAGE = "https://i.imgur.com/example.png";

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String provider = userRequest.getClientRegistration().getRegistrationId();

        if (attributes.get("id") == null) {
            throw new OAuth2AuthenticationException("Facebook did not return a user ID.");
        }
        String providerId = attributes.get("id").toString();

        if (attributes.get("name") == null) {
            throw new OAuth2AuthenticationException("Facebook did not return a name.");
        }
        String name = attributes.get("name").toString();

        Object emailObject = attributes.get("email");
        if (emailObject == null) {
            throw new OAuth2AuthenticationException("Email not found from Facebook. Please ensure your Facebook account has a verified email and that you have granted permission to share it.");
        }
        String email = emailObject.toString();

        String profileImageUrl = DEFAULT_PROFILE_IMAGE;
        if (attributes.containsKey("picture")) {
            try {
                Map<String, Object> picture = (Map<String, Object>) attributes.get("picture");
                Map<String, Object> data = (Map<String, Object>) picture.get("data");
                profileImageUrl = data.get("url").toString();
            } catch (Exception e) {
            }
        }

        Optional<User> userOptional = userRepository.findByProviderAndProviderId(provider, providerId);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.setName(name);
            user.setProfileImageUrl(profileImageUrl);
        } else {
            Optional<User> localUserOptional = userRepository.findByEmail(email);
            if (localUserOptional.isPresent()) {
                user = localUserOptional.get();
                user.setProvider(provider);
                user.setProviderId(providerId);
                user.setName(name);
                user.setProfileImageUrl(profileImageUrl);
                user.setVerified(true);
            } else {
                // This is a brand new user
                user = User.builder()
                        .name(name)
                        .email(email)
                        .department("Not Set")
                        .profileImageUrl(profileImageUrl)
                        .provider(provider)
                        .providerId(providerId)
                        .role(Role.USER)
                        .isVerified(true)
                        .build();
            }

        }
        userRepository.save(user);
        return oAuth2User;
    }
}