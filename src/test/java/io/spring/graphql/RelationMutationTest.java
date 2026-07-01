package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ProfilePayload;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class RelationMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private ProfileQueryService profileQueryService;

  @InjectMocks private RelationMutation relationMutation;

  private User currentUser;
  private User targetUser;

  @BeforeEach
  void setUp() {
    currentUser = new User("current@example.com", "currentuser", "pass", "", "");
    targetUser = new User("target@example.com", "targetuser", "pass", "", "");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(
            currentUser, null, AuthorityUtils.createAuthorityList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void follow_withExistingUser_returnsProfilePayload() {
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(targetUser));
    ProfileData profileData = new ProfileData(targetUser.getId(), "targetuser", "", "", true);
    when(profileQueryService.findByUsername("targetuser", currentUser))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.follow("targetuser");

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("targetuser", result.getProfile().getUsername());
    assertTrue(result.getProfile().getFollowing());
    verify(userRepository).saveRelation(any(FollowRelation.class));
  }

  @Test
  void follow_withNonExistentUser_throwsResourceNotFoundException() {
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.follow("nonexistent"));
  }

  @Test
  void follow_withNoAuth_throwsAuthenticationException() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    assertThrows(AuthenticationException.class, () -> relationMutation.follow("targetuser"));
  }

  @Test
  void unfollow_withExistingRelation_returnsProfilePayload() {
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(targetUser));
    FollowRelation relation = new FollowRelation(currentUser.getId(), targetUser.getId());
    when(userRepository.findRelation(currentUser.getId(), targetUser.getId()))
        .thenReturn(Optional.of(relation));
    ProfileData profileData = new ProfileData(targetUser.getId(), "targetuser", "", "", false);
    when(profileQueryService.findByUsername("targetuser", currentUser))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.unfollow("targetuser");

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertFalse(result.getProfile().getFollowing());
    verify(userRepository).removeRelation(relation);
  }

  @Test
  void unfollow_withNonExistentUser_throwsResourceNotFoundException() {
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("nonexistent"));
  }

  @Test
  void unfollow_withNoExistingRelation_throwsResourceNotFoundException() {
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(targetUser));
    when(userRepository.findRelation(currentUser.getId(), targetUser.getId()))
        .thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("targetuser"));
  }

  @Test
  void unfollow_withNoAuth_throwsAuthenticationException() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    assertThrows(AuthenticationException.class, () -> relationMutation.unfollow("targetuser"));
  }
}
