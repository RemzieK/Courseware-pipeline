package bg.tu.unicloud.model;

public enum IntegrationEventType {
    MATERIAL_VIEWED("material.viewed"),
    MATERIAL_READ("material.read"),
    MATERIAL_UNREAD("material.unread"),
    MATERIAL_UPLOADED("material.uploaded");

    private final String value;

    IntegrationEventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static IntegrationEventType fromValue(String value) {
        for (IntegrationEventType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported integration event type: " + value);
    }
}
