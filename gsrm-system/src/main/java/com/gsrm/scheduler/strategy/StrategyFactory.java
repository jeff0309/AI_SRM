package com.gsrm.scheduler.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * Pass 縮短策略工廠.
 * 
 * <p>使用工廠模式 (Factory Pattern) 根據策略名稱取得對應的策略實作。
 * 支援動態切換不同的排程策略。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Component
public class StrategyFactory {

    /**
     * 策略映射表.
     */
    private final Map<String, PassShorteningStrategy> strategyMap;

    /**
     * 預設策略名稱.
     */
    private static final String DEFAULT_STRATEGY = "PROPORTIONAL";

    /**
     * 前端策略名稱別名 → 後端策略名稱.
     * 前端使用 FAIR_SHARE / PRIORITY_BASED / EMERGENCY_FIRST，
     * 後端內部使用 PROPORTIONAL / PRIORITY / NO_SHORTENING。
     */
    private static final Map<String, String> ALIASES = Map.of(
            "FAIR_SHARE",      "PROPORTIONAL",
            "PRIORITY_BASED",  "PRIORITY",
            "EMERGENCY_FIRST", "PRIORITY"
    );

    /**
     * 建構策略工廠.
     * 
     * <p>透過 Spring 依賴注入自動收集所有策略實作。</p>
     * 
     * @param strategies 所有策略實作列表
     */
    @Autowired
    public StrategyFactory(List<PassShorteningStrategy> strategies) {
        this.strategyMap = new HashMap<>();
        for (PassShorteningStrategy strategy : strategies) {
            strategyMap.put(strategy.getStrategyName(), strategy);
        }
    }

    /**
     * 根據策略名稱取得策略實作.
     * 
     * @param strategyName 策略名稱
     * @return 策略實作
     * @throws IllegalArgumentException 如果策略名稱不存在
     */
    public PassShorteningStrategy getStrategy(String strategyName) {
        if (strategyName == null || strategyName.isBlank()) {
            return getDefaultStrategy();
        }

        String upperName = strategyName.toUpperCase();
        // 解析前端別名（FAIR_SHARE → PROPORTIONAL 等）
        upperName = ALIASES.getOrDefault(upperName, upperName);

        PassShorteningStrategy strategy = strategyMap.get(upperName);

        if (strategy == null) {
            // 未知策略時 fallback 到預設，不拋出例外
            return getDefaultStrategy();
        }

        return strategy;
    }

    /**
     * 取得預設策略.
     * 
     * @return 預設策略實作
     */
    public PassShorteningStrategy getDefaultStrategy() {
        return strategyMap.get(DEFAULT_STRATEGY);
    }

    /**
     * 取得所有可用的策略名稱.
     * 
     * @return 策略名稱列表
     */
    public List<String> getAvailableStrategies() {
        return strategyMap.keySet().stream().sorted().toList();
    }

    /**
     * 取得策略描述.
     * 
     * @param strategyName 策略名稱
     * @return 策略描述
     */
    public String getStrategyDescription(String strategyName) {
        PassShorteningStrategy strategy = strategyMap.get(strategyName.toUpperCase());
        return strategy != null ? strategy.getDescription() : "未知策略";
    }

    /**
     * 取得所有策略的描述.
     * 
     * @return 策略名稱與描述的映射
     */
    public Map<String, String> getAllStrategyDescriptions() {
        Map<String, String> descriptions = new HashMap<>();
        for (Map.Entry<String, PassShorteningStrategy> entry : strategyMap.entrySet()) {
            descriptions.put(entry.getKey(), entry.getValue().getDescription());
        }
        return descriptions;
    }

    /**
     * 檢查策略是否存在.
     * 
     * @param strategyName 策略名稱
     * @return 如果策略存在則回傳 true
     */
    public boolean hasStrategy(String strategyName) {
        if (strategyName == null || strategyName.isBlank()) {
            return false;
        }
        return strategyMap.containsKey(strategyName.toUpperCase());
    }
}
