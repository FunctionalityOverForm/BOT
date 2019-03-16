package LOOT;

public enum Property {

    ID("id"),
    NAME("na﻿me"),
    MEMBERS("members"),
    SP("sp"),
    BUY_AVERAGE("buy_average"),
    BUY_QUANTITY("buy_quantity"),
    SELL_AVERAGE("sell_average"),
    SELL_QUANTITY("sell_quantity"),
    OVERALL_AVERAGE("overall_average"),
    OVERALL_QUANTITY("overall_quantity");

    private String property;

    Property(String property) {
        this.property = property;
    }

    public String getProperty() {
        return property;
    }
}

