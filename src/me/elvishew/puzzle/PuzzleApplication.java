package me.elvishew.puzzle;

import java.lang.reflect.Field;
import java.util.HashMap;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;

public class PuzzleApplication extends Application {

    private static HashMap<String, Integer> sThumbnailIdsMap = new HashMap<String, Integer>();

    private static HashMap<String, Drawable> sThumbnailDrawablesMap = new HashMap<String, Drawable>();

    static {
        mapImages2Ids();
    }

    private static void mapImages2Ids() {
        Field[] fields = R.drawable.class.getDeclaredFields();
        try {
            for (Field field : fields) {
                if (field.getName().startsWith("image")) {
                    sThumbnailIdsMap.put(field.getName(), field.getInt(null));
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static int getThumbnailId(String imageName) {
        return sThumbnailIdsMap.get(imageName);
    }

    public static Drawable getThumbnailDrawable(Context context, String imageName) {
        Drawable image = sThumbnailDrawablesMap.get(imageName);
        if (image == null) {
            image = context.getResources().getDrawable(sThumbnailIdsMap.get(imageName));
            sThumbnailDrawablesMap.put(imageName, image);
        }
        return image;
    }
}
