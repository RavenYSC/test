package com.raven.valorantcoach;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int BG=0xff10141d,PANEL=0xff1b202b,TEXT=0xfff2f4f8,MUTED=0xffa1aab9,RED=0xfff04b5e,GREEN=0xff40cfa4,BORDER=0xff343b4a;
    private LinearLayout root,content,nav;private CoachStore store;private LocalDate selected=LocalDate.now();private YearMonth shown=YearMonth.now();private int tab=0;private SharedPreferences prefs;
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);store=new CoachStore(this);prefs=getSharedPreferences("settings",MODE_PRIVATE);ReminderReceiver.schedule(this);show(0);}
    private int d(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable shape(int color,int stroke){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(d(16));if(stroke!=0)g.setStroke(d(1),stroke);return g;}
    private LinearLayout column(){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);return x;}
    private LinearLayout row(){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.HORIZONTAL);x.setGravity(Gravity.CENTER_VERTICAL);return x;}
    private TextView text(String s,int sp,int color,boolean bold){TextView x=new TextView(this);x.setText(s);x.setTextSize(sp);x.setTextColor(color);if(bold)x.setTypeface(null,Typeface.BOLD);return x;}
    private void pad(View v,int all){v.setPadding(d(all),d(all),d(all),d(all));}
    private void gap(LinearLayout l,int h){View v=new View(this);l.addView(v,new LinearLayout.LayoutParams(1,d(h)));}
    private void card(LinearLayout parent,View child){LinearLayout holder=column();holder.setBackground(shape(PANEL,BORDER));pad(holder,16);holder.addView(child);parent.addView(holder,new LinearLayout.LayoutParams(-1,-2));gap(parent,12);}
    private TextView button(String title,Runnable action){TextView b=text(title,14,TEXT,true);b.setGravity(Gravity.CENTER);b.setBackground(shape(RED,0));pad(b,12);b.setOnClickListener(v->action.run());return b;}
    private void heading(LinearLayout l,String label,String sub){l.addView(text(label,25,TEXT,true));if(sub!=null){gap(l,5);l.addView(text(sub,13,MUTED,false));}gap(l,16);}
    private void show(int which){tab=which;root=column();root.setBackgroundColor(BG);setContentView(root);LinearLayout top=row();top.setPadding(d(20),d(15),d(20),d(12));TextView brand=text("✦  ASCEND COACH",17,RED,true);top.addView(brand,new LinearLayout.LayoutParams(0,-2,1));TextView settings=text("⚙",24,TEXT,false);settings.setOnClickListener(v->reminderSettings());top.addView(settings);root.addView(top);
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);content=column();content.setPadding(d(18),d(15),d(18),d(24));sc.addView(content);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        if(which==0)today();else if(which==1)calendar();else progress();
        nav=row();nav.setPadding(d(8),d(8),d(8),d(8));nav.setBackgroundColor(PANEL);String[] titles={"TODAY","CALENDAR","PROGRESS"};for(int i=0;i<3;i++){final int id=i;TextView b=text(titles[i],12,i==tab?RED:MUTED,true);b.setGravity(Gravity.CENTER);b.setPadding(0,d(12),0,d(12));b.setOnClickListener(v->show(id));nav.addView(b,new LinearLayout.LayoutParams(0,-2,1));}root.addView(nav);
    }
    private void today(){String day=selected.toString();heading(content,selected.equals(LocalDate.now())?"Today's mission":"Training log",selected.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy",Locale.UK)));
        LinearLayout dateRow=row();TextView prev=text("◀  Previous",12,MUTED,true),next=text("Next  ▶",12,MUTED,true);dateRow.addView(prev,new LinearLayout.LayoutParams(0,-2,1));next.setGravity(Gravity.RIGHT);dateRow.addView(next,new LinearLayout.LayoutParams(0,-2,1));prev.setOnClickListener(v->{selected=selected.minusDays(1);show(0);});next.setOnClickListener(v->{if(selected.isBefore(LocalDate.now())){selected=selected.plusDays(1);show(0);}});content.addView(dateRow);gap(content,14);
        int completed=store.completed(day);LinearLayout summary=column();summary.addView(text(completed+" / "+CoachStore.TOTAL+" tasks completed",20,TEXT,true));gap(summary,8);ProgressBar bar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);bar.setMax(CoachStore.TOTAL);bar.setProgress(completed);bar.setProgressTintList(android.content.res.ColorStateList.valueOf(GREEN));summary.addView(bar);gap(summary,7);summary.addView(text("Consistency before intensity. Tick tasks only after completion.",12,MUTED,false));card(content,summary);
        content.addView(text("DAILY TRAINING CHECKLIST",13,MUTED,true));gap(content,10);
        for(int i=0;i<CoachStore.TOTAL;i++){final int index=i;CheckBox cb=new CheckBox(this);cb.setText(CoachStore.TASKS[i]);cb.setTextSize(14);cb.setTextColor(TEXT);cb.setButtonTintList(android.content.res.ColorStateList.valueOf(RED));cb.setChecked(store.checked(day,i));cb.setPadding(d(10),d(8),d(8),d(8));cb.setBackground(shape(PANEL,0));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=d(7);content.addView(cb,lp);cb.setOnCheckedChangeListener((button,isChecked)->{store.setChecked(day,index,isChecked);show(0);});}
        gap(content,10);content.addView(button("＋  LOG RANKED & PRACTICE STATS",()->editRecord(day)));gap(content,15);
        CoachStore.Record r=store.record(day);LinearLayout stats=column();stats.addView(text("SESSION SNAPSHOT",13,MUTED,true));gap(stats,10);stats.addView(text(r.games+" games  ·  "+r.wins+" wins  ·  "+r.minutes+" training minutes",16,TEXT,true));if(!r.note.isEmpty()){gap(stats,8);stats.addView(text("Next focus: "+r.note,13,MUTED,false));}card(content,stats);
        content.addView(text("Training structure: 20m warm-up · 30m aim · 20m DM · 2 ranked blocks · 40m VOD review · agent & map drills. Adjust ranked blocks to energy and match length.",12,MUTED,false));
    }
    private void editRecord(String day){CoachStore.Record r=store.record(day);ScrollView sc=new ScrollView(this);LinearLayout form=column();pad(form,18);sc.addView(form);
        EditText games=field(form,"Ranked games",r.games,true),wins=field(form,"Ranked wins",r.wins,true),kills=field(form,"Total kills",r.kills,true),deaths=field(form,"Total deaths",r.deaths,true),first=field(form,"First deaths",r.firstDeaths,true),aim=field(form,"Aim trainer score (same benchmark)",r.aim,true),minutes=field(form,"Total practice minutes",r.minutes,true);
        EditText agent=field(form,"Main agent played",r.agent),note=field(form,"Next improvement priority / VOD note",r.note);new AlertDialog.Builder(this).setTitle("Log session · "+day).setView(sc).setNegativeButton("Cancel",null).setPositiveButton("Save",(dialog,w)->{
            r.games=num(games);r.wins=num(wins);r.kills=num(kills);r.deaths=num(deaths);r.firstDeaths=num(first);r.aim=num(aim);r.minutes=num(minutes);r.agent=agent.getText().toString().trim();r.note=note.getText().toString().trim();
            if(r.wins>r.games||r.firstDeaths>r.deaths){Toast.makeText(this,"Wins must not exceed games; first deaths must not exceed total deaths",Toast.LENGTH_LONG).show();return;}store.save(r);show(tab);
        }).show();}
    private EditText field(LinearLayout parent,String label,int v,boolean number){EditText e=field(parent,label,v==0?"":String.valueOf(v));e.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);return e;}
    private EditText field(LinearLayout parent,String label,String v){parent.addView(text(label,12,MUTED,true));EditText e=new EditText(this);e.setSingleLine(!label.contains("note"));e.setText(v);e.setTextSize(15);e.setTextColor(TEXT);e.setHintTextColor(MUTED);parent.addView(e,new LinearLayout.LayoutParams(-1,-2));gap(parent,10);return e;}
    private int num(EditText e){try{return Math.max(0,Integer.parseInt(e.getText().toString().trim()));}catch(Exception x){return 0;}}
    private void calendar(){heading(content,"Training calendar","Tap any day to view or edit its training log");LinearLayout controls=row();TextView back=text("◀",23,RED,true),name=text(shown.format(DateTimeFormatter.ofPattern("MMMM yyyy",Locale.UK)),18,TEXT,true),forward=text("▶",23,RED,true);name.setGravity(Gravity.CENTER);controls.addView(back);controls.addView(name,new LinearLayout.LayoutParams(0,-2,1));controls.addView(forward);back.setOnClickListener(v->{shown=shown.minusMonths(1);show(1);});forward.setOnClickListener(v->{shown=shown.plusMonths(1);show(1);});content.addView(controls);gap(content,16);
        LinearLayout week=row();for(String title:new String[]{"M","T","W","T","F","S","S"}){TextView t=text(title,12,MUTED,true);t.setGravity(Gravity.CENTER);week.addView(t,new LinearLayout.LayoutParams(0,d(30),1));}content.addView(week);
        LocalDate first=shown.atDay(1);int offset=first.getDayOfWeek().getValue()-1;int days=shown.lengthOfMonth();for(int gridRow=0;gridRow<6;gridRow++){LinearLayout weekRow=row();for(int col=0;col<7;col++){int number=gridRow*7+col-offset+1;LinearLayout cell=column();cell.setGravity(Gravity.CENTER);cell.setPadding(d(1),d(7),d(1),d(7));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,d(65),1);lp.setMargins(d(2),d(2),d(2),d(2));weekRow.addView(cell,lp);
            if(number>=1&&number<=days){LocalDate day=shown.atDay(number);int count=store.completed(day.toString());cell.setBackground(shape(day.equals(selected)?0xff33303c:PANEL,day.equals(LocalDate.now())?RED:0));TextView n=text(""+number,15,TEXT,true);n.setGravity(Gravity.CENTER);cell.addView(n);gap(cell,4);TextView status=text(count==CoachStore.TOTAL?"●":count>0?count+"/10":"·",11,count==CoachStore.TOTAL?GREEN:count>0?RED:MUTED,false);status.setGravity(Gravity.CENTER);cell.addView(status);cell.setOnClickListener(v->{selected=day;show(0);});}
        }content.addView(weekRow);}gap(content,14);card(content,text("Green ● = all 10 tasks complete    •    Red = partial training    •    Tap a date to add past records",12,MUTED,false));
        int done=0,partial=0;for(int i=1;i<=days;i++){int c=store.completed(shown.atDay(i).toString());if(c==CoachStore.TOTAL)done++;else if(c>0)partial++;}content.addView(text("This month: "+done+" completed days · "+partial+" partial days",16,TEXT,true));
    }
    private void progress(){heading(content,"Performance analytics","Local-first statistics • compare trends, not single games");LocalDate now=LocalDate.now();List<CoachStore.Record> last30=store.records(now.minusDays(29).toString(),now.toString());int games=0,wins=0,kills=0,deaths=0,firstDeaths=0,minutes=0;for(CoachStore.Record r:last30){games+=r.games;wins+=r.wins;kills+=r.kills;deaths+=r.deaths;firstDeaths+=r.firstDeaths;minutes+=r.minutes;}
        LinearLayout metrics=column();metrics.addView(text("LAST 30 DAYS",13,MUTED,true));gap(metrics,9);metrics.addView(text(games+" ranked games    •    "+wins+" wins",19,TEXT,true));gap(metrics,6);metrics.addView(text((games>0?String.format(Locale.UK,"%.1f",wins*100.0/games):"—")+"% win rate    •    "+(deaths>0?String.format(Locale.UK,"%.2f",kills*1.0/deaths):"—")+" K/D",16,GREEN,true));gap(metrics,6);metrics.addView(text(firstDeaths+" first deaths    •    "+String.format(Locale.UK,"%.1f",minutes/60.0)+" practice hours",14,MUTED,false));card(content,metrics);
        List<Float> wr=new ArrayList<>(),completion=new ArrayList<>(),aimLine=new ArrayList<>();for(int i=27;i>=0;i-=7){LocalDate start=now.minusDays(i);LocalDate end=start.plusDays(6);int g=0,w=0,c=0,a=0;for(CoachStore.Record r:store.records(start.toString(),end.toString())){g+=r.games;w+=r.wins;if(r.aim>0){a+=r.aim;}}for(LocalDate date=start;!date.isAfter(end);date=date.plusDays(1))c+=store.completed(date.toString());wr.add(g==0?0f:w*100f/g);completion.add(c*100f/(7*CoachStore.TOTAL));aimLine.add((float)a);}
        chartCard("Weekly ranked win rate (%)",wr,RED,"A week with no ranked games displays as zero. Compare alongside matches played.");chartCard("Weekly checklist completion (%)",completion,GREEN,"Completion covers all seven days, including recovery days; use the trend rather than expecting 100%.");
        ArrayList<Float> aimPoints=new ArrayList<>();for(CoachStore.Record r:last30)if(r.aim>0)aimPoints.add((float)r.aim);chartCard("Aim benchmark score (logged days)",aimPoints,0xff66aaff,"Keep the same Aim Lab/Kovaak's scenario and settings for comparable scores.");
        content.addView(text("TRAINING CONSISTENCY",13,MUTED,true));gap(content,10);int checked=0,full=0;for(int i=0;i<30;i++){int c=store.completed(now.minusDays(i).toString());checked+=c;if(c==CoachStore.TOTAL)full++;}card(content,text(checked+" / 300 checklist items completed\n"+full+" full-completion days in the last 30",16,TEXT,true));
        content.addView(button("EXPORT TRAINING DATA (CSV)",this::exportCsv));gap(content,10);content.addView(text("Ranked and practice data are entered manually. The app does not connect to Riot's API or automatically verify match results.",12,MUTED,false));
    }
    private void chartCard(String title,List<Float> values,int tint,String note){LinearLayout c=column();c.addView(text(title,16,TEXT,true));gap(c,8);TrendChart chart=new TrendChart(this);chart.setData(values,tint);c.addView(chart,new LinearLayout.LayoutParams(-1,d(170)));gap(c,4);c.addView(text(note,11,MUTED,false));card(content,c);}
    private void exportCsv(){StringBuilder b=new StringBuilder("day,games,wins,kills,deaths,first_deaths,aim_score,practice_minutes,agent,notes,completed_tasks\n");for(CoachStore.Record r:store.records("2000-01-01","2100-01-01")){b.append(r.day).append(',').append(r.games).append(',').append(r.wins).append(',').append(r.kills).append(',').append(r.deaths).append(',').append(r.firstDeaths).append(',').append(r.aim).append(',').append(r.minutes).append(',').append(csv(r.agent)).append(',').append(csv(r.note)).append(',').append(store.completed(r.day)).append('\n');}
       Intent send=new Intent(Intent.ACTION_SEND).setType("text/csv").putExtra(Intent.EXTRA_TEXT,b.toString());startActivity(Intent.createChooser(send,"Share or save training CSV"));}
    private String csv(String s){return "\""+s.replace("\"","\"\"")+"\"";}
    private void reminderSettings(){LinearLayout body=column();pad(body,20);Switch toggle=new Switch(this);toggle.setText("Enable daily reminders");toggle.setTextColor(TEXT);toggle.setChecked(prefs.getBoolean("enabled",false));body.addView(toggle);gap(body,16);TextView[] times=new TextView[3];for(int i=0;i<3;i++){final int j=i;times[i]=text(ReminderReceiver.TITLES[i]+"  ·  "+formatTime(i),15,TEXT,true);pad(times[i],12);times[i].setBackground(shape(PANEL,0));body.addView(times[i]);gap(body,8);times[i].setOnClickListener(v->{int hour=prefs.getInt("hour"+j,new int[]{10,14,19}[j]),minute=prefs.getInt("minute"+j,0);new TimePickerDialog(this,(picker,h,m)->{prefs.edit().putInt("hour"+j,h).putInt("minute"+j,m).apply();times[j].setText(ReminderReceiver.TITLES[j]+"  ·  "+formatTime(j));},hour,minute,true).show();});}
        body.addView(text("Reminders are approximate and may be delayed by Android battery optimisation. No account or internet required.",12,MUTED,false));new AlertDialog.Builder(this).setTitle("Notification settings").setView(body).setNegativeButton("Cancel",null).setPositiveButton("Save",(dialog,w)->{prefs.edit().putBoolean("enabled",toggle.isChecked()).apply();if(toggle.isChecked()&&Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},44);ReminderReceiver.schedule(this);Toast.makeText(this,"Reminder preferences saved",Toast.LENGTH_SHORT).show();}).show();}
    private String formatTime(int i){return String.format(Locale.UK,"%02d:%02d",prefs.getInt("hour"+i,new int[]{10,14,19}[i]),prefs.getInt("minute"+i,0));}
}
