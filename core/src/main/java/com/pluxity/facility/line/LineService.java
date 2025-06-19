package com.pluxity.facility.line;

import com.pluxity.facility.line.dto.LineCreateRequest;
import com.pluxity.facility.line.dto.LineResponse;
import com.pluxity.facility.line.dto.LineUpdateRequest;
import com.pluxity.facility.line.mapper.LineMapper; // Added import
import com.pluxity.facility.station.Station;
import com.pluxity.facility.station.StationService;
import com.pluxity.global.exception.CustomException;
// import java.util.ArrayList; // Removed
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LineService {

    private final LineRepository lineRepository;
    private final LineMapper lineMapper; // Added field
    private StationService stationService; // Setter injection remains

    @Autowired
    public void setStationService(@Lazy StationService stationService) {
        this.stationService = stationService;
    }

    // Updated constructor
    public LineService(LineRepository lineRepository, LineMapper lineMapper) {
        this.lineRepository = lineRepository;
        this.lineMapper = lineMapper;
    }

    @Transactional
    public Long save(LineCreateRequest request) {
        lineRepository
                .findByName(request.name())
                .ifPresent(
                        line -> {
                            throw new CustomException(
                                    "Line already exists", HttpStatus.BAD_REQUEST, "이미 존재하는 호선입니다.");
                        });
        Line line = lineMapper.fromLineCreateRequest(request); // Use mapper
        return lineRepository.save(line).getId();
    }

    @Transactional(readOnly = true)
    public List<LineResponse> findAll() {
        return lineRepository.findAll().stream()
                .map(lineMapper::toLineResponse) // Use mapper
                .toList(); // .toList() is fine, or .collect(Collectors.toList())
    }

    @Transactional(readOnly = true)
    public LineResponse findById(Long id) {
        Line line = lineRepository.findById(id).orElseThrow(LineService::notFoundException);
        return lineMapper.toLineResponse(line); // Use mapper
    }

    @Transactional(readOnly = true)
    public Line findLineById(Long id) {
        return lineRepository.findById(id).orElseThrow(LineService::notFoundException);
    }

    @Transactional(readOnly = true)
    public List<Long> findStationsByLineId(Long lineId) {
        Line line = findLineById(lineId);
        // Assuming Line.getStations() returns List<Station> or similar
        // And Station entity has getId(). This part is not directly related to LineMapper.
        return line.getStationLines().stream() // Changed from getStations() to getStationLines() as per Station entity
                .map(stationLine -> stationLine.getStation().getId()) // Access station from StationLine
                .collect(Collectors.toList());
    }

    @Transactional
    public void update(Long id, LineUpdateRequest request) {
        Line line = findLineById(id);
        lineMapper.updateLineFromRequest(request, line); // Use mapper
        lineRepository.save(line); // Explicitly save
    }

    @Transactional
    public void delete(Long id) {
        Line line = findLineById(id);

        // Create a new list to avoid ConcurrentModificationException if removeLine modifies the list being iterated
        List<Station> stationsAssociatedWithLine = line.getStationLines().stream()
                                                       .map(sl -> sl.getStation())
                                                       .collect(Collectors.toList());

        for (Station station : stationsAssociatedWithLine) {
            station.removeLine(line);
            // stationService.save(station) or similar might be needed if removeLine doesn't trigger persistence
            // or if Station is not managing the bidirectional relationship persistence fully.
            // For now, assuming Station.removeLine handles its side of the relationship.
        }
        // Clear the association from Line's side before deleting Line
        // This is important if StationLine has FK to Line and cascade isn't set from StationLine to Line
        // Or if Line's @OneToMany to StationLine is not orphanRemoval=true
        // line.getStationLines().clear(); // This might be an alternative if Station.removeLine is not sufficient or if cascading is an issue

        lineRepository.delete(line);
    }

    @Transactional
    public void addStationToLine(Long lineId, Long stationId) {
        Line line = findLineById(lineId);
        Station station = stationService.findStationById(stationId);
        // The addLine method in Station handles the bidirectional relationship
        station.addLine(line);
        // No explicit save needed for line or station here if they are managed entities
        // and addLine correctly updates the relationship from the owning side (StationLine).
    }

    private static CustomException notFoundException() {
        return new CustomException("Line not found", HttpStatus.NOT_FOUND, "해당 호선을 찾을 수 없습니다.");
    }
}
