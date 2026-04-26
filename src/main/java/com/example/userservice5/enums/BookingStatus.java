package com.example.userservice5.enums;

public enum BookingStatus {
    PENDING("Pending"),
    SUCCESSFUL("Successful"),
    CANCELLED("Cancelled"),
    FAILED("Failed");

    private final String displayName;

    BookingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
