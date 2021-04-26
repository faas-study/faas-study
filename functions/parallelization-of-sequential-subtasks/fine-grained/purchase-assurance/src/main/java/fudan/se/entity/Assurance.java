package fudan.se.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Assurance {
    private UUID id;

    /**
     * which order the assurance is related to
     */
    private UUID orderId;

    /**
     * the type of assurance
     */
    private AssuranceType type;

    public Assurance(){
        this.orderId = UUID.randomUUID();
    }

    public Assurance(UUID id, UUID orderId, AssuranceType type){
        this.id = id;
        this.orderId = orderId;
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public AssuranceType getType() {
        return type;
    }

    public void setType(AssuranceType type) {
        this.type = type;
    }
}
