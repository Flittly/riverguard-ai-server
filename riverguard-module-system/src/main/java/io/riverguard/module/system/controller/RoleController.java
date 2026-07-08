package io.riverguard.module.system.controller;

import io.riverguard.common.result.R;
import io.riverguard.module.system.service.UserService;
import io.riverguard.module.system.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final UserService userService;

    @GetMapping("/scopes")
    public R<List<RoleVO>> scopes() {
        return R.ok(userService.getManageableRoles());
    }
}
