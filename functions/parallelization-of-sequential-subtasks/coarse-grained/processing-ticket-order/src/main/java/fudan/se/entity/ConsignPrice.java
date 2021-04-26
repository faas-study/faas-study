package fudan.se.entity;

import java.util.UUID;

/**
 * @author fdse
 */

public class ConsignPrice {
    private UUID id;
    private int index;
    private double initialWeight;
    private double initialPrice;
    private double withinPrice;
    private double beyondPrice;

    public ConsignPrice(){
        //Default Constructor
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public double getInitialWeight() {
        return initialWeight;
    }

    public void setInitialWeight(double initialWeight) {
        this.initialWeight = initialWeight;
    }

    public double getInitialPrice() {
        return initialPrice;
    }

    public void setInitialPrice(double initialPrice) {
        this.initialPrice = initialPrice;
    }

    public double getWithinPrice() {
        return withinPrice;
    }

    public void setWithinPrice(double withinPrice) {
        this.withinPrice = withinPrice;
    }

    public double getBeyondPrice() {
        return beyondPrice;
    }

    public void setBeyondPrice(double beyondPrice) {
        this.beyondPrice = beyondPrice;
    }
}
