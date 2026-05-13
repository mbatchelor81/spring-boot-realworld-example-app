package com.ftgo.SERVICENAME.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.ftgo.testlib.containers.FtgoContainers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Template: Repository integration test with Testcontainers MySQL.
 *
 * <p>Uses {@code @DataJpaTest} for a thin JPA slice and Testcontainers for a real
 * MySQL instance. Flyway migrations run automatically against the container.
 *
 * <p>Instructions:
 * <ol>
 *   <li>Replace SERVICENAME with your service name</li>
 *   <li>Replace YourRepository and YourEntity with actual class names</li>
 *   <li>Add test methods for CRUD operations and custom queries</li>
 * </ol>
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Tag("integration")
class YourRepositoryIntegrationTest {

  @Container static MySQLContainer<?> mysql = FtgoContainers.mysql();

  @DynamicPropertySource
  static void dbProperties(DynamicPropertyRegistry registry) {
    FtgoContainers.configureMysql(registry, mysql);
  }

  // @Autowired private YourRepository yourRepository;

  @Test
  void save_validEntity_persistsSuccessfully() {
    // var entity = YourBuilder.aYourEntity().withField("value").build();
    //
    // var saved = yourRepository.save(entity);
    //
    // assertThat(saved.getId()).isNotNull();
    // assertThat(saved.getField()).isEqualTo("value");
  }

  @Test
  void findById_existingEntity_returnsEntity() {
    // var entity = YourBuilder.aYourEntity().build();
    // var saved = yourRepository.save(entity);
    //
    // var found = yourRepository.findById(saved.getId());
    //
    // assertThat(found).isPresent();
    // assertThat(found.get().getId()).isEqualTo(saved.getId());
  }

  @Test
  void findById_nonExistentId_returnsEmpty() {
    // var found = yourRepository.findById(999L);
    //
    // assertThat(found).isEmpty();
  }

  @Test
  void delete_existingEntity_removesFromDatabase() {
    // var entity = YourBuilder.aYourEntity().build();
    // var saved = yourRepository.save(entity);
    //
    // yourRepository.deleteById(saved.getId());
    //
    // assertThat(yourRepository.findById(saved.getId())).isEmpty();
  }
}
