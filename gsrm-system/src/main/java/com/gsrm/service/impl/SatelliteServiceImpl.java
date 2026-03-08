package com.gsrm.service.impl;

import com.gsrm.domain.dto.request.SatelliteCreateRequest;
import com.gsrm.domain.entity.GroundStation;
import com.gsrm.domain.entity.GroundStationPreference;
import com.gsrm.domain.entity.Satellite;
import com.gsrm.domain.enums.FrequencyBand;
import com.gsrm.exception.ConflictException;
import com.gsrm.exception.ResourceNotFoundException;
import com.gsrm.repository.GroundStationRepository;
import com.gsrm.repository.SatelliteRepository;
import com.gsrm.service.SatelliteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 衛星服務實作.
 *
 * <p>涵蓋衛星 CRUD 操作與地面站偏好優先序管理。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SatelliteServiceImpl implements SatelliteService {

    private final SatelliteRepository     satelliteRepository;
    private final GroundStationRepository groundStationRepository;

    /* ─────────── CRUD ─────────── */

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Satellite createSatellite(SatelliteCreateRequest request) {
        if (satelliteRepository.existsByName(request.getName())) {
            throw new ConflictException("衛星", "name", request.getName());
        }

        Satellite satellite = Satellite.builder()
                .name(request.getName())
                .code(request.getCode())
                .company(request.getCompany())
                .frequencyBand(request.getFrequencyBand())
                .minDailyPasses(request.getMinDailyPasses())
                .minPassDuration(request.getMinPassDuration())
                .priorityWeight(request.getPriorityWeight())
                .isEmergency(Boolean.TRUE.equals(request.getIsEmergency()))
                .description(request.getDescription())
                .contactPerson(request.getContactPerson())
                .contactEmail(request.getContactEmail())
                .enabled(request.getEnabled() == null || request.getEnabled())
                .build();

        // 建立地面站偏好
        if (request.getGroundStationPreferences() != null) {
            List<GroundStationPreference> preferences =
                    buildPreferences(satellite, request.getGroundStationPreferences());
            satellite.setGroundStationPreferences(preferences);
        }

        Satellite saved = satelliteRepository.save(satellite);
        log.info("[SatelliteService] 建立衛星：{}，頻段：{}", saved.getName(), saved.getFrequencyBand());
        return saved;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Satellite updateSatellite(Long id, SatelliteCreateRequest request) {
        Satellite satellite = getSatelliteById(id);

        // 名稱變更時，檢查是否重複
        if (!satellite.getName().equals(request.getName())
                && satelliteRepository.existsByName(request.getName())) {
            throw new ConflictException("衛星", "name", request.getName());
        }

        satellite.setName(request.getName());
        satellite.setCode(request.getCode());
        satellite.setCompany(request.getCompany());
        satellite.setFrequencyBand(request.getFrequencyBand());
        satellite.setMinDailyPasses(request.getMinDailyPasses());
        satellite.setMinPassDuration(request.getMinPassDuration());
        satellite.setPriorityWeight(request.getPriorityWeight());
        satellite.setIsEmergency(Boolean.TRUE.equals(request.getIsEmergency()));
        satellite.setDescription(request.getDescription());
        satellite.setContactPerson(request.getContactPerson());
        satellite.setContactEmail(request.getContactEmail());
        if (request.getEnabled() != null) {
            satellite.setEnabled(request.getEnabled());
        }

        // 重建地面站偏好
        if (request.getGroundStationPreferences() != null) {
            satellite.getGroundStationPreferences().clear();
            List<GroundStationPreference> preferences =
                    buildPreferences(satellite, request.getGroundStationPreferences());
            satellite.getGroundStationPreferences().addAll(preferences);
        }

        Satellite saved = satelliteRepository.save(satellite);
        log.info("[SatelliteService] 更新衛星：{}", saved.getName());
        return saved;
    }

    /** {@inheritDoc} */
    @Override
    public Satellite getSatelliteById(Long id) {
        return satelliteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("衛星", id));
    }

    /** {@inheritDoc} */
    @Override
    public Satellite getSatelliteByName(String name) {
        return satelliteRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("衛星", "name", name));
    }

    /** {@inheritDoc} */
    @Override
    public Page<Satellite> getAllSatellites(Pageable pageable) {
        return satelliteRepository.findAll(pageable);
    }

    /** {@inheritDoc} */
    @Override
    public List<Satellite> getEnabledSatellites() {
        return satelliteRepository.findByEnabledTrue();
    }

    /** {@inheritDoc} */
    @Override
    public List<Satellite> getSatellitesByFrequencyBand(FrequencyBand frequencyBand) {
        return satelliteRepository.findByFrequencyBand(frequencyBand);
    }

    /** {@inheritDoc} */
    @Override
    public List<Satellite> getEmergencySatellites() {
        return satelliteRepository.findByIsEmergencyTrue();
    }

    /** {@inheritDoc} */
    @Override
    public List<Satellite> getSatellitesByPriority() {
        return satelliteRepository.findEnabledOrderByPriority();
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getDistinctCompanies() {
        return satelliteRepository.findDistinctCompanies();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void deleteSatellite(Long id) {
        Satellite satellite = getSatelliteById(id);
        satelliteRepository.delete(satellite);
        log.info("[SatelliteService] 刪除衛星：{}", satellite.getName());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Satellite setSatelliteEnabled(Long id, boolean enabled) {
        Satellite satellite = getSatelliteById(id);
        satellite.setEnabled(enabled);
        Satellite saved = satelliteRepository.save(satellite);
        log.info("[SatelliteService] 衛星 {} 啟用狀態設為：{}", satellite.getName(), enabled);
        return saved;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Satellite setEmergency(Long id, boolean isEmergency) {
        Satellite satellite = getSatelliteById(id);
        satellite.setIsEmergency(isEmergency);
        Satellite saved = satelliteRepository.save(satellite);
        log.info("[SatelliteService] 衛星 {} 緊急狀態設為：{}", satellite.getName(), isEmergency);
        return saved;
    }

    /* ─────────── 私有輔助 ─────────── */

    /**
     * 從 DTO 清單建構地面站偏好實體列表.
     *
     * @param satellite   所屬衛星
     * @param dtoList     偏好 DTO 列表
     * @return GroundStationPreference 列表
     */
    private List<GroundStationPreference> buildPreferences(
            Satellite satellite,
            List<SatelliteCreateRequest.GroundStationPreferenceDto> dtoList) {

        List<GroundStationPreference> result = new ArrayList<>();
        for (SatelliteCreateRequest.GroundStationPreferenceDto dto : dtoList) {
            GroundStation gs = groundStationRepository.findById(dto.getGroundStationId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "地面站", dto.getGroundStationId()));

            result.add(GroundStationPreference.builder()
                    .satellite(satellite)
                    .groundStation(gs)
                    .preferenceOrder(dto.getPreferenceOrder())
                    .isMandatory(Boolean.TRUE.equals(dto.getIsMandatory()))
                    .build());
        }
        return result;
    }
}
