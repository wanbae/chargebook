package com.oneship.chargebook.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class ChargeData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
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

    @Column(nullable = false)
    private Double batteryStatus = 0.0;

    @Column(nullable = false)
    private Double drivingRange = 0.0;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public ChargeData(Date date, Double amountOfCharge, Integer price, Integer point, Integer discountedPrice, Integer discountRate, Integer finalPrice, Integer finalUnitPrice, Integer distance, String card, String company, User user) {
        this.date = date;
        this.amountOfCharge = amountOfCharge;
        this.price = price;
        this.point = point;
        this.discountedPrice = discountedPrice;
        this.discountRate = discountRate;
        this.finalPrice = finalPrice;
        this.finalUnitPrice = finalUnitPrice;
        this.distance = distance;
        this.card = card;
        this.company = company;
        this.user = user;
    }

    public Integer getUnitPrice() {
        if (amountOfCharge != null && amountOfCharge > 0) {
            return (int) Math.round(price / amountOfCharge);
        } else {
            return 0;
        }
    }
}
