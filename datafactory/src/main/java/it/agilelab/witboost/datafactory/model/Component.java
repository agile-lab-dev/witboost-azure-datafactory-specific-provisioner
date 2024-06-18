package it.agilelab.witboost.datafactory.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "kind",
        visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = OutputPort.class, name = "outputport"),
    @JsonSubTypes.Type(value = StorageArea.class, name = "storage"),
    @JsonSubTypes.Type(value = Workload.class, name = "workload")
})
@Validated
public abstract class Component<T> {

    @NotBlank
    private String id;

    @NotBlank
    private String name;

    private Optional<String> fullyQualifiedName;

    @NotBlank
    private String description;

    @NotBlank
    private String kind;

    @NotNull
    private @Valid T specific;
}
