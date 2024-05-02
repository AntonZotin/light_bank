package com.bank.light.controllers;

import com.bank.light.interfaces.NotificationService;
import java.security.Principal;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping
@PreAuthorize("hasAuthority('ROLE_MANAGER')")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public String notificationsPage(Principal principal, Model model, @RequestParam Map<String,String> params) {
        Long pages = notificationService.pageCount(principal.getName());
        model.addAttribute("pages", pages);
        int page = (params.containsKey("page")) ? Integer.parseInt(params.get("page")) - 1 : 0;
        if (page >= pages || page < 0) page = 0;
        model.addAttribute("page", page);
        model.addAttribute("notifications", notificationService.notificationList(page, principal.getName()));
        return "notifications";
    }

    @GetMapping("/readNotification")
    public String readNotification(@RequestParam Map<String,String> params, RedirectAttributes redirectAttributes) {
        if (params.containsKey("id")) {
            notificationService.readNotification(Long.parseLong(params.get("id")));
        }
        if (params.containsKey("page") && !params.get("page").isEmpty()) {
            redirectAttributes.addAttribute("page", params.get("page"));
        }
        return "redirect:/notifications";
    }
}
