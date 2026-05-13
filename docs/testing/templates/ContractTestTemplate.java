package com.ftgo.SERVICENAME.contract;

import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Template: Contract test base class for Spring Cloud Contract verification.
 *
 * <p>This base class is referenced by the Spring Cloud Contract plugin to generate
 * verifier tests from contracts defined in src/test/resources/contracts/.
 *
 * <p>Instructions:
 * <ol>
 *   <li>Replace SERVICENAME with your service name</li>
 *   <li>Replace YourService with the actual service class</li>
 *   <li>Set up mock responses matching your contract definitions</li>
 *   <li>Add contracts in src/test/resources/contracts/</li>
 *   <li>Configure the contracts plugin in build.gradle:
 *       <pre>
 *       contracts {
 *           testFramework = TestFramework.JUNIT5
 *           baseClassForTests = 'com.ftgo.SERVICENAME.contract.YourServiceContractBase'
 *       }
 *       </pre>
 *   </li>
 * </ol>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Tag("contract")
public abstract class YourServiceContractBase {

  @Autowired private MockMvc mockMvc;

  // @MockBean private YourService yourService;

  @BeforeEach
  void setup() {
    RestAssuredMockMvc.mockMvc(mockMvc);

    // Set up mocks matching your contract scenarios:
    //
    // when(yourService.findById(1L))
    //     .thenReturn(Optional.of(YourFixtures.defaultEntity()));
    //
    // when(yourService.findById(999L))
    //     .thenReturn(Optional.empty());
  }
}
