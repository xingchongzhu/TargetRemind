package com.wtach.stationremind.object;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FavoriteInfo {
    public List<CollectInfo> favoriteMap = new ArrayList<>();

    private CollectInfo currentCollectInfo = null;

    public void addFavorite(CollectInfo collectInfo){
        favoriteMap.add(collectInfo);
    }

    public void updateFavorite(CollectInfo collectInfo){
        Iterator<CollectInfo> iterator = favoriteMap.iterator();
        while (iterator.hasNext()){
            CollectInfo temp = iterator.next();
            if(temp.getName().equals(collectInfo.getName())){
                temp.setList(collectInfo.getList());
                break;
            }
        }
    }

    public void removeFavorite(CollectInfo collectInfo){
        Iterator<CollectInfo> iterator = favoriteMap.iterator();
        while (iterator.hasNext()){
            CollectInfo temp = iterator.next();
            if(temp.getName().equals(collectInfo.getName())){
                iterator.remove();
                break;
            }
        }
    }

    public void clear(){
        favoriteMap.clear();
    }

    public void setCurrentCollectInfo(CollectInfo currentCollectInfo) {
        this.currentCollectInfo = currentCollectInfo;
    }

    public CollectInfo getCurrentCollectInfo() {
        return currentCollectInfo;
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        for(CollectInfo collectInfo : favoriteMap){
            stringBuilder.append(collectInfo.toString());
        }
        return stringBuilder.toString();
    }

    public boolean collectIsExist(FavoriteInfo favoriteInfo, String key){
        if(favoriteInfo == null){
            return false;
        }
        for(CollectInfo collectInfo : favoriteMap){
            if(collectInfo.getName().equals(key)){
                return true;
            }
        }
        return false;
    }

}
