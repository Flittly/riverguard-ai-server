package io.riverguard.module.system.service;

import io.riverguard.common.result.PageResult;
import io.riverguard.module.system.dto.*;
import io.riverguard.module.system.vo.LoginVO;
import io.riverguard.module.system.vo.UserVO;
import io.riverguard.module.system.vo.RoleVO;

import java.util.List;

public interface UserService {

    LoginVO login(LoginDTO dto);

    UserVO getCurrentUser();

    PageResult<UserVO> listUsers(UserPageQuery query);

    void createUser(UserCreateDTO dto);

    void updateUser(UserUpdateDTO dto);

    void deleteUser(Long id);

    void assignRoles(AssignRoleDTO dto);

    List<RoleVO> getManageableRoles();
}
