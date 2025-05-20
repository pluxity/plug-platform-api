package com.pluxity.config;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.domain.ConsoleAuditLogger;
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Configuration
@EnableCaching
@EnableMethodSecurity(securedEnabled = true)
public class AclConfig {

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;

    @Autowired
    public AclConfig(DataSource dataSource, RedisConnectionFactory redisConnectionFactory) {
        this.dataSource = dataSource;
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Bean
    public RedisTemplate<String, Object> aclRedisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        // JDK 직렬화 사용 - Spring Security ACL 객체가 Jackson과 잘 맞지 않음
        template.setValueSerializer(new JdkSerializationRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new JdkSerializationRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public PermissionGrantingStrategy permissionGrantingStrategy() {
        return new DefaultPermissionGrantingStrategy(new ConsoleAuditLogger());
    }

    @Bean
    public AclAuthorizationStrategy aclAuthorizationStrategy() {
        return new AclAuthorizationStrategyImpl(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Bean
    public AclCache aclCache() {
        return new RedisAclCache(
                aclRedisTemplate(), permissionGrantingStrategy(), aclAuthorizationStrategy());
    }

    @Bean
    public LookupStrategy lookupStrategy(AclCache aclCache) {
        return new BasicLookupStrategy(
                dataSource, aclCache, aclAuthorizationStrategy(), permissionGrantingStrategy());
    }

    @Bean
    public JdbcMutableAclService aclService(LookupStrategy lookupStrategy, AclCache aclCache) {
        return new JdbcMutableAclService(dataSource, lookupStrategy, aclCache);
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(AclService aclService) {
        DefaultMethodSecurityExpressionHandler expressionHandler =
                new DefaultMethodSecurityExpressionHandler();
        AclPermissionEvaluator permissionEvaluator = new AclPermissionEvaluator(aclService);
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        return expressionHandler;
    }

    /** Redis 기반 AclCache 구현 Spring Security ACL 객체를 Redis에 저장할 때 JDK 직렬화를 사용하여 호환성 문제 해결 */
    public static class RedisAclCache implements AclCache {
        private final RedisTemplate<String, Object> redisTemplate;
        private final PermissionGrantingStrategy permissionGrantingStrategy;
        private final AclAuthorizationStrategy aclAuthorizationStrategy;
        private final long expireTime = 600; // 10분 (초 단위)

        // 역직렬화 후 필드 주입을 위한 리플렉션 캐시
        private final Map<Class<?>, Field> aclAuthorizationStrategyFields = new ConcurrentHashMap<>();
        private final Map<Class<?>, Field> permissionGrantingStrategyFields = new ConcurrentHashMap<>();

        public RedisAclCache(
                RedisTemplate<String, Object> redisTemplate,
                PermissionGrantingStrategy permissionGrantingStrategy,
                AclAuthorizationStrategy aclAuthorizationStrategy) {
            this.redisTemplate = redisTemplate;
            this.permissionGrantingStrategy = permissionGrantingStrategy;
            this.aclAuthorizationStrategy = aclAuthorizationStrategy;
        }

        @Override
        public void evictFromCache(Serializable pk) {
            redisTemplate.delete(getKey(pk));
        }

        @Override
        public void evictFromCache(ObjectIdentity objectIdentity) {
            redisTemplate.delete(getKey(objectIdentity));
        }

        @Override
        public MutableAcl getFromCache(ObjectIdentity objectIdentity) {
            Object cached = redisTemplate.opsForValue().get(getKey(objectIdentity));
            if (cached instanceof MutableAcl) {
                return initializeTransientFields((MutableAcl) cached);
            }
            return null;
        }

        @Override
        public MutableAcl getFromCache(Serializable pk) {
            Object cached = redisTemplate.opsForValue().get(getKey(pk));
            if (cached instanceof MutableAcl) {
                return initializeTransientFields((MutableAcl) cached);
            }
            return null;
        }

        @Override
        public void putInCache(MutableAcl acl) {
            ObjectIdentity oid = acl.getObjectIdentity();
            redisTemplate.opsForValue().set(getKey(oid), acl, expireTime, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(getKey(acl.getId()), acl, expireTime, TimeUnit.SECONDS);
        }

        @Override
        public void clearCache() {
            // 실제 구현에서는 특정 패턴의 키만 삭제하도록 구현할 수 있음
            // 여기서는 단순히 비워두었음
        }

        private String getKey(ObjectIdentity oid) {
            return "acl_oid_" + oid.getType() + "_" + oid.getIdentifier();
        }

        private String getKey(Serializable pk) {
            return "acl_pk_" + pk;
        }

        /** 역직렬화된 ACL 객체에 필요한 전략 객체들을 다시 주입 */
        private MutableAcl initializeTransientFields(MutableAcl acl) {
            try {
                // AclAuthorizationStrategy 필드 주입
                Field aclAuthField = getAclAuthorizationStrategyField(acl);
                if (aclAuthField != null) {
                    aclAuthField.setAccessible(true);
                    aclAuthField.set(acl, this.aclAuthorizationStrategy);
                }

                // PermissionGrantingStrategy 필드 주입
                Field permGrantField = getPermissionGrantingStrategyField(acl);
                if (permGrantField != null) {
                    permGrantField.setAccessible(true);
                    permGrantField.set(acl, this.permissionGrantingStrategy);
                }

                // 부모 ACL이 있으면 재귀적으로 처리
                if (acl.getParentAcl() instanceof MutableAcl) {
                    initializeTransientFields((MutableAcl) acl.getParentAcl());
                }

                return acl;
            } catch (Exception ex) {
                throw new IllegalStateException("ACL 객체 필드 초기화 중 오류가 발생했습니다", ex);
            }
        }

        private Field getAclAuthorizationStrategyField(MutableAcl acl) {
            return getField(acl, "aclAuthorizationStrategy", aclAuthorizationStrategyFields);
        }

        private Field getPermissionGrantingStrategyField(MutableAcl acl) {
            return getField(acl, "permissionGrantingStrategy", permissionGrantingStrategyFields);
        }

        private Field getField(Object obj, String fieldName, Map<Class<?>, Field> cache) {
            Class<?> clazz = obj.getClass();

            if (!cache.containsKey(clazz)) {
                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    cache.put(clazz, field);
                } catch (NoSuchFieldException e) {
                    // 클래스 계층 구조에서 필드 찾기
                    Class<?> superClass = clazz.getSuperclass();
                    while (superClass != null) {
                        try {
                            Field field = superClass.getDeclaredField(fieldName);
                            cache.put(clazz, field);
                            break;
                        } catch (NoSuchFieldException ex) {
                            superClass = superClass.getSuperclass();
                        }
                    }

                    if (!cache.containsKey(clazz)) {
                        cache.put(clazz, null);
                    }
                }
            }

            return cache.get(clazz);
        }
    }
}
