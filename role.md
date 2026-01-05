# Role Permission Model

## Overview
- Replace `permission` with:
  - `resource_permission` (resource_id 포함)
  - `domain_permission` (resource_id 없음)
- `PermissionLevel`: `READ`, `WRITE`, `ADMIN`
- Domain permission represents global access for a resource domain.

## Key Behavior
- ID-based permission (resource):
  - `resource_permission` with `resource_id = <resourceId>`
- Domain permission:
  - `domain_permission` with `resource_name = <resource>`

## Enforcement
- `PermissionStrategy`:
  - RUD: domain permission 먼저 확인 후 resource permission 확인
- `PermissionCheckAspect`:
  - `CREATE` requires domain `WRITE`
  - `UPDATE` uses `WRITE`, `DELETE` uses `ADMIN` (unchanged)

## DTO Changes
- Removed:
  - `RoleGlobalPolicyRequest`
  - `RoleGlobalPolicyResponse`
  - `RoleGlobalPermissionType`
- `RoleCreateRequest` / `RoleUpdateRequest` no longer include `globalPolicies`.
- `RoleResponse` no longer includes `globalPolicies`.

## Data Model Notes
- No `role_global_policy` table.
- Domain permissions are stored in `domain_permission`.
- Resource permissions are stored in `resource_permission`.
