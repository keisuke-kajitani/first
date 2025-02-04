package com.example.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import com.example.domain.Administrator;
import com.example.form.InsertAdministratorForm;
import com.example.form.LoginForm;
import com.example.service.AdministratorService;

@SessionAttributes("administrator")
@Controller
@RequestMapping("/")
public class AdministratorController {

    @Autowired
    private AdministratorService administratorService;

    @Autowired
    private HttpSession session;

    @ModelAttribute
    public InsertAdministratorForm setUpInsertAdministratorForm() {
        return new InsertAdministratorForm();
    }

    @ModelAttribute
    public LoginForm setUpLoginForm() {
        return new LoginForm();
    }

    @GetMapping("/toInsert")
    public String toInsert() {
        return "administrator/insert";
    }

    @PostMapping("/insert")
    public String insert(@Valid InsertAdministratorForm form, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "administrator/insert"; // エラーがある場合はフォームに戻す
        }
        Administrator administrator = new Administrator();
        BeanUtils.copyProperties(form, administrator);

        // メールアドレス重複チェック
        if (administratorService.findByMailAddress(form.getMailAddress()) != null) {
            result.rejectValue("mailAddress", "error.mailAddress", "このメールアドレスは既に登録されています");
            return "administrator/insert";
        }

        administratorService.insert(administrator);
        return "employee/list"; // 登録後、従業員一覧画面に遷移
    }

    @GetMapping("/")
    public String toLogin() {
        return "administrator/login";
    }

    @PostMapping("/login")
    public String login(LoginForm form, RedirectAttributes redirectAttributes) {
        Administrator administrator = administratorService.login(form.getMailAddress(), form.getPassword());
        if (administrator == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "メールアドレスまたはパスワードが不正です。");
            return "redirect:/";
        }
        session.setAttribute("administratorName", administrator.getName());
        return "redirect:/employee/showList";
    }

    @GetMapping(value = "/logout")
    public String logout() {
        session.invalidate();
        return "redirect:/";
    }
}
