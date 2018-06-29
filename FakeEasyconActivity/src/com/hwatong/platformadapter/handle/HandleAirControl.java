package com.hwatong.platformadapter.handle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.hwatong.platformadapter.utils.L;

import android.canbus.ACStatus;
import android.canbus.IACStatusListener;
import android.canbus.ICanbusService;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
/**
 * @author caochao
 */
public class HandleAirControl {

	private static final String thiz = HandleAirControl.class.getSimpleName();
	
	public static boolean isOpenAirControlView = false ;
	/**
	 * 单例,空调控制把柄
	 */
	private static HandleAirControl mHandAirControl = null;

	private Context mContext;
	private static ICanbusService mCanbusService;
	private ACStatus acStatus = null;

	public HandleAirControl(Context mContext) {
		super();
		this.mContext = mContext;
	}

	public static HandleAirControl getInstance(Context context, ICanbusService canbusService) {
		L.d(thiz, "HandleAirControl init");
		if (mHandAirControl == null) {
			mHandAirControl = new HandleAirControl(context);
		}
		mCanbusService = canbusService;
		return mHandAirControl;
	}

	public boolean handleAppIsAirControlScence(JSONObject result, String sence) {
		String operation = null;
		String name = null;
		try {
			operation = result.getString("operation");
			name = result.getString("name");
			L.d(thiz, "operation:" + operation + "name:" + name);
		} catch (JSONException e) {

		}
		return false;

	}
	public boolean handleAirControlScence(JSONObject result) {
		try {
			acStatus = mCanbusService.getLastACStatus(mContext.getPackageName());
			if (acStatus == null) {
				return false;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		String operation = "";
		String device = "";
		String mode = "";
		String temperature = "";
		String fan_speed = "";
		String airflow_direction = "";
		String rawText = "";
		String target = "";
		try {
			operation = result.getString("operation");
			L.d(thiz, "operation:" + operation);
		} catch (JSONException e) {
		}
		try {
			device = result.getString("device");
			L.d(thiz, "device:" + device);
		} catch (JSONException e) {
		}
		try {
			mode = result.getString("mode");
			L.d(thiz, "mode:" + mode);
		} catch (JSONException e) {
		}
		try {
			temperature = result.getString("temperature");
			L.d(thiz, "temperature:" + temperature);
		} catch (JSONException e) {
		}
		try {
			fan_speed = result.getString("fan_speed");
			L.d(thiz, "fan_speed:" + fan_speed);
		} catch (JSONException e) {
		}
		try {
			airflow_direction = result.getString("airflow_direction");
			L.d(thiz, "airflow_direction:" + airflow_direction);
		} catch (JSONException e) {
		}
		try {
			airflow_direction = result.getString("airflow_direction");
			L.d(thiz, "airflow_direction:" + airflow_direction);
		} catch (JSONException e) {
		}
		try {
			rawText = result.getString("rawText");
			L.d(thiz, "rawText:" + rawText);
		} catch (JSONException e) {
		}
		try {
			target = result.getString("target");
			L.d(thiz, "target:" + target);
		} catch (JSONException e) {
		}
        if(rawText!=null){
            if(rawText.contains("打开后空调") || rawText.contains("打开后排空调")){
                if(acStatus.getStatus3()!=0){
                    sendCmd(7, 1);
                    sendCmd(7, 0);                    
                }
                return true ;
            }
            if(rawText.contains("关闭后空调") || rawText.contains("关闭后排空调")){
                if(acStatus.getStatus3()!=1){
                    sendCmd(7, 1);
                    sendCmd(7, 0);
                }                
                return true ;
            }
        }		
        /**
        if("打开后排空调".equals(rawText) || "打开后空调".equals(rawText)){
            if(acStatus.getStatus3()!=1){
                sendCmd(7, 1);
                sendCmd(7, 0);
            }
            return true ;
        }
        if("关闭后排空调".equals(rawText) || "关闭后空调".equals(rawText) ){
            if(acStatus.getStatus3()!=0){
                sendCmd(7, 1);
                sendCmd(7, 0);
            }
            return true ;
        }*/
		if (operation != null) {
			if ("OPEN".equals(operation)  ) {
				if ("空调".equals(device) && acStatus.getStatus7() != 0x00) {
					sendCmd(8, 1);
					sendCmd(8, 0);
					isOpenAirControlView = true ;
				}
				return true;
			} else if ("CLOSE".equals(operation)) {
				if ("空调".equals(device) && acStatus.getStatus7() != 0x01) {
					sendCmd(8, 1);
					sendCmd(8, 0);
					mContext.sendBroadcast(new Intent("com.hwatong.aircondition.CLOSE"));
					return true;
				}
				if("前除霜".equals(mode) && acStatus.getStatus6()!= 0x0){
				    sendCmd(0,1);
				    sendCmd(0,0);
				    return true;
				}
                if("后除霜".equals(mode) && acStatus.getStatus12()!= 0x0){
                    sendCmd(2,1);
                    sendCmd(2,0);
                    return true;
                }				
			} else if ("SET".equals(operation)) {
				/**
				 * 模式
				 */
				if (mode != null) {
				    ACStatus status = null;
                    try {
                        status = mCanbusService.getLastACStatus(mContext.getPackageName());
                    } catch (RemoteException e1) {
                        e1.printStackTrace();
                    }
					if ("外循环".equals(mode) && status!=null) {
					    if(status.getStatus4()!=0){
					        switchXHMode(0);
					    }
					    isOpenAirControlView = true ;
						return true;
					} else if ("内循环".equals(mode)) {
                        if(status.getStatus4()!=1){
                            switchXHMode(1);
                        }
                        isOpenAirControlView = true ;
                        return true;
					} else if ("前除霜".equals(mode)) {
						if (acStatus.getStatus6() != 0x01) {
							sendCmd(0, 1);
							sendCmd(0, 0);
						}
						return true;
					} else if ("后除霜".equals(mode)) {
						if (acStatus.getStatus12() != 0x01) {
							sendCmd(2, 1);
							sendCmd(2, 0);
						}
						return true;
					} else if("除霜".equals(mode)){
	                    if (acStatus.getStatus12() != 0x01) {
	                        sendCmd(4, 1);
	                        sendCmd(4, 0);
	                    }
					    return true ;
					}
				}
				/**
				 * 风向
				 */
				if (airflow_direction != null) {
					if ("面".equals(airflow_direction) || "头".equals(airflow_direction)) {
						if (acStatus.getStatus12() != 0x01) {
							sendCmd(5, 1);
							sendCmd(5, 0);
							isOpenAirControlView = true ;
						}
						return true;
					} else if ("吹面吹脚".equals(airflow_direction)) {
						if (acStatus.getStatus12() != 0x01) {
                            sendCmd(5, 1);
                            sendCmd(5, 0);
                            sendCmd(6, 1);
                            sendCmd(6, 0);
							isOpenAirControlView = true ;
						}
						return true;
					} else if ("脚".equals(airflow_direction)) {
						if (acStatus.getStatus12() != 0x01) {
							sendCmd(6, 1);
							sendCmd(6, 0);
							isOpenAirControlView = true ;
						}
						return true;
					}
				}

				/**
				 * 风速
				 */
				if (fan_speed != null) {
					if ("1".equals(fan_speed)) {
						sendCmd(3, 1);
						isOpenAirControlView = true ;
						return true;
					}
					if ("2".equals(fan_speed)) {
						sendCmd(3, 2);
						isOpenAirControlView = true ;
						return true;
					}
					if ("3".equals(fan_speed)) {
						sendCmd(3, 3);
						isOpenAirControlView = true ;
						return true;
					}
					if ("4".equals(fan_speed)) {
						sendCmd(3, 4);
						isOpenAirControlView = true ;
						return true;
					}
					if ("5".equals(fan_speed)) {
						sendCmd(3, 5);
						isOpenAirControlView = true ;
						return true;
					}
					if ("6".equals(fan_speed)) {
						sendCmd(3, 6);
						isOpenAirControlView = true ;
						return true;
					}
					if ("7".equals(fan_speed)) {
						sendCmd(3, 7);
						isOpenAirControlView = true ;
						return true;
					}
					if ("最大".equals(fan_speed)) {
						sendCmd(3, 7);
						isOpenAirControlView = true ;
						return true;
					}
					// dengshun 2017.5.22 修改
					else if ("中风".equals(fan_speed)) {
						sendCmd(3, 4);
						isOpenAirControlView = true ;
						return true;
					}
					// 修改结束
					else if ("最小".equals(fan_speed)) {
						sendCmd(3, 1);
						isOpenAirControlView = true ;
						return true;
					}
					if("+".equals(fan_speed)){
					    sendCmd(3, acStatus.getStatus2()+1);
					    isOpenAirControlView = true ;
					    return true ;
					}
	                if("-".equals(fan_speed)){
	                    sendCmd(3, acStatus.getStatus2()-1);
	                    isOpenAirControlView = true ;
	                    return true ;
	                }
				}
				/**
				 * 温度
				 */
				if (temperature != null) {
					return handleTemperature2(temperature);
				}
			}
		}
		return false;
	}

	/**
	 * 处理语音温度控制
	 * 
	 * @param temperature
	 * @return
	 */
	private boolean handleTemperature(String temperature, String rawText) {
		try {
			JSONObject temperatureJson = new JSONObject(temperature);
			String direct = null; // 温度"+" "-"
			String offset = null; // 温度加减的偏移量
			try {
				direct = temperatureJson.getString("direct");
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				offset = temperatureJson.getString("offset");
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (direct != null && offset != null && isNumeric(offset)) {
				if ("+".equals(direct)) {
					/*
					 * if (rawText.contains("主驾")) { sendCmd(10, 1); return
					 * true; } else if (rawText.contains("副驾")) { sendCmd(10,
					 * 1); return true; }
					 */
					sendCmd(10, Integer.valueOf(offset) * 2);
					isOpenAirControlView = true ;
					return true;
				} else if ("-".equals(direct)) {
					/*
					 * if (rawText.contains("主驾")) { sendCmd(10, 2); return
					 * true; } else if (rawText.contains("副驾")) { sendCmd(10,
					 * 2); return true; }
					 */
					sendCmd(10, Integer.valueOf(offset) * 2);
					isOpenAirControlView = true ;
					return true;
				}
				return false;
			}

		} catch (JSONException e) {
			if (isNumeric(temperature)) {
				/*
				 * if (rawText.contains("主驾")) { sendCmd(10,
				 * Integer.valueOf(temperature) * 2); return false; } else if
				 * (rawText.contains("副驾")) { sendCmd(10,
				 * Integer.valueOf(temperature) * 2); return true; }
				 */
				sendCmd(10, Integer.valueOf(temperature) * 2);
				return true;
			} /*
			 * else if ("+".equals(temperature)) { if (rawText.contains("主驾")) {
			 * sendCmd(10, 1); return true; } else if (rawText.contains("副驾")) {
			 * sendCmd(10, 1); return true; } sendCmd(10, 1); return true; }
			 * else if ("-".equals(temperature)) { if (rawText.contains("主驾")) {
			 * sendCmd(10, 2); return true; } else if (rawText.contains("副驾")) {
			 * sendCmd(10, 2); return true; } sendCmd(10, 2); return true; }
			 */
			e.printStackTrace();
		}
		return false;
	}
    /**
     * 处理语音温度控制
     * 
     * @param temperature
     * @return
     */
    private boolean handleTemperature2(String temperature) {
        if(acStatus==null){
            return false ;
        }
        int cur = acStatus.getStatus1(); 
        if(temperature.contains("+")){
            sendCmd(10, cur+2);
            return true;
        } else if(isNumeric(temperature)){
            sendCmd(10, (int)(Float.valueOf(temperature)*2));
            return true;
        } else if(temperature.contains("-")){
            sendCmd(10, cur-2);
            return true;
        }
        return false;
    }
	private boolean isNumeric(String str) {
	    
		Pattern pattern = Pattern.compile("[0-9]*|[0-9]*\\.5");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}
	/**
	 * 切换循环模式
	 * @param i
	 */
	private synchronized void switchXHMode(int i){
	    final int modeCase =  i ;
	    if(mCanbusService!=null){
	        try {
                mCanbusService.addACStatusListener(new IACStatusListener.Stub() {
                    
                    @Override
                    public void onReceived(ACStatus status) throws RemoteException {
                        if(status.getStatus4() != modeCase){
                            sendCmd(9, 1);
                            sendCmd(9, 0);
                        } else {
                            mCanbusService.removeACStatusListener(this);
                        }
                    }
                });
            } catch (RemoteException e) {
                e.printStackTrace();
            }
	    }
        sendCmd(9, 1);
        sendCmd(9, 0);
	}

	private void sendCmd(int i, int j) {
		try {
			if (mCanbusService == null) {
				L.d(thiz, "CanbusService is null");
				return;
			}
			mCanbusService.writeACControl(i, j);
			L.d(thiz, "CanbusService send CMD : type=" + i + "; value =" + j);
		} catch (RemoteException e) {
			L.d(thiz, "CanbusService RemoteException");
			e.printStackTrace();
		}
	}

}
