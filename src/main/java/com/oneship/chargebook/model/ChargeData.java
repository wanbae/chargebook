package com.oneship.chargebook.model;

import lombok.Data;

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
    private Integer amountOfCharge;
    private Integer price;
    private Integer unitPrice;
    private Integer point;
    private Integer distance;
    private Integer discountRate;
    private String card;
    private String company;
    private Integer discountedPrice;
    private Integer finalPrice;
    private Integer finalUnitPrice;
}
