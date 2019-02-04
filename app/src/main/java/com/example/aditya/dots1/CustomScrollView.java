package com.example.aditya.dots1;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class CustomScrollView extends ScrollView {

    private boolean enableScrolling = true;

    public boolean isEnableScrolling(){
        return enableScrolling;
    }

    public void setEnableScrolling(boolean enableScrolling){
        this.enableScrolling = enableScrolling;
    }

    public CustomScrollView(Context context) {
        super(context);
    }

    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(isEnableScrolling()){
            return super.onInterceptTouchEvent(ev);
        }
        else {
            return false;
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(isEnableScrolling()) {
            return super.onTouchEvent(ev);
        }
        else {
            return false;
        }
    }
}
