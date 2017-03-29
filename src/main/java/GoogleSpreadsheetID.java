public enum GoogleSpreadsheetID {

    TEST(0, "teste"),
    VRAU(712794908, "vrau");

    private int value;
    private String desc;

    GoogleSpreadsheetID(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static GoogleSpreadsheetID findByValue(int value) {
        for (GoogleSpreadsheetID val : values()) {
            if (val.value == value) {
                return val;
            }
        }
        return null;
    }

}