package vn.vcraft.backend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.vcraft.backend.model.Actor;

@RestController
@RequestMapping("/api/buyer")
class BuyerController {
    @GetMapping("/me")
    Actor me(@AuthenticationPrincipal Actor actor) {
        return actor;
    }
}
