# CustomView
自定义view

### 前言
详解可见简书：https://www.jianshu.com/p/e7150e27588d
网上有很多自定义View的说明的文章，首先通过以下问题，测试自己是否掌握自定义View：

1、Google提出View这个概念的目的是什么？

2、View这个概念与Activtiy、Fragment以及Drawable之间是一种什么样的关系？

3、View能够感知Activity的生命周期事件吗？为什么？

4、View的生命周期是什么？

5、当View所在的Activity进入stop状态后，View去哪了？如果我在一个后台线程中持有一个View的引用，我此时能够改变它的状态吗？为什么？

6、View能够与其他的View交叉重叠吗？重叠区域发生的点击事件交给谁去处理呢？可不可以重叠的两个View都处理？

7、View控制一个Drawable的方法途径有哪些？Drawable能不能与View通信？如果能如何通信？

8、假如View所在的ViewGroup中的子View减少了，View因此获得了更大的空间，View如何及时有效地利用这些空间，改变自己的绘制？

9、假如我要在View中动态地注册与解除广播接收器，应该在哪里完成呢？

10、假如我的手机带键盘（自带或者外接），你的自定义View应该如何响应键盘事件。

11、AnimationDrawable作为View的背景，会自动进行动画，View在其中扮演了怎样的角色？

假如以上问题你都能准确地回答出来，那你的自定义View已经学到家了。

好了，说了这么多，到底怎样才能学好自定义View？其实只需掌握三个问题，就可以轻松搞定它：

问题一：从Android系统设计者的角度，View这个概念究竟是做什么的？

问题二：Android系统中那个View类，它有哪些默认功能和行为，能干什么，不能干什么？（知己知彼，才好自定义！）

问题三：我要改变这个View的行为，外观，肯定是覆写View类中的方法，但是怎么覆写，覆写哪些方法能够改变哪些行为？

### View的说明
看官方文档给出的对于View的描述


能够看出几点：1、view是用户界面的基础构建区块。2、view在屏幕上占一个矩形区域，用来绘制以及事件响应。

View.class的功能，行为
看几个问题，带着问题去思考学习：

View是怎样被显示到屏幕上的？

View在屏幕上的位置是怎样决定的？

View所占据的矩形大小是怎样决定的？

屏幕上肯定不止一个View，View之间互相知道对方吗？它们之间能协作吗？



一个UI上有很多view，有基础view，也有复合view。所有的view是怎么组合起来的呢？Google是这么解决的：用Window来展示用户界面，Window加载一个DecorView，用DecorView来包含所有的其他的view。

#### 1、确定view的位置
我们在activity中setContentView，实际上就是将用户界面的所有的View交给了DecorView中的一个FrameLayout，这个FrameLayou代表着可以分配给用户界面使用的屏幕区域。更常见的情况是，用户界面是一个ViewGroup，里面包含了其他ViewGroup和View。

开发者在使用view的时候，向ViewGroup说明想把view放在什么位置，以LinearLayout，vertical为例，在写布局文件时，子View在LinearLayout中的出现顺序将决定它们在屏幕上的上下顺序，同时还可以借助layout_margin ,layout_gravity等配置进一步调整子View在分给自己的矩形区域中的位置。layout_*虽然是跟view的属性写在一起，但是其实并不是view的属性。这些值在Inflate时，是由ViewGroup读取，然后生成一个ViewGroup特定的LayoutParams对象，再把这个对象存入子View中的，这样，ViewGroup在为该子View安排位置时，就可以参考这个LayoutParams中的信息了。

#### 2、确定View大小
第一步，开发者在书写布局文件时，会为一个View写上android:layout_width="***"android:layout_height="***"两个配置，这是开发者向ViewGroup表达的，我这个View需要的大小是多少。星号的取值有三种：

具体值，如50dp，很简单，不多讲

match_parent ，表示开发者向ViewGroup说，把你所有的屏幕区域都给这个View吧。

wrap_parent，表示开发者向ViewGroup说，只要给这个View够他展示自己的空间就行，至于到底给多少，你直接跟View沟通吧，看它怎么说。

第二步，ViewGroup收到了开发者对View大小的说明，然后ViewGroup会综合考虑自己的空间大小以及开发者的请求，然后生成两个MeasureSpec对象（width与height）传给View，这两个对象是ViewGroup向子View提出的要求。然后，这两个对象将会传到子View的protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)方法中。子View再看ViewGroup的要求，于是，它从传入的两个对象中解译出如下信息：

Mode与Size一起，准确表达出了ViewGroup的要求。


#### Mode的取值有三种，它们代表了ViewGroup的要求条件：

1、MeasureSpec.EXACTLY，精确模式

在这种模式下，尺寸的值是多少，那么这个组件的长或宽就是多少。

2、MeasureSpec.AT_MOST，最大模式

这个也就是父组件，能够给出的最大的空间，当前组件的长或宽最大只能为这么大，当然也可以比这个小。

3、MeasureSpec.UNSPECIFIED，未指定模式

这个就是说，当前组件，可以随便用空间，不受限制。

如果子view不想遵守ViewGroup的要求怎么办？就是子view一定要设置自己考虑后的尺寸，如果不设置就相当于没有告诉ViewGroup自己想要的大小，这会导致ViewGroup无法正常工作，设置的办法就是在onMeasure方法的最后，调用setMeasuredDimension方法。




### 3、view的绘制
主要就是onDraw方法，主要执行几步：

绘制背景;

通过onDraw()绘制自身内容;

通过dispatchDraw()绘制子View;

绘制滚动条

### 4、改变view的行为，显示外观
肯定是要重载View.class中的方法，来看官方怎么说的








从上可以看出view的生命周期。View被inflated出来后，系统会回调该View的onFinishInflate方法。其他方法可以自己看文档。

### 5、实战
代码可见：https://github.com/feb07/CustomView

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


