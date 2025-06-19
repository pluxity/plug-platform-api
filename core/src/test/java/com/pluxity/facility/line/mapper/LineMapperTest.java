package com.pluxity.facility.line.mapper;

import com.pluxity.facility.line.Line;
import com.pluxity.facility.line.dto.LineCreateRequest;
import com.pluxity.facility.line.dto.LineResponse;
import com.pluxity.facility.line.dto.LineUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LineMapperTest {

    private final LineMapper lineMapper = LineMapper.INSTANCE;

    @Test
    @DisplayName("Line 엔티티를 LineResponse DTO로 정확히 매핑한다")
    void toLineResponse_shouldMapCorrectly() {
        // Arrange
        Line line = Line.builder().name("Test Line").color("Blue").build();
        line.setId(1L); // Simulate ID set by persistence

        // Act
        LineResponse response = lineMapper.toLineResponse(line);

        // Assert
        assertNotNull(response);
        assertEquals(line.getId(), response.id());
        assertEquals(line.getName(), response.name());
        assertEquals(line.getColor(), response.color());
    }

    @Test
    @DisplayName("Line 엔티티가 null일 경우 null을 반환한다")
    void toLineResponse_whenLineIsNull_shouldReturnNull() {
        assertNull(lineMapper.toLineResponse(null));
    }

    @Test
    @DisplayName("LineCreateRequest DTO를 Line 엔티티로 정확히 매핑한다")
    void fromLineCreateRequest_shouldMapCorrectly() {
        // Arrange
        LineCreateRequest request = new LineCreateRequest("New Line", "Red");

        // Act
        Line line = lineMapper.fromLineCreateRequest(request);

        // Assert
        assertNotNull(line);
        assertEquals(request.name(), line.getName());
        assertEquals(request.color(), line.getColor());
        assertNull(line.getId()); // ID should not be set from create request by mapper
    }

    @Test
    @DisplayName("LineCreateRequest DTO가 null일 경우 null을 반환한다")
    void fromLineCreateRequest_whenRequestIsNull_shouldReturnNull() {
        assertNull(lineMapper.fromLineCreateRequest(null));
    }

    @Test
    @DisplayName("LineUpdateRequest DTO로 Line 엔티티 필드를 업데이트한다")
    void updateLineFromRequest_shouldUpdateFields() {
        // Arrange
        Line line = Line.builder().name("Old Name").color("Old Color").build();
        line.setId(1L); // Simulate existing entity
        LineUpdateRequest request = new LineUpdateRequest("New Name", "New Color");

        // Act
        lineMapper.updateLineFromRequest(request, line);

        // Assert
        assertEquals("New Name", line.getName());
        assertEquals("New Color", line.getColor());
        assertEquals(1L, line.getId()); // ID should remain unchanged
    }

    @Test
    @DisplayName("LineUpdateRequest의 name이 null이면 Line 엔티티의 name을 업데이트하지 않는다")
    void updateLineFromRequest_withNullName_shouldNotUpdateName() {
        // Arrange
        Line line = Line.builder().name("Old Name").color("Old Color").build();
        LineUpdateRequest request = new LineUpdateRequest(null, "New Color");

        // Act
        lineMapper.updateLineFromRequest(request, line);

        // Assert
        assertEquals("Old Name", line.getName()); // Name should not change
        assertEquals("New Color", line.getColor());
    }

    @Test
    @DisplayName("LineUpdateRequest의 color가 null이면 Line 엔티티의 color를 업데이트하지 않는다")
    void updateLineFromRequest_withNullColor_shouldNotUpdateColor() {
        // Arrange
        Line line = Line.builder().name("Old Name").color("Old Color").build();
        LineUpdateRequest request = new LineUpdateRequest("New Name", null);

        // Act
        lineMapper.updateLineFromRequest(request, line);

        // Assert
        assertEquals("New Name", line.getName());
        assertEquals("Old Color", line.getColor()); // Color should not change
    }

    @Test
    @DisplayName("LineUpdateRequest의 모든 필드가 null이면 Line 엔티티를 업데이트하지 않는다")
    void updateLineFromRequest_withAllNulls_shouldNotUpdateAnything() {
        // Arrange
        Line line = Line.builder().name("Old Name").color("Old Color").build();
        LineUpdateRequest request = new LineUpdateRequest(null, null);

        // Act
        lineMapper.updateLineFromRequest(request, line);

        // Assert
        assertEquals("Old Name", line.getName());
        assertEquals("Old Color", line.getColor());
    }

    @Test
    @DisplayName("LineUpdateRequest가 null이면 예외를 던지지 않고 Line 엔티티를 업데이트하지 않는다")
    void updateLineFromRequest_whenRequestIsNull_shouldNotThrowExceptionAndNotUpdate() {
        // Arrange
        Line line = Line.builder().name("Old Name").color("Old Color").build();
        String originalName = line.getName();
        String originalColor = line.getColor();

        // Act & Assert
        assertDoesNotThrow(() -> lineMapper.updateLineFromRequest(null, line));
        assertEquals(originalName, line.getName());
        assertEquals(originalColor, line.getColor());
    }

     @Test
     @DisplayName("대상 Line 엔티티가 null이면 예외를 던지지 않는다")
    void updateLineFromRequest_whenTargetLineIsNull_shouldNotThrowException() {
        // Arrange
        LineUpdateRequest request = new LineUpdateRequest("New Name", "New Color");

        // Act & Assert
        // MapStruct typically handles null @MappingTarget gracefully (does nothing).
        assertDoesNotThrow(() -> lineMapper.updateLineFromRequest(request, null));
    }
}
