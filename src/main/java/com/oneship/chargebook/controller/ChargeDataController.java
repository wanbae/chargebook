package com.oneship.chargebook.controller;

import java.security.Principal;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.oneship.chargebook.model.ChargeData;
import com.oneship.chargebook.model.User;
import com.oneship.chargebook.service.ChargeDataService;
import com.oneship.chargebook.service.CustomUserDetailsService;

@Controller
public class ChargeDataController {

    @Autowired
    private ChargeDataService chargeDataService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @GetMapping("/input")
    public String input(Model model) {
        model.addAttribute("chargeData", new ChargeData());
        model.addAttribute("today", new Date());
        return "input";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute ChargeData chargeData, Principal principal) throws Exception {
        User user = userDetailsService.getUserByPrincipal(principal);
        chargeData.setUser(user);
        chargeDataService.saveChargeData(chargeData, principal);
        return "redirect:/";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, Principal principal) {
        User user = userDetailsService.getUserByPrincipal(principal);
        Optional<ChargeData> chargeDataOptional = chargeDataService.getChargeDataByIdAndUser(id, user);
        if (chargeDataOptional.isPresent()) {
            model.addAttribute("chargeData", chargeDataOptional.get());
            return "edit";
        } else {
            return "redirect:/";
        }
    }

    @PostMapping("/update")
    public String update(@ModelAttribute ChargeData chargeData, Principal principal) throws Exception {
        User user = userDetailsService.getUserByPrincipal(principal);
        chargeData.setUser(user);
        chargeDataService.updateChargeData(chargeData, principal);
        return "redirect:/";
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public void delete(@PathVariable Long id, Principal principal) {
        User user = userDetailsService.getUserByPrincipal(principal);
        chargeDataService.deleteChargeData(id, user);
    }
}
