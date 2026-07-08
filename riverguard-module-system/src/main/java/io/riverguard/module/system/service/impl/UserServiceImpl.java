package io.riverguard.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.riverguard.common.exception.BusinessException;
import io.riverguard.common.exception.ResultCode;
import io.riverguard.common.result.PageResult;
import io.riverguard.module.system.dto.*;
import io.riverguard.module.system.entity.*;
import io.riverguard.module.system.mapper.*;
import io.riverguard.module.system.security.CustomUserDetails;
import io.riverguard.module.system.security.JwtTokenProvider;
import io.riverguard.module.system.service.UserService;
import io.riverguard.module.system.vo.LoginVO;
import io.riverguard.module.system.vo.UserVO;
import io.riverguard.module.system.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleScopeMapper sysRoleScopeMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginVO login(LoginDTO dto) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException(ResultCode.BAD_CREDENTIALS);
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.BAD_CREDENTIALS);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        List<String> roleCodes = getUserRoleCodes(user.getId());
        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername(), roleCodes);
        UserVO userVO = buildUserVO(user, roleCodes);
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(userVO);
        return loginVO;
    }

    @Override
    public UserVO getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        SysUser user = sysUserMapper.selectById(details.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        List<String> roleCodes = getUserRoleCodes(user.getId());
        return buildUserVO(user, roleCodes);
    }

    @Override
    public PageResult<UserVO> listUsers(UserPageQuery query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            wrapper.and(w -> w.like(SysUser::getUsername, query.getKeyword())
                    .or().like(SysUser::getNickname, query.getKeyword()));
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(SysUser::getCreateTime);

        IPage<SysUser> iPage = sysUserMapper.selectPage(
                Page.of(query.getPage(), query.getSize()), wrapper);

        List<Long> userIds = iPage.getRecords().stream().map(SysUser::getId).toList();
        Map<Long, List<String>> roleCodeMap = getUserRoleCodesBatch(userIds);

        PageResult<UserVO> result = new PageResult<>();
        result.setRecords(iPage.getRecords().stream()
                .map(u -> buildUserVO(u, roleCodeMap.getOrDefault(u.getId(), List.of())))
                .toList());
        result.setTotal(iPage.getTotal());
        result.setPage(iPage.getCurrent());
        result.setSize(iPage.getSize());
        return result;
    }

    @Override
    @Transactional
    public void createUser(UserCreateDTO dto) {
        LambdaQueryWrapper<SysUser> check = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername());
        if (sysUserMapper.selectCount(check) > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(1);
        sysUserMapper.insert(user);
    }

    @Override
    @Transactional
    public void updateUser(UserUpdateDTO dto) {
        SysUser user = sysUserMapper.selectById(dto.getId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        validateRoleScope(user.getId());
        sysUserMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void assignRoles(AssignRoleDTO dto) {
        SysUser user = sysUserMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        validateRoleScope(dto.getUserId());

        List<Long> manageableRoleIds = getCurrentUserManageableRoleIds();
        if (dto.getRoleIds() != null) {
            for (Long roleId : dto.getRoleIds()) {
                if (!manageableRoleIds.contains(roleId)) {
                    throw new BusinessException(ResultCode.ROLE_NOT_IN_SCOPE);
                }
            }
        }

        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, dto.getUserId()));
        if (dto.getRoleIds() != null) {
            for (Long roleId : dto.getRoleIds()) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(dto.getUserId());
                userRole.setRoleId(roleId);
                sysUserRoleMapper.insert(userRole);
            }
        }
    }

    @Override
    public List<RoleVO> getManageableRoles() {
        List<Long> manageableRoleIds = getCurrentUserManageableRoleIds();
        if (manageableRoleIds.isEmpty()) {
            return List.of();
        }
        List<SysRole> roles = sysRoleMapper.selectBatchIds(manageableRoleIds);
        return roles.stream()
                .map(r -> {
                    RoleVO vo = new RoleVO();
                    vo.setId(r.getId());
                    vo.setCode(r.getCode());
                    vo.setName(r.getName());
                    vo.setDescription(r.getDescription());
                    return vo;
                }).toList();
    }

    private void validateRoleScope(Long targetUserId) {
        List<String> targetRoleCodes = getUserRoleCodes(targetUserId);
        for (String roleCode : targetRoleCodes) {
            SysRole role = sysRoleMapper.selectOne(
                    new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, roleCode));
            if (role != null && !getCurrentUserManageableRoleIds().contains(role.getId())) {
                throw new BusinessException(ResultCode.ROLE_NOT_IN_SCOPE);
            }
        }
    }

    private List<String> getUserRoleCodes(Long userId) {
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return List.of();
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
        List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);
        return roles.stream().map(SysRole::getCode).toList();
    }

    private Map<Long, List<String>> getUserRoleCodesBatch(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        List<SysUserRole> allUserRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds));
        if (allUserRoles.isEmpty()) {
            return Map.of();
        }
        Set<Long> roleIds = allUserRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());
        Map<Long, String> roleIdToCode = new HashMap<>();
        if (!roleIds.isEmpty()) {
            List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);
            for (SysRole role : roles) {
                roleIdToCode.put(role.getId(), role.getCode());
            }
        }
        Map<Long, List<String>> result = new HashMap<>();
        for (SysUserRole ur : allUserRoles) {
            result.computeIfAbsent(ur.getUserId(), k -> new ArrayList<>())
                    .add(roleIdToCode.get(ur.getRoleId()));
        }
        return result;
    }

    private List<Long> getCurrentUserManageableRoleIds() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            return List.of();
        }
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        List<String> currentRoleCodes = details.getRoleCodes();
        if (currentRoleCodes == null || currentRoleCodes.isEmpty()) {
            return List.of();
        }
        List<SysRole> currentRoles = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().in(SysRole::getCode, currentRoleCodes));
        List<Long> currentRoleIds = currentRoles.stream().map(SysRole::getId).toList();
        if (currentRoleIds.isEmpty()) {
            return List.of();
        }
        List<SysRoleScope> scopes = sysRoleScopeMapper.selectList(
                new LambdaQueryWrapper<SysRoleScope>().in(SysRoleScope::getRoleId, currentRoleIds));
        return scopes.stream().map(SysRoleScope::getManageableRoleId).distinct().toList();
    }

    private UserVO buildUserVO(SysUser user, List<String> roleCodes) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus());
        vo.setRoleCodes(roleCodes);
        vo.setCreateTime(user.getCreateTime());

        if (!roleCodes.isEmpty()) {
            List<SysRole> roles = sysRoleMapper.selectList(
                    new LambdaQueryWrapper<SysRole>().in(SysRole::getCode, roleCodes));
            vo.setRoleNames(roles.stream().map(SysRole::getName).toList());
        } else {
            vo.setRoleNames(List.of());
        }
        return vo;
    }
}
