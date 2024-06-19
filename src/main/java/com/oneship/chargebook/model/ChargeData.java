package com.oneship.chargebook.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;

@Entity
@Data
public class ChargeData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Date date;
    private Double amountOfCharge;
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

    @PrePersist
    @PreUpdate
    private void setDefaultValues() {
        if (amountOfCharge == null) {
            amountOfCharge = 0.0;
        }
        if (price == null) {
            price = 0;
        }
        if (unitPrice == null) {
            unitPrice = 0;
        }
        if (point == null) {
            point = 0;
        }
        if (distance == null) {
            distance = 0;
        }
        if (discountRate == null) {
            discountRate = 0;
        }
        if (discountedPrice == null) {
            discountedPrice = 0;
        }
        if (finalPrice == null) {
            finalPrice = 0;
        }
        if (finalUnitPrice == null) {
            finalUnitPrice = 0;
        }
    }
}
