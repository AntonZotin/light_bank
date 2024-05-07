package com.bank.light.controllers;

import com.bank.light.domain.Role;
import com.bank.light.domain.User;
import com.bank.light.exceptions.UserEditException;
import com.bank.light.exceptions.UserNotFoundException;
import com.bank.light.interfaces.UserService;
import com.bank.light.models.UserEditDto;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final UserService userService;

    public AdminController(final UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public String usersPage(Model model, @RequestParam Map<String,String> params) {
        final String role = params.getOrDefault("role", null);
        final Long pages = userService.pageCount(role);
        model.addAttribute("pages", pages);
        int page = (params.containsKey("page")) ? Integer.parseInt(params.get("page")) - 1 : 0;
        if (page >= pages || page < 0) page = 0;
        final List<User> list = userService.findAllByPage(page, role);
        model.addAttribute("users", list);
        model.addAttribute("page", page);
        model.addAttribute("role", role);
        model.addAttribute("roles", List.of(ROLE, Role.USER_VALUE, Role.MANAGER_VALUE, Role.ADMIN_VALUE));
        return "users";
    }

    @GetMapping("/users/edit")
    public String userEditPage(Model model, @RequestParam Map<String,String> params, RedirectAttributes redirectAttributes) {
        final String userId = params.getOrDefault("id", "");
        if (userId.isEmpty() || !isInteger(userId)) {
            redirectAttributes.addFlashAttribute(MSG_ERROR, "Request has incorrect id parameter.");
            return REDIRECT_USERS;
        }
        User user;
        try {
            user = userService.get(Long.parseLong(userId));
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute(MSG_ERROR, "Requested user not found");
            return REDIRECT_USERS;
        }
        model.addAttribute("user", user);
        model.addAttribute("roles", List.of(Role.USER_VALUE, Role.MANAGER_VALUE, Role.ADMIN_VALUE));
        return "user_edit";
    }

    @PostMapping("/users/edit")
    public String userEdit(@Valid UserEditDto userEditDto, RedirectAttributes redirectAttributes) {
        try {
            userService.edit(userEditDto);
            redirectAttributes.addFlashAttribute("MSG_SUCCESS", "User change successfully");
            return REDIRECT_USERS;
        } catch (UserEditException e) {
            redirectAttributes.addFlashAttribute(MSG_ERROR, e.getMessage());
            redirectAttributes.addAttribute("id", userEditDto.getUserId());
            return "redirect:/users/edit";
        }
    }

    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static final String ROLE = "Select role";

    private static final String MSG_ERROR = "MSG_ERROR";

    private static final String REDIRECT_USERS = "redirect:/users";
}
