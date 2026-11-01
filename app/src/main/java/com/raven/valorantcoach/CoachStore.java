package com.raven.valorantcoach;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class CoachStore extends SQLiteOpenHelper {
    public static final String[] TASKS = {"Range + movement warm-up", "Aim trainer — 25 minutes", "Deathmatch — focus drill", "Ranked block 1 — 3 games", "Take recovery breaks", "Ranked block 2 — up to 3 games", "VOD review — 3+ lost rounds", "Agent/map practice", "Pro VOD study", "Log stats + next objective"};
    public static final int TOTAL = TASKS.length;
    public CoachStore(Context context) { super(context,"coach.db",null,2); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE checks(day TEXT NOT NULL, task INTEGER NOT NULL, done INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(day,task))");
        db.execSQL("CREATE TABLE sessions(day TEXT PRIMARY KEY, games INTEGER NOT NULL DEFAULT 0, wins INTEGER NOT NULL DEFAULT 0, kills INTEGER NOT NULL DEFAULT 0, deaths INTEGER NOT NULL DEFAULT 0, first_deaths INTEGER NOT NULL DEFAULT 0, aim_score INTEGER NOT NULL DEFAULT 0, minutes INTEGER NOT NULL DEFAULT 0, agent TEXT NOT NULL DEFAULT '', note TEXT NOT NULL DEFAULT '')");
        db.execSQL("CREATE TABLE IF NOT EXISTS matches(match_id TEXT PRIMARY KEY, day TEXT NOT NULL, map TEXT, agent TEXT, won INTEGER NOT NULL, kills INTEGER NOT NULL, deaths INTEGER NOT NULL, assists INTEGER NOT NULL, headshots INTEGER NOT NULL DEFAULT 0, bodyshots INTEGER NOT NULL DEFAULT 0, legshots INTEGER NOT NULL DEFAULT 0)");
    }
    @Override public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion) { if(oldVersion<2) db.execSQL("CREATE TABLE IF NOT EXISTS matches(match_id TEXT PRIMARY KEY, day TEXT NOT NULL, map TEXT, agent TEXT, won INTEGER NOT NULL, kills INTEGER NOT NULL, deaths INTEGER NOT NULL, assists INTEGER NOT NULL, headshots INTEGER NOT NULL DEFAULT 0, bodyshots INTEGER NOT NULL DEFAULT 0, legshots INTEGER NOT NULL DEFAULT 0)"); }
    public boolean checked(String day,int task) {
        try(Cursor c=getReadableDatabase().rawQuery("SELECT done FROM checks WHERE day=? AND task=?",new String[]{day,String.valueOf(task)})) { return c.moveToFirst() && c.getInt(0)==1; }
    }
    public void setChecked(String day,int task,boolean done) {
        ContentValues v=new ContentValues();v.put("day",day);v.put("task",task);v.put("done",done?1:0);
        getWritableDatabase().insertWithOnConflict("checks",null,v,SQLiteDatabase.CONFLICT_REPLACE);
    }
    public int completed(String day) {
        try(Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM(done),0) FROM checks WHERE day=?",new String[]{day})) { return c.moveToFirst()?c.getInt(0):0; }
    }
    public Record record(String day) {
        Record r=new Record();r.day=day;
        try(Cursor c=getReadableDatabase().rawQuery("SELECT games,wins,kills,deaths,first_deaths,aim_score,minutes,agent,note FROM sessions WHERE day=?",new String[]{day})) {
            if(c.moveToFirst()){r.games=c.getInt(0);r.wins=c.getInt(1);r.kills=c.getInt(2);r.deaths=c.getInt(3);r.firstDeaths=c.getInt(4);r.aim=c.getInt(5);r.minutes=c.getInt(6);r.agent=c.getString(7);r.note=c.getString(8);}
        }return r;
    }
    public void save(Record r) {
        ContentValues v=new ContentValues();v.put("day",r.day);v.put("games",r.games);v.put("wins",r.wins);v.put("kills",r.kills);v.put("deaths",r.deaths);v.put("first_deaths",r.firstDeaths);v.put("aim_score",r.aim);v.put("minutes",r.minutes);v.put("agent",r.agent);v.put("note",r.note);
        getWritableDatabase().insertWithOnConflict("sessions",null,v,SQLiteDatabase.CONFLICT_REPLACE);
    }
    public List<Record> records(String start,String end){
        ArrayList<Record> result=new ArrayList<>();
        try(Cursor c=getReadableDatabase().rawQuery("SELECT day,games,wins,kills,deaths,first_deaths,aim_score,minutes,agent,note FROM sessions WHERE day>=? AND day<=? ORDER BY day",new String[]{start,end})){
            while(c.moveToNext()){Record r=new Record();r.day=c.getString(0);r.games=c.getInt(1);r.wins=c.getInt(2);r.kills=c.getInt(3);r.deaths=c.getInt(4);r.firstDeaths=c.getInt(5);r.aim=c.getInt(6);r.minutes=c.getInt(7);r.agent=c.getString(8);r.note=c.getString(9);result.add(r);}
        }return result;
    }
    public int saveMatch(Match m) {
        ContentValues v=new ContentValues();v.put("match_id",m.id);v.put("day",m.day);v.put("map",m.map);v.put("agent",m.agent);v.put("won",m.won?1:0);v.put("kills",m.kills);v.put("deaths",m.deaths);v.put("assists",m.assists);v.put("headshots",m.headshots);v.put("bodyshots",m.bodyshots);v.put("legshots",m.legshots);
        return getWritableDatabase().insertWithOnConflict("matches",null,v,SQLiteDatabase.CONFLICT_IGNORE)==-1?0:1;
    }
    public List<Match> matches(String start,String end){
        ArrayList<Match> result=new ArrayList<>();
        try(Cursor c=getReadableDatabase().rawQuery("SELECT match_id,day,map,agent,won,kills,deaths,assists,headshots,bodyshots,legshots FROM matches WHERE day>=? AND day<=? ORDER BY day DESC",new String[]{start,end})){
            while(c.moveToNext()){Match m=new Match();m.id=c.getString(0);m.day=c.getString(1);m.map=c.getString(2);m.agent=c.getString(3);m.won=c.getInt(4)==1;m.kills=c.getInt(5);m.deaths=c.getInt(6);m.assists=c.getInt(7);m.headshots=c.getInt(8);m.bodyshots=c.getInt(9);m.legshots=c.getInt(10);result.add(m);}
        }return result;
    }
    public static class Match { public String id,day,map,agent;public boolean won;public int kills,deaths,assists,headshots,bodyshots,legshots; }
    public static class Record { public String day= LocalDate.now().toString(),agent="",note="";public int games,wins,kills,deaths,firstDeaths,aim,minutes; }
}
