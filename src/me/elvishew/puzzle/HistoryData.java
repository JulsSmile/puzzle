package me.elvishew.puzzle;

import me.elvishew.puzzle.Puzzle.Historys;
import android.content.ContentValues;
import android.os.Parcel;

public class HistoryData extends AchievementData {

    private long mGameId;
    private long mDate;

    public HistoryData(long gameId, long date, long time, int steps, int score) {
        super(time, steps, score);
        mGameId = gameId;
        mDate = date;
    }

    public HistoryData(Parcel parcel) {
        super(parcel);
        mGameId = parcel.readLong();
        mDate = parcel.readLong();
    }

    public long gameId() {
        return mGameId;
    }

    public long date() {
        return mDate;
    }

    @Override
    public ContentValues createValues() {
        ContentValues values = super.createValues();
        values.put(Historys.GAME_ID, mGameId);
        values.put(Historys.DATE, mDate);
        return values;
    }

    public static ContentValues createVaules(long gameId, long date,
            long time, int steps, int score) {
        ContentValues values = AchievementData.createValues(time, steps, score);
        values.put(Historys.GAME_ID, gameId);
        values.put(Historys.DATE, date);
        return values;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(mGameId);
        dest.writeLong(mDate); 
    }

    public static final Creator<HistoryData> CREATOR = new Creator<HistoryData>() {
        @Override
        public HistoryData createFromParcel(Parcel source) {
            return new HistoryData(source);
        }

        @Override
        public HistoryData[] newArray(int size) {
            return new HistoryData[size];
        }
    };

}
