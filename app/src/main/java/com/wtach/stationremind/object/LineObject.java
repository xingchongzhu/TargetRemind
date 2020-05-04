package com.wtach.stationremind.object;

import java.util.List;

import com.wtach.stationremind.model.item.bean.StationInfo;

public class LineObject {
    public List<StationInfo> stationList ;
    public List<Integer> lineidList ;
    public List<StationInfo> transferList ;
    public LineObject(){
        
    }
    public LineObject(List<StationInfo> stationList, List<Integer> lineidList ){
        this.stationList = stationList;
        this.lineidList = lineidList;
    }

    public LineObject(List<StationInfo> stationList, List<Integer> lineidList , List<StationInfo> transferList){
        this.stationList = stationList;
        this.lineidList = lineidList;
        this.transferList = transferList;
    }

    public String toString(){
        StringBuffer stringBuffer = new StringBuffer();
        if(transferList != null){
            stringBuffer.append("[");
            for(StationInfo stationInfo:transferList){
                stringBuffer.append(stationInfo.lineid+" "+stationInfo.cname+" ->");
            }
            stringBuffer.append("]");
        }
        if(stationList != null){
            stringBuffer.append("===");
            stringBuffer.append("(");
            for(StationInfo stationInfo:stationList){
                stringBuffer.append(stationInfo.cname+" ->");
            }
            stringBuffer.append(")");
        }
        return stringBuffer.toString();
    }
}
