package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.ValidateUnwrappedValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GraphQLCustomizeExceptionHandlerTest {

  private GraphQLCustomizeExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
  }

  private DataFetchingEnvironment mockDataFetchingEnvironment() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    ExecutionStepInfo stepInfo = mock(ExecutionStepInfo.class);
    when(stepInfo.getPath()).thenReturn(ResultPath.rootPath());
    when(dfe.getExecutionStepInfo()).thenReturn(stepInfo);
    return dfe;
  }

  @Test
  void onException_withInvalidAuthenticationException_returnsUnauthenticatedError() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(mockDataFetchingEnvironment())
            .exception(exception)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void onException_withConstraintViolationException_returnsBadRequestError() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(new TestConstraintViolation("createUser.param.email", "should be an email"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(mockDataFetchingEnvironment())
            .exception(cve)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void onException_withOtherException_delegatesToDefaultHandler() {
    RuntimeException exception = new RuntimeException("some error");
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(mockDataFetchingEnvironment())
            .exception(exception)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
  }

  @Test
  void getErrorsAsData_withConstraintViolations_returnsError() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(new TestConstraintViolation("createUser.param.email", "should be an email"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertFalse(error.getErrors().isEmpty());
    assertEquals("email", error.getErrors().get(0).getKey());
  }

  @Test
  void getErrorsAsData_withMultipleViolationsOnSameField_groupsErrors() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(new TestConstraintViolation("createUser.param.email", "should be an email"));
    violations.add(new TestConstraintViolation("createUser.param.email", "already exists"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
  }

  @Test
  void getErrorsAsData_withSingleSegmentPath_returnsPathAsIs() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(new TestConstraintViolation("email", "invalid"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertFalse(error.getErrors().isEmpty());
    assertEquals("email", error.getErrors().get(0).getKey());
  }

  @Test
  void getErrorsAsData_withMultipleFields_createsMultipleErrorItems() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(new TestConstraintViolation("createUser.param.email", "invalid email"));
    violations.add(new TestConstraintViolation("createUser.param.username", "already taken"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertEquals(2, error.getErrors().size());
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static class TestConstraintViolation implements ConstraintViolation<Object> {
    private final String propertyPath;
    private final String message;

    TestConstraintViolation(String propertyPath, String message) {
      this.propertyPath = propertyPath;
      this.message = message;
    }

    @Override
    public String getMessage() {
      return message;
    }

    @Override
    public String getMessageTemplate() {
      return message;
    }

    @Override
    public Object getRootBean() {
      return null;
    }

    @Override
    public Class<Object> getRootBeanClass() {
      return Object.class;
    }

    @Override
    public Object getLeafBean() {
      return null;
    }

    @Override
    public Object[] getExecutableParameters() {
      return new Object[0];
    }

    @Override
    public Object getExecutableReturnValue() {
      return null;
    }

    @Override
    public Path getPropertyPath() {
      return new Path() {
        @Override
        public java.util.Iterator<Node> iterator() {
          return Collections.emptyIterator();
        }

        @Override
        public String toString() {
          return propertyPath;
        }
      };
    }

    @Override
    public Object getInvalidValue() {
      return null;
    }

    @Override
    public ConstraintDescriptor<?> getConstraintDescriptor() {
      return new TestConstraintDescriptor();
    }

    @Override
    public <U> U unwrap(Class<U> type) {
      return null;
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static class TestConstraintDescriptor implements ConstraintDescriptor {
    @Override
    public Annotation getAnnotation() {
      return new TestAnnotation();
    }

    @Override
    public String getMessageTemplate() {
      return "";
    }

    @Override
    public Set getGroups() {
      return new HashSet();
    }

    @Override
    public Set getPayload() {
      return new HashSet();
    }

    @Override
    public javax.validation.ConstraintTarget getValidationAppliesTo() {
      return null;
    }

    @Override
    public List getConstraintValidatorClasses() {
      return Collections.emptyList();
    }

    @Override
    public Map getAttributes() {
      return new HashMap();
    }

    @Override
    public Set getComposingConstraints() {
      return new HashSet();
    }

    @Override
    public boolean isReportAsSingleViolation() {
      return false;
    }

    @Override
    public ValidateUnwrappedValue getValueUnwrapping() {
      return ValidateUnwrappedValue.DEFAULT;
    }

    @Override
    public Object unwrap(Class type) {
      return null;
    }
  }

  private static class TestAnnotation implements Annotation {
    @Override
    public Class<? extends Annotation> annotationType() {
      return Override.class;
    }
  }
}
