package me.elvishew.puzzle;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import me.elvishew.puzzle.Puzzle.Games;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class GameData implements Parcelable {

    private long mGameId;
    private int mLevel;
    private String mName;
    private String mImage;
    private int mScore;
    private long mTime;
    private int mSteps;

    private static final String TAG = GameData.class.getSimpleName();

    private static final String NODE_GAME = "game";
    private static final String ATTR_GAME_ID="game_id";
    private static final String ATTR_LEVEL = "level";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_IMAGE = "image";

    public GameData(long gameId, int level, String name, String image) {
        this.mGameId = gameId;
        this.mLevel = level;
        this.mName = name;
        this.mImage = image;
    }

    public GameData(long gameId, int level, String name, String image, int score,
            long time, int steps) {
        this.mGameId = gameId;
        this.mLevel = level;
        this.mName = name;
        this.mImage = image;
        this.mScore = score;
        this.mTime = time;
        this.mSteps = steps;
    }

    public GameData(Parcel parcel) {
        mGameId = parcel.readLong();
        mLevel = parcel.readInt();
        mName = parcel.readString();
        mImage = parcel.readString();
        mScore = parcel.readInt();
        mTime = parcel.readLong();
        mSteps = parcel.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mGameId);
        dest.writeInt(mLevel); 
        dest.writeString(mName); 
        dest.writeString(mImage); 
        dest.writeInt(mScore); 
        dest.writeLong(mTime); 
        dest.writeInt(mSteps); 
    }

    public static final Creator<GameData> CREATOR = new Creator<GameData>() {
        @Override
        public GameData createFromParcel(Parcel source) {
            return new GameData(source);
        }

        @Override
        public GameData[] newArray(int size) {
            return new GameData[size];
        }
    };

    public long gameId() {
        return mGameId;
    }

    public int level() {
        return mLevel;
    }

    public String name() {
        return mName;
    }

    public String image() {
        return mImage;
    }

    public int score() {
        return mScore;
    }

    public long time() {
        return mTime;
    }

    public int steps() {
        return mSteps;
    }

    public static List<GameData> readXML(InputStream inStream) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        List<GameData> games = new ArrayList<GameData>();

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document dom = builder.parse(inStream);
            Element root = dom.getDocumentElement();

            NodeList items = root.getElementsByTagName(NODE_GAME);
            for (int length = items.getLength(), i = 0; i < length; i++) {
                Element gameNode = (Element) items.item(i);
                games.add(new GameData(Integer.parseInt(gameNode
                        .getAttribute(ATTR_GAME_ID)), Integer.parseInt(gameNode
                        .getAttribute(ATTR_LEVEL)), gameNode
                        .getAttribute(ATTR_NAME), gameNode
                        .getAttribute(ATTR_IMAGE)));
            }
            inStream.close();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error when parsing!");
        }
        return games;
    }

    public void updateAchievement(long time, int steps, int score) {
        this.mTime = time;
        this.mSteps = steps;
        this.mScore = score;
    }

    public ContentValues createValues() {
        ContentValues values = new ContentValues();
        values.put(Games.GAME_ID, mGameId);
        values.put(Games.LEVEL, mLevel);
        values.put(Games.NAME, mName);
        values.put(Games.IMAGE, mImage);
        values.put(Games.SCORE, mScore);
        values.put(Games.TIME, mTime);
        values.put(Games.STEPS, mSteps);
        return values;
    }

    public static ContentValues createValues(long gameId, String level, String name,
            String image, int score, long time, int steps) {
        ContentValues values = new ContentValues();
        values.put(Games.GAME_ID, gameId);
        values.put(Games.LEVEL, level);
        values.put(Games.NAME, name);
        values.put(Games.IMAGE, image);
        values.put(Games.SCORE, score);
        values.put(Games.TIME, time);
        values.put(Games.STEPS, steps);
        return values;
    }

    public ContentValues createSimpleValues() {
        ContentValues values = new ContentValues();
        values.put(Games.GAME_ID, mGameId);
        values.put(Games.LEVEL, mLevel);
        values.put(Games.NAME, mName);
        values.put(Games.IMAGE, mImage);
        return values;
    }

    public static ContentValues createSummaryValues(long gameId, String level, String name,
            String image) {
        ContentValues values = new ContentValues();
        values.put(Games.GAME_ID, gameId);
        values.put(Games.LEVEL, level);
        values.put(Games.NAME, name);
        values.put(Games.IMAGE, image);
        return values;
    }

    public ContentValues createAchievementValues() {
        ContentValues values = new ContentValues();
        values.put(Games.TIME, mTime);
        values.put(Games.STEPS, mSteps);
        values.put(Games.SCORE, mScore);
        return values;
    }

    public static ContentValues createAchievementValues(long time, int steps, int score) {
        ContentValues values = new ContentValues();
        values.put(Games.TIME, time);
        values.put(Games.STEPS, steps);
        values.put(Games.SCORE, score);
        return values;
    }

    @Override
    public String toString() {
        return "GameData[gameId(" + mGameId + 
                "), level(" + mLevel + 
                "), name(" + mName + 
                "), image(" + mImage + 
                "), score(" + mScore + 
                "), time(" +  mTime + 
                "), steps(" + mSteps + 
                ")]\n";
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
