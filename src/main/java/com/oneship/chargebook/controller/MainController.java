package com.oneship.chargebook.controller;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestParam;

import com.oneship.chargebook.model.ChargeData;
import com.oneship.chargebook.model.User;
import com.oneship.chargebook.service.ChargeDataService;
import com.oneship.chargebook.service.CustomUserDetailsService;

@Controller
public class MainController {

    @Autowired
    private ChargeDataService chargeDataService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new org.springframework.beans.propertyeditors.CustomDateEditor(dateFormat, true));
    }

    @GetMapping("/")
    public String index(Model model, @RequestParam(value = "startDate", required = false) Date startDate, @RequestParam(value = "endDate", required = false) Date endDate, Principal principal) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (startDate == null || endDate == null) {
            Calendar cal = Calendar.getInstance();
            // 현재 월의 1일 0시 0분 0초로 설정
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            startDate = cal.getTime();
            
            // 현재 월의 마지막 날, 23시 59분 59초로 설정
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            endDate = cal.getTime();
        }

        User user = userDetailsService.getUserByPrincipal(principal);
        List<ChargeData> chargeDataList = chargeDataService.getChargeDataByDateRangeAndUser(startDate, endDate, user);
        
        sdf = new SimpleDateFormat("yyyy-MM-dd");

        model.addAttribute("chargeDataList", chargeDataList);
        model.addAttribute("startDate", sdf.format(startDate));
        model.addAttribute("endDate", sdf.format(endDate));

        // 카드별 청구금액 총합 및 사업자별 충전량 총합 계산
        Map<String, Integer> totalPriceByCard = chargeDataService.getTotalPriceByCard(startDate, endDate, user);
        Map<String, Integer> totalChargeByCompany = chargeDataService.getTotalChargeByCompany(startDate, endDate, user);
        model.addAttribute("totalPriceByCard", totalPriceByCard);
        model.addAttribute("totalChargeByCompany", totalChargeByCompany);

        return "index";
    }
}