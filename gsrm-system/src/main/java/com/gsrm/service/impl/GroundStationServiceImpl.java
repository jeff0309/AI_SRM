package com.gsrm.service.impl;

import com.gsrm.domain.dto.request.GroundStationRequest;
import com.gsrm.domain.entity.GroundStation;
import com.gsrm.domain.entity.StationUnavailability;
import com.gsrm.domain.enums.FrequencyBand;
import com.gsrm.exception.ConflictException;
import com.gsrm.exception.ResourceNotFoundException;
import com.gsrm.repository.GroundStationRepository;
import com.gsrm.repository.StationUnavailabilityRepository;
import com.gsrm.service.GroundStationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 地面站服務實作.
 *
 * <p>涵蓋地面站 CRUD 操作與維護時段管理。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroundStationServiceImpl implements GroundStationService {

    private final GroundStationRepository         groundStationRepository;
    private final StationUnavailabilityRepository unavailabilityRepository;

    /* ─────────── CRUD ─────────── */

    /** {@inheritDoc} */
    @Override
    @Transactional
    public GroundStation createGroundStation(GroundStationRequest request) {
        if (groundStationRepository.existsByName(request.getName())) {
            throw new ConflictException("地面站", "name", request.getName());
        }

        GroundStation station = GroundStation.builder()
                .name(request.getName())
                .code(request.getCode())
                .longitude(request.getLongitude())
                .latitude(request.getLatitude())
                .altitude(request.getAltitude())
                .setupTime(request.getSetupTime())
                .teardownTime(request.getTeardownTime())
                .frequencyBand(request.getFrequencyBand())
                .minElevation(request.getMinElevation() != null ? request.getMinElevation() : 5.0)
                .description(request.getDescription())
                .contactPerson(request.getContactPerson())
                .contactPhone(request.getContactPhone())
                .enabled(request.getEnabled() == null || request.getEnabled())
                .build();

        GroundStation saved = groundStationRepository.save(station);
        log.info("[GroundStationService] 建立地面站：{}，頻段：{}", saved.getName(), saved.getFrequencyBand());
        return saved;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public GroundStation updateGroundStation(Long id, GroundStationRequest request) {
        GroundStation station = getGroundStationById(id);

        if (!station.getName().equals(request.getName())
                && groundStationRepository.existsByName(request.getName())) {
            throw new ConflictException("地面站", "name", request.getName());
        }

        station.setName(request.getName());
        station.setCode(request.getCode());
        station.setLongitude(request.getLongitude());
        station.setLatitude(request.getLatitude());
        station.setAltitude(request.getAltitude());
        station.setSetupTime(request.getSetupTime());
        station.setTeardownTime(request.getTeardownTime());
        station.setFrequencyBand(request.getFrequencyBand());
        if (request.getMinElevation() != null) {
            station.setMinElevation(request.getMinElevation());
        }
        station.setDescription(request.getDescription());
        station.setContactPerson(request.getContactPerson());
        station.setContactPhone(request.getContactPhone());
        if (request.getEnabled() != null) {
            station.setEnabled(request.getEnabled());
        }

        GroundStation saved = groundStationRepository.save(station);
        log.info("[GroundStationService] 更新地面站：{}", saved.getName());
        return saved;
    }

    /** {@inheritDoc} */
    @Override
    public GroundStation getGroundStationById(Long id) {
        return groundStationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("地面站", id));
    }

    /** {@inheritDoc} */
    @Override
    public GroundStation getGroundStationByName(String name) {
        return groundStationRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("地面站", "name", name));
    }

    /** {@inheritDoc} */
    @Override
    public Page<GroundStation> getAllGroundStations(Pageable pageable) {
        return groundStationRepository.findAll(pageable);
    }

    /** {@inheritDoc} */
    @Override
    public List<GroundStation> getEnabledGroundStations() {
        return groundStationRepository.findByEnabledTrue();
    }

    /** {@inheritDoc} */
    @Override
    public List<GroundStation> getGroundStationsByFrequencyBand(FrequencyBand frequencyBand) {
        return groundStationRepository.findByFrequencyBand(frequencyBand);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void deleteGroundStation(Long id) {
        GroundStation station = getGroundStationById(id);
        groundStationRepository.delete(station);
        log.info("[GroundStationService] 刪除地面站：{}", station.getName());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public GroundStation setGroundStationEnabled(Long id, boolean enabled) {
        GroundStation station = getGroundStationById(id);
        station.setEnabled(enabled);
        GroundStation saved = groundStationRepository.save(station);
        log.info("[GroundStationService] 地面站 {} 啟用狀態設為：{}", station.getName(), enabled);
        return saved;
    }

    /* ─────────── 維護時段管理 ─────────── */

    /** {@inheritDoc} */
    @Override
    @Transactional
    public StationUnavailability addUnavailability(Long groundStationId,
                                                   LocalDateTime startTime,
                                                   LocalDateTime endTime,
                                                   String reason,
                                                   Long userId) {
        GroundStation station = getGroundStationById(groundStationId);

        StationUnavailability unavailability = StationUnavailability.builder()
                .groundStation(station)
                .startTime(startTime)
                .endTime(endTime)
                .reason(reason)
                .createdBy(userId)
                .build();

        StationUnavailability saved = unavailabilityRepository.save(unavailability);
        log.info("[GroundStationService] 新增維護時段，地面站：{}，{} ~ {}",
                station.getName(), startTime, endTime);
        return saved;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public int importUnavailabilities(List<StationUnavailability> unavailabilities, Long userId) {
        int count = 0;
        for (StationUnavailability u : unavailabilities) {
            if (u.getCreatedBy() == null) {
                u.setCreatedBy(userId);
            }
            unavailabilityRepository.save(u);
            count++;
        }
        log.info("[GroundStationService] 批次匯入維護時段 {} 筆", count);
        return count;
    }

    /** {@inheritDoc} */
    @Override
    public List<StationUnavailability> getUnavailabilities(Long groundStationId,
                                                           LocalDateTime startTime,
                                                           LocalDateTime endTime) {
        if (startTime != null && endTime != null) {
            return unavailabilityRepository.findByGroundStationAndTimeRange(
                    groundStationId, startTime, endTime);
        }
        return unavailabilityRepository.findByGroundStationId(groundStationId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void deleteUnavailability(Long unavailabilityId) {
        StationUnavailability u = unavailabilityRepository.findById(unavailabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("維護時段", unavailabilityId));
        unavailabilityRepository.delete(u);
        log.info("[GroundStationService] 刪除維護時段 ID：{}", unavailabilityId);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAvailable(Long groundStationId, LocalDateTime startTime, LocalDateTime endTime) {
        return unavailabilityRepository
                .findByGroundStationAndTimeRange(groundStationId, startTime, endTime)
                .isEmpty();
    }
}
