package com.wtach.stationremind.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CollectInfo {
    private String name;
    private List<SelectResultInfo> list = new ArrayList<>();

    public CollectInfo(String name,List list){
        this.name = name;
        this.list = list;
    }

    public String getName() {
        return name;
    }

    public List<SelectResultInfo> getList() {
        return list;
    }

    public void setList(List list){
        this.list = list;
    }

    public String getContent(){
        StringBuilder stringBuilder = new StringBuilder();
        if(list == null){
            return stringBuilder.toString();
        }
        String nextArrow = "->";
        Iterator<SelectResultInfo> iterable = list.iterator();
        while (iterable.hasNext()){
            SelectResultInfo selectResultInfo = iterable.next();
            stringBuilder.append(selectResultInfo.getKey());
            if(iterable.hasNext()){
                stringBuilder.append(nextArrow);
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("name = "+name);
        if(list == null){
            return stringBuilder.toString();
        }
        for(SelectResultInfo selectResultInfo : list){
            stringBuilder.append(" -> "+selectResultInfo.toString());
        }

        return stringBuilder.toString();
    }
}
