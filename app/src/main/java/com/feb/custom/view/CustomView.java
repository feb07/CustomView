package com.feb.custom.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;

import java.util.TimeZone;


/**
 * Created by lilichun on 18/7/28.
 */

public class CustomView extends View {

    private Time time;

    //图片
    private Drawable bgDrawable;
    private Drawable hourDrawable;
    private Drawable minDrawable;

    //尺寸
    private int bgWidth;
    private int bgHeight;

    private boolean mAttached;

    //看名字
    private float mMinutes;
    private float mHour;

    //用来跟踪我们的View 的尺寸的变化，
//当发生尺寸变化时，我们在绘制自己
//时要进行适当的缩放。
    private boolean mChanged;


    public CustomView(Context context) {
        this(context, null);

    }

    public CustomView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);

    }

    public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        //初始化图片信息
        if (bgDrawable == null) {
            bgDrawable = context.getDrawable(R.drawable.bg);
        }
        if (hourDrawable == null) {
            hourDrawable = context.getDrawable(R.drawable.hour);
        }
        if (minDrawable == null) {
            minDrawable =
                    context.getDrawable(R.drawable.min);
        }

        time = new Time();

        bgWidth = bgDrawable.getIntrinsicWidth();
        bgHeight = bgDrawable.getIntrinsicHeight();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        float hScale = 1.0f;
        float vScale = 1.0f;

        if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < bgWidth) {
            hScale = (float) widthSize / (float) bgWidth;
        }
        if (heightMode != MeasureSpec.UNSPECIFIED && heightSize < bgHeight) {
            vScale = (float) heightSize / (float) bgHeight;
        }
        float scale = Math.min(hScale, vScale);
        setMeasuredDimension(
                resolveSizeAndState((int) (bgWidth * scale), widthMeasureSpec, 0),
                resolveSizeAndState((int) (bgHeight * scale), heightMeasureSpec, 0)
        );


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mChanged = true;
    }

    private void onTimeChanged() {
        time.setToNow();

        int hour = time.hour;
        int minute = time.minute;
        int second = time.second;
        /*这里我们为什么不直接把minute设置给mMinutes，而是要加上
            second /60.0f呢，这个值不是应该一直为0吗？
            这里又涉及到Calendar的 一个知识点，
            也就是它可以是Linient模式，
            此模式下，second和minute是可能超过60和24的，具体这里就不展开了，
            如果不是很清楚，建议看看Google的官方文档中讲Calendar的部分*/
        mMinutes = minute + second / 60.0f;
        mHour = hour + mMinutes / 60.0f;
        mChanged = true;
    }


    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //这个if判断主要是用来在时区发生变化时，更新mCalendar的时区的，这
            //样，我们的自定义View在全球都可以使用了。
            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                String tz = intent.getStringExtra("time-zone");
                time = new Time(TimeZone.getTimeZone(tz).getID());
            }
            //进行时间的更新
            onTimeChanged();
            //invalidate当然是用来引发重绘了。
            invalidate();
        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mAttached) {
            mAttached = true;
            IntentFilter filter = new IntentFilter();
            //这里确定我们要监听的三种系统广播
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            getContext().registerReceiver(mIntentReceiver, filter);
        }

        time = new Time();
        onTimeChanged();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            getContext().unregisterReceiver(mIntentReceiver);
            mAttached = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //View尺寸变化后，我们用changed变量记录下来，
        //同时，恢复mChanged为false，以便继续监听View的尺寸变化。
        boolean changed = mChanged;
        if (changed) {
            mChanged = false;
        }

        int availableWidth = getRight() - getLeft();
        int availableHeight = getBottom() - getTop();

        int x = availableWidth / 2;
        int y = availableHeight / 2;
//画背景
        final Drawable drawable = bgDrawable;
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        boolean scaled = false;

        /*如果可用的宽高小于表盘图片的宽高，
           就要进行缩放，不过这里，我们是通过坐标系的缩放来实现的。
          而且，这个缩放效果影响是全局的，
          也就是下面绘制的表盘、时针、分针都会受到缩放的影响。*/
        if (availableWidth < w || availableHeight < h) {
            scaled = true;
            float scale = Math.min((float) availableWidth / (float) w,
                    (float) availableHeight / (float) h);
            canvas.save();
            canvas.scale(scale, scale, x, y);
        }
        /*如果尺寸发生变化，我们要重新为表盘设置Bounds。
           这里的Bounds就相当于是为Drawable在View中确定位置，
           只是确定的方式更直接，直接在View中框出一个与Drawable大小
           相同的矩形，
           Drawable就在这个矩形里绘制自己。
           这里框出的矩形，是以(x,y)为中心的，宽高等于表盘图片的宽高的一个矩形，
           不用担心表盘图片太大绘制不完整，
            因为我们已经提前进行了缩放了。*/
        if (changed) {
            drawable.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        drawable.draw(canvas);

        canvas.save();

//接下来画时针
        /*根据小时数，以点(x,y)为中心旋转坐标系。
            */

        canvas.rotate(mHour / 12.0f * 360.0f, x, y);
        final Drawable hourHand = hourDrawable;

        //同样，根据变化重新设置时针的Bounds
        if (changed) {
            w = hourHand.getIntrinsicWidth();
            h = hourHand.getIntrinsicHeight();

            /* 仔细体会这里设置的Bounds，我们所画出的矩形，
                同样是以(x,y)为中心的
                矩形，时针图片放入该矩形后，时针的根部刚好在点(x,y)处，
                因为我们之前做时针图片时，
                已经让图片中的时针根部在图片的中心位置了，
                虽然，看起来浪费了一部分图片空间（就是时针下半部分是空白的），
                但却换来了建模的简单性，还是很值的。*/
            hourHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        hourHand.draw(canvas);
        canvas.restore();

        canvas.save();

        //根据分针旋转坐标系
        canvas.rotate(mMinutes / 60.0f * 360.0f, x, y);
        final Drawable minuteHand = minDrawable;

        if (changed) {
            w = minuteHand.getIntrinsicWidth();
            h = minuteHand.getIntrinsicHeight();
            minuteHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        minuteHand.draw(canvas);
        canvas.restore();
        //最后，我们把缩放的坐标系复原。
        if (scaled) {
            canvas.restore();
        }


    }
}
