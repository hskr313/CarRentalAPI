package com.example.carlocation.controllers;

import com.example.carlocation.exceptions.HttpNotFoundException;
import com.example.carlocation.exceptions.HttpPreconditionFailedException;
import com.example.carlocation.models.dtos.fin.FinDTO;
import com.example.carlocation.models.dtos.rental.RentalDTO;
import com.example.carlocation.models.entities.Fin;
import com.example.carlocation.models.entities.Rental;
import com.example.carlocation.models.entities.Reservation;
import com.example.carlocation.models.forms.RentalAddForm;
import com.example.carlocation.models.forms.RentalReturnForm;
import com.example.carlocation.services.car.CarService;
import com.example.carlocation.services.fin.FinService;
import com.example.carlocation.services.rental.RentalService;
import com.example.carlocation.services.reservation.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;

@RestController
@RequestMapping(path = "/rental")
public class RentalController implements BaseRestController<RentalDTO, Long> {

    private final RentalService rentalService;

    private final ReservationService reservationService;

    private final CarService carService;

    private final FinService finService;

    public RentalController(RentalService rentalService, ReservationService reservationService, CarService carService, FinService finService) {
        this.rentalService = rentalService;
        this.reservationService = reservationService;
        this.carService = carService;
        this.finService = finService;
    }

    @GetMapping(path = "/{id:[0_9]+}")
    public ResponseEntity<RentalDTO> readOne(@PathVariable Long id) {

        Rental rental = this.rentalService.readOneByKey(id).orElseThrow( () -> new HttpNotFoundException("Rental with id : " + id + "does not exist"));

        return ResponseEntity.ok(RentalDTO.toDTO(rental)) ;
    }

    @GetMapping(path = "")
    public ResponseEntity<Collection<RentalDTO>> readAll() {

        return ResponseEntity.ok(this.rentalService.readAll()
                .map(RentalDTO::toDTO)
                .toList());
    }

    @PostMapping(path = "")
    public ResponseEntity<RentalDTO> addOne(@Valid @RequestBody RentalAddForm form){
        Rental rental = form.toBLL();

        Reservation reservation = this.reservationService.readOneByKey(form.getReservationId()).orElseThrow( () -> new HttpNotFoundException("There is no reservation with id : " + form.getReservationId()));
        rental.setReservation(reservation);
        rental.setDeposit(this.carService.getIndicativePriceByPricingAndFormula(
                reservation.getCar().getId(),
                reservation.getCar().getModel().getPricingClass().getId(),
                reservation.getRentalFormula().getId()
                ) * 0.2 );

        if (!this.carService.isAvailable(reservation.getCar(), reservation.getRemoval(), reservation.getRestitution())){
            throw new HttpPreconditionFailedException("car is not available", new ArrayList<>());
        }

        try{
            this.rentalService.save(rental);
        } catch (Exception exception) {
            throw new HttpPreconditionFailedException("Form is not valid", new ArrayList<>());
        }

        return ResponseEntity.ok(RentalDTO.toDTO(rental));
    }

    @PatchMapping(path = "/{id:[0-9]+}")
    public ResponseEntity<RentalDTO> updateReturn(@PathVariable Long id, @Valid @RequestBody RentalReturnForm form){
        Rental rental = this.rentalService.readOneByKey(id).orElseThrow(  () -> new HttpNotFoundException("Theres is no rental with id: " + id));

        rental.setReturnKm(form.getReturnKm());
        rental.setReturnDate(form.getReturnDate());
        if (Period.between(rental.getReservation().getTheoricRestitution(), rental.getReturnDate()).getDays() != 0){
            Fin fin = new Fin();
            fin.setAmount(Period.between(rental.getReservation().getTheoricRestitution(), rental.getReturnDate()).getDays() *
                    rental.getReservation().getCar().getModel().getPricingClass().getFine_day());
            fin.setReason("extra days : " + Period.between(rental.getReservation().getTheoricRestitution(), rental.getReturnDate()).getDays());
            rental.getFins().add(fin);
        }

        if ((rental.getReturnKm() - rental.getStartKm()) > rental.getReservation().getRentalFormula().getMaxKm()){
            Fin fin = new Fin();
            fin.setAmount((rental.getReturnKm() - rental.getStartKm()) *
                    rental.getReservation().getCar().getModel().getPricingClass().getPrice_km());
            fin.setReason("extra KM: " + (rental.getReturnKm() - rental.getStartKm()));
            rental.getFins().add(fin);
        }

        this.rentalService.save(rental);


        return ResponseEntity.ok(RentalDTO.toDTO(rental));
    }

    @PatchMapping(path = "/fin/{id:[0-9]+}")
    public ResponseEntity<FinDTO> changePayedStatus(@PathVariable Long id) {
            return ResponseEntity.ok(FinDTO.ToDTO(this.finService.ChangePayedStatus(id)));
    }

}
