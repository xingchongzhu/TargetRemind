package com.wtach.stationremind.utils;

import java.util.Comparator;

import com.wtach.stationremind.model.item.bean.ExitInfo;

public class MapComparator implements Comparator<ExitInfo> {
    public int compare(ExitInfo lhs, ExitInfo rhs) {
        return lhs.getExitname().compareTo(rhs.getExitname());
    }
}
