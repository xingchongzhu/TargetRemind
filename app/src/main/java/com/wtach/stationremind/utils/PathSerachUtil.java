package com.wtach.stationremind.utils;

import android.util.Log;

import com.baidu.location.BDLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.wtach.stationremind.model.item.bean.LineInfo;
import com.wtach.stationremind.model.item.bean.StationInfo;
import com.wtach.stationremind.database.DataManager;
import com.wtach.stationremind.model.item.IteratorNodeTool;
import com.wtach.stationremind.model.item.Node;
import com.wtach.stationremind.object.LineObject;

public class PathSerachUtil {
    private static final int MAXLINENUMBER = 30;
    private static final int MAXRECOMENDLINENUMBER = 10;
    static String TAG = "PathSerachUtil";
    static boolean debug = false;

    /**
     * 往图片上写入文字、图片等内容
     */
    public static void findLinedStation(LineInfo lineInfo, StationInfo start, StationInfo end, List<StationInfo> list) {
        List<StationInfo> stationInfoList = lineInfo.getStationInfoList();

        if (start == null || end == null) {
            return;
        }
        //Log.d(TAG,"起点 "+start.getCname()+" 终点 "+end.getCname()+" lineInfo.line = "+lineInfo.lineid);
        //Log.d(TAG,"  start.pm  = "+start.pm+" end.pm = "+end.pm);
        if (start.pm < end.pm) {
            int next = start.pm;
            for (StationInfo mStationInfo : stationInfoList) {
                if (next == mStationInfo.pm) {
                    int size = list.size();
                    if (size >= 1) {
                        if (list.get(size - 1).getCname().equals(mStationInfo.getCname())) {
                            list.remove(size - 1);
                        }
                    }
                    list.add(mStationInfo);
                    if (debug)
                        Log.d(TAG, mStationInfo.getCname() + "   ");
                    if (next == end.pm) {
                        break;
                    }
                    next++;
                }
            }
        } else {
            int next = start.pm;
            int size = stationInfoList.size() - 1;
            for (int i = size; i >= 0; i--) {
                StationInfo mStationInfo = stationInfoList.get(i);
                if (next == mStationInfo.pm) {
                    int size1 = list.size();
                    if (size1 >= 1) {
                        if (list.get(size1 - 1).getCname().equals(mStationInfo.getCname())) {
                            list.remove(size1 - 1);
                        }
                    }
                    list.add(mStationInfo);
                    if (debug)
                        Log.d(TAG, mStationInfo.getCname() + "   ");
                    if (next == end.pm) {
                        break;
                    }
                    next--;
                }
            }
        }
    }

    public static List<LineObject> getRecomendLines(List<LineObject> lastLinesLast){
        if(lastLinesLast.size() <= 0){
            return lastLinesLast;
        }
        PathSerachUtil.sortStationNum(lastLinesLast);
        LineObject first = lastLinesLast.get(0);
        //去除相同
        //换乘次数排序
        PathSerachUtil.sortChangeTime(lastLinesLast);
        if(lastLinesLast.get(0) == first){
            lastLinesLast.clear();
            lastLinesLast.add(first);
            //mCardAdapter.setData(lastLinesLast);
            return lastLinesLast;
        }
        //去除相同
        List<LineObject> add = new ArrayList<>();
        Map<Integer, Integer> array = new HashMap();
        for(LineObject entry: lastLinesLast){
            boolean isEqual = false;
            for(LineObject listEntry: add){
                if(entry.lineidList.toString().equals(listEntry.lineidList.toString()) &&
                        entry.lineidList.size() == listEntry.lineidList.size()){
                    isEqual = true;
                }
            }
            if(!isEqual)
                add.add(entry);
            array.put(entry.lineidList.size(),entry.lineidList.size());
        }
        //换乘次数分类
        List<Integer> list = new ArrayList<>(array.keySet());
        Collections.sort(list, new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {
                if (o1< o2) {
                    return -1;
                } else if (o1 == o2) {
                    return 0;
                }
                return 1;
            }
        });
        List<LineObject> needadd = new ArrayList<>();
        List<LineObject> templist = new ArrayList<>();
        if(lastLinesLast != null)
            lastLinesLast.clear();
        for(Integer size:list){
            needadd.clear();
            templist.clear();
            for(LineObject entry: add){
                if(size == entry.lineidList.size()){
                    needadd.add(entry);
                }
            }
            PathSerachUtil.sortStationNum(needadd);
            if(needadd.size() >0){
                int number = needadd.get(0).stationList.size();
                for(LineObject entry: needadd){
                    if(number == entry.stationList.size()){
                        templist.add(entry);
                    }
                }
                lastLinesLast.addAll(templist);
            }
        }
        needadd.clear();
        templist.clear();
        PathSerachUtil.sortChangeTime(lastLinesLast);
        return lastLinesLast;
    }
    public static List<LineObject> getLastRecomendLines(List<LineObject> currentAllStationList) {
        //List<Map.Entry<List<Integer>, List<StationInfo>>> lastLinesLast = new ArrayList<Map.Entry<List<Integer>, List<StationInfo>>>(currentAllStationList.entrySet());
        //currentAllStationList.clear();
        return getRecomendLines(currentAllStationList);
    }

    public static void sortChangeTime(List<LineObject> lastLinesLast){
        if(lastLinesLast.size() <2){
            return;
        }
        Collections.sort(lastLinesLast, new Comparator<LineObject>() {
            public int compare(LineObject o1,
                               LineObject o2) {
                if (o1.lineidList.size() < o2.lineidList.size()) {
                    return -1;
                } else if (o1.lineidList.size() == o2.lineidList.size()) {
                    return 0;
                }
                return 1;
            }
        });
        /*Collections.sort(lastLinesLast, new Comparator<Map.Entry<List<Integer>, List<StationInfo>>>() {
            public int compare(Map.Entry<List<Integer>, List<StationInfo>> o1,
                               Map.Entry<List<Integer>, List<StationInfo>> o2) {
                if (o1.getKey().size() < o2.getKey().size()) {
                    return -1;
                } else if (o1.getKey().size() == o2.getKey().size()) {
                    return 0;
                }
                return 1;
            }
        });*/
    }

    public static void sortStationNum(List<LineObject> lastLinesLast){

        if(lastLinesLast.size() < 2){
            return;
        }

        Collections.sort(lastLinesLast, new Comparator<LineObject>() {
            public int compare(LineObject o1,
                               LineObject o2) {
                if (o1.stationList.size() < o2.stationList.size()) {
                    return -1;
                } else if (o1.stationList.size() == o2.stationList.size()) {
                    return 0;
                }
                return 1;
            }
        });
        /*Collections.sort(lastLinesLast, new Comparator<Map.Entry<List<Integer>, List<StationInfo>>>() {
            public int compare(Map.Entry<List<Integer>, List<StationInfo>> o1,
                               Map.Entry<List<Integer>, List<StationInfo>> o2) {
                if (o1.getValue().size() < o2.getValue().size()) {
                    return -1;
                } else if (o1.getValue().size() == o2.getValue().size()) {
                    return 0;
                }
                return 1;
            }
        });*/
    }
    public static List<LineObject> getAllLineStation(Map<Integer, LineInfo> mLineInfoList, List<List<Integer>> transferLine
            , StationInfo start, final StationInfo end) {
        List<LineObject> currentAllStationList = new ArrayList<>();//正在导航线路
        if(transferLine == null || transferLine.size() <=0){
            return currentAllStationList;
        }
        //取出一条路线
        int n = 0;
        for (List<Integer> list : transferLine) {
            StationInfo startStation = null, endStation = null;
            final int size = list.size();
            //构建多叉树
            //起点
            LineInfo lineInfo = getLineInfoByLineid(mLineInfoList, list.get(0));
            if(lineInfo == null){
                continue;
            }
            startStation = getStationInfoByLineidAndName(lineInfo.getStationInfoList(), start.getCname());//再找相同站台
            Node root = new Node(startStation);//根节点
            //终点
            lineInfo = getLineInfoByLineid(mLineInfoList, list.get(size - 1));
            if(lineInfo == null){
                continue;
            }
            endStation = getStationInfoByLineidAndName(lineInfo.getStationInfoList(), end.getCname());//再找相同站台
            for (int i = 0; i < size; i++) {
                final int lined = list.get(i);
                if (size > i + 1) {
                    List<StationInfo> stationInfoList = getTwoLineCommonStation(mLineInfoList, lined, list.get(i + 1));//找到当前线路和下一条线路交汇站台
                    if (stationInfoList != null && stationInfoList.size() > 0) {
                        addChild(root, stationInfoList);
                    }
                }
            }
            List<StationInfo> stationInfoList = new ArrayList<>();
            stationInfoList.add(endStation);
            addChild(root, stationInfoList);

            //多叉树查找所有路径
            IteratorNodeTool tool = new IteratorNodeTool();
            Stack<Node> pathstack = new Stack();
            tool.iteratorNode(root, pathstack);
            List<LineObject> all = new ArrayList<>();
            for (List<StationInfo> entry : tool.pathMap) {
                all.add(getLineStation(list,entry, mLineInfoList));
            }
            if (all != null && all.size() > 0) {
                LineObject min = all.get(0);
                for (LineObject ll : all) {
                    if (min.stationList.size() > ll.stationList.size()) {
                        min = ll;
                    }
                }
                Map<Integer, Integer> map = new HashMap<>();
                for(StationInfo stationInfo:min.stationList){
                    map.put(stationInfo.lineid,stationInfo.lineid);
                }
                //if(map.size() == list.size()) {
                    currentAllStationList.add(min);
                //}
                map.clear();
                map = null;
                all.clear();
                all = null;
            }
        }
        return currentAllStationList;
    }

    public static void addChild(Node node, List<StationInfo> stationInfoList) {
        if (node.getChildNodes() == null) {
            for (StationInfo stationInfo : stationInfoList) {
                Node nextNode = new Node(stationInfo);
                node.addChildNode(nextNode);
            }
        } else {
            List<Node> stationInfos = node.getChildNodes();
            for (Node node1 : stationInfos) {
                addChild(node1, stationInfoList);
            }
        }
    }

    public static LineObject getLineStation(List<Integer> tranfers, List list, Map<Integer, LineInfo> mLineInfoList) {
        List<StationInfo> oneLineMap = new ArrayList<>();
        List<StationInfo> transferList = new ArrayList<>();
        int size = list.size();
        StringBuffer buf = new StringBuffer();

        if(tranfers.size() < (list.size() -1)) {
            int i = 0;
            int lastLineid = 0 ;
            for (i = 0; i < tranfers.size(); i++) {
                Node start = (Node) list.get(i + 1);
                StationInfo stationInfo = (StationInfo) start.getNodeEntity();
                stationInfo.lineid = tranfers.get(i);
                lastLineid = stationInfo.lineid;
            }
            for(;i<list.size();i++){
                Node start = (Node) list.get(i);
                StationInfo stationInfo = (StationInfo) start.getNodeEntity();
                stationInfo.lineid = lastLineid;
            }
        }else if(tranfers.size() > (list.size() -1)){
        }
        for (int i = 0; i < tranfers.size(); i++) {
            Node start = (Node) list.get(i + 1);
            StationInfo stationInfo = (StationInfo) start.getNodeEntity();
            transferList.add(stationInfo);
        }
        for (int i = 0; i < size; i++) {
            if (i + 1 < size) {
                Node start = (Node) list.get(i);
                StationInfo stationInfo = (StationInfo) start.getNodeEntity();
                Node end = (Node) list.get(i + 1);
                StationInfo endInfo = (StationInfo) end.getNodeEntity();
                if(stationInfo == null || endInfo == null){
                    continue;
                }
                int lineid = stationInfo.lineid;
                if (i != 0) {
                    lineid = endInfo.lineid;
                }
                LineInfo lineInfo = getLineInfoByLineid(mLineInfoList, lineid);
                for (StationInfo stationInfo1 : lineInfo.getStationInfoList()) {
                    if (stationInfo1.getCname().equals(endInfo.getCname())) {
                        endInfo = stationInfo1;
                    }
                    if (stationInfo1.getCname().equals(stationInfo.getCname())) {
                        stationInfo = stationInfo1;
                    }
                }
                PathSerachUtil.findLinedStation(lineInfo, stationInfo, endInfo, oneLineMap);
            }
            /*Node start = (Node)list.get(i);
            StationInfo stationInfo = (StationInfo) start.getNodeEntity();
            buf.append(stationInfo.lineid+"  "+stationInfo.getCname()+" ->");*/
        }
        return new LineObject(oneLineMap,tranfers,transferList);
    }

    public static StationInfo gitNearestStation(List<StationInfo> stationInfoList, StationInfo stationInfo) {
        if (stationInfoList == null || stationInfoList.size() <= 0) {
            return null;
        }
        StationInfo minStationInfo = stationInfoList.get(0);
        int min = 0;
        for (StationInfo station : stationInfoList) {
            int dis = Math.abs(station.pm - stationInfo.pm);
            if (min > dis) {
                minStationInfo = station;
            }
        }
        return minStationInfo;
    }

    public static StationInfo getStationInfoByLineidAndName(List<StationInfo> stationInfoList, String name) {
        for (StationInfo mStationInfo : stationInfoList) {//查询最站台和当前路线相同站
            if (mStationInfo.getCname().equals(name)) {
                return mStationInfo;
            }
        }
        return null;
    }

    public static List<StationInfo> getTwoLineCommonStation(Map<Integer, LineInfo> mLineInfoList, int line1, int line2) {
        List<StationInfo> stationlist = new ArrayList<>();
        LineInfo lineInfo1 = PathSerachUtil.getLineInfoByLineid(mLineInfoList, line1);
        if(lineInfo1 == null){
            return stationlist;
        }
        for (StationInfo stationInfo : lineInfo1.getStationInfoList()) {
            if (stationInfo.canTransfer()) {
                int lined = PathSerachUtil.isSameLine(stationInfo, line2);
                if (lined > 0) {
                    stationlist.add(stationInfo);
                }
            }
        }
        return stationlist;
    }

    public static LineInfo getLineInfoByLineid(Map<Integer, LineInfo> lineInfoList, int lineid) {
        return lineInfoList.get(lineid);
    }

    public static String printAllRecomindLine(List<LineObject> lastLines) {
        StringBuffer str = new StringBuffer();
        Log.d(TAG, "------------------------devide-----------------------lastLines.size = " + lastLines.size());
        for (LineObject entry : lastLines) {
            str.append(entry.lineidList.toString() + ":");
            for (StationInfo stationInfo : entry.stationList) {
                str.append(stationInfo.getCname() + "->");
            }
            str.append("\n");
            //Log.d(TAG,"key = "+entry.getKey()+" change = "+entry.getKey().size()+" stationnumber = "+entry.getValue().size()+" station = "+str.toString());
        }
        Log.d(TAG, str.toString());
        Log.d(TAG, "------------------------end-----------------------");
        return str.toString();
    }

    //查询站台是否与目标线路有相同线路
    public static int isSameLine(StationInfo start, int lined) {
        String lines[] = start.getTransfer().split(CommonFuction.TRANSFER_SPLIT);
        if (!start.canTransfer()) {
            lines = new String[1];
            lines[0] = "" + start.lineid;
        }
        int size = lines.length;
        for (int i = 0; i < size; i++) {//找出和终点站相同点不用换乘
            if (CommonFuction.convertToInt(lines[i], 0) == lined) {//找到相同线路站不用换乘
                return CommonFuction.convertToInt(lines[i], 0);
            }
        }
        return -1;
    }

    //查询站台是否与目标线路有相同线路
    public static int isTwoStationSameLine(StationInfo start, StationInfo end) {
        String lines[] = start.getTransfer().split(CommonFuction.TRANSFER_SPLIT);
        String lines1[] = end.getTransfer().split(CommonFuction.TRANSFER_SPLIT);

        if (!start.canTransfer()) {
            lines = new String[1];
            lines[0] = "" + start.lineid;
        }
        if (!end.canTransfer()) {
            lines1 = new String[1];
            lines1[0] = "" + end.lineid;
        }
        int size = lines.length;
        int size1 = lines1.length;
        for (int i = 0; i < size; i++) {//找出和终点站相同点不用换乘
            for (int j = 0; j < size1; j++) {
                if (CommonFuction.convertToInt(lines[i], 0) == CommonFuction.convertToInt(lines1[j], -1)) {//找到相同线路站不用换乘
                    return CommonFuction.convertToInt(lines[i], 0);
                }
            }
        }
        return -1;
    }

    public static StationInfo getNerastStation(BDLocation location, Map<Integer, LineInfo> mLineInfoList) {
        double min = Double.MAX_VALUE;
        double longitude = 0;
        double latitude = 0;
        double dis = 0;
        StationInfo nerstStationInfo = null;
        if (location != null && mLineInfoList != null) {
            for (Map.Entry<Integer, LineInfo> entry : mLineInfoList.entrySet()) {
                for (StationInfo stationInfo : entry.getValue().getStationInfoList()) {
                    longitude = CommonFuction.convertToDouble(stationInfo.getLot(), 0);
                    latitude = CommonFuction.convertToDouble(stationInfo.getLat(), 0);
                    dis = CommonFuction.getDistanceLat(longitude, latitude, location.getLongitude(), location.getLatitude());
                    if (min > dis) {
                        min = dis;
                        nerstStationInfo = stationInfo;
                    }
                }
            }
        }
        return nerstStationInfo;
    }

    public static final int MINDIS = 600;

    public static StationInfo getNerastNextStation(BDLocation location, Map<Integer, LineInfo> mLineInfoList) {
        double min = Double.MAX_VALUE;
        double longitude = 0;
        double latitude = 0;
        double dis = 0;
        StationInfo nerstStationInfo = null;
        if (location != null && mLineInfoList != null) {
            for (Map.Entry<Integer, LineInfo> entry : mLineInfoList.entrySet()) {
                for (StationInfo stationInfo : entry.getValue().getStationInfoList()) {
                    longitude = CommonFuction.convertToDouble(stationInfo.getLot(), 0);
                    latitude = CommonFuction.convertToDouble(stationInfo.getLat(), 0);
                    dis = CommonFuction.getDistanceLat(longitude, latitude, location.getLongitude(), location.getLatitude());
                    if (min > dis) {
                        min = dis;
                        nerstStationInfo = stationInfo;
                    }
                }
            }
        }
        if (MINDIS < min) {
            return null;
        }
        return nerstStationInfo;
    }

    public static StationInfo getNerastNextStation(BDLocation location, List<StationInfo> list) {
        double min = Double.MAX_VALUE;
        double longitude = 0;
        double latitude = 0;
        double dis = 0;
        StationInfo nerstStationInfo = null;
        if (location != null && list != null) {
            for (StationInfo stationInfo : list) {
                longitude = CommonFuction.convertToDouble(stationInfo.getLot(), 0);
                latitude = CommonFuction.convertToDouble(stationInfo.getLat(), 0);
                dis = CommonFuction.getDistanceLat(longitude, latitude, location.getLongitude(), location.getLatitude());
                if (min > dis) {
                    min = dis;
                    nerstStationInfo = stationInfo;
                }
            }
        }
        /*if (MINDIS < min) {
            return null;
        }*/
        return nerstStationInfo;
    }

    public static boolean arriveNextStatison(BDLocation location, StationInfo stationInfo) {
        double longitude = CommonFuction.convertToDouble(stationInfo.getLot(), 0);
        double latitude = CommonFuction.convertToDouble(stationInfo.getLat(), 0);
        double dis = CommonFuction.getDistanceLat(longitude, latitude, location.getLongitude(), location.getLatitude());
        if (MINDIS > dis) {
            return true;
        }
        return false;
    }

    public static List<LineObject> getReuslt(List<List<Integer>> transferLine,
                                             final DataManager mDataManager, final StationInfo start, final StationInfo end) {
        Collections.sort(transferLine, new Comparator<List<Integer>>() {
            public int compare(List<Integer> p1, List<Integer> p2) {
                //按照换乘次数
                if (p1.size() > p2.size()) {
                    return 1;
                }
                if (p1.size() == p2.size()) {
                    return 0;
                }
                return -1;
            }
        });
        //找出所有路径
        return PathSerachUtil.getLastRecomendLines(PathSerachUtil.getAllLineStation(mDataManager.getLineInfoList(), transferLine, start, end));//查询最终线路
    }

    public static Map<Integer, Integer> getLineAllLined(List<StationInfo> list) {
        Map<Integer, Integer> listStr = new HashMap<Integer, Integer>();
        if (list == null && list.size() <= 0)
            return listStr;
        StringBuffer str = new StringBuffer();
        for (StationInfo stationInfo : list) {
            String lineList[] = stationInfo.getTransfer().split(CommonFuction.TRANSFER_SPLIT);
            int size = lineList.length;
            for (int i = 0; i < size; i++) {
                int line = CommonFuction.convertToInt(lineList[i], 0);
                if (!listStr.containsKey(line) && line != stationInfo.lineid) {
                    listStr.put(line, line);
                    str.append(lineList[i] + "  ");
                }
            }
        }
        //Log.d(TAG, "getLineAllLined lineid = " + list.get(0).lineid + " all lined = " + str);
        return listStr;
    }
}
