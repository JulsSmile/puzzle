package me.elvishew.puzzle;

import java.util.Comparator;

import me.elvishew.puzzle.Puzzle.Ranks;
import android.content.ContentValues;
import android.os.Parcel;

public class RankData extends HistoryData {

    private int mRank;
    private String mPlayer;

    public RankData(int rank, String player, long gameId, long date,
            long time, int steps, int score) {
        super(gameId, date, time, steps, score);
        mRank = rank;
        mPlayer = player;
    }

    public RankData(Parcel parcel) {
        super(parcel);
        mRank = parcel.readInt();
        mPlayer = parcel.readString();
    }

    public int rank() {
        return mRank;
    }

    public String player() {
        return mPlayer;
    }

    /**
     * Only should be called after load ranks from internet.
     * @param rank rank in a array
     */
    public void updateRank(int rank) {
        mRank = rank;
    }

    @Override
    public ContentValues createValues() {
        ContentValues values = super.createValues();
        values.put(Ranks.RANK, mRank);
        values.put(Ranks.PLAYER, mPlayer);
        return values;
    }

    public static ContentValues createValues(int rank, String player,
            long gameId, long date, long time, int steps, int score) {
        ContentValues values = HistoryData.createVaules(gameId, date, time, steps, score);
        values.put(Ranks.RANK, rank);
        values.put(Ranks.PLAYER, player);
        return values;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(mRank);
        dest.writeString(mPlayer); 
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

    static class DescComparator implements Comparator<RankData> {

        @Override
        public int compare(RankData lhs, RankData rhs) {
            if (lhs.score() > rhs.score()) {
                return -1;
            } else if (lhs.score() < rhs.score()) {
                return 1;
            }
            return 0;
        }
        
    }

}
