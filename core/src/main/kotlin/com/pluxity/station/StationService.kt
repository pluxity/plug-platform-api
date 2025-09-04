package com.pluxity.station

import com.pluxity.facility.FacilityService
import com.pluxity.facility.dto.toResponse
import com.pluxity.facility.strategy.FloorService
import com.pluxity.feature.dto.toFeatureResponse
import com.pluxity.feature.entity.Feature
import com.pluxity.file.service.FileService
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.MappingUtils
import com.pluxity.global.utils.SortUtils
import com.pluxity.label3d.Label3DRepository
import com.pluxity.label3d.toLabel3DResponse
import com.pluxity.station.dto.StationCreateRequest
import com.pluxity.station.dto.StationInfoResponse
import com.pluxity.station.dto.StationResponse
import com.pluxity.station.dto.StationResponseWithFeature
import com.pluxity.station.dto.StationUpdateRequest
import jakarta.persistence.EntityManager
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Stream

@Service
class StationService(
    private val fileService: FileService,
    private val facilityService: FacilityService,
    private val floorService: FloorService,
    private val stationRepository: StationRepository,
    private val lineService: LineService,
    private val label3DRepository: Label3DRepository,
    private val stationCodeService: StationCodeService,
    private val stationLineService: StationLineService,
    private val em: EntityManager,
) {
    @Transactional
    fun save(request: StationCreateRequest): Long {
        val station = Station(request.facility.name, request.facility.description)
        val saved = facilityService.save(station, request.facility)

        floorService.save(saved, request.floors)

        if (!request.lineIds.isEmpty()) {
            for (lineId in request.lineIds) {
                val line = lineService.findLineById(lineId)
                stationLineService.save(station, line)
            }
        }
        if (!request.stationCodes.isEmpty()) {
            for (stationCode in request.stationCodes) {
                stationCodeService.save(station, stationCode)
            }
        }

        return saved.id
    }

    @Transactional(readOnly = true)
    fun findAll(): List<StationResponse> {
        val stations = stationRepository.findAll(SortUtils.orderByCreatedAtDesc)
        val fileMap =
            MappingUtils.getFileMapByIds(
                stations,
                { v: Station -> Stream.of(v.drawingFileId, v.thumbnailFileId) },
                fileService,
            )
        val floorMap = floorService.findAllByFacilities(stations)
        val stationCodeMap: Map<Station, List<String>> =
            stationCodeService.findCodeMapByStationIds(stations)
        val lineMap: Map<Station, List<Long>> = stationLineService.findLineMapByStationIds(stations)
        return stations.map {
            StationResponse(
                it.toResponse(
                    fileMap[it.drawingFileId],
                    fileMap[it.thumbnailFileId],
                ),
                floorMap[it] ?: emptyList(),
                StationInfoResponse(lineMap[it] ?: emptyList(), stationCodeMap[it] ?: emptyList()),
            )
        }
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): StationResponse {
        val station = facilityService.findById(id) as Station
        val floorResponse = floorService.findAllByFacility(station)

        return StationResponse(
            station.toResponse(
                fileService.getFileResponse(station.drawingFileId),
                fileService.getFileResponse(station.thumbnailFileId),
            ),
            floorResponse,
            StationInfoResponse(
                stationLineService.findLinesByStation(station),
                stationCodeService.findCodesByStation(station),
            ),
        )
    }

    @Transactional(readOnly = true)
    fun findStationById(id: Long): Station =
        stationRepository
            .findByIdOrNull(id)
            ?: throw CustomException(ErrorCode.NOT_FOUND_STATION, id)

    @Transactional
    fun putUpdate(
        id: Long,
        request: StationUpdateRequest,
    ) {
        val station = findStationById(id)

        facilityService.putUpdate(id, request.facility)
        floorService.update(station, request.floors)

        stationLineService.deleteByStation(station)
        stationCodeService.deleteByStation(station)

        request.stationInfo?.let {
            for (lineId in it.lineIds) {
                val line = lineService.findLineById(lineId)
                stationLineService.save(station, line)
            }
            for (code in it.stationCodes) {
                stationCodeService.save(station, code)
            }
        }
    }

    @Transactional
    fun delete(id: Long) {
        // 삭제할 스테이션 조회
        val station = findStationById(id)

        // Floor 삭제 및 Facility 삭제
        floorService.delete(station)
        stationLineService.deleteByStation(station)
        stationCodeService.deleteByStation(station)
        em.flush()
        em.clear()
        facilityService.deleteFacility(id)
    }

    @Transactional
    fun addLineToStation(
        stationId: Long,
        lineId: Long,
    ) {
        val station = findStationById(stationId)
        val line = lineService.findLineById(lineId)

        // 이미 연결되어 있는지 확인
        if (!stationLineService.checkAlreadyConnect(station, line)) {
            stationLineService.save(station, line)
        }
    }

    @Transactional
    fun removeLineFromStation(
        stationId: Long,
        lineId: Long,
    ) {
        val station = findStationById(stationId)
        val line = lineService.findLineById(lineId)
        stationLineService.deleteStationLine(station, line)
    }

    @Transactional(readOnly = true)
    fun findStationWithFeatures(id: Long): StationResponseWithFeature {
        val station = findStationById(id)
        val floorResponse = floorService.findAllByFacility(station)

        val lineIds: List<Long> = stationLineService.findLinesByStation(station)

        val facilityResponse =
            station.toResponse(
                fileService.getFileResponse(station.drawingFileId),
                fileService.getFileResponse(station.thumbnailFileId),
            )

        val label3DFeatureIds =
            label3DRepository
                .findAllByFacilityId(id)
                .map { it.feature.id }

        val features =
            station.features
                .filter { feature: Feature -> !label3DFeatureIds.contains(feature.id) }
                .map { it.toFeatureResponse() }

        val label3Ds =
            label3DRepository
                .findAllByFacilityId(id)
                .map { it.toLabel3DResponse() }

        val stationCodes: List<String> = stationCodeService.findCodesByStation(station)

        return StationResponseWithFeature(
            facilityResponse,
            floorResponse,
            lineIds,
            features,
            label3Ds,
            stationCodes,
        )
    }
}
