package com.pluxity.domains.acl.service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@Transactional
public class AclManagerServiceImpl implements AclManagerService {

    private static final Logger logger = LoggerFactory.getLogger(AclManagerServiceImpl.class);
    private final MutableAclService mutableAclService;

    public AclManagerServiceImpl(MutableAclService mutableAclService) {
        this.mutableAclService = mutableAclService;
    }

    private <T> ObjectIdentity createObjectIdentity(Class<T> domainType, Serializable identifier) {
        validateParams(domainType, identifier);
        return new ObjectIdentityImpl(domainType, identifier);
    }

    private <T> MutableAcl getOrCreateAcl(Class<T> domainType, Serializable identifier) {
        ObjectIdentity oi = createObjectIdentity(domainType, identifier);
        return Optional.ofNullable(getExistingAcl(oi))
                .orElseGet(
                        () -> {
                            logger.debug(
                                    "ACL not found for {}:{}, creating new ACL", domainType.getName(), identifier);
                            return mutableAclService.createAcl(oi);
                        });
    }

    private MutableAcl getExistingAcl(ObjectIdentity oi) {
        try {
            return (MutableAcl) mutableAclService.readAclById(oi);
        } catch (NotFoundException nfe) {
            return null;
        }
    }

    @Override
    public <T> void addPermission(
            Class<T> domainType, Serializable identifier, Sid sid, Permission permission) {
        validateSidAndPermission(sid, permission);

        MutableAcl acl = getOrCreateAcl(domainType, identifier);

        // 중복 ACE 체크 로직 추가
        boolean alreadyExists =
                acl.getEntries().stream()
                        .anyMatch(
                                entry ->
                                        entry.getSid().equals(sid)
                                                && entry.getPermission().equals(permission)
                                                && entry.isGranting());

        if (!alreadyExists) {
            acl.insertAce(acl.getEntries().size(), permission, sid, true);
            mutableAclService.updateAcl(acl);
            logger.debug(
                    "Added permission {} for {} on {}:{}", permission, sid, domainType.getName(), identifier);
        } else {
            logger.debug(
                    "Permission {} for {} on {}:{} already exists, skipping",
                    permission,
                    sid,
                    domainType.getName(),
                    identifier);
        }
    }

    @Override
    public <T> void addPermissions(
            Class<T> domainType, Serializable identifier, Sid sid, List<Permission> permissions) {
        validatePermissions(permissions);

        if (permissions.isEmpty()) {
            return;
        }

        MutableAcl acl = getOrCreateAcl(domainType, identifier);
        boolean aclChanged = false;

        for (Permission permission : permissions) {
            // 중복 ACE 체크 로직
            boolean alreadyExists =
                    acl.getEntries().stream()
                            .anyMatch(
                                    entry ->
                                            entry.getSid().equals(sid)
                                                    && entry.getPermission().equals(permission)
                                                    && entry.isGranting());

            if (!alreadyExists) {
                acl.insertAce(acl.getEntries().size(), permission, sid, true);
                aclChanged = true;
                logger.debug(
                        "Added permission {} for {} on {}:{}",
                        permission,
                        sid,
                        domainType.getName(),
                        identifier);
            } else {
                logger.debug(
                        "Permission {} for {} on {}:{} already exists, skipping",
                        permission,
                        sid,
                        domainType.getName(),
                        identifier);
            }
        }

        if (aclChanged) {
            mutableAclService.updateAcl(acl);
        }
    }

    @Override
    public <T> void addPermissionForUser(
            Class<T> domainType, Serializable identifier, String username, Permission permission) {
        validateUsername(username);
        addPermission(domainType, identifier, new PrincipalSid(username), permission);
    }

    @Override
    public <T> void addPermissionsForUser(
            Class<T> domainType, Serializable identifier, String username, List<Permission> permissions) {
        validateUsername(username);
        validatePermissions(permissions);

        PrincipalSid userSid = new PrincipalSid(username);
        addPermissions(domainType, identifier, userSid, permissions);
    }

    @Override
    public <T> void addPermissionForRole(
            Class<T> domainType, Serializable identifier, String role, Permission permission) {
        validateRole(role);
        addPermission(domainType, identifier, new GrantedAuthoritySid(role), permission);
    }

    @Override
    public <T> void addPermissionsForRole(
            Class<T> domainType, Serializable identifier, String role, List<Permission> permissions) {
        validateRole(role);
        validatePermissions(permissions);

        GrantedAuthoritySid roleSid = new GrantedAuthoritySid(role);
        addPermissions(domainType, identifier, roleSid, permissions);
    }

    @Override
    public <T> void removePermission(
            Class<T> domainType, Serializable identifier, Sid sid, Permission permission) {
        validateSidAndPermission(sid, permission);

        getAclOptional(domainType, identifier)
                .map(acl -> (MutableAcl) acl)
                .ifPresent(
                        mutableAcl -> {
                            boolean removed = findAndRemoveAce(mutableAcl, sid, permission);
                            if (removed) {
                                mutableAclService.updateAcl(mutableAcl);
                                logger.debug(
                                        "Removed permission {} for {} on {}:{}",
                                        permission,
                                        sid,
                                        domainType.getName(),
                                        identifier);
                            } else {
                                logger.debug(
                                        "No matching permission {} for {} on {}:{} found to remove",
                                        permission,
                                        sid,
                                        domainType.getName(),
                                        identifier);
                            }
                        });
    }

    private boolean findAndRemoveAce(MutableAcl acl, Sid sid, Permission permission) {
        List<AccessControlEntry> entries = acl.getEntries();
        boolean removed = false;

        // 뒤에서부터 모든 일치하는 ACE를 삭제 (역순으로 처리해야 인덱스 문제 방지)
        for (int i = entries.size() - 1; i >= 0; i--) {
            AccessControlEntry entry = entries.get(i);
            if (entry.getSid().equals(sid) && entry.getPermission().equals(permission)) {
                acl.deleteAce(i);
                removed = true;
            }
        }

        return removed;
    }

    @Override
    public <T> void removePermissions(
            Class<T> domainType, Serializable identifier, Sid sid, List<Permission> permissions) {
        validatePermissions(permissions);

        if (permissions.isEmpty()) {
            return;
        }

        getAclOptional(domainType, identifier)
                .map(acl -> (MutableAcl) acl)
                .ifPresent(
                        mutableAcl -> {
                            boolean aclChanged = false;

                            for (Permission permission : permissions) {
                                boolean removed = findAndRemoveAce(mutableAcl, sid, permission);
                                if (removed) {
                                    aclChanged = true;
                                    logger.debug(
                                            "Removed permission {} for {} on {}:{}",
                                            permission,
                                            sid,
                                            domainType.getClass().getName(),
                                            identifier);
                                }
                            }

                            if (aclChanged) {
                                mutableAclService.updateAcl(mutableAcl);
                            }
                        });
    }

    @Override
    public <T> void removePermissionForUser(
            Class<T> domainType, Serializable identifier, String username, Permission permission) {
        validateUsername(username);
        removePermission(domainType, identifier, new PrincipalSid(username), permission);
    }

    @Override
    public <T> void removePermissionsForUser(
            Class<T> domainType, Serializable identifier, String username, List<Permission> permissions) {
        validateUsername(username);
        validatePermissions(permissions);

        PrincipalSid userSid = new PrincipalSid(username);
        removePermissions(domainType, identifier, userSid, permissions);
    }

    @Override
    public <T> void removePermissionForRole(
            Class<T> domainType, Serializable identifier, String role, Permission permission) {
        validateRole(role);
        removePermission(domainType, identifier, new GrantedAuthoritySid(role), permission);
    }

    @Override
    public <T> void removePermissionsForRole(
            Class<T> domainType, Serializable identifier, String role, List<Permission> permissions) {
        validateRole(role);
        validatePermissions(permissions);

        GrantedAuthoritySid roleSid = new GrantedAuthoritySid(role);
        removePermissions(domainType, identifier, roleSid, permissions);
    }

    @Override
    public <T> void removeAllPermissions(Class<T> domainType, Serializable identifier) {
        validateParams(domainType, identifier);

        ObjectIdentity oi = createObjectIdentity(domainType, identifier);
        try {
            mutableAclService.deleteAcl(oi, false); // false: do not delete children
            logger.debug("Removed all permissions for {}:{}", domainType.getName(), identifier);
        } catch (NotFoundException e) {
            logger.debug("ACL not found for {}:{}, nothing to remove", domainType.getName(), identifier);
            // ACL이 존재하지 않는 경우 무시
        }
    }

    @Override
    public <T> boolean hasPermission(
            Class<T> domainType, Serializable identifier, Sid sid, Permission permission) {
        validateSidAndPermission(sid, permission);

        return getAclOptional(domainType, identifier)
                .map(acl -> isPermissionGranted(acl, sid, permission))
                .orElse(false);
    }

    private boolean isPermissionGranted(Acl acl, Sid sid, Permission permission) {
        try {
            return acl.isGranted(List.of(permission), List.of(sid), false);
        } catch (NotFoundException e) {
            return false;
        }
    }

    private <T> Optional<Acl> getAclOptional(Class<T> domainType, Serializable identifier) {
        ObjectIdentity oi = createObjectIdentity(domainType, identifier);
        try {
            return Optional.of(mutableAclService.readAclById(oi));
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public <T> boolean hasPermissionForUser(
            Class<T> domainType, Serializable identifier, String username, Permission permission) {
        validateUsername(username);
        return hasPermission(domainType, identifier, new PrincipalSid(username), permission);
    }

    @Override
    public <T> boolean hasPermissionForRole(
            Class<T> domainType, Serializable identifier, String role, Permission permission) {
        validateRole(role);
        return hasPermission(domainType, identifier, new GrantedAuthoritySid(role), permission);
    }

    @Override
    public <T> void removeAllPermissionsForUser(
            Class<T> domainType, Serializable identifier, String username) {
        validateUsername(username);
        validateParams(domainType, identifier);

        withMutableAcl(
                domainType,
                identifier,
                mutableAcl -> {
                    PrincipalSid userSid = new PrincipalSid(username);
                    boolean removed = removeAllAcesForSid(mutableAcl, userSid);
                    if (removed) {
                        mutableAclService.updateAcl(mutableAcl);
                        logger.debug(
                                "Removed all permissions for user {} on {}:{}",
                                username,
                                domainType.getName(),
                                identifier);
                    } else {
                        logger.debug(
                                "No permissions found for user {} on {}:{}",
                                username,
                                domainType.getName(),
                                identifier);
                    }
                });
    }

    @Override
    public <T> void removeAllPermissionsForRole(
            Class<T> domainType, Serializable identifier, String role) {
        validateRole(role);
        validateParams(domainType, identifier);

        withMutableAcl(
                domainType,
                identifier,
                mutableAcl -> {
                    GrantedAuthoritySid roleSid = new GrantedAuthoritySid(role);
                    boolean removed = removeAllAcesForSid(mutableAcl, roleSid);
                    if (removed) {
                        mutableAclService.updateAcl(mutableAcl);
                        logger.debug(
                                "Removed all permissions for role {} on {}:{}",
                                role,
                                domainType.getName(),
                                identifier);
                    } else {
                        logger.debug(
                                "No permissions found for role {} on {}:{}",
                                role,
                                domainType.getName(),
                                identifier);
                    }
                });
    }

    private <T> void withMutableAcl(
            Class<T> domainType, Serializable identifier, Consumer<MutableAcl> action) {
        getAclOptional(domainType, identifier)
                .map(
                        acl -> {
                            if (!(acl instanceof MutableAcl)) {
                                logger.warn("Expected MutableAcl but got {}", acl.getClass().getName());
                                return null;
                            }
                            return (MutableAcl) acl;
                        })
                .ifPresent(action);
    }

    private boolean removeAllAcesForSid(MutableAcl acl, Sid sid) {
        List<AccessControlEntry> entries = acl.getEntries();
        boolean removed = false;

        // 뒤에서부터 삭제해야 인덱스에 영향을 주지 않음
        for (int i = entries.size() - 1; i >= 0; i--) {
            if (entries.get(i).getSid().equals(sid)) {
                acl.deleteAce(i);
                removed = true;
            }
        }

        return removed;
    }

    private <T> void validateParams(Class<T> domainType, Serializable identifier) {
        Assert.notNull(domainType, "Domain type cannot be null");
        Assert.notNull(identifier, "Identifier cannot be null");
    }

    private void validateSidAndPermission(Sid sid, Permission permission) {
        Assert.notNull(sid, "Sid cannot be null");
        Assert.notNull(permission, "Permission cannot be null");
    }

    private void validateUsername(String username) {
        Assert.hasText(username, "Username cannot be empty");
    }

    private void validateRole(String role) {
        Assert.hasText(role, "Role cannot be empty");
    }

    private void validatePermissions(List<Permission> permissions) {
        Assert.notNull(permissions, "Permissions list cannot be null");
        Assert.notEmpty(permissions, "Permissions list cannot be empty");
        permissions.forEach(permission -> Assert.notNull(permission, "Permission cannot be null"));
    }
}
