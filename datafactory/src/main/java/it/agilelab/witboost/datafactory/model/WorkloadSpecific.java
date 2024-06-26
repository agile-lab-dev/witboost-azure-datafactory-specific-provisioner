package it.agilelab.witboost.datafactory.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@Valid
public class WorkloadSpecific extends Specific {

    @NotBlank
    private String gitRepo;

    @NotBlank
    private String projectName;

    @NotBlank
    private String repositoryName;

    @NotBlank
    private String resourceGroup;

    @NotBlank
    private String region;
}
