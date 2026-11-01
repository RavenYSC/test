package com.raven.valorantcoach;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import java.util.List;

public class TrendChart extends View {
 private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);private float[] values=new float[0];private int color=0xfff04b5e;
 public TrendChart(Context c){super(c);setMinimumHeight((int)(150*getResources().getDisplayMetrics().density));}
 public void setData(List<Float> points,int tint){values=new float[points.size()];for(int i=0;i<values.length;i++)values[i]=points.get(i);color=tint;invalidate();}
 @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);float w=getWidth(),h=getHeight(),left=18,top=20,right=w-18,bottom=h-22;
 p.setStrokeWidth(1);p.setColor(0xff343a47);for(int i=0;i<5;i++){float y=top+(bottom-top)*i/4f;canvas.drawLine(left,y,right,y,p);}
 if(values.length==0){p.setColor(0xffa0a6b4);p.setTextSize(14*getResources().getDisplayMetrics().scaledDensity);canvas.drawText("Log your sessions to see a trend",left+8,(top+bottom)/2,p);return;}
 float min=Float.MAX_VALUE,max=-Float.MAX_VALUE;for(float v:values){min=Math.min(min,v);max=Math.max(max,v);}if(min==max){min-=1;max+=1;}else {float pad=(max-min)*.15f;min-=pad;max+=pad;}
 p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3*getResources().getDisplayMetrics().density);
 android.graphics.Path path=new android.graphics.Path();for(int i=0;i<values.length;i++){float x=left+(right-left)*(values.length==1?.5f:i/(float)(values.length-1));float y=bottom-(values[i]-min)/(max-min)*(bottom-top);if(i==0)path.moveTo(x,y);else path.lineTo(x,y);}canvas.drawPath(path,p);p.setStyle(Paint.Style.FILL);
 for(int i=0;i<values.length;i++){float x=left+(right-left)*(values.length==1?.5f:i/(float)(values.length-1));float y=bottom-(values[i]-min)/(max-min)*(bottom-top);canvas.drawCircle(x,y,3.5f*getResources().getDisplayMetrics().density,p);}
 }
}
