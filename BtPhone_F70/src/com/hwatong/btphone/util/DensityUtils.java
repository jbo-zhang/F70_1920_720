package com.hwatong.btphone.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class DensityUtils {
	/**
     * convert px to its equivalent dp
     * 
     * 将px转换为与之相等的dp
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale =  context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    /**
     * convert dp to its equivalent px
     * 
     * 将dp转换为与之相等的px
     */
    public static int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

   
   /**
    * convert px to its equivalent sp 
    * 
    * 将px转换为sp
    */
   public static int px2sp(Context context, float pxValue) {
       final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
       return (int) (pxValue / fontScale + 0.5f);
   }


   /**
    * convert sp to its equivalent px
    * 
    * 将sp转换为px
    */
   public static int sp2px(Context context, float spValue) {
       final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
       return (int) (spValue * fontScale + 0.5f);
   }
   
   
   /**
    * 得到屏幕宽度
    * @param context
    * @return
    */
   public static int getScreenWidth(Context context) {
	    return getScreenWidthOrHeight(context, true);
   }
   
   /**
    * 得到屏幕高度
    * @param context
    * @return
    */
   public static int getScreenHeight(Context context) {
	   return getScreenWidthOrHeight(context, false);
   }
   
   private static int getScreenWidthOrHeight(Context context, boolean getWidth) {
		WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		manager.getDefaultDisplay().getMetrics(outMetrics);
		int width = outMetrics.widthPixels;
		int height = outMetrics.heightPixels;
		
		if(getWidth) {
			return width;
		} else {
			return height;
		}
		
   }
   
   
   
}
