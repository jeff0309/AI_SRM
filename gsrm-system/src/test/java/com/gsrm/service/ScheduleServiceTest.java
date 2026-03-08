package com.gsrm.service;

import com.gsrm.domain.dto.request.ScheduleSessionRequest;
import com.gsrm.domain.entity.ScheduleSession;
import com.gsrm.domain.enums.ScheduleStatus;
import com.gsrm.exception.ResourceNotFoundException;
import com.gsrm.exception.SchedulingException;
import com.gsrm.repository.*;
import com.gsrm.scheduler.SchedulingEngine;
import com.gsrm.scheduler.strategy.StrategyFactory;
import com.gsrm.service.impl.ScheduleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * ScheduleServiceImpl 單元測試.
 *
 * @author Jeff
 * @since 2026-03-06
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleService 測試")
class ScheduleServiceTest {

    @Mock private ScheduleSessionRepository  sessionRepository;
    @Mock private SatelliteRepository        satelliteRepository;
    @Mock private GroundStationRepository    groundStationRepository;
    @Mock private SatelliteRequestRepository requestRepository;
    @Mock private ScheduledPassRepository    passRepository;
    @Mock private StationUnavailabilityRepository unavailRepository;
    @Mock private SchedulingEngine           schedulingEngine;
    @Mock private StrategyFactory            strategyFactory;

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    private ScheduleSession sampleSession;
    private ScheduleSessionRequest sessionRequest;

    @BeforeEach
    void setUp() {
        sampleSession = ScheduleSession.builder()
                .id(1L)
                .name("Test Session")
                .scheduleStartTime(LocalDateTime.now().plusDays(1))
                .scheduleEndTime(LocalDateTime.now().plusDays(2))
                .status(ScheduleStatus.DRAFT)
                .satellites(Set.of())
                .groundStations(Set.of())
                .shorteningStrategy("PROPORTIONAL")
                .build();

        sessionRequest = ScheduleSessionRequest.builder()
                .name("New Session")
                .scheduleStartTime(LocalDateTime.now().plusDays(1))
                .scheduleEndTime(LocalDateTime.now().plusDays(2))
                .satelliteIds(Set.of())
                .groundStationIds(Set.of())
                .build();
    }

    /* ─────────── createSession ─────────── */

    @Nested
    @DisplayName("createSession()")
    class CreateSession {

        @Test
        @DisplayName("時間正確時成功建立")
        void shouldCreateSessionSuccessfully() {
            given(satelliteRepository.findByIdIn(any())).willReturn(Collections.emptyList());
            given(groundStationRepository.findByIdIn(any())).willReturn(Collections.emptyList());
            given(sessionRepository.save(any(ScheduleSession.class)))
                    .willAnswer(inv -> {
                        ScheduleSession s = inv.getArgument(0);
                        s.setId(10L);
                        return s;
                    });

            ScheduleSession result = scheduleService.createSession(sessionRequest, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(ScheduleStatus.DRAFT);
        }

        @Test
        @DisplayName("結束時間早於開始時間時拋出 SchedulingException")
        void shouldThrowWhenInvalidTimeRange() {
            sessionRequest.setScheduleEndTime(sessionRequest.getScheduleStartTime().minusHours(1));

            assertThatThrownBy(() -> scheduleService.createSession(sessionRequest, 1L))
                    .isInstanceOf(SchedulingException.class)
                    .hasMessageContaining("結束時間");
        }
    }

    /* ─────────── getSessionById ─────────── */

    @Nested
    @DisplayName("getSessionById()")
    class GetSessionById {

        @Test
        @DisplayName("存在時正確回傳")
        void shouldReturnSessionWhenExists() {
            given(sessionRepository.findById(1L)).willReturn(Optional.of(sampleSession));

            ScheduleSession result = scheduleService.getSessionById(1L);

            assertThat(result.getName()).isEqualTo("Test Session");
        }

        @Test
        @DisplayName("不存在時拋出 ResourceNotFoundException")
        void shouldThrowWhenNotFound() {
            given(sessionRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> scheduleService.getSessionById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    /* ─────────── updateSession ─────────── */

    @Nested
    @DisplayName("updateSession()")
    class UpdateSession {

        @Test
        @DisplayName("COMPLETED 狀態時拋出 SchedulingException")
        void shouldThrowWhenSessionIsCompleted() {
            sampleSession.setStatus(ScheduleStatus.COMPLETED);
            given(sessionRepository.findById(1L)).willReturn(Optional.of(sampleSession));

            assertThatThrownBy(() -> scheduleService.updateSession(1L, sessionRequest))
                    .isInstanceOf(SchedulingException.class)
                    .hasMessageContaining("無法編輯");
        }
    }

    /* ─────────── deleteSession ─────────── */

    @Nested
    @DisplayName("deleteSession()")
    class DeleteSession {

        @Test
        @DisplayName("成功刪除 Session")
        void shouldDeleteSessionSuccessfully() {
            given(sessionRepository.findById(1L)).willReturn(Optional.of(sampleSession));
            willDoNothing().given(passRepository).deleteByScheduleSessionId(1L);
            willDoNothing().given(requestRepository).deleteByScheduleSessionId(1L);
            willDoNothing().given(sessionRepository).delete(sampleSession);

            assertThatNoException().isThrownBy(() -> scheduleService.deleteSession(1L));
        }
    }

    /* ─────────── getAllSessions ─────────── */

    @Test
    @DisplayName("getAllSessions() 回傳分頁結果")
    void shouldReturnPagedSessions() {
        var pageable = PageRequest.of(0, 10);
        given(sessionRepository.findAll(pageable))
                .willReturn(new PageImpl<>(Collections.singletonList(sampleSession)));

        var page = scheduleService.getAllSessions(pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
    }
}
