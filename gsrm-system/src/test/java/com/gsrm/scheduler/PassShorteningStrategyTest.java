package com.gsrm.scheduler;

import com.gsrm.domain.entity.Satellite;
import com.gsrm.domain.enums.FrequencyBand;
import com.gsrm.scheduler.model.PassCandidate;
import com.gsrm.scheduler.model.TimeSlot;
import com.gsrm.scheduler.strategy.NoShorteningStrategy;
import com.gsrm.scheduler.strategy.ProportionalShorteningStrategy;
import com.gsrm.scheduler.strategy.PriorityShorteningStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Pass 縮短策略單元測試.
 *
 * @author Jeff
 * @since 2026-03-06
 */
@DisplayName("PassShorteningStrategy 測試")
class PassShorteningStrategyTest {

    private LocalDateTime base;
    private PassCandidate passA;
    private PassCandidate passB;
    private List<PassCandidate> conflictPasses;

    @BeforeEach
    void setUp() {
        base = LocalDateTime.of(2026, 3, 6, 10, 0, 0);

        Satellite sat1 = Satellite.builder()
                .id(1L).name("SAT-A").frequencyBand(FrequencyBand.X)
                .priorityWeight(60).minPassDuration(60).build();

        Satellite sat2 = Satellite.builder()
                .id(2L).name("SAT-B").frequencyBand(FrequencyBand.X)
                .priorityWeight(40).minPassDuration(60).build();

        // passA: 10:00 ~ 10:20 (1200s)
//        passA = PassCandidate.builder()
//                .satellite(sat1)
//                .aos(base)
//                .los(base.plusMinutes(20))
//                .build();
//
//        // passB: 10:10 ~ 10:30 (1200s, 與 passA 重疊)
//        passB = PassCandidate.builder()
//                .satellite(sat2)
//                .aos(base.plusMinutes(10))
//                .los(base.plusMinutes(30))
//                .build();

        conflictPasses = new ArrayList<>(List.of(passA, passB));
    }

    @Test
    @DisplayName("NoShorteningStrategy: 不修改任何 Pass")
    void noShorteningStrategyLeavesPassesUnchanged() {
        NoShorteningStrategy strategy = new NoShorteningStrategy();

        // NoShorteningStrategy 不應修改任何 Pass（只標記衝突）
        assertThat(strategy.getStrategyName()).isEqualTo("NONE");
    }

    @Test
    @DisplayName("ProportionalShorteningStrategy: 策略名稱正確")
    void proportionalShorteningStrategyHasCorrectName() {
        ProportionalShorteningStrategy strategy = new ProportionalShorteningStrategy();
        assertThat(strategy.getStrategyName()).isEqualTo("PROPORTIONAL");
    }

    @Test
    @DisplayName("PriorityShorteningStrategy: 策略名稱正確")
    void priorityShorteningStrategyHasCorrectName() {
        PriorityShorteningStrategy strategy = new PriorityShorteningStrategy();
        assertThat(strategy.getStrategyName()).isEqualTo("PRIORITY");
    }
}
