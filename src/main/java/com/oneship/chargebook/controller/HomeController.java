package com.oneship.chargebook.controller;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
import com.oneship.chargebook.model.User;
import com.oneship.chargebook.service.CardDiscountService;
import com.oneship.chargebook.service.ChargeDataService;
import com.oneship.chargebook.service.CustomUserDetailsService;

@Controller
public class HomeController {

    @Autowired
    private ChargeDataService chargeDataService;

    @Autowired
    private CardDiscountService cardDiscountService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new org.springframework.beans.propertyeditors.CustomDateEditor(dateFormat, true));
    }

    @GetMapping("/")
    public String index(Model model, @RequestParam(value = "month", required = false) String month, Principal principal) {
        if (month == null || month.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            month = sdf.format(new Date());
        }

        User user = userDetailsService.getUserByPrincipal(principal);
        List<ChargeData> chargeDataList = chargeDataService.getChargeDataByMonthAndUser(month, user);
        model.addAttribute("chargeDataList", chargeDataList);
        model.addAttribute("selectedMonth", month);

        // 카드별 청구금액 총합 및 사업자별 충전량 총합 계산
        Map<String, Integer> totalPriceByCard = chargeDataService.getTotalPriceByCard(month, user);
        Map<String, Integer> totalChargeByCompany = chargeDataService.getTotalChargeByCompany(month, user);
        model.addAttribute("totalPriceByCard", totalPriceByCard);
        model.addAttribute("totalChargeByCompany", totalChargeByCompany);

        return "index";
    }

    @GetMapping("/chart")
    public String chart(Model model, @RequestParam(value = "month", required = false) String month, Principal principal) {
        if (month == null || month.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            month = sdf.format(new Date());
        }

        User user = userDetailsService.getUserByPrincipal(principal);
        List<ChargeData> chargeDataList = chargeDataService.getChargeDataByMonthAndUser(month, user);
        model.addAttribute("selectedMonth", month);

        // 차트 데이터 추가
        Map<String, Integer> totalPriceByCard = chargeDataService.getTotalPriceByCard(month, user);
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

    @GetMapping("/api/accumulatedDistance")
    @ResponseBody
    public Map<String, Object> getAccumulatedDistance(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userDetailsService.getUserByPrincipal(principal);
            long distance = chargeDataService.getAccumulatedDistance(user);
            double batteryStatus = chargeDataService.getBatteryStatus(user);
            double drivingRange = chargeDataService.getDrivingRange(user);
            response.put("distance", distance);
            response.put("batteryStatus", batteryStatus);
            response.put("drivingRange", drivingRange);
        } catch (Exception e) {
            response.put("error", 1);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/user")
    @ResponseBody
    public Map<String, Object> getUser(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) principal;
            OAuth2User oAuth2User = oAuth2AuthenticationToken.getPrincipal();
            String email = oAuth2User.getAttribute("email");
            User user = userDetailsService.getUserByPrincipal(principal);
            response.put("email", email);
            response.put("user", user);
        } else {
            response.put("error", "Principal is not OAuth2AuthenticationToken");
        }
        return response;
    }

    @GetMapping("/api/chargeHistory/{companyName}")
    @ResponseBody
    public List<ChargeData> getChargeHistoryByCompanyAndDate(
        @PathVariable String companyName, 
        @RequestParam String date,
        Principal principal
    ) {
        User user = userDetailsService.getUserByPrincipal(principal);
        return chargeDataService.getChargeDataByCompanyAndDateAndUser(companyName, date, user);
    }

}
