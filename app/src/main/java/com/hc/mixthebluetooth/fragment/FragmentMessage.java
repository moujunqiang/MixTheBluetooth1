package com.hc.mixthebluetooth.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hc.basiclibrary.ioc.OnClick;
import com.hc.basiclibrary.ioc.ViewById;
import com.hc.basiclibrary.ioc.ViewByIds;
import com.hc.basiclibrary.titleBasic.DefaultNavigationBar;
import com.hc.basiclibrary.viewBasic.BasFragment;
import com.hc.bluetoothlibrary.DeviceModule;
import com.hc.mixthebluetooth.R;
import com.hc.mixthebluetooth.activity.AlarmActivity;
import com.hc.mixthebluetooth.activity.CommunicationActivity;
import com.hc.mixthebluetooth.activity.tool.Analysis;
import com.hc.mixthebluetooth.customView.CheckBoxSample;
import com.hc.mixthebluetooth.customView.PopWindowFragment;
import com.hc.mixthebluetooth.recyclerData.FragmentMessAdapter;
import com.hc.mixthebluetooth.recyclerData.itemHolder.FragmentMessageItem;
import com.hc.mixthebluetooth.storage.Storage;

import java.util.ArrayList;
import java.util.List;

public class FragmentMessage extends BasFragment {


    @ViewByIds(value = {R.id.pull_message_fragment}, name = {"mPullBT"})
    private ImageView mPullBT;

    @ViewByIds(value = {R.id.size_read_message_fragment, R.id.size_send_message_fragment, R.id.size_unsent_message_fragment}
            , name = {"mReadNumberTV", "mSendNumberTv", "mUnsentNumberTv"})
    private TextView mReadNumberTV, mSendNumberTv, mUnsentNumberTv;

    @ViewByIds(value = {R.id.read_hint_message_fragment, R.id.unsent_hint_message_fragment}, name = {"mReadingHint", "mUnsentHint"})
    private LinearLayout mReadingHint, mUnsentHint;


    private DefaultNavigationBar mTitle;//activity的头部

    private Runnable mRunnable;//循环发送的线程

    private Handler mHandler;

    private FragmentMessAdapter mAdapter;

    private List<FragmentMessageItem> mDataList = new ArrayList<>();

    private DeviceModule module;

    private Storage mStorage;

    private int mUnsentNumber = 0;

    private boolean isShowMyData;
    private boolean isShowTime;

    private boolean isReadHex;

    @ViewById(R.id.tv_ir)
    private TextView tvIr;
    @ViewById(R.id.tv_avg)
    private TextView tvAvg;
    @ViewById(R.id.tv_bmp)
    private TextView tvBMp;
    private CountDownTimer timer;

    @Override
    public int setFragmentViewId() {
        return R.layout.fragment_message;
    }

    @Override
    public void initAll() {
    }

    @Override
    public void initAll(View view, Context context) {
        mStorage = new Storage(context);
        setListState();
        super.initAll(view, context);
    }

    @Override
    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    @Override
    public void updateState(int state) {

        switch (state) {
            case CommunicationActivity.FRAGMENT_STATE_1:
                mReadingHint.setVisibility(View.VISIBLE);
                break;
            case CommunicationActivity.FRAGMENT_STATE_2:
                mReadingHint.setVisibility(View.GONE);
                break;
        }
    }

    private boolean isShowAlarm = false;

    @Override
    public void readData(int state, Object o, final byte[] data) {
        switch (state) {
            case CommunicationActivity.FRAGMENT_STATE_DATA:
                if (module == null) {
                    module = (DeviceModule) o;
                }
                if (data != null) {
                    timer = new CountDownTimer(Integer.MAX_VALUE, 2000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            String byteToString = Analysis.getByteToString(data, isReadHex);
                            String[] split = byteToString.split(",");
                            String ir = split[0].split("=")[1];
                            String bmp = split[1].split("=")[1];
                            String avgBmp = split[2].split("=")[1];

                            tvIr.setText("血氧：" + ir);
                            tvBMp.setText("平均心率：" + avgBmp);
                            double i = Double.parseDouble(ir);//血氧
                            double i1 = Double.parseDouble(bmp);//心率
                            double i2 = Double.parseDouble(avgBmp);//平均心率
                            if (i > 70 || i2 < 60 || i2 > 110) {
                                if (isShowAlarm) {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            startActivity(new Intent(getContext(), AlarmActivity.class));
                                        }
                                    }, 10000);
                                }

                            }
                        }

                        @Override
                        public void onFinish() {

                        }
                    };
                    timer.start();

                }
                break;
            case CommunicationActivity.FRAGMENT_STATE_NUMBER:
                mSendNumberTv.setText(String.valueOf(Integer.parseInt(mSendNumberTv.getText().toString()) + ((int) o)));
                setUnsentNumberTv();
                break;
            case CommunicationActivity.FRAGMENT_STATE_SEND_SEND_TITLE:
                mTitle = (DefaultNavigationBar) o;
                break;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isShowAlarm = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isShowAlarm = false;
    }

    @OnClick({R.id.pull_message_fragment})
    private void onClick(View view) {
        switch (view.getId()) {


            case R.id.pull_message_fragment:
                mPullBT.setImageResource(R.drawable.pull_up);
                new PopWindowFragment(view, getActivity(), new PopWindowFragment.DismissListener() {
                    @Override
                    public void onDismissListener() {
                        mPullBT.setImageResource(R.drawable.pull_down);
                        setListState();
                    }

                    @Override
                    public void clearRecycler() {
                        mDataList.clear();
                        mReadNumberTV.setText(String.valueOf(0));
                        mSendNumberTv.setText(String.valueOf(0));
                        mUnsentNumber = 0;
                        mAdapter.notifyDataSetChanged();
                    }
                });
                break;


        }
    }


    private void setListState() {
        isShowMyData = mStorage.getData(PopWindowFragment.KEY_DATA);
        isShowTime = mStorage.getData(PopWindowFragment.KEY_TIME);
        isReadHex = mStorage.getData(PopWindowFragment.KEY_HEX_READ);

    }

    private void setUnsentNumberTv() {
        int number = Integer.parseInt(mSendNumberTv.getText().toString());
        if ((mUnsentNumber - number) > 2000) {
            if (mUnsentHint.getVisibility() == View.GONE)
                mUnsentHint.setVisibility(View.VISIBLE);
        } else if ((mUnsentNumber - number) <= 0) {
            if (mUnsentHint.getVisibility() == View.VISIBLE)
                mUnsentHint.setVisibility(View.GONE);
        }
        if (mUnsentHint.getVisibility() == View.VISIBLE)
            mUnsentNumberTv.setText(String.valueOf(mUnsentNumber - number));
    }


}
