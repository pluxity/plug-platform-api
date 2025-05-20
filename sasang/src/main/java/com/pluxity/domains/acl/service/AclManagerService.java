package com.pluxity.domains.acl.service;

import java.io.Serializable;
import java.util.List;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

public interface AclManagerService {

    <T> void addPermission(
            Class<T> domainType, Serializable identifier, Sid sid, Permission permission);

    <T> void addPermissions(
            Class<T> domainType, Serializable identifier, Sid sid, List<Permission> permissions);

    <T> void addPermissionForUser(
            Class<T> domainType, Serializable identifier, String username, Permission permission);

    <T> void addPermissionsForUser(
            Class<T> domainType, Serializable identifier, String username, List<Permission> permissions);

    <T> void addPermissionForRole(
            Class<T> domainType, Serializable identifier, String role, Permission permission);

    <T> void addPermissionsForRole(
            Class<T> domainType, Serializable identifier, String role, List<Permission> permissions);

    <T> void removePermission(
            Class<T> domainType, Serializable identifier, Sid sid, Permission permission);

    <T> void removePermissions(
            Class<T> domainType, Serializable identifier, Sid sid, List<Permission> permissions);

    <T> void removePermissionForUser(
            Class<T> domainType, Serializable identifier, String username, Permission permission);

    <T> void removePermissionsForUser(
            Class<T> domainType, Serializable identifier, String username, List<Permission> permissions);

    <T> void removePermissionForRole(
            Class<T> domainType, Serializable identifier, String role, Permission permission);

    <T> void removePermissionsForRole(
            Class<T> domainType, Serializable identifier, String role, List<Permission> permissions);

    <T> void removeAllPermissions(Class<T> domainType, Serializable identifier);

    <T> void removeAllPermissionsForUser(
            Class<T> domainType, Serializable identifier, String username);

    <T> void removeAllPermissionsForRole(Class<T> domainType, Serializable identifier, String role);

    <T> boolean hasPermission(
            Class<T> domainType, Serializable identifier, Sid sid, Permission permission);

    <T> boolean hasPermissionForUser(
            Class<T> domainType, Serializable identifier, String username, Permission permission);

    <T> boolean hasPermissionForRole(
            Class<T> domainType, Serializable identifier, String role, Permission permission);
}
