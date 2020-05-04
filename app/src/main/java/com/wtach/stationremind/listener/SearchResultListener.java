package com.wtach.stationremind.listener;
import java.util.List;

import com.wtach.stationremind.object.LineObject;

public interface SearchResultListener{
    void updateSingleResult(List<Integer> list);
    void updateResultList(List<List<Integer>> list);
    void updateResult(List<LineObject> lastLinesLast);
    void cancleDialog(List<LineObject> lastLinesLast);
    void setLineNumber(int number);
}