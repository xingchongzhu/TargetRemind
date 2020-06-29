package com.wtach.stationremind.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.heytap.wearable.support.recycler.widget.DividerItemDecoration;
import com.heytap.wearable.support.recycler.widget.GridLayoutManager;
import com.heytap.wearable.support.recycler.widget.RecyclerView;
import com.wtach.stationremind.BaseActivity;
import com.wtach.stationremind.R;
import com.wtach.stationremind.RecognizerObserver;
import com.wtach.stationremind.adapter.CustomAdapter;
import com.wtach.stationremind.listener.OnRecyItemClickListener;
import com.wtach.stationremind.model.item.bean.CollectNameInfo;
import com.wtach.stationremind.object.CollectInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.core.content.ContextCompat;

/**
 * description:自定义dialog
 */

public class RecyclerDialog extends Dialog implements OnRecyItemClickListener {
    private final static String TAG = "RecyclerDialog";
    /**
     * 显示的标题
     */
    private TextView titleTv ;

    private RecyclerView mRecyclerView;
    private CustomAdapter mCustomAdapter;
    private BaseActivity.NameCallBack mNameCallBack;
    public RecyclerDialog(Context context) {
        super(context, R.style.CustomDialog);
    }

    private String title;
    private RecyclerDialog dialog;

    public void showRecognizeDialog(final RecyclerDialog dialog, final BaseActivity.NameCallBack nameCallBack) {
        if(dialog != null){
            dialog.dismiss();
        }
        Log.d(TAG,"showRecognizeDialog");
        this.dialog = dialog;
        mNameCallBack = nameCallBack;
        dialog.setTitle(R.string.collect_name_title);
        dialog.setTitle(getContext().getString(R.string.collect_name_title));
        dialog.show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_dialog_layout);
        //按空白处不能取消动画
        //setCanceledOnTouchOutside(false);
        //初始化界面控件
        initView();
        //初始化界面数据
        refreshView();

        initRecycler();
    }

    private void initRecycler() {
        GridLayoutManager favoritelayoutManager = new GridLayoutManager( getContext(),1);
        mRecyclerView.setLayoutManager(favoritelayoutManager);
        DividerItemDecoration divider = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.custom_divider));
        mRecyclerView.addItemDecoration(divider);
        mRecyclerView.setAdapter(mCustomAdapter);
    }

    private CustomAdapter initAdapter(List<Object> list, RecyclerView recyclerView) {
        CustomAdapter adapter = new CustomAdapter(list);
        adapter.setOnRecyItemClickListener(this);
        recyclerView.setAdapter(adapter);
        return adapter;
    }

    /**
     * 初始化界面控件的显示数据
     */
    private void refreshView() {
        titleTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        //如果用户自定了title和message
        if (!TextUtils.isEmpty(title)) {
            titleTv.setText(title);
            titleTv.setVisibility(View.VISIBLE);
        }else {
            titleTv.setVisibility(View.GONE);
        }
        String[] nameList = getContext().getResources().getStringArray(R.array.name_collect_array);
        List<Object> list = new ArrayList<>();
        for(String string : nameList){
            list.add(new CollectNameInfo(string));
        }

        mCustomAdapter = initAdapter(list,mRecyclerView);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void show() {
        super.show();
        refreshView();
    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        titleTv = (TextView) findViewById(R.id.title);
        mRecyclerView = findViewById(R.id.recyler);
    }

    @Override
    public void onItemClick(View view, int position) {
        if (mNameCallBack != null){
            CollectNameInfo collectNameInfo = (CollectNameInfo) mCustomAdapter.getDataIndex(position);
            if(collectNameInfo != null) {
                String name = collectNameInfo.getName();
                if (!name.equals(getContext().getString(R.string.new_collect_nema))) {
                    mNameCallBack.nameComplete(collectNameInfo.getName());
                } else {
                    RecognizerObserver.getInstance(getContext()).showRecognizeDialog(getContext(), mNameCallBack);
                }
            }
        }
        dismiss();
    }

    @Override
    public void onItemDelete(View view, int position) {

    }

    public String getTitle() {
        return title;
    }

    public RecyclerDialog setTitle(String title) {
        this.title = title;
        return this ;
    }

}
