package com.gsrm.service;

import com.gsrm.domain.dto.request.GroundStationRequest;
import com.gsrm.domain.entity.GroundStation;
import com.gsrm.domain.enums.FrequencyBand;
import com.gsrm.exception.ConflictException;
import com.gsrm.exception.ResourceNotFoundException;
import com.gsrm.repository.GroundStationRepository;
import com.gsrm.repository.StationUnavailabilityRepository;
import com.gsrm.service.impl.GroundStationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * GroundStationServiceImpl 單元測試.
 *
 * @author Jeff
 * @since 2026-03-06
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GroundStationService 測試")
class GroundStationServiceTest {

    @Mock private GroundStationRepository         groundStationRepository;
    @Mock private StationUnavailabilityRepository unavailabilityRepository;

    @InjectMocks
    private GroundStationServiceImpl groundStationService;

    private GroundStation sampleStation;
    private GroundStationRequest createRequest;

    @BeforeEach
    void setUp() {
        sampleStation = GroundStation.builder()
                .id(1L)
                .name("Taiwan Hsinchu")
                .code("TW-HCU")
                .longitude(121.0)
                .latitude(24.8)
                .setupTime(300)
                .teardownTime(300)
                .frequencyBand(FrequencyBand.X)
                .enabled(true)
                .build();

        createRequest = GroundStationRequest.builder()
                .name("New Station")
                .longitude(120.0)
                .latitude(23.0)
                .setupTime(180)
                .teardownTime(180)
                .frequencyBand(FrequencyBand.S)
                .build();
    }

    /* ─────────── createGroundStation ─────────── */

    @Nested
    @DisplayName("createGroundStation()")
    class CreateGroundStation {

        @Test
        @DisplayName("正常建立地面站")
        void shouldCreateGroundStationSuccessfully() {
            given(groundStationRepository.existsByName("New Station")).willReturn(false);
            given(groundStationRepository.save(any(GroundStation.class)))
                    .willAnswer(inv -> {
                        GroundStation gs = inv.getArgument(0);
                        gs.setId(2L);
                        return gs;
                    });

            GroundStation result = groundStationService.createGroundStation(createRequest);

            assertThat(result.getName()).isEqualTo("New Station");
            assertThat(result.getFrequencyBand()).isEqualTo(FrequencyBand.S);
        }

        @Test
        @DisplayName("名稱重複時拋出 ConflictException")
        void shouldThrowConflictWhenNameExists() {
            given(groundStationRepository.existsByName("New Station")).willReturn(true);

            assertThatThrownBy(() -> groundStationService.createGroundStation(createRequest))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("New Station");
        }
    }

    /* ─────────── getGroundStationById ─────────── */

    @Nested
    @DisplayName("getGroundStationById()")
    class GetGroundStationById {

        @Test
        @DisplayName("存在時正確回傳")
        void shouldReturnStationWhenExists() {
            given(groundStationRepository.findById(1L)).willReturn(Optional.of(sampleStation));

            GroundStation result = groundStationService.getGroundStationById(1L);

            assertThat(result.getName()).isEqualTo("Taiwan Hsinchu");
        }

        @Test
        @DisplayName("不存在時拋出 ResourceNotFoundException")
        void shouldThrowWhenNotFound() {
            given(groundStationRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> groundStationService.getGroundStationById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    /* ─────────── getEnabledGroundStations ─────────── */

    @Test
    @DisplayName("getEnabledGroundStations() 回傳正確列表")
    void shouldReturnEnabledStations() {
        given(groundStationRepository.findByEnabledTrue()).willReturn(List.of(sampleStation));

        List<GroundStation> result = groundStationService.getEnabledGroundStations();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEnabled()).isTrue();
    }

    /* ─────────── setGroundStationEnabled ─────────── */

    @Test
    @DisplayName("成功停用地面站")
    void shouldDisableGroundStation() {
        given(groundStationRepository.findById(1L)).willReturn(Optional.of(sampleStation));
        given(groundStationRepository.save(any(GroundStation.class)))
                .willAnswer(inv -> inv.getArgument(0));

        GroundStation result = groundStationService.setGroundStationEnabled(1L, false);

        assertThat(result.getEnabled()).isFalse();
    }
}
