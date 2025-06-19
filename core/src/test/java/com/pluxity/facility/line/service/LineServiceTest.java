package com.pluxity.facility.line.service;

import com.pluxity.facility.line.Line;
import com.pluxity.facility.line.LineRepository;
import com.pluxity.facility.line.LineService; // Import the service under test
import com.pluxity.facility.line.dto.LineCreateRequest;
import com.pluxity.facility.line.dto.LineResponse;
import com.pluxity.facility.line.dto.LineUpdateRequest;
import com.pluxity.facility.line.mapper.LineMapper;
import com.pluxity.facility.station.Station;
import com.pluxity.facility.station.StationLine; // Import for mocking
import com.pluxity.facility.station.StationService;
import com.pluxity.global.exception.CustomException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
// import org.mockito.Spy; // Removed, spies created locally
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

// import java.util.ArrayList; // Removed
// import java.util.Collections; // Removed
import java.util.List;
import java.util.Optional;
// import java.util.stream.Collectors; // Not directly used in test logic

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class LineServiceTest {

    @Mock
    private LineRepository lineRepository;

    @Mock
    private LineMapper lineMapper;

    @Mock
    private StationService stationService;

    @InjectMocks
    private LineService lineService;

    private Line line; // A general line instance for some tests
    private LineCreateRequest createRequest;
    private LineUpdateRequest updateRequest;
    private LineResponse lineResponseDto;
    private Station station; // A general station instance

    @BeforeEach
    void setUp() {
        // This manual injection is needed because of @Lazy and setter injection in LineService
        lineService.setStationService(stationService);

        line = Line.builder().name("Test Line").color("Blue").build();
        line.setId(1L); // Simulate it's a persisted entity

        createRequest = new LineCreateRequest("New Line", "Red");
        updateRequest = new LineUpdateRequest("Updated Line", "Green");

        // This DTO is what the mapper is expected to return for 'line'
        lineResponseDto = new LineResponse(1L, "Test Line", "Blue");

        station = new Station("Test Station Name", "Test Station Desc", "Route X");
        station.setId(10L); // Simulate persisted station
    }

    @Test
    @DisplayName("save: 중복되지 않는 이름으로 라인 저장 시 ID 반환")
    void save_whenNameNotExists_shouldSaveAndReturnId() {
        Line lineFromMapper = Line.builder().name(createRequest.name()).color(createRequest.color()).build();
        Line savedLine = Line.builder().name(createRequest.name()).color(createRequest.color()).build();
        savedLine.setId(2L); // Simulate ID set after save

        when(lineRepository.findByName(createRequest.name())).thenReturn(Optional.empty());
        when(lineMapper.fromLineCreateRequest(createRequest)).thenReturn(lineFromMapper);
        when(lineRepository.save(lineFromMapper)).thenReturn(savedLine);

        Long savedId = lineService.save(createRequest);

        assertEquals(savedLine.getId(), savedId);
        verify(lineRepository).save(lineFromMapper);
    }

    @Test
    @DisplayName("save: 중복된 이름으로 라인 저장 시 CustomException 발생")
    void save_whenNameExists_shouldThrowCustomException() {
        when(lineRepository.findByName(createRequest.name())).thenReturn(Optional.of(line)); // line has name "Test Line"

        LineCreateRequest duplicateNameRequest = new LineCreateRequest("Test Line", "Purple");

        CustomException exception = assertThrows(CustomException.class, () -> lineService.save(duplicateNameRequest));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("이미 존재하는 호선입니다.", exception.getErrorMessage());
        verify(lineRepository, never()).save(any(Line.class));
    }

    @Test
    @DisplayName("findById: 라인이 존재할 경우 LineResponse 반환")
    void findById_whenLineExists_shouldReturnLineResponse() {
        when(lineRepository.findById(1L)).thenReturn(Optional.of(line));
        when(lineMapper.toLineResponse(line)).thenReturn(lineResponseDto);

        LineResponse actualResponse = lineService.findById(1L);

        assertEquals(lineResponseDto, actualResponse);
        verify(lineMapper).toLineResponse(line);
    }

    @Test
    @DisplayName("findById: 라인이 존재하지 않을 경우 CustomException 발생")
    void findById_whenLineNotExists_shouldThrowCustomException() {
        when(lineRepository.findById(1L)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> lineService.findById(1L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    @DisplayName("findLineById: 라인이 존재하지 않을 경우 CustomException 발생 (내부 헬퍼)")
    void findLineById_whenLineNotExists_shouldThrowCustomException() {
        when(lineRepository.findById(1L)).thenReturn(Optional.empty());
        CustomException exception = assertThrows(CustomException.class, () -> lineService.findLineById(1L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    @DisplayName("findAll: 모든 라인을 LineResponse 리스트로 반환")
    void findAll_shouldReturnListOfLineResponses() {
        List<Line> lines = List.of(line);
        when(lineRepository.findAll()).thenReturn(lines);
        when(lineMapper.toLineResponse(line)).thenReturn(lineResponseDto);

        List<LineResponse> responses = lineService.findAll();

        assertThat(responses).isNotNull().hasSize(1);
        assertThat(responses.get(0)).isEqualTo(lineResponseDto);
        verify(lineMapper).toLineResponse(line);
    }

    @Test
    @DisplayName("update: 라인이 존재할 경우 정보 업데이트")
    void update_whenLineExists_shouldUpdateLine() {
        when(lineRepository.findById(1L)).thenReturn(Optional.of(line));
        // lineMapper.updateLineFromRequest is void
        // lineRepository.save is called in the service

        lineService.update(1L, updateRequest);

        verify(lineMapper).updateLineFromRequest(updateRequest, line);
        verify(lineRepository).save(line);
    }

    @Test
    @DisplayName("update: 라인이 존재하지 않을 경우 CustomException 발생")
    void update_whenLineNotExists_shouldThrowCustomException() {
        when(lineRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CustomException.class, () -> lineService.update(1L, updateRequest));
    }

    @Test
    @DisplayName("delete: 라인이 존재하고 연관된 스테이션이 있을 경우, 스테이션에서 라인 제거 후 라인 삭제")
    void delete_whenLineExists_shouldRemoveLineFromStationsAnddeleteLine() {
        // Arrange
        Line spiedLine = spy(line); // Use a spy to mock getStationLines()

        Station mockStation = mock(Station.class); // Mock the station to verify removeLine call
        StationLine mockStationLine = mock(StationLine.class);
        when(mockStationLine.getStation()).thenReturn(mockStation);

        // Use doReturn for spies
        doReturn(List.of(mockStationLine)).when(spiedLine).getStationLines();

        when(lineRepository.findById(1L)).thenReturn(Optional.of(spiedLine));

        // Act
        lineService.delete(1L);

        // Assert
        verify(mockStation).removeLine(spiedLine);
        verify(lineRepository).delete(spiedLine);
    }

    @Test
    @DisplayName("delete: 라인이 존재하지 않을 경우 CustomException 발생")
    void delete_whenLineNotExists_shouldThrowCustomException() {
        when(lineRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CustomException.class, () -> lineService.delete(1L));
    }

    @Test
    @DisplayName("addStationToLine: 라인과 스테이션이 존재할 경우 스테이션에 라인 추가")
    void addStationToLine_whenLineAndStationExist_shouldCallStationAddLine() {
        when(lineRepository.findById(1L)).thenReturn(Optional.of(line));
        when(stationService.findStationById(10L)).thenReturn(station);

        lineService.addStationToLine(1L, 10L);

        verify(station).addLine(line);
    }

    @Test
    @DisplayName("findStationsByLineId: 라인이 존재할 경우 연관된 스테이션 ID 목록 반환")
    void findStationsByLineId_whenLineExists_shouldReturnStationIds() {
        // Arrange
        Line spiedLine = spy(line);

        Station stationInLine = new Station("Station In Line", "Desc", "Route S");
        stationInLine.setId(20L); // Critical: ensure the station has an ID

        StationLine mockStationLine = mock(StationLine.class);
        when(mockStationLine.getStation()).thenReturn(stationInLine);

        doReturn(List.of(mockStationLine)).when(spiedLine).getStationLines();

        when(lineRepository.findById(1L)).thenReturn(Optional.of(spiedLine));

        // Act
        List<Long> stationIds = lineService.findStationsByLineId(1L);

        // Assert
        assertThat(stationIds).isNotNull().hasSize(1).containsExactly(20L);
    }
}
