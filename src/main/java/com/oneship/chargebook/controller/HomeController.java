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
import com.oneship.chargebook.service.CardDiscountService;
import com.oneship.chargebook.service.ChargeDataService;

@Controller
public class HomeController {

    @Autowired
    private ChargeDataService chargeDataService;

    @Autowired
    private CardDiscountService cardDiscountService;

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

        List<ChargeData> chargeDataList = chargeDataService.getChargeDataByMonth(month);
        model.addAttribute("chargeDataList", chargeDataList);
        model.addAttribute("selectedMonth", month);

        // 카드별 청구금액 총합 및 사업자별 충전량 총합 계산
        Map<String, Integer> totalPriceByCard = chargeDataService.getTotalPriceByCard(month);
        Map<String, Integer> totalChargeByCompany = chargeDataService.getTotalChargeByCompany(month);
        model.addAttribute("totalPriceByCard", totalPriceByCard);
        model.addAttribute("totalChargeByCompany", totalChargeByCompany);

        return "index";
    }

    @GetMapping("/chart")
    public String chart(Model model, @RequestParam(value = "month", required = false) String month) {
        if (month == null || month.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            month = sdf.format(new Date());
        }

        List<ChargeData> chargeDataList = chargeDataService.getChargeDataByMonth(month);
        model.addAttribute("selectedMonth", month);

        // 차트 데이터 추가
        Map<String, Integer> totalPriceByCard = chargeDataService.getTotalPriceByCard(month);
        model.addAttribute("cardNames", totalPriceByCard.keySet());
        model.addAttribute("cardPrices", totalPriceByCard.values());

        return "chart";
    }

    @GetMapping("/input")
    public String input(Model model) {
        model.addAttribute("chargeData", new ChargeData());
        model.addAttribute("today", new Date());
        return "input";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute ChargeData chargeData) {
        chargeDataService.saveChargeData(chargeData);
        return "redirect:/";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Optional<ChargeData> chargeDataOptional = chargeDataService.getChargeDataById(id);
        if (chargeDataOptional.isPresent()) {
            model.addAttribute("chargeData", chargeDataOptional.get());
            return "edit";
        } else {
            return "redirect:/";
        }
    }

    @PostMapping("/update")
    public String update(@ModelAttribute ChargeData chargeData) {
        chargeDataService.saveChargeData(chargeData);
        return "redirect:/";
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public void delete(@PathVariable Long id) {
        chargeDataService.deleteChargeData(id);
    }

    @GetMapping("/api/getDiscountRate")
    @ResponseBody
    public Map<String, Object> getDiscountRate(@RequestParam String cardName) {
        Map<String, Object> response = new HashMap<>();
        try {
            int discountRate = cardDiscountService.getDiscountByCardName(cardName);
            response.put("discountRate", discountRate);
        } catch (IllegalArgumentException e) {
            response.put("error", 1); // Error code to indicate failure
            response.put("message", e.getMessage());
        }
        return response;
    }
}
