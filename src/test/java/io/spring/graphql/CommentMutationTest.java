package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.user.User;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.CommentPayload;
import io.spring.graphql.types.DeletionStatus;
import java.util.Arrays;
import java.util.Optional;
import org.joda.time.DateTime;
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
class CommentMutationTest {

  @Mock private ArticleRepository articleRepository;
  @Mock private CommentRepository commentRepository;
  @Mock private CommentQueryService commentQueryService;

  @InjectMocks private CommentMutation commentMutation;

  private User user;
  private Article article;

  @BeforeEach
  void setUp() {
    user = new User("test@example.com", "testuser", "password", "", "");
    article = new Article("Title", "Desc", "Body", Arrays.asList(), user.getId());
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, AuthorityUtils.createAuthorityList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void createComment_withValidInput_returnsCommentPayload() {
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    ProfileData profileData = new ProfileData(user.getId(), "testuser", "", "", false);
    CommentData commentData =
        new CommentData(
            "comment-id",
            "Great article!",
            article.getId(),
            new DateTime(),
            new DateTime(),
            profileData);
    when(commentQueryService.findById(any(), eq(user))).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result =
        commentMutation.createComment("title", "Great article!");

    assertNotNull(result);
    assertNotNull(result.getData());
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  void createComment_withNoAuth_throwsAuthenticationException() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    assertThrows(
        AuthenticationException.class, () -> commentMutation.createComment("title", "body"));
  }

  @Test
  void createComment_withNonExistentArticle_throwsResourceNotFoundException() {
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.createComment("nonexistent", "body"));
  }

  @Test
  void removeComment_withAuthorizedUser_deletesAndReturnsSuccess() {
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    Comment comment = new Comment("body", user.getId(), article.getId());
    when(commentRepository.findById(article.getId(), comment.getId()))
        .thenReturn(Optional.of(comment));

    DeletionStatus result = commentMutation.removeComment("title", comment.getId());

    assertTrue(result.getSuccess());
    verify(commentRepository).remove(comment);
  }

  @Test
  void removeComment_withUnauthorizedUser_throwsNoAuthorizationException() {
    User articleOwner = new User("owner@example.com", "owner", "pass", "", "");
    User commentOwner = new User("commenter@example.com", "commenter", "pass", "", "");
    Article otherArticle =
        new Article("Other", "Desc", "Body", Arrays.asList(), articleOwner.getId());
    when(articleRepository.findBySlug("other")).thenReturn(Optional.of(otherArticle));
    Comment comment = new Comment("body", commentOwner.getId(), otherArticle.getId());
    when(commentRepository.findById(otherArticle.getId(), comment.getId()))
        .thenReturn(Optional.of(comment));

    assertThrows(
        NoAuthorizationException.class,
        () -> commentMutation.removeComment("other", comment.getId()));
  }

  @Test
  void removeComment_withNonExistentComment_throwsResourceNotFoundException() {
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), "nonexistent")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("title", "nonexistent"));
  }

  @Test
  void removeComment_withNoAuth_throwsAuthenticationException() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    assertThrows(AuthenticationException.class, () -> commentMutation.removeComment("title", "id"));
  }

  @Test
  void removeComment_withNonExistentArticle_throwsResourceNotFoundException() {
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> commentMutation.removeComment("nonexistent", "id"));
  }
}
