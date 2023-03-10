package com.example.carlocation.models.forms;

import com.example.carlocation.models.entities.Car;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CarAddForm {

    @NotBlank(message = "Must be not blank")
    private int buyPrice;

    private LocalDate buyDate;
    @PositiveOrZero
    private Long km;
    @NotBlank(message = "Must be not blank")
    private String supplier;

    private boolean repair = false;

    private LocalDate returnDate;
    @NotNull
    private ModelAddForm model;

    public Car toBLL(){
        return Car.builder()
                .buyPrice(buyPrice)
                .buyDate(buyDate)
                .km(km)
                .supplier(supplier)
                .repair(repair)
                .returnDate(returnDate)
                .model(model.toBLL())
                .build();
    }
}
