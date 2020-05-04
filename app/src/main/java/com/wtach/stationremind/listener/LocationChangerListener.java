package com.wtach.stationremind.listener;

import com.baidu.location.BDLocation;

public interface LocationChangerListener {
    void loactionStation(BDLocation location);
    void stopRemind();
}