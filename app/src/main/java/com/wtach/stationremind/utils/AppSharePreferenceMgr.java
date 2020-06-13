package com.wtach.stationremind.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wtach.stationremind.object.SelectResultInfo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class AppSharePreferenceMgr {
    /**
     * The path to save the launcher's data referring to grid.
     */
    public static final String FILE_NAME = "data";

    /**
     * According to parameter's type to decide which type is saved.
     */
    public static void put(Context context, String key, Object object)
    {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (object instanceof String)
        {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer)
        {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean)
        {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float)
        {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long)
        {
            editor.putLong(key, (Long) object);
        } else
        {
            editor.putString(key, object.toString());
        }

        SharedPreferencesCompat.apply(editor);
    }

    public static Object get(Context context, String key, Object defaultObject)
    {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);

        if (defaultObject instanceof String)
        {
            return sp.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer)
        {
            return sp.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean)
        {
            return sp.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float)
        {
            return sp.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long)
        {
            return sp.getLong(key, (Long) defaultObject);
        }

        return null;
    }

    /**
     * Remove the key.
     */
    public static void remove(Context context, String key)
    {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 获取保存的序列化的实体
     *
     * @param context
     * @param key
     * @return
     */
    public static Object getSerializableEntity(Context context, String key) {
        SharedPreferences sharePre = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        try {
            String wordBase64 = sharePre.getString(key, "");
            // 将base64格式字符串还原成byte数组
            if (wordBase64 == null || wordBase64.equals("")) { // 不可少，否则在下面会报java.io
                // .StreamCorruptedException
                return null;
            }
            byte[] objBytes = Base64.decode(wordBase64.getBytes(), Base64.DEFAULT);
            ByteArrayInputStream bais = new ByteArrayInputStream(objBytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            // 将byte数组转换成product对象
            Object obj = ois.readObject();
            bais.close();
            ois.close();
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 保存序列号的实体类
     *
     * @param context
     * @param key
     * @param object
     */
    public static void putSerializableEntity(Context context, String key, Serializable object) {

        SharedPreferences share =  context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        if (object == null) {
            SharedPreferences.Editor editor = share.edit().remove(key);
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        String objectStr = "";
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            // 将对象放到OutputStream中
            // 将对象转换成byte数组，并将其进行base64编码
            objectStr = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
            baos.close();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        SharedPreferences.Editor editor = share.edit();
        // 将编码后的字符串写到base64.xml文件中
        editor.putString(key, objectStr);
        SharedPreferencesCompat.apply(editor);
    }

    public static  List<SelectResultInfo> getSerializableList(Context context, String key){
        SharedPreferences sharePre = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        String json = sharePre.getString(key, null);
        List<SelectResultInfo > list = new ArrayList<SelectResultInfo>();
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<SelectResultInfo>>(){}.getType();
            list = gson.fromJson(json, type);
        }
        return list;
    }

    public static void putSerializableList(Context context, String key, List<Object> list){
        SharedPreferences share =  context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(list);
        Log.d("zxc", "saved json is "+ json);

        SharedPreferences.Editor editor = share.edit();
        // 将编码后的字符串写到base64.xml文件中
        editor.putString(key, json);
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * Clear all data.
     */
    public static void clear(Context context)
    {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * Query the key if it exists.
     */
    public static boolean contains(Context context, String key)
    {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        return sp.contains(key);
    }

    /**
     * Return all key-value.
     */
    public static Map<String, ?> getAll(Context context)
    {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        return sp.getAll();
    }


    /**
     * Create a compat class to deal with SharePreferences.
     */
    private static class SharedPreferencesCompat
    {
        private static final Method sApplyMethod = findApplyMethod();

        /**
         * Reflect to search apply method.
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        private static Method findApplyMethod()
        {
            try
            {
                Class clz = SharedPreferences.Editor.class;
                return clz.getMethod("apply");
            } catch (NoSuchMethodException e)
            {
            }

            return null;
        }

        public static void apply(SharedPreferences.Editor editor)
        {
            try
            {
                if (sApplyMethod != null)
                {
                    sApplyMethod.invoke(editor);
                    return;
                }
            } catch (IllegalArgumentException e)
            {
            } catch (IllegalAccessException e)
            {
            } catch (InvocationTargetException e)
            {
            }
            editor.commit();
        }
    }
}

