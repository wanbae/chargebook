package com.oneship.chargebook.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.oneship.chargebook.model.ChargeData;
import com.oneship.chargebook.service.ChargeDataService;

@Controller
public class HomeController {

    @Autowired
    private ChargeDataService chargeDataService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new org.springframework.beans.propertyeditors.CustomDateEditor(dateFormat, true));
    }

    @GetMapping("/")
    public String index(Model model, @RequestParam(value = "month", required = false) String month) {
        if (month == null || month.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            month = sdf.format(new Date());
        }

        List<ChargeData> chargeDataList;
        try {
            chargeDataList = chargeDataService.getChargeDataByMonth(month);
        } catch (Exception e) {
            e.printStackTrace();
            chargeDataList = chargeDataService.getAllChargeData();
        }

        model.addAttribute("chargeDataList", chargeDataList);
        model.addAttribute("selectedMonth", month);

        return "index";
    }

    @GetMapping("/popup")
    public String popup() {
        return "popup";
    }

    @GetMapping("/input")
    public String input(Model model) {
        model.addAttribute("chargeData", new ChargeData());

        int accumulatedDistance = chargeDataService.getAccumulatedDistance();
        model.addAttribute("accumulatedDistance", accumulatedDistance);

        return "input";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute ChargeData chargeData) {
        chargeDataService.saveChargeData(chargeData);
        return "redirect:/";
    }

    @PostMapping("/api/getLatestChargeData")
    public @ResponseBody
    Map<String, String> getLatestChargeData(@RequestParam String company) {
        Map<String, String> data = new HashMap<>();
        data.put("amountOfCharge", "150");
        data.put("price", "3000");
        data.put("point", "100");
        data.put("card", "삼성");

        return data;
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public void delete(@PathVariable Long id) {
        chargeDataService.deleteChargeData(id);
    }

    @GetMapping("/edit")
    public String edit(@RequestParam("month") String month, Model model) {
        List<ChargeData> chargeDataList = chargeDataService.getChargeDataByMonth(month);
        model.addAttribute("chargeDataList", chargeDataList);
        model.addAttribute("selectedMonth", month);
        return "edit";
    }

    @GetMapping("/edit/{id}")
    public String editPopup(@PathVariable Long id, @RequestParam("month") String month, Model model) {
        Optional<ChargeData> chargeDataOptional = chargeDataService.getChargeDataById(id);
        if (chargeDataOptional.isPresent()) {
            model.addAttribute("chargeData", chargeDataOptional.get());
            model.addAttribute("selectedMonth", month);
            return "edit_popup";
        } else {
            return "redirect:/edit?month=" + month;
        }
    }

    @PostMapping("/update")
    public String update(@ModelAttribute ChargeData chargeData, @RequestParam("month") String month) {
        chargeDataService.saveChargeData(chargeData);
        return "redirect:/edit?month=" + month;
    }
}
