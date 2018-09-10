package com.hwatong.settings.view;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

import com.hwatong.settings.R;
  
@SuppressLint({ "ResourceAsColor", "DrawAllocation" })  
public class ClockView extends View {  
    public ClockView(Context context) {  
        super(context);  
    }  
  
    public void onDraw(Canvas canvas) {  
        Paint paint = new Paint();  
        paint.setAntiAlias(true);  
        paint.setColor(Color.BLACK);  
        paint.setStyle(Paint.Style.STROKE); // 绌哄績鐨勭敾绗�  
        paint.setStrokeWidth(3); // 璁剧疆paint鐨勫妗嗗搴�  
  
//        drawDial(canvas, paint); // 缁樺埗琛ㄧ洏  
        drawHand(canvas, paint); // 缁樺埗鏃堕拡銆佸垎閽堛�佺閽�  
  
    }  
  
    public void drawHand(Canvas canvas, Paint paint) {  
        int x = 310;  
        int y = x;  
        int hour;  
        int minute;  
        int second;  
  
        final Calendar calendar = Calendar.getInstance();  
        hour = calendar.get(Calendar.HOUR);  
        minute = calendar.get(Calendar.MINUTE);  
        second = calendar.get(Calendar.SECOND);  
  
        float h = ((hour + (float) minute / 60) / 12) * 360;  
        float m = ((minute + (float) second / 60) / 60) * 360;  
        float s = ((float) second / 60) * 360;  
  
        // 鏃堕拡  
        paint.setColor(Color.BLACK);  
        canvas.save(); // 绾块攣瀹氱敾甯�  
        canvas.rotate(h, x / 2, y / 2); // 鏃嬭浆鐢诲竷  
        Path path1 = new Path();  
        path1.moveTo(x / 2, y / 2); // 寮�濮嬬殑鍩虹偣  
        path1.lineTo(x / 2, y / 4); // 鏈�鍚庣殑鍩虹偣  
        canvas.drawPath(path1, paint);  
        canvas.restore();  
  
        // 鍒嗛拡  
        paint.setColor(Color.BLACK);  
        canvas.save();  
        canvas.rotate(m, x / 2, y / 2); // 鏃嬭浆鐢诲竷  
        Path path2 = new Path();  
        path2.moveTo(x / 2, y / 2); // 寮�濮嬬殑鍩虹偣  
        path2.lineTo(x / 2, y / 5); // 鏈�鍚庣殑鍩虹偣  
        canvas.drawPath(path2, paint);  
        canvas.restore();  
  
        // 绉掗拡  
        paint.setColor(Color.RED);  
        canvas.save();  
        canvas.rotate(s, x / 2, y / 2); // 鏃嬭浆鐢诲竷  
        Path path3 = new Path();  
        path3.moveTo(x / 2, y / 2); // 寮�濮嬬殑鍩虹偣  
        path3.lineTo(x / 2, y / 9); // 鏈�鍚庣殑鍩虹偣  
        canvas.drawPath(path3, paint);  
        canvas.restore();  
  
    }  
  
    public void drawDial(Canvas canvas, Paint paint) {  
        int x = 310;  
        int y = x;  
        paint.setColor(Color.WHITE);  
  
        canvas.drawCircle(x / 2, y / 2, x / 2 - 2, paint);  
        canvas.drawCircle(x / 2, y / 2, x / 40, paint);  
  
        Path path9 = new Path(); // 鎺ヤ笅鏉ョ殑鏄紝鐢绘椂閽堢殑鍒诲害  
        path9.moveTo(2, y / 2);  
        path9.lineTo(y / 18, y / 2);  
        canvas.drawPath(path9, paint);  
  
        Path path12 = new Path();  
        path12.moveTo(x / 2, 2);  
        path12.lineTo(x / 2, y / 18);  
        canvas.drawPath(path12, paint);  
  
        Path path3 = new Path();  
        path3.moveTo(x - 2, y / 2);  
        path3.lineTo(x - x / 18, y / 2);  
        canvas.drawPath(path3, paint);  
  
        Path path6 = new Path();  
        path6.moveTo(x / 2, y - 2);  
        path6.lineTo(x / 2, y - y / 18);  
        canvas.drawPath(path6, paint);  
  
        canvas.save();  
        canvas.rotate(32, x / 2, y / 2);  
        Path path10 = new Path();  
        path10.moveTo(2, y / 2);  
        path10.lineTo(x / 32, y / 2);  
        canvas.drawPath(path10, paint);  
  
        Path path1 = new Path();  
        path1.moveTo(x / 2, 2);  
        path1.lineTo(x / 2, y / 32);  
        canvas.drawPath(path1, paint);  
  
        Path path4 = new Path();  
        path4.moveTo(x - 1, y / 2);  
        path4.lineTo(x - x / 32, y / 2);  
        canvas.drawPath(path4, paint);  
  
        Path path7 = new Path();  
        path7.moveTo(x / 2, y - 2);  
        path7.lineTo(x / 2, y - y / 32);  
        canvas.drawPath(path7, paint);  
        canvas.restore();  
  
        canvas.save();  
        canvas.rotate(60, x / 2, y / 2);  
        Path path11 = new Path();  
        path11.moveTo(2, y / 2);  
        path11.lineTo(x / 32, y / 2);  
        canvas.drawPath(path11, paint);  
  
        Path path2 = new Path();  
        path2.moveTo(x / 2, 2);  
        path2.lineTo(x / 2, y / 32);  
        canvas.drawPath(path2, paint);  
  
        Path path5 = new Path();  
        path5.moveTo(x - 2, y / 2);  
        path5.lineTo(x - x / 32, y / 2);  
        canvas.drawPath(path5, paint);  
  
        Path path8 = new Path();  
        path8.moveTo(x / 2, y - 2);  
        path8.lineTo(x / 2, y - y / 32);  
        canvas.drawPath(path8, paint);  
        canvas.restore();  
    }  
} 