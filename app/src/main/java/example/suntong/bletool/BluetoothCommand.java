package example.suntong.bletool;

public class BluetoothCommand {
    public static final byte[] READ_DATE_CMD = {0b0000, 0b0110, 0b0100, 0b0000};//读取时间命令
    public static final byte[] READ_BATTERY_CMD = {0x00, 0x08, 0x04, 0x00};//读取电量命令
    public static final byte[] SETTIMEFORMATTO12H = {0x00, (byte) 0x82, 0x05, 0x00, 0x01};//设置时间格式为12小时
    public static final byte[] SETTIMEFORMATTO24H = {0x00, (byte) 0x82, 0x05, 0x00, 0x00};//设置时间格式为24小时
    public static final byte[] SYNCDATESUCCESS = {(byte) 0x80, (byte) 0x87, 0x05, 0x00, 0x01};
    public static final byte[] NORMAL_VIBRATION_CMD = {0x04, 0x08, 0x05, 0x00, 0x01};//设置震动为normal
    public static final byte[] NO_VIBRATION_CMD = {0x04, 0x08, 0x05, 0x00, 0x00};//设置一般震动
    public static final byte[] STRONG_VIBRATION_CMD = {0x04, 0x08, 0x05, 0x00, 0x02};//设置强震动
    public static final byte[] FLASH_ID_CMD = {0x08, 0x08, 0x04, 0x00};//获取flashId
    public static final byte[] G_SENSOR_CMD = {0x08, 0x07, 0x04, 0x00};//获取六轴数据
    public static final byte[] GET_VIBRATION_CMD = {0x04, 0x00, 0x04, 0x00};//获取震动类型
    public static final byte[] START_HEART_RATE_CMD = {0x01, (byte) 0x85, 0x05, 0x00, 0x01};
    public static final byte[] STOP_HEART_RATE_CMD = {0x01, (byte) 0x85, 0x05, 0x00, 0x02};
    public static final byte[] START_TEMP_CMD = {0x01, 0x1B, 0x05, 0x00, 0x01};
    public static final byte[] STOP_TEMP_CMD = {0x01, 0x1B, 0x05, 0x00, 0x02};
    public static final byte[] TEST_VIBRATO_CMD={0X08, (byte) 0X80,0X04,0X00};
}
