package com.bookstore.controller;

import com.bookstore.model.User;
import com.bookstore.service.UserService;
import com.bookstore.util.LoginGuard;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {
    private final UserService users;

    public UserController(UserService users) { this.users = users; }

    @GetMapping("/register")
    public String registerForm() { return "users/register"; }

    @PostMapping("/register")
    public String register(@RequestParam String name, @RequestParam String email, @RequestParam String password,
                           @RequestParam(defaultValue = "") String address, @RequestParam(defaultValue = "") String phone,
                           RedirectAttributes ra) {
        String err = users.register(name, email, password, address, phone);
        if (err != null) { ra.addFlashAttribute("error", err); return "redirect:/register"; }
        ra.addFlashAttribute("msg", "Account created. Log in to continue.");
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm() { return "users/login"; }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, HttpServletRequest req, HttpSession s, RedirectAttributes ra) {
        String key = "user:" + req.getRemoteAddr() + ":" + email.trim().toLowerCase();
        if (LoginGuard.isLocked(key)) {
            ra.addFlashAttribute("error", "Too many wrong attempts. Try again in " + LoginGuard.minutesLeft(key) + " minute(s).");
            return "redirect:/login";
        }
        User u = users.login(email, password);
        if (u == null) { LoginGuard.fail(key); ra.addFlashAttribute("error", "Wrong email or password."); return "redirect:/login"; }
        LoginGuard.success(key);
        req.changeSessionId();
        s.setAttribute("userId", u.id);
        s.setAttribute("userName", u.name);
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession s) {
        s.removeAttribute("userId");
        s.removeAttribute("userName");
        s.removeAttribute("cart");
        return "redirect:/";
    }

    @GetMapping("/profile")
    public String profile(HttpSession s, Model m, RedirectAttributes ra) {
        String id = (String) s.getAttribute("userId");
        if (id == null) { ra.addFlashAttribute("error", "Log in to see your profile."); return "redirect:/login"; }
        m.addAttribute("user", users.find(id));
        return "users/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam String name, @RequestParam(defaultValue = "") String address,
                                @RequestParam(defaultValue = "") String phone, HttpSession s, RedirectAttributes ra) {
        String id = (String) s.getAttribute("userId");
        if (id == null) return "redirect:/login";
        String err = users.updateProfile(id, name, address, phone);
        if (err != null) { ra.addFlashAttribute("error", err); return "redirect:/profile"; }
        s.setAttribute("userName", name.trim());
        ra.addFlashAttribute("msg", "Profile saved.");
        return "redirect:/profile";
    }

    /** Read-only user list for admins. Editing happens under /admin/users. */
    @GetMapping("/users")
    public String list(HttpSession s, Model m) {
        if (s.getAttribute("adminId") == null) return "redirect:/admin/login";
        m.addAttribute("users", users.all());
        return "users/user-list";
    }
}
