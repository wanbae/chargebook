package com.oneship.chargebook.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Data
public class ChargeData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Date date;
    
    @Column(nullable = false)
    private Double amountOfCharge = 0.0;

    @Column(nullable = false)
    private Integer price = 0;

    private Integer point = 0;
    private Integer distance = 0;
    private Integer discountRate = 0;
    private String card;
    private String company;
    private Integer discountedPrice = 0;
    private Integer finalPrice = 0;
    private Integer finalUnitPrice = 0;

    public Integer getUnitPrice() {
        if (amountOfCharge != null && amountOfCharge > 0) {
            return (int) Math.round(price / amountOfCharge);
        } else {
            return 0;
        }
    }
}
