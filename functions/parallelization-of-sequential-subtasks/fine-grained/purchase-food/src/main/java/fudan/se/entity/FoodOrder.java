package fudan.se.entity;

import java.util.UUID;

public class FoodOrder {
    private UUID id;

    private UUID orderId;

    private int foodType;

    private String stationName;

    private String storeName;

    private String foodName;

    private double price;


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

    public int getFoodType() {
        return foodType;
    }

    public void setFoodType(int foodType) {
        this.foodType = foodType;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "FoodOrder{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", foodType=" + foodType +
                ", stationName='" + stationName + '\'' +
                ", storeName='" + storeName + '\'' +
                ", foodName='" + foodName + '\'' +
                ", price=" + price +
                '}';
    }
}
