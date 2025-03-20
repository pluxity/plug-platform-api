package com.pluxity.permission.repository;

import com.pluxity.permission.entity.Permission;
import com.pluxity.user.entity.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;

@NoRepositoryBean
public interface PermissionRepository<T extends Permission> extends JpaRepository<T, Long> {

    List<T> findByRole(Role role);

    @EntityGraph(
            type = EntityGraph.EntityGraphType.FETCH,
            value = "Permission.withRoleAndUser"
    )
    @Query("""
            SELECT p 
            FROM #{#entityName} p 
                JOIN p.role r 
                JOIN r.userRoles ur 
                JOIN ur.user u 
            WHERE u.username = :username
            """)
    List<T> findByUsername(@Param("username") String username);

    @Query("""
            SELECT CASE 
                WHEN COUNT(p) > 0 THEN true 
                ELSE false 
            END 
            FROM #{#entityName} p 
            JOIN p.role r 
            JOIN r.userRoles ur 
            JOIN ur.user u 
            WHERE u.username = :username 
            AND p.resourceId = :resourceId
            """)
    boolean existsByUsernameAndResourceId(@Param("username") String username, @Param("resourceId") Long resourceId);

    @Query("""
            SELECT CASE 
                WHEN COUNT(r) > 0 THEN true 
                ELSE false 
            END 
            FROM Role r 
            JOIN r.userRoles ur 
            JOIN ur.user u 
            WHERE u.username = :username 
            AND r.roleName = 'ADMIN'
            """)
    boolean isAdmin(@Param("username") String username);
    
    @EntityGraph(
            type = EntityGraph.EntityGraphType.FETCH,
            value = "Permission.withRoleAndUser"
    )
    @Query("""
            SELECT p 
            FROM Permission p 
                JOIN p.role r 
                JOIN r.userRoles ur 
                JOIN ur.user u 
            WHERE u.username = :username 
            AND TYPE(p) = :type
            """)
    <S extends Permission> List<S> findByUsernameAndType(@Param("username") String username, @Param("type") Class<S> type);
} 