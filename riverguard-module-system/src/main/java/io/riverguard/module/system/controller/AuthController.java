package io.riverguard.module.system.controller;

import io.riverguard.common.result.R;
import io.riverguard.module.system.dto.LoginDTO;
import io.riverguard.module.system.service.UserService;
import io.riverguard.module.system.vo.LoginVO;
import io.riverguard.module.system.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public R<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return R.ok(userService.login(dto));
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        return R.ok();
    }

    @GetMapping("/me")
    public R<UserVO> me() {
        return R.ok(userService.getCurrentUser());
    }
}
