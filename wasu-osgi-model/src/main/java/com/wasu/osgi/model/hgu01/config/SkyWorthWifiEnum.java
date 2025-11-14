package com.wasu.osgi.model.hgu01.config;

public enum SkyWorthWifiEnum {
    /**
     * encrypt
     */
    ENCRYPT_WPA_PSK("WPA_PSK", "WPAPSK"),
    ENCRYPT_WPA2_PSK("WPA2_PSK", "WPAPSK2"),
    //    ENCRYPT_WPA_PSK_WPA2_PSK("WPA_PSK/WPA2_PSK","MIXED-WPAPSK2"),  //康特
    ENCRYPT_WPA_WPA2_PSK("WPA/WPA2_PSK", "MIXED-WPAPSK2"),
    ENCRYPT_OPEN("open", "OPEN"),
    ENCRYPT_WEP("WEP", "WEP"),

    /**
     * wireType
     */
    WIRE_TYPE_802_11b("802.11b", "11b"),
    WIRE_TYPE_802_11g("802.11g", "11g"),
    WIRE_TYPE_802_11n("802.11n", "11n"),
    WIRE_TYPE_802_11bg("802.11b/g", "11bg"),
    WIRE_TYPE_802_11bgn("802.11b/g/n", "11bgn"),
    WIRE_TYPE_802_11a("802.11a", "11a"),
    WIRE_TYPE_802_11an("802.11a/n", "11na"),
    WIRE_TYPE_802_11ac("802.11ac", "11ac"),
    WIRE_TYPE_802_11anac("802.11a/n/ac", "11anac"),
    WIRE_TYPE_802_11gn("802.11g/n", "11gn"),

    /**
     * bandWidth
     */
    BANDWIDTH_20("20MHz", "20M"),
    BANDWIDTH_40("40MHz", "40M"),
    BANDWIDTH_80("80MHz", "80M"),
    BANDWIDTH_20_40("20/40MHz", "Auto20M40M"),
    BANDWIDTH_20_40_80("20/40/80MHz","Auto20M40M80M");

    private final String iotKey;  // 原始Key（如WPA_PSK）
    private final String customValue; // 产品线A的自定义值（如WPAPSK）


    SkyWorthWifiEnum(String iotKey, String customValue) {
        this.iotKey = iotKey;
        this.customValue = customValue;
    }

    public static String getCustomValue(String iotKey) {
        for (SkyWorthWifiEnum item : values()) {
            if (item.iotKey.equals(iotKey)) {
                return item.customValue;
            }
        }
        return null;
    }

    public static String getIotKey(String customValue) {
        for (SkyWorthWifiEnum item : values()) {
            if (item.customValue.equals(customValue)) {
                return item.iotKey;
            }
        }
        return null;
    }
}
