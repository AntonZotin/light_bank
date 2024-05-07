package com.bank.light.controllers;

import com.bank.light.exceptions.GatewayException;
import com.bank.light.exceptions.UserAlreadyExistException;
import com.bank.light.interfaces.GatewayService;
import com.bank.light.interfaces.UserService;
import com.bank.light.models.UserDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping
public class AuthController {

    private final GatewayService gatewayService;

    private final UserService userService;

    public AuthController(final GatewayService gatewayService, final UserService userService) {
        this.gatewayService = gatewayService;
        this.userService = userService;
    }

    @GetMapping
    public String homePage(Model model) {
        try {
            final Map<String, Double> currencies = gatewayService.getCurrencies();
            model.addAttribute("usd", currencies.get("usd"));
            model.addAttribute("eur", currencies.get("eur"));
        } catch (GatewayException e) {
            model.addAttribute(MSG_ERROR, e);
        }
        return "home";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/registration")
    public String registrationPage(Model model) {
        model.addAttribute("user", new UserDto());
        return "registration";
    }

    @PostMapping("/registration")
    public String registerUser(@Valid UserDto userDto, BindingResult result, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if(result.hasErrors()){
            String errors = result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(", "));
            model.addAttribute(MSG_ERROR, errors);
        } else {
            try {
                userService.register(userDto);
                request.login(userDto.getUsername(), userDto.getPassword());
                redirectAttributes.addFlashAttribute("MSG_SUCCESS", "Account successfully created");
                return "redirect:/";
            } catch (UserAlreadyExistException e) {
                model.addAttribute(MSG_ERROR, "An account for that username already exists.");
            } catch (ServletException e) {
                model.addAttribute(MSG_ERROR, "Error: " + e.getMessage());
            }
        }
        return "registration";
    }

    private static final String MSG_ERROR = "MSG_ERROR";
}
