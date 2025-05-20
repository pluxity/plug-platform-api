package com.pluxity.domains.acl.service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;
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
        return Optional.ofNullable(getExistingAcl(oi)).orElseGet(() -> mutableAclService.createAcl(oi));
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
        acl.insertAce(acl.getEntries().size(), permission, sid, true);
        mutableAclService.updateAcl(acl);
    }

    @Override
    public <T> void addPermissions(
            Class<T> domainType, Serializable identifier, Sid sid, List<Permission> permissions) {
        validatePermissions(permissions);
        permissions.forEach(permission -> addPermission(domainType, identifier, sid, permission));
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
        permissions.forEach(permission -> addPermission(domainType, identifier, userSid, permission));
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
        permissions.forEach(permission -> addPermission(domainType, identifier, roleSid, permission));
    }

    @Override
    public <T> void removePermission(
            Class<T> domainType, Serializable identifier, Sid sid, Permission permission) {
        validateSidAndPermission(sid, permission);

        getAclOptional(domainType, identifier)
                .ifPresent(
                        acl -> {
                            findAndRemoveAce((MutableAcl) acl, sid, permission);
                            mutableAclService.updateAcl((MutableAcl) acl);
                        });
    }

    private void findAndRemoveAce(MutableAcl acl, Sid sid, Permission permission) {
        List<AccessControlEntry> entries = acl.getEntries();
        // 뒤에서부터 엔트리를 확인하면서 일치하는 첫 번째 항목을 찾아 삭제
        IntStream.range(0, entries.size())
                .map(i -> entries.size() - i - 1) // 역순으로 인덱스 생성
                .filter(
                        i -> {
                            AccessControlEntry entry = entries.get(i);
                            return entry.getSid().equals(sid) && entry.getPermission().equals(permission);
                        })
                .findFirst()
                .ifPresent(acl::deleteAce);
    }

    @Override
    public <T> void removePermissions(
            Class<T> domainType, Serializable identifier, Sid sid, List<Permission> permissions) {
        validatePermissions(permissions);
        permissions.forEach(permission -> removePermission(domainType, identifier, sid, permission));
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
        permissions.forEach(
                permission -> removePermission(domainType, identifier, userSid, permission));
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
        permissions.forEach(
                permission -> removePermission(domainType, identifier, roleSid, permission));
    }

    @Override
    public <T> void removeAllPermissions(Class<T> domainType, Serializable identifier) {
        validateParams(domainType, identifier);

        ObjectIdentity oi = createObjectIdentity(domainType, identifier);
        try {
            mutableAclService.deleteAcl(oi, false); // false: do not delete children
        } catch (NotFoundException e) {
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
                    removeAllAcesForSid(mutableAcl, userSid);
                    mutableAclService.updateAcl(mutableAcl);
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
                    removeAllAcesForSid(mutableAcl, roleSid);
                    mutableAclService.updateAcl(mutableAcl);
                });
    }

    private <T> void withMutableAcl(
            Class<T> domainType, Serializable identifier, Consumer<MutableAcl> action) {
        getAclOptional(domainType, identifier).map(acl -> (MutableAcl) acl).ifPresent(action);
    }

    private void removeAllAcesForSid(MutableAcl acl, Sid sid) {
        List<AccessControlEntry> entries = acl.getEntries();

        // 뒤에서부터 삭제해야 인덱스에 영향을 주지 않음
        IntStream.range(0, entries.size())
                .map(i -> entries.size() - i - 1) // 역순으로 인덱스 생성
                .filter(i -> entries.get(i).getSid().equals(sid))
                .forEach(acl::deleteAce);
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
