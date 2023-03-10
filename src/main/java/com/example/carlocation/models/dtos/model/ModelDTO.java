package com.example.carlocation.models.dtos.model;

import com.example.carlocation.models.dtos.option.OptionDTO;
import com.example.carlocation.models.dtos.pricingClass.PricingClassDTO;

import com.example.carlocation.models.entities.Model;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ModelDTO {

    private String brand;

    private String type;

    private int power;

    private PricingClassDTO tarificationClass;

    private List<OptionDTO> options;

    public static ModelDTO toDTO(Model model){
        if (model == null) return null;
        return ModelDTO.builder()
                .brand(model.getId().getBrand())
                .type(model.getId().getType())
                .power(model.getId().getPower())
                .tarificationClass(PricingClassDTO.toDTO(model.getPricingClass()))
                .options(model.getOptions().stream()
                        .map(OptionDTO::toDTO)
                        .toList())
                .build();
    }

}
