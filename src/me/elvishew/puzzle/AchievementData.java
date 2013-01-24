package me.elvishew.puzzle;

import me.elvishew.puzzle.Puzzle.Achievements;
import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

public class AchievementData implements Parcelable {
    private long mTime;
    private int mSteps;
    private int mScore;

    public AchievementData(long time, int steps, int score) {
        mTime = time;
        mSteps = steps;
        mScore = score;
    }

    public AchievementData(Parcel parcel) {
        mTime = parcel.readLong();
        mSteps = parcel.readInt();
        mScore = parcel.readInt();
    }

    public long time() {
        return mTime;
    }

    public int steps() {
        return mSteps;
    }

    public int score() {
        return mScore;
    }

    public ContentValues createValues() {
        ContentValues values = new ContentValues();
        values.put(Achievements.TIME, mTime);
        values.put(Achievements.STEPS, mSteps);
        values.put(Achievements.SCORE, mScore);
        return values;
    }

    /**
     * Create content values from data.
     */
    public static ContentValues createValues(long time, int steps, int score) {
        ContentValues values = new ContentValues();
        values.put(Achievements.TIME, time);
        values.put(Achievements.STEPS, steps);
        values.put(Achievements.SCORE, score);
        return values;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mTime);
        dest.writeInt(mSteps);
        dest.writeInt(mScore);
    }

    public static final Creator<AchievementData> CREATOR = new Creator<AchievementData>() {
        @Override
        public AchievementData createFromParcel(Parcel source) {
            return new AchievementData(source);
        }

        @Override
        public AchievementData[] newArray(int size) {
            return new AchievementData[size];
        }
    };
}
