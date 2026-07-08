package io.riverguard.module.system.controller;

import io.riverguard.common.result.PageResult;
import io.riverguard.common.result.R;
import io.riverguard.module.system.dto.AssignRoleDTO;
import io.riverguard.module.system.dto.UserCreateDTO;
import io.riverguard.module.system.dto.UserPageQuery;
import io.riverguard.module.system.dto.UserUpdateDTO;
import io.riverguard.module.system.service.UserService;
import io.riverguard.module.system.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public R<PageResult<UserVO>> list(@Valid UserPageQuery query) {
        return R.ok(userService.listUsers(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public R<UserVO> getById(@PathVariable Long id) {
        UserPageQuery query = new UserPageQuery();
        query.setPage(1);
        query.setSize(1);
        PageResult<UserVO> result = userService.listUsers(query);
        UserVO found = result.getRecords().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
        return R.ok(found);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public R<Void> create(@RequestBody @Valid UserCreateDTO dto) {
        userService.createUser(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public R<Void> update(@PathVariable Long id, @RequestBody @Valid UserUpdateDTO dto) {
        dto.setId(id);
        userService.updateUser(dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public R<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return R.ok();
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public R<Void> assignRoles(@PathVariable Long id, @RequestBody @Valid AssignRoleDTO dto) {
        dto.setUserId(id);
        userService.assignRoles(dto);
        return R.ok();
    }
}
