package com.bank.light.controllers;

import com.bank.light.exceptions.GatewayException;
import com.bank.light.exceptions.UserAlreadyExistException;
import com.bank.light.interfaces.GatewayService;
import com.bank.light.interfaces.UserService;
import com.bank.light.models.UserDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
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

    public AuthController(GatewayService gatewayService, UserService userService) {
        this.gatewayService = gatewayService;
        this.userService = userService;
    }

    @GetMapping
    public String homePage(Model model) {
        try {
            Map<String, Double> currencies = gatewayService.getCurrencies();
            model.addAttribute("usd", currencies.get("usd"));
            model.addAttribute("eur", currencies.get("eur"));
        } catch (GatewayException e) {
            model.addAttribute("MSG_ERROR", e);
        }
        return "home";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/registration")
    public String registrationPage(Model model) {
        UserDto userDto = new UserDto();
        model.addAttribute("user", userDto);
        return "registration";
    }

    @PostMapping("/registration")
    public String registerUser(@Valid UserDto userDto, BindingResult result, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if(result.hasErrors()){
            List<String> errors = result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());
            model.addAttribute("MSG_ERROR", String.join(", ", errors));
        } else {
            try {
                userService.register(userDto);
                request.login(userDto.getUsername(), userDto.getPassword());
                redirectAttributes.addFlashAttribute("MSG_SUCCESS", "Account successfully created");
                return "redirect:/";
            } catch (UserAlreadyExistException e) {
                model.addAttribute("MSG_ERROR", "An account for that username already exists.");
            } catch (ServletException e) {
                model.addAttribute("MSG_ERROR", "Error: " + e.getMessage());
            }
        }
        return "registration";
    }
}
