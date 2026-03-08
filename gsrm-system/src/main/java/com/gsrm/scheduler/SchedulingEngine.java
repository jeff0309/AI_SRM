package com.gsrm.scheduler;

import com.gsrm.domain.entity.*;
import com.gsrm.domain.enums.PassStatus;
import com.gsrm.repository.*;
import com.gsrm.scheduler.model.ConflictGroup;
import com.gsrm.scheduler.model.PassCandidate;
import com.gsrm.scheduler.strategy.PassShorteningStrategy;
import com.gsrm.scheduler.strategy.StrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 排程引擎主類別.
 *
 * <p>負責將 ScheduleSession 中的 SatelliteRequest 逐一排程，
 * 偵測地面站衝突後委派策略處理，最終將結果持久化為 ScheduledPass。</p>
 *
 * <p>排程流程：</p>
 * <ol>
 *   <li>從 Session 讀取所有待處理 (PENDING) 需求。</li>
 *   <li>依 AOS 升冪排序。</li>
 *   <li>逐地面站分組，於同一地面站內偵測時間重疊（含 Setup/Teardown Gap）。</li>
 *   <li>將衝突群組交由 {@link PassShorteningStrategy} 處理。</li>
 *   <li>將結果寫入 {@link ScheduledPass}，更新 {@link SatelliteRequest} 狀態。</li>
 *   <li>更新 Session 統計數據。</li>
 * </ol>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@Slf4j
@Service
public class SchedulingEngine {

    private final SatelliteRequestRepository requestRepository;
    private final ScheduledPassRepository    passRepository;
    private final ScheduleSessionRepository  sessionRepository;
    private final StationUnavailabilityRepository unavailRepository;
    private final StrategyFactory strategyFactory;

    /**
     * 建構排程引擎.
     *
     * @param requestRepository   衛星需求 Repository
     * @param passRepository      已排程 Pass Repository
     * @param sessionRepository   Session Repository
     * @param unavailRepository   維護時段 Repository
     * @param strategyFactory     策略工廠
     */
    public SchedulingEngine(SatelliteRequestRepository requestRepository,
                             ScheduledPassRepository passRepository,
                             ScheduleSessionRepository sessionRepository,
                             StationUnavailabilityRepository unavailRepository,
                             StrategyFactory strategyFactory) {
        this.requestRepository  = requestRepository;
        this.passRepository     = passRepository;
        this.sessionRepository  = sessionRepository;
        this.unavailRepository  = unavailRepository;
        this.strategyFactory    = strategyFactory;
    }

    /**
     * 執行指定 Session 的排程演算.
     *
     * @param session      要排程的 Session
     * @param strategyName 使用的策略名稱（null 時使用 Session 預設值）
     * @param executorId   執行者 ID
     * @return 已建立的 ScheduledPass 列表
     */
    @Transactional
    public List<ScheduledPass> execute(ScheduleSession session,
                                       String strategyName,
                                       Long executorId) {

        log.info("[SchedulingEngine] 開始排程 Session: {} (id={})", session.getName(), session.getId());

        // 取得策略
        String effectiveStrategy = (strategyName != null && !strategyName.isBlank())
                ? strategyName : session.getShorteningStrategy();
        PassShorteningStrategy strategy = strategyFactory.getStrategy(effectiveStrategy);
        log.info("[SchedulingEngine] 使用策略: {}", strategy.getStrategyName());

        // 取得本 Session 的待處理需求
        List<SatelliteRequest> pendingRequests =
                requestRepository.findPendingBySessionOrderByAos(session.getId());
        log.info("[SchedulingEngine] 待處理需求數: {}", pendingRequests.size());

        if (pendingRequests.isEmpty()) {
            return Collections.emptyList();
        }

        // 取得維護時段（Session 時間範圍內）
        Map<Long, List<StationUnavailability>> unavailMap =
                buildUnavailMap(session);

        // 將 SatelliteRequest 轉為 PassCandidate
        List<PassCandidate> candidates = pendingRequests.stream()
                .map(req -> toCandidate(req))
                .collect(Collectors.toList());

        // 過濾掉落在維護時段的 Candidate
        filterUnavailableSlots(candidates, unavailMap);

        // 依地面站分組，偵測並解決衝突
        Map<Long, List<PassCandidate>> byStation = candidates.stream()
                .collect(Collectors.groupingBy(PassCandidate::getGroundStationId));

        List<PassCandidate> allResolved = new ArrayList<>();
        for (Map.Entry<Long, List<PassCandidate>> entry : byStation.entrySet()) {
            List<PassCandidate> resolved = resolveForStation(entry.getValue(), strategy);
            allResolved.addAll(resolved);
        }

        // 持久化結果
        List<ScheduledPass> savedPasses = persistResults(allResolved, pendingRequests, session, executorId);

        // 更新統計
        updateSessionStats(session, savedPasses);

        log.info("[SchedulingEngine] 排程完成：共 {} 個 Pass，成功 {}, 失敗 {}",
                savedPasses.size(),
                savedPasses.stream().filter(p -> Boolean.TRUE.equals(p.getIsAllowed())).count(),
                savedPasses.stream().filter(p -> !Boolean.TRUE.equals(p.getIsAllowed())).count());

        return savedPasses;
    }

    /* ═══════════════════════════════════════════════
       私有輔助方法
       ═══════════════════════════════════════════════ */

    /**
     * 建立各地面站的維護時段 Map.
     *
     * @param session Session 實體
     * @return groundStationId -> 維護時段列表
     */
    private Map<Long, List<StationUnavailability>> buildUnavailMap(ScheduleSession session) {
        List<StationUnavailability> all = unavailRepository.findOverlapping(
                session.getScheduleStartTime(), session.getScheduleEndTime());
        return all.stream().collect(
                Collectors.groupingBy(u -> u.getGroundStation().getId()));
    }

    /**
     * 將 SatelliteRequest 轉換為排程引擎內部的 PassCandidate.
     *
     * @param req SatelliteRequest
     * @return PassCandidate
     */
    private PassCandidate toCandidate(SatelliteRequest req) {
        GroundStation station = req.getGroundStation();
        Satellite satellite   = req.getSatellite();
        return PassCandidate.builder()
                .requestId(req.getId())
                .satelliteId(satellite.getId())
                .satelliteName(satellite.getName())
                .groundStationId(station.getId())
                .groundStationName(station.getName())
                .frequencyBand(req.getFrequencyBand())
                .originalAos(req.getAos())
                .originalLos(req.getLos())
                .scheduledAos(req.getAos())
                .scheduledLos(req.getLos())
                .priorityWeight(satellite.getPriorityWeight())
                .isEmergency(Boolean.TRUE.equals(satellite.getIsEmergency()))
                .minPassDuration(satellite.getMinPassDuration())
                .stationGap(station.getMinimumGap())
                .status(PassStatus.PENDING)
                .isAllowed(false)
                .shortenedSeconds(0)
                .build();
    }

    /**
     * 過濾落在維護時段的候選（直接標記為拒絕）.
     *
     * @param candidates   候選列表（就地修改）
     * @param unavailMap   維護時段 Map
     */
    private void filterUnavailableSlots(List<PassCandidate> candidates,
                                         Map<Long, List<StationUnavailability>> unavailMap) {
        for (PassCandidate c : candidates) {
            List<StationUnavailability> unavails =
                    unavailMap.getOrDefault(c.getGroundStationId(), Collections.emptyList());
            for (StationUnavailability u : unavails) {
                if (u.overlaps(c.getOriginalAos(), c.getOriginalLos())) {
                    c.markAsRejected(
                            "地面站在 " + u.getStartTime() + " ~ " + u.getEndTime() + " 進行維護",
                            null);
                    break;
                }
            }
        }
    }

    /**
     * 針對單一地面站的候選列表，偵測衝突並呼叫策略解決.
     *
     * @param stationCandidates 同一地面站的候選列表
     * @param strategy          縮短策略
     * @return 解決後的候選列表
     */
    private List<PassCandidate> resolveForStation(List<PassCandidate> stationCandidates,
                                                   PassShorteningStrategy strategy) {
        // 排除已被維護時段拒絕的候選
        List<PassCandidate> active = stationCandidates.stream()
                .filter(c -> c.getStatus() == PassStatus.PENDING)
                .sorted(Comparator.comparing(PassCandidate::getOriginalAos))
                .collect(Collectors.toList());

        List<PassCandidate> rejected = stationCandidates.stream()
                .filter(c -> c.getStatus() != PassStatus.PENDING)
                .collect(Collectors.toList());

        if (active.isEmpty()) {
            return rejected;
        }

        // 逐一偵測衝突，組成衝突群組
        List<ConflictGroup> groups = buildConflictGroups(active);

        List<PassCandidate> resolved = new ArrayList<>(rejected);
        for (ConflictGroup group : groups) {
            resolved.addAll(strategy.resolveConflict(group));
        }
        return resolved;
    }

    /**
     * 將同一地面站的候選依時間重疊關係分組成衝突群組.
     *
     * @param sorted 已依 AOS 排序的候選列表
     * @return 衝突群組列表（無衝突的候選各自成一個只含一個元素的群組）
     */
    private List<ConflictGroup> buildConflictGroups(List<PassCandidate> sorted) {
        List<ConflictGroup> groups = new ArrayList<>();
        if (sorted.isEmpty()) return groups;

        ConflictGroup current = new ConflictGroup();
        current.setGroundStationId(sorted.get(0).getGroundStationId());
        current.setGroundStationName(sorted.get(0).getGroundStationName());
        current.addCandidate(sorted.get(0));

        for (int i = 1; i < sorted.size(); i++) {
            PassCandidate cand = sorted.get(i);
            boolean conflictsWithGroup = current.getCandidates().stream()
                    .anyMatch(existing -> existing.conflictsWith(cand));

            if (conflictsWithGroup) {
                current.addCandidate(cand);
            } else {
                groups.add(current);
                current = new ConflictGroup();
                current.setGroundStationId(cand.getGroundStationId());
                current.setGroundStationName(cand.getGroundStationName());
                current.addCandidate(cand);
            }
        }
        groups.add(current);
        return groups;
    }

    /**
     * 將解決後的候選持久化為 ScheduledPass，並更新 SatelliteRequest 狀態.
     *
     * @param resolved     解決後的候選列表
     * @param origRequests 原始需求列表（用於更新狀態）
     * @param session      所屬 Session
     * @param executorId   執行者 ID
     * @return 儲存後的 ScheduledPass 列表
     */
    private List<ScheduledPass> persistResults(List<PassCandidate> resolved,
                                                List<SatelliteRequest> origRequests,
                                                ScheduleSession session,
                                                Long executorId) {

        // 建立 requestId -> SatelliteRequest 的快速查找 Map
        Map<Long, SatelliteRequest> reqMap = origRequests.stream()
                .collect(Collectors.toMap(SatelliteRequest::getId, r -> r));

        List<ScheduledPass> passes = new ArrayList<>();

        for (PassCandidate c : resolved) {
            SatelliteRequest origReq = reqMap.get(c.getRequestId());

            ScheduledPass pass = ScheduledPass.builder()
                    .scheduleSession(session)
                    .satelliteRequest(origReq)
                    .satellite(origReq.getSatellite())
                    .groundStation(origReq.getGroundStation())
                    .frequencyBand(c.getFrequencyBand())
                    .originalAos(c.getOriginalAos())
                    .originalLos(c.getOriginalLos())
                    .scheduledAos(c.getScheduledAos())
                    .scheduledLos(c.getScheduledLos())
                    .status(c.getStatus())
                    .isAllowed(c.getIsAllowed())
                    .shortenedSeconds(c.getShortenedSeconds())
                    .rejectionReason(c.getRejectionReason())
                    .conflictWithPassId(c.getConflictWithId())
                    .isForced(false)
                    .createdBy(executorId)
                    .build();

            passes.add(passRepository.save(pass));

            // 同步更新 SatelliteRequest 狀態
            if (origReq != null) {
                origReq.setStatus(c.getStatus());
                requestRepository.save(origReq);
            }
        }

        return passes;
    }

    /**
     * 更新 Session 的統計欄位.
     *
     * @param session Session 實體
     * @param passes  已儲存的 ScheduledPass 列表
     */
    private void updateSessionStats(ScheduleSession session, List<ScheduledPass> passes) {
        int total     = passes.size();
        int scheduled = (int) passes.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsAllowed())).count();
        int rejected  = total - scheduled;

        session.setTotalRequests(total);
        session.setScheduledCount(scheduled);
        session.setRejectedCount(rejected);
        sessionRepository.save(session);
    }
}
