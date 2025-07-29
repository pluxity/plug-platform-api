package com.pluxity.user.entity;

import com.pluxity.global.annotation.ResolvePermission;
import jakarta.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermissionStrategyResolver {

    private final ApplicationContext applicationContext;
    private final Map<PermissionType, PermissionStrategy> strategyMap =
            new EnumMap<>(PermissionType.class);

    @PostConstruct
    public void initializeStrategies() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(ResolvePermission.class);

        beans.values().stream()
                .filter(bean -> bean instanceof PermissionStrategy)
                .forEach(
                        bean -> {
                            PermissionStrategy strategy = (PermissionStrategy) bean;
                            ResolvePermission annotation =
                                    strategy.getClass().getAnnotation(ResolvePermission.class);
                            strategyMap.put(annotation.value(), strategy);
                        });
    }

    public PermissionStrategy resolve(PermissionType type) {
        PermissionStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for type " + type);
        }
        return strategy;
    }
}
