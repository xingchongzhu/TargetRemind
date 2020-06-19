package com.wtach.stationremind.object;

import java.util.ArrayList;
import java.util.HashMap;
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

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("name = "+name);
        if(list != null){
            return stringBuilder.toString();
        }
        for(SelectResultInfo selectResultInfo : list){
            stringBuilder.append(" vaule = "+selectResultInfo.toString());
        }

        return stringBuilder.toString();
    }
}
