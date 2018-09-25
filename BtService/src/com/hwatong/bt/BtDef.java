package com.hwatong.bt;
import com.nforetek.bt.res.NfDef;

public class BtDef {
	public static final int BT_STATE_OFF = NfDef.BT_STATE_OFF;
	public static final int BT_STATE_TURNING_ON  = NfDef.BT_STATE_TURNING_ON;
	public static final int BT_STATE_ON  = NfDef.BT_STATE_ON;
	public static final int BT_STATE_TURNING_OFF = NfDef.BT_STATE_TURNING_OFF;
	
	public static final int BT_PROFILE_HFP = 0x01;
	public static final int BT_PROFILE_PBAP = 0x02;
	public static final int BT_PROFILE_A2DP = 0x04;
	public static final int BT_PROFILE_AVRCP = 0x08;

    public static final int BT_STATE_INVALID = 0x00;
    public static final int BT_STATE_READY = 0x01;
    public static final int BT_STATE_CONNECTING = 0x02;
    public static final int BT_STATE_CONNECTED = 0x03;
    public static final int BT_STATE_DISCONNECTING = 0x04;
    public static final int BT_STATE_STREAMING = 0x05;
    public static final int BT_STATE_BROWSING = 0x06;
}
