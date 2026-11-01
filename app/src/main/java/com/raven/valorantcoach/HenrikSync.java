package com.raven.valorantcoach;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/** Network call must run off the UI thread. Only approved, documented HenrikDev endpoints. */
public final class HenrikSync {
    public static final class Result { public int scanned,added; public String note; }
    private static String enc(String s) throws Exception {return URLEncoder.encode(s,"UTF-8").replace("+","%20");}
    private static JSONObject obj(JSONObject p,String k){return p==null?new JSONObject():p.optJSONObject(k)==null?new JSONObject():p.optJSONObject(k);}
    private static String name(JSONObject p,String key){Object value=p.opt(key);if(value instanceof JSONObject){JSONObject j=(JSONObject)value;return j.optString("name",j.optString("display_name", ""));}return p.optString(key,"");}
    private static String day(JSONObject metadata){
        long seconds=metadata.optLong("started_at",0);
        if(seconds==0)seconds=metadata.optLong("game_start",0);
        if(seconds>100000000000L)seconds/=1000;
        if(seconds>1000000000L)return Instant.ofEpochSecond(seconds).atZone(ZoneId.systemDefault()).toLocalDate().toString();
        String start=metadata.optString("started_at",metadata.optString("game_start_patched",""));
        if(start.length()>=10 && start.substring(0,10).matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"))return start.substring(0,10);
        return "";
    }
    public static Result sync(Context context,String key,String region,String riotName,String tag) throws Exception {
        if(!region.matches("na|eu|ap|kr|latam|br")) throw new IllegalArgumentException("Choose a valid region");
        String address="https://api.henrikdev.xyz/valorant/v4/matches/"+region+"/pc/"+enc(riotName)+"/"+enc(tag)+"?mode=competitive&size=20";
        HttpURLConnection connection=(HttpURLConnection)new URL(address).openConnection();connection.setRequestMethod("GET");connection.setRequestProperty("Authorization",key);
        connection.setRequestProperty("Accept","application/json");connection.setConnectTimeout(12000);connection.setReadTimeout(20000);
        try{
            int code=connection.getResponseCode();if(code!=200)throw new IllegalStateException("API returned HTTP "+code+(code==401?" — check your API key":code==429?" — rate limit reached":""));
            String raw;try(InputStream in=connection.getInputStream()){raw=new String(in.readAllBytes(),StandardCharsets.UTF_8);}
            JSONObject response=new JSONObject(raw);JSONArray matches=response.optJSONArray("data");if(matches==null)throw new IllegalStateException("Unrecognised match history response");
            CoachStore db=new CoachStore(context);Result out=new Result();out.scanned=matches.length();
            for(int i=0;i<matches.length();i++){
                JSONObject game=matches.optJSONObject(i);if(game==null)continue;
                JSONObject metadata=obj(game,"metadata");String id=metadata.optString("match_id",metadata.optString("id",""));
                String date=day(metadata);if(id.isEmpty()||date.isEmpty())continue;
                JSONArray players=game.optJSONArray("players");if(players==null)continue;
                JSONObject player=null;
                for(int j=0;j<players.length();j++){JSONObject p=players.optJSONObject(j);if(p==null)continue;
                    if(riotName.equalsIgnoreCase(p.optString("name",obj(p,"account").optString("name",""))) && tag.equalsIgnoreCase(p.optString("tag",obj(p,"account").optString("tag","")))){player=p;break;}
                }
                if(player==null)continue;JSONObject stats=obj(player,"stats");
                JSONObject teamData=obj(game,"teams");String playerTeam=player.optString("team_id",player.optString("team",""));
                boolean won=false;boolean known=false;
                JSONObject team=teamData.optJSONObject(playerTeam.toLowerCase());if(team==null)team=teamData.optJSONObject(playerTeam.toUpperCase());
                if(team!=null){known=team.has("won");won=team.optBoolean("won",false);}
                if(!known){JSONArray teams=game.optJSONArray("teams");if(teams!=null)for(int t=0;t<teams.length();t++){JSONObject item=teams.optJSONObject(t);if(item!=null&&playerTeam.equalsIgnoreCase(item.optString("team_id"))){won=item.optBoolean("won");known=item.has("won");break;}}}
                if(!known)continue; // Never silently record a draw or unknown result as a loss.
                CoachStore.Match m=new CoachStore.Match();m.id=id;m.day=date;m.map=name(metadata,"map");m.agent=name(player,"agent");m.won=won;
                m.kills=stats.optInt("kills",player.optInt("kills"));m.deaths=stats.optInt("deaths",player.optInt("deaths"));m.assists=stats.optInt("assists",player.optInt("assists"));
                JSONObject shots=obj(stats,"shots");m.headshots=shots.optInt("head",stats.optInt("headshots"));m.bodyshots=shots.optInt("body",stats.optInt("bodyshots"));m.legshots=shots.optInt("leg",stats.optInt("legshots"));
                out.added+=db.saveMatch(m);
            }
            out.note=out.added+" new matches imported out of "+out.scanned+" returned. Matches with unrecognised results are skipped.";
            return out;
        }finally{connection.disconnect();}
    }
}
