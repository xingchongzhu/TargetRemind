package com.wtach.stationremind.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoriteInfo {
    private List<CollectInfo> favoriteMap = new ArrayList<>();

    public void addFavorite(CollectInfo collectInfo){
        favoriteMap.add(collectInfo);
    }

    public void removeFavorite(CollectInfo collectInfo){
        favoriteMap.remove(collectInfo);
    }

    public void clear(){
        favoriteMap.clear();
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        for(CollectInfo collectInfo : favoriteMap){
            stringBuilder.append(collectInfo.toString());
        }
        return stringBuilder.toString();
    }
}
