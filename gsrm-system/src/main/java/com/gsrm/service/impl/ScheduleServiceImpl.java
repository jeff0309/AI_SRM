package com.gsrm.service.impl;

import com.gsrm.domain.dto.request.ManualPassRequest;
import com.gsrm.domain.dto.request.ScheduleSessionRequest;
import com.gsrm.domain.dto.response.GanttChartData;
import com.gsrm.domain.dto.response.ScheduleResultResponse;
import com.gsrm.domain.dto.response.ScheduledPassDto;
import com.gsrm.domain.entity.*;
import com.gsrm.domain.enums.PassStatus;
import com.gsrm.domain.enums.ScheduleStatus;
import com.gsrm.exception.ResourceNotFoundException;
import com.gsrm.exception.SchedulingException;
import com.gsrm.repository.*;
import com.gsrm.scheduler.SchedulingEngine;
import com.gsrm.scheduler.strategy.StrategyFactory;
import com.gsrm.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 排程服務實作.
 *
 * <p>涵蓋 Session CRUD、排程執行、甘特圖資料組裝、手動 Pass 管理等操作。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleSessionRepository  sessionRepository;
    private final SatelliteRepository        satelliteRepository;
    private final GroundStationRepository    groundStationRepository;
    private final SatelliteRequestRepository requestRepository;
    private final ScheduledPassRepository    passRepository;
    private final StationUnavailabilityRepository unavailRepository;
    private final SchedulingEngine           schedulingEngine;
    private final StrategyFactory            strategyFactory;

    /* ─────────── Session CRUD ─────────── */

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ScheduleSession createSession(ScheduleSessionRequest req, Long userId) {
        validateSessionTime(req.getScheduleStartTime(), req.getScheduleEndTime());

        Set<Satellite>     satellites = fetchSatellites(req.getSatelliteIds());
        Set<GroundStation> stations   = fetchGroundStations(req.getGroundStationIds());

        ScheduleSession session = ScheduleSession.builder()
                .name(req.getName())
                .description(req.getDescription())
                .scheduleStartTime(req.getScheduleStartTime())
                .scheduleEndTime(req.getScheduleEndTime())
                .status(ScheduleStatus.DRAFT)
                .satellites(satellites)
                .groundStations(stations)
                .shorteningStrategy(
                        req.getShorteningStrategy() != null
                                ? req.getShorteningStrategy() : "PROPORTIONAL")
                .createdBy(userId)
                .build();

        return sessionRepository.save(session);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ScheduleSession updateSession(Long sessionId, ScheduleSessionRequest req) {
        ScheduleSession session = getSessionById(sessionId);

        if (!session.getStatus().canEdit()) {
            throw new SchedulingException("Session 狀態為 " + session.getStatus() + "，無法編輯");
        }

        validateSessionTime(req.getScheduleStartTime(), req.getScheduleEndTime());

        session.setName(req.getName());
        session.setDescription(req.getDescription());
        session.setScheduleStartTime(req.getScheduleStartTime());
        session.setScheduleEndTime(req.getScheduleEndTime());
        session.setSatellites(fetchSatellites(req.getSatelliteIds()));
        session.setGroundStations(fetchGroundStations(req.getGroundStationIds()));
        if (req.getShorteningStrategy() != null) {
            session.setShorteningStrategy(req.getShorteningStrategy());
        }

        return sessionRepository.save(session);
    }

    /** {@inheritDoc} */
    @Override
    public ScheduleSession getSessionById(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 Session: " + sessionId));
    }

    /** {@inheritDoc} */
    @Override
    public Page<ScheduleSession> getAllSessions(Pageable pageable) {
        return sessionRepository.findAll(pageable);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void deleteSession(Long sessionId) {
        ScheduleSession session = getSessionById(sessionId);
        passRepository.deleteByScheduleSessionId(sessionId);
        requestRepository.deleteByScheduleSessionId(sessionId);
        sessionRepository.delete(session);
    }

    /* ─────────── 排程執行 ─────────── */

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ScheduleResultResponse executeScheduling(Long sessionId, Long userId, String strategyName) {
        ScheduleSession session = getSessionById(sessionId);

        if (!session.canExecute()) {
            throw new SchedulingException(
                    "Session 狀態為 " + session.getStatus().name() + "，無法執行排程。" +
                    "請先確認狀態為 DRAFT，或先執行重置。");
        }

        session.markAsProcessing();
        sessionRepository.save(session);

        long start = System.currentTimeMillis();
        List<ScheduledPass> passes;

        try {
            passes = schedulingEngine.execute(session, strategyName, userId);
            session.markAsCompleted(userId);
            sessionRepository.save(session);
        } catch (Exception e) {
            session.setStatus(ScheduleStatus.DRAFT); // 失敗回滾狀態
            sessionRepository.save(session);
            throw new SchedulingException("排程執行失敗: " + e.getMessage(), e);
        }

        long elapsed = System.currentTimeMillis() - start;

        long allowed   = passes.stream().filter(p -> Boolean.TRUE.equals(p.getIsAllowed())).count();
        long shortened = passes.stream().filter(p -> p.getStatus() == PassStatus.SHORTENED).count();
        long rejected  = passes.stream().filter(p -> !Boolean.TRUE.equals(p.getIsAllowed())).count();
        long forced    = passes.stream().filter(p -> Boolean.TRUE.equals(p.getIsForced())).count();

        return ScheduleResultResponse.builder()
                .sessionId(session.getId())
                .sessionName(session.getName())
                .status(session.getStatus())
                .scheduleStartTime(session.getScheduleStartTime())
                .scheduleEndTime(session.getScheduleEndTime())
                .executedAt(session.getExecutedAt())
                .totalRequests((int) passes.size())
                .scheduledCount((int) allowed)
                .shortenedCount((int) shortened)
                .rejectedCount((int) rejected)
                .forcedCount((int) forced)
                .successRate(passes.isEmpty() ? 0.0 : allowed * 100.0 / passes.size())
                .conflictsResolved((int)(shortened + rejected))
                .strategyUsed(session.getShorteningStrategy())
                .executionTimeMs(elapsed)
                .build();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void resetSession(Long sessionId) {
        ScheduleSession session = getSessionById(sessionId);

        if (!session.canReset()) {
            throw new SchedulingException(
                    "Session 狀態為 " + session.getStatus().name() + "，無法重置");
        }

        passRepository.deleteByScheduleSessionId(sessionId);
        requestRepository.findByScheduleSessionId(sessionId)
                .forEach(r -> {
                    r.setStatus(PassStatus.PENDING);
                    requestRepository.save(r);
                });

        session.setStatus(ScheduleStatus.DRAFT);
        session.setExecutedAt(null);
        session.setExecutedBy(null);
        session.setTotalRequests(0);
        session.setScheduledCount(0);
        session.setRejectedCount(0);
        sessionRepository.save(session);

        log.info("[ScheduleService] Session {} 已重置", sessionId);
    }

    /* ─────────── 甘特圖 ─────────── */

    /** {@inheritDoc} */
    @Override
    public GanttChartData getGanttChartData(Long sessionId) {
        ScheduleSession session = getSessionById(sessionId);
        List<ScheduledPass> allPasses = passRepository.findGanttDataBySession(sessionId);

        // 依地面站分組
        Map<Long, List<ScheduledPass>> byStation = allPasses.stream()
                .collect(Collectors.groupingBy(p -> p.getGroundStation().getId()));

        // 取得 Session 維護時段
        Map<Long, List<StationUnavailability>> unavailMap =
                buildUnavailMap(session);

        // 優先使用 session 連結的地面站；若為空則 fallback 到 passes 實際出現的地面站
        Set<GroundStation> stationSet = new java.util.LinkedHashSet<>(session.getGroundStations());
        if (stationSet.isEmpty()) {
            allPasses.stream()
                    .map(ScheduledPass::getGroundStation)
                    .filter(java.util.Objects::nonNull)
                    .forEach(stationSet::add);
        }

        List<GanttChartData.GroundStationRow> rows = new ArrayList<>();
        for (GroundStation gs : stationSet) {
            List<ScheduledPass> stationPasses =
                    byStation.getOrDefault(gs.getId(), Collections.emptyList());

            List<GanttChartData.PassItem> passItems = stationPasses.stream()
                    .map(this::toPassItem).collect(Collectors.toList());

            List<GanttChartData.UnavailabilityItem> unavailItems =
                    unavailMap.getOrDefault(gs.getId(), Collections.emptyList())
                            .stream().map(this::toUnavailItem).collect(Collectors.toList());

            rows.add(GanttChartData.GroundStationRow.builder()
                    .groundStationId(gs.getId())
                    .groundStationName(gs.getName())
                    .frequencyBand(gs.getFrequencyBand())
                    .passes(passItems)
                    .unavailabilities(unavailItems)
                    .build());
        }

        return GanttChartData.builder()
                .sessionId(session.getId())
                .sessionName(session.getName())
                .scheduleStartTime(session.getScheduleStartTime())
                .scheduleEndTime(session.getScheduleEndTime())
                .groundStations(rows)
                .build();
    }

    /* ─────────── 手動 Pass 管理 ─────────── */

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ScheduledPass addManualPass(ManualPassRequest req, Long userId) {
        ScheduleSession session = getSessionById(req.getSessionId());

        Satellite sat = satelliteRepository.findById(req.getSatelliteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "找不到衛星: " + req.getSatelliteId()));

        GroundStation gs = groundStationRepository.findById(req.getGroundStationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "找不到地面站: " + req.getGroundStationId()));

        if (req.getLos() == null || !req.getLos().isAfter(req.getAos())) {
            throw new SchedulingException("LOS 必須晚於 AOS");
        }

        ScheduledPass pass = ScheduledPass.builder()
                .scheduleSession(session)
                .satellite(sat)
                .groundStation(gs)
                .frequencyBand(req.getFrequencyBand())
                .originalAos(req.getAos())
                .originalLos(req.getLos())
                .scheduledAos(req.getAos())
                .scheduledLos(req.getLos())
                .status(PassStatus.FORCED)
                .isAllowed(true)
                .isForced(true)
                .notes(req.getNotes())
                .createdBy(userId)
                .build();

        return passRepository.save(pass);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void removePass(Long passId) {
        ScheduledPass pass = passRepository.findById(passId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 Pass: " + passId));

        // 若對應的需求存在，回退為 PENDING
        if (pass.getSatelliteRequest() != null) {
            SatelliteRequest req = pass.getSatelliteRequest();
            req.setStatus(PassStatus.PENDING);
            // requestRepository.save(req); // 由 cascade 或手動呼叫
        }

        passRepository.delete(pass);
    }

    /** {@inheritDoc} */
    @Override
    public List<ScheduledPassDto> getScheduledPasses(Long sessionId) {
        return passRepository.findAllowedBySessionOrderByAos(sessionId)
                .stream()
                .map(this::toPassDto)
                .collect(Collectors.toList());
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getAvailableStrategies() {
        return strategyFactory.getAvailableStrategies();
    }

    /* ─────────── 私有輔助方法 ─────────── */

    /**
     * 驗證排程時間範圍合法性.
     *
     * @param start 開始時間
     * @param end   結束時間
     */
    private void validateSessionTime(LocalDateTime start, LocalDateTime end) {
        if (end == null || start == null || !end.isAfter(start)) {
            throw new SchedulingException("排程結束時間必須晚於開始時間");
        }
    }

    /**
     * 批次取得衛星集合.
     *
     * @param ids 衛星 ID 集合
     * @return Satellite 集合
     */
    private Set<Satellite> fetchSatellites(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        return new HashSet<>(satelliteRepository.findByIdIn(new ArrayList<>(ids)));
    }

    /**
     * 批次取得地面站集合.
     *
     * @param ids 地面站 ID 集合
     * @return GroundStation 集合
     */
    private Set<GroundStation> fetchGroundStations(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        return new HashSet<>(groundStationRepository.findByIdIn(new ArrayList<>(ids)));
    }

    /**
     * 建立地面站 -> 維護時段的 Map.
     *
     * @param session Session 實體
     * @return Map
     */
    private Map<Long, List<StationUnavailability>> buildUnavailMap(ScheduleSession session) {
        return unavailRepository
                .findOverlapping(session.getScheduleStartTime(), session.getScheduleEndTime())
                .stream()
                .collect(Collectors.groupingBy(u -> u.getGroundStation().getId()));
    }

    /**
     * 將 ScheduledPass 轉換為甘特圖 PassItem.
     *
     * @param p ScheduledPass
     * @return PassItem
     */
    private GanttChartData.PassItem toPassItem(ScheduledPass p) {
        return GanttChartData.PassItem.builder()
                .passId(p.getId())
                .satelliteId(p.getSatellite().getId())
                .satelliteName(p.getSatellite().getName())
                .frequencyBand(p.getFrequencyBand())
                .originalAos(p.getOriginalAos())
                .originalLos(p.getOriginalLos())
                .scheduledAos(p.getScheduledAos())
                .scheduledLos(p.getScheduledLos())
                .status(p.getStatus())
                .isAllowed(p.getIsAllowed())
                .isForced(p.getIsForced())
                .shortenedSeconds(p.getShortenedSeconds())
                .durationSeconds(p.getScheduledDurationSeconds())
                .notes(p.getNotes())
                .build();
    }

    /**
     * 將 StationUnavailability 轉換為甘特圖 UnavailabilityItem.
     *
     * @param u StationUnavailability
     * @return UnavailabilityItem
     */
    private GanttChartData.UnavailabilityItem toUnavailItem(StationUnavailability u) {
        return GanttChartData.UnavailabilityItem.builder()
                .id(u.getId())
                .startTime(u.getStartTime())
                .endTime(u.getEndTime())
                .reason(u.getReason())
                .build();
    }

    /**
     * 將 ScheduledPass 實體轉換為 ScheduledPassDto（平坦化關聯欄位）.
     *
     * @param p ScheduledPass 實體
     * @return ScheduledPassDto
     */
    private ScheduledPassDto toPassDto(ScheduledPass p) {
        return ScheduledPassDto.builder()
                .passId(p.getId())
                .satelliteId(p.getSatellite() != null ? p.getSatellite().getId() : null)
                .satelliteName(p.getSatellite() != null ? p.getSatellite().getName() : null)
                .groundStationId(p.getGroundStation() != null ? p.getGroundStation().getId() : null)
                .groundStationName(p.getGroundStation() != null ? p.getGroundStation().getName() : null)
                .frequencyBand(p.getFrequencyBand())
                .originalAos(p.getOriginalAos())
                .originalLos(p.getOriginalLos())
                .scheduledAos(p.getScheduledAos())
                .scheduledLos(p.getScheduledLos())
                .status(p.getStatus())
                .isAllowed(p.getIsAllowed())
                .isForced(p.getIsForced())
                .shortenedSeconds(p.getShortenedSeconds())
                .durationSeconds(p.getScheduledDurationSeconds())
                .rejectionReason(p.getRejectionReason())
                .notes(p.getNotes())
                .build();
    }
}
