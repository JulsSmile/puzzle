package me.elvishew.puzzle;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import me.elvishew.puzzle.Puzzle.Achievements;
import me.elvishew.puzzle.Puzzle.Historys;
import me.elvishew.puzzle.Puzzle.Ranks;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

public class HttpHelper {

    /**
     * Create a json to post to web server.
     * @param historyData rank data just with player name and history, without rank
     * @return json object to be posted
     */
    public static JSONObject createRequestJSON(String player, HistoryData historyData) {
        JSONObject json = new JSONObject();
        try {
            json.put(Historys.GAME_ID, historyData.gameId());
            json.put(Ranks.PLAYER, player);
            json.put(Historys.DATE, historyData.date());
            json.put(Achievements.SCORE, historyData.score());
            json.put(Achievements.STEPS, historyData.steps());
            json.put(Achievements.TIME, historyData.time());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Create a json to post to web server.
     * @param gameId id of game, whose ranks will be download
     * @return json object to be posted
     */
    public static JSONObject createRequestJSON(long gameId) {
        JSONObject json = new JSONObject();
        try {
            json.put(Historys.GAME_ID, gameId);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return json;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager con = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = con.getActiveNetworkInfo();
        return (info != null && info.isAvailable());
    }

    public static void showNoConnectionDialog(final Context context,
            final DialogHost dialogHost, final int dialogId) {
        View view = LayoutInflater.from(context).inflate(R.layout.connect_dialog,
                null, false);

        view.findViewById(R.id.open_mobile_networks_setting).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        context.startActivity(new Intent(
                                Settings.ACTION_WIRELESS_SETTINGS));
                        dialogHost.dismissDialogById(dialogId);
                    }
                });

        view.findViewById(R.id.open_wifi_setting).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        context.startActivity(new Intent(
                                Settings.ACTION_WIFI_SETTINGS));
                        dialogHost.dismissDialogById(dialogId);
                    }
                });

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.tip);
        builder.setMessage(R.string.not_connected_to_internet);
        builder.setView(view);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        dialogHost.setDialogById(dialogId, builder.setView(view).create());
        dialogHost.setOnDialogDissmissListener(dialogId, new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dialogHost.setDialogById(dialogId, null);
            }
        });
        dialogHost.showDialogById(dialogId);
    }

    public static int loadRanksFromInternet(long gameId, String player,
            HistoryData historyData, List<RankData> results, boolean insert) {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        // TODO Add Verification some day.
        params.add(new BasicNameValuePair("username", ""));
        params.add(new BasicNameValuePair("password", ""));

        params.add(new BasicNameValuePair(Historys.GAME_ID, String
                .valueOf(gameId)));
        if (historyData != null) {
            params.add(new BasicNameValuePair(Historys.SCORE, String
                    .valueOf(historyData.score())));
            if (/*TODO test*/insert) {
                params.add(new BasicNameValuePair(Ranks.PLAYER, player));
                params.add(new BasicNameValuePair(Historys.STEPS, String
                        .valueOf(historyData.steps())));
                params.add(new BasicNameValuePair(Historys.TIME, String
                        .valueOf(historyData.time())));
                params.add(new BasicNameValuePair(Historys.DATE, String
                        .valueOf(historyData.date())));
            }
        }

        // Whether want to insert this history or not.
        //params.add(new BasicNameValuePair("Insert", String.valueOf(insert)));

        Response response = post("http://elvishew.me/api/index.php", params);
        if (response.errorCode != Response.ERROR_NOERROR) {
            return response.errorCode;
        }

        int rank = Constants.INVALID_RANK;
        try {
            if (TextUtils.isEmpty(response.responseString)) {
                return Response.ERROR_SERVER;
            }
            JSONObject jsonResponse = new JSONObject(response.responseString);
            rank = jsonResponse.getInt("your_rank");
            JSONArray data = jsonResponse.getJSONArray("ranks_data");
            int count = data.length();
            JSONObject result;
            for (int i = 0; i < count; i++) {
                result = data.getJSONObject(i);
                results.add(new RankData(0, result.getString(Ranks.PLAYER),
                        result.getLong(Ranks.GAME_ID), result
                                .getLong(Ranks.DATE), result
                                .getLong(Ranks.TIME), result
                                .getInt(Ranks.STEPS), result
                                .getInt(Ranks.SCORE)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.ERROR_JSON;
        }
        return rank;
    }

    public static int submitFeedback(String content, String contact, int type) {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        // TODO Add Verification some day.
        params.add(new BasicNameValuePair("content", content));
        params.add(new BasicNameValuePair("contact", contact));
        params.add(new BasicNameValuePair("type", String.valueOf(type)));
        Response response = post("http://elvishew.me/api/feedback.php", params);
        if (response.errorCode != Response.ERROR_NOERROR) {
            return response.errorCode;
        }

        try {
            if (TextUtils.isEmpty(response.responseString)) {
                return Response.ERROR_SERVER;
            }
            JSONObject jsonResponse = new JSONObject(response.responseString);
            boolean success = jsonResponse.getBoolean("success");
            if (success) {
                return Response.ERROR_NOERROR;
            } else {
                return Response.ERROR_SERVER;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.ERROR_JSON;
        }
    }

    public static Response post(String uri, ArrayList<NameValuePair> params) {
        Response response = new Response();
        InputStream is = null;
        StringBuilder sb;

        // post
        HttpPost httpPost = new HttpPost(uri);
        HttpClient client = new DefaultHttpClient();

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpResponse httpRes = client.execute(httpPost);
            if (httpRes.getStatusLine().getStatusCode() == 200) {
                is = httpRes.getEntity().getContent();
            } else {
                Log.e("tag_http", "Error in http connection");
                response.errorCode = Response.ERROR_NETWORK;
                return response;
            }
        } catch (Exception e) {
            Log.e("tag_http", "Error in http connection" + e.toString());
            response.errorCode = Response.ERROR_NETWORK;
            return response;
        }

        // Convert http response to string.
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is));
            sb = new StringBuilder();
            sb.append(reader.readLine() + "\n");
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            response.responseString = sb.toString();
            return response;
        } catch (Exception e) {
            Log.e("tag_convert", "Error converting result " + e.toString());
            response.errorCode = Response.ERROR_RESPONSE;
            return response;
        }
    }

    public static int toastMsgOf(int errorCode) {
        int toastMsg;
        switch (errorCode) {
        case Response.ERROR_NETWORK:
            toastMsg = R.string.error_network;
            break;
        case Response.ERROR_RESPONSE:
            toastMsg = R.string.error_response;
            break;
        case Response.ERROR_SERVER:
            toastMsg = R.string.error_server;
            break;
        case Response.ERROR_JSON:
            toastMsg = R.string.error_json;
            break;
        default:
            toastMsg = R.string.error_unknown;
            break;
        }
        return toastMsg;
    }

    public static class Response {
        public static final int ERROR_NOERROR = 0;
        public static final int ERROR_NETWORK = -1;
        public static final int ERROR_RESPONSE = -2;
        public static final int ERROR_SERVER = -3;
        public static final int ERROR_JSON = -4;

        int errorCode = ERROR_NOERROR;
        String responseString;
    }
}