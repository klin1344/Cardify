package com.klin1344.cardify;

import android.content.Context;
import android.widget.Checkable;
import android.widget.FrameLayout;

public class CheckableLayout extends FrameLayout implements Checkable {
    private boolean mChecked;

    public CheckableLayout(Context context) {
        super(context);
    }

    @SuppressWarnings("deprecation")
    public void setChecked(boolean checked) {
        mChecked = checked;
        setBackgroundDrawable(checked ? getResources().getDrawable(R.color.HoloBlue) : null);
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void toggle() {
        setChecked(!mChecked);
    }

}