package com.example.carlocation.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Reservation extends BaseEntity<Long>{

    @Column(nullable = false)
    private LocalDate removal;

    @Column(nullable = false)
    private LocalDate theoricRestitution;

    private LocalDate restitution;

    @Enumerated(EnumType.ORDINAL)
    private ReservationStatus reservStatus;

    @OneToOne(cascade = CascadeType.PERSIST)
    private Reservation substitution;


    @ManyToOne
    @MapsId("CustomerId")
    private Customer customer;

    @ManyToOne
    private RentalFormula rentalFormula;

    @ManyToOne
    private Car car;

    private LocalDate cancellationDate;

    private double finDeleted;


    private LocalDate closingDate;

    public void delete() {
        this.cancellationDate = LocalDate.now();
    }

    public void cancel(){
        this.substitution = new Reservation();
        this.closingDate = LocalDate.now();
    }

    public void finish(){
        this.closingDate = LocalDate.now();
    }

    public double getIndicativePrice(){
        double maxKm = this.getRentalFormula().getMaxKm();
        double price_km = this.getCar().getModel().getPricingClass().getPrice_km();

        return maxKm * price_km ;
    }
}
