package com.pluxity.facility.line;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager; // Not strictly necessary if using repository.save only
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LineRepositoryTest {

    @Autowired
    private TestEntityManager entityManager; // Can be useful for more complex scenarios

    @Autowired
    private LineRepository lineRepository;

    private Line line1;
    // private Line line2; // Not used in the provided tests, can be removed from setUp if not needed

    @BeforeEach
    void setUp() {
        // Consider cleaning up, though @DataJpaTest usually handles rollback for each test.
        // lineRepository.deleteAll();

        line1 = Line.builder().name("Line 1").color("Red").build();
        lineRepository.save(line1); // ID will be generated here

        Line line2 = Line.builder().name("Line 2").color("Blue").build();
        lineRepository.save(line2);

        entityManager.flush(); // Ensure data is persisted
        entityManager.clear(); // Detach all entities to ensure fresh loads from DB
    }

    @Test
    @DisplayName("새로운 Line 저장 및 ID로 조회 시 정상적으로 동작한다")
    void saveAndFindById_shouldWorkCorrectly() {
        // Arrange
        Line newLine = Line.builder().name("Line Alpha").color("Green").build();

        // Act
        Line savedLine = lineRepository.save(newLine);
        entityManager.flush();
        entityManager.clear();

        Optional<Line> foundLineOpt = lineRepository.findById(savedLine.getId());

        // Assert
        assertThat(foundLineOpt).isPresent();
        Line foundLine = foundLineOpt.get();
        assertThat(foundLine.getName()).isEqualTo("Line Alpha");
        assertThat(foundLine.getColor()).isEqualTo("Green");
    }

    @Test
    @DisplayName("findByName: 존재하는 이름으로 Line 조회 시 해당 Line 반환")
    void findByName_whenLineExists_shouldReturnLine() {
        // Act
        // Fetch line1 again to ensure it's managed in the current persistence context for ID comparison if needed
        Line persistedLine1 = lineRepository.findByName("Line 1").orElseThrow();

        Optional<Line> foundOpt = lineRepository.findByName("Line 1");

        // Assert
        assertThat(foundOpt).isPresent();
        Line foundLine = foundOpt.get();
        assertThat(foundLine.getName()).isEqualTo("Line 1");
        assertThat(foundLine.getColor()).isEqualTo("Red");
        assertThat(foundLine.getId()).isEqualTo(persistedLine1.getId()); // Compare ID with a fetched instance
    }

    @Test
    @DisplayName("findByName: 존재하지 않는 이름으로 Line 조회 시 빈 Optional 반환")
    void findByName_whenLineDoesNotExist_shouldReturnEmpty() {
        // Act
        Optional<Line> foundOpt = lineRepository.findByName("NonExistent Line");

        // Assert
        assertThat(foundOpt).isNotPresent();
    }
}
