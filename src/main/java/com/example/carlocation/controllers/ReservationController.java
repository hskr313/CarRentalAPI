package com.example.carlocation.controllers;

import com.example.carlocation.exceptions.HttpNotFoundException;
import com.example.carlocation.exceptions.HttpPreconditionFailedException;
import com.example.carlocation.models.dtos.reservation.ReservationDTO;
import com.example.carlocation.models.entities.Car;
import com.example.carlocation.models.entities.Client;
import com.example.carlocation.models.entities.Reservation;
import com.example.carlocation.models.forms.ReservationAddForm;
import com.example.carlocation.services.car.CarService;
import com.example.carlocation.services.client.ClientService;
import com.example.carlocation.services.reservation.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;

@RestController
@RequestMapping(path = {"/reservation"})
public class ReservationController implements BaseRestController<ReservationDTO, Long> {

    private final ReservationService reservationService;

    private final CarService carService;

    private final ClientService clientService;

    public ReservationController(ReservationService reservationService, CarService carService, ClientService clientService) {
        this.reservationService = reservationService;
        this.carService = carService;
        this.clientService = clientService;
    }

    @Override
    @GetMapping(path = {"/id:[0-9]+"})
    public ResponseEntity<ReservationDTO> readOne(Long id) {
        Reservation reservation = this.reservationService.readOneByKey(id).orElseThrow(() -> new HttpNotFoundException("Reservation with id :(" + id + ") does not exist"));

        return ResponseEntity.ok(ReservationDTO.toDTO(reservation));
    }

    @Override
    @GetMapping
    public ResponseEntity<Collection<ReservationDTO>> readAll() {
        return ResponseEntity.ok(this.reservationService.readAll()
                .map(ReservationDTO::toDTO)
                .toList());
    }



    @PostMapping(path = "")
    public ResponseEntity<ReservationDTO> addReservation(@Valid @RequestBody ReservationAddForm form){
        Reservation reservation = new Reservation();

        Car car = this.carService.readOneByKey(form.getIdCar()).orElseThrow(() -> new HttpNotFoundException("There is no car with id:(" + form.getIdCar()+ ")"));
        if (!this.carService.isAvailable(car,form.getRemoval(),form.getRestitution())){
            throw new HttpPreconditionFailedException("Car is not available", new ArrayList<>());
        }

        Client client = this.clientService
                .readOneByKey(form.getIdCustomer())
                .orElse(this.clientService.save(form.getClient().toBLL()));
        reservation.setClient(client);

        try{
            reservation = this.reservationService.save(reservation);
        }catch (Exception exception){
            throw new HttpPreconditionFailedException("Form is not valid", new ArrayList<>());
        }
        return ResponseEntity.ok(ReservationDTO.toDTO(reservation));
    }
}