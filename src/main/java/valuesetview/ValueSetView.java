package valuesetview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import online.ruiheng.valuesetview.R;

public class ValueSetView extends View {
    static final int MIN_CURSOR=1;
    static final int MAX_CURSOR=0;
    static  final int UP=0;
    static  final int DOWN=1;
    private Paint selectedline_paint;                     //选择区域画笔样式
    private Paint unselectedline_paint;                   //未选择区域 画笔样式
    private Paint curser_paint;                          //游标图形 画笔 样式
    private Paint minnumber_paint;                          //数值画笔样式
    private Paint maxnumber_paint;                          //数值画笔样式
    private Point min_cursor_point;                       //上游标顶点位置
    private Point max_cursor_point;                    //下游标定点位置
    private int min_cursor_status=UP;                     //小值游标状态，大小游标重合时自动跳到滑杆上面
    private int view_height;                             //控件高度
    private int view_width;                              //控件宽度
    private int line_height;                             //滑杆高度
    private int line_width;                              //滑杆的宽度
    private int line_toleft_width;                       //滑杆致左侧留白宽度
    private int line_toright_width;                      //花噶至右侧留白
    private int line_totop_width;                        //滑杆至右侧留白宽度
    private int selected_cursor=3;                       //当前选中的游标
    private int maxNumber;                           //最大值
    private int minNumber;                             //最小值
    private int currentLargeValue;                       //当前大值
    private int currentSmallValue;                       //当前小值
    private int mainColor;
    public ValueSetView(Context context) {
        super(context);
    }

    public ValueSetView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ValueSetView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ValueSetView);
        mainColor=a.getColor(R.styleable.ValueSetView_mainColor,Color.parseColor("#047120"));
        maxNumber=a.getInt(R.styleable.ValueSetView_maxvalue,100);
        minNumber=a.getInt(R.styleable.ValueSetView_minvalue,0);
        currentLargeValue=maxNumber;
        currentSmallValue=minNumber;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取控件的宽高、中线位置、起始点、起始数值
        view_height = MeasureSpec.getSize(heightMeasureSpec);//获取总高度,是包含padding值
        view_width = MeasureSpec.getSize(widthMeasureSpec);//获取总宽度,是包含padding值
        initData();
        init();
    }
    private void initData()
    {
        line_toleft_width=50;
        line_toright_width=line_toleft_width;
        line_totop_width=view_height/2;
        line_width=view_width-line_toleft_width-line_toright_width;
        line_height=15;
        min_cursor_point=new Point(line_toleft_width,line_totop_width-line_height/2);
        max_cursor_point=new Point(line_toleft_width,line_totop_width+line_height/2);
    }
    private void init()
    {
        //绘制选中滑杆部分
        selectedline_paint = new Paint();
        selectedline_paint.setColor(mainColor);
        selectedline_paint.setStrokeWidth(line_height);
        selectedline_paint.setAntiAlias(true);
        //绘制非选中滑杆部分
        unselectedline_paint=new Paint();
        unselectedline_paint.setColor(mainColor);
        unselectedline_paint.setAlpha(100);
        unselectedline_paint.setStrokeWidth(line_height);
        unselectedline_paint.setAntiAlias(true);
        //绘制游标图形 画笔
        curser_paint = new Paint();
        curser_paint.setColor(mainColor);
        curser_paint.setAntiAlias(true);
        //绘制当前数值
        maxnumber_paint=new Paint();
        maxnumber_paint.setColor(mainColor);
        maxnumber_paint.setTextSize(40);
        maxnumber_paint.setAntiAlias(true);
        minnumber_paint=new Paint();
        minnumber_paint.setColor(mainColor);
        minnumber_paint.setTextSize(40);
        minnumber_paint.setAntiAlias(true);
    }


    /*定义滑动事件*/
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //如果点击的点在第一个圆内就是起点,并且可以滑动
                if (isInCursor(event.getX(),event.getY())==MIN_CURSOR) {
                    selected_cursor=MIN_CURSOR;
                    //如果点击的点在第二个圆内就是终点,并且可以滑动
                } else if (isInCursor(event.getX(),event.getY())==MAX_CURSOR) {
                    selected_cursor=MAX_CURSOR;
                } else {
                    //如果触控点不在圆环内，则不能滑动
                    selected_cursor=-1;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (selected_cursor!=-1) {
                    if (selected_cursor==MIN_CURSOR) {
                        //起点滑动时，重置起点的位置和进度值
                       min_cursor_point=new Point((int)(event.getX()),line_totop_width-line_height/2);
                        if(!isCoincide())
                            min_cursor_point.y=line_totop_width+line_height/2;
                        else
                            min_cursor_point.y= line_totop_width-line_height/2;
                        if(min_cursor_point.x<line_toleft_width)
                            min_cursor_point.x=line_toleft_width;
                        if(min_cursor_point.x>line_toleft_width+line_width)
                            min_cursor_point.x=line_toleft_width+line_width;
                       if(min_cursor_point.x>max_cursor_point.x)
                       {
                           max_cursor_point.x = min_cursor_point.x;
                           currentLargeValue=getValue(max_cursor_point.x);
                       }

                        currentSmallValue=getValue(min_cursor_point.x);

                    } else {
                        //始点滑动时，重置始点的位置和进度值
                        max_cursor_point=new Point((int)(event.getX()),line_totop_width+line_height/2);
                        if(!isCoincide())
                            min_cursor_point.y=line_totop_width+line_height/2;
                        else
                            min_cursor_point.y= line_totop_width-line_height/2;
                        if(max_cursor_point.x-min_cursor_point.x<50)
                        if(max_cursor_point.x<line_toleft_width)
                            max_cursor_point.x=line_toleft_width;
                        if(max_cursor_point.x>line_toleft_width+line_width)
                            max_cursor_point.x=line_toleft_width+line_width;
                        if(min_cursor_point.x>max_cursor_point.x)
                        {
                            min_cursor_point.x=max_cursor_point.x ;
                            currentSmallValue=getValue(min_cursor_point.x);
                        }

                        currentLargeValue=getValue(max_cursor_point.x);
                    }

                    invalidate();
                }

                break;
            case MotionEvent.ACTION_UP:
                selected_cursor=-1;
                invalidate();
                break;
        }

        return true;
    }

    /*判断触摸位置 是否是游标位置，在哪个游标上*/
    private int isInCursor(float X,float Y)
    {
        if(X>min_cursor_point.x-35&&X<min_cursor_point.x+35&&Y<min_cursor_point.y&&Y>min_cursor_point.y-60&&min_cursor_status==UP)
            return MIN_CURSOR;
        else if(X>min_cursor_point.x-35&&X<min_cursor_point.x+35&&Y>min_cursor_point.y&&Y<min_cursor_point.y+60&&min_cursor_status==DOWN)
            return MIN_CURSOR;
        else if(X>max_cursor_point.x-35&&X<max_cursor_point.x+35&&Y>max_cursor_point.y&&Y<max_cursor_point.y+60)
            return MAX_CURSOR;
        else return -1;
    }

    /*绘制刻度值，触摸显示，非触摸状态消失*/
    private void drawValue(Canvas canvas )
    {
        if(min_cursor_status==DOWN) {
            canvas.drawText(currentLargeValue + "", max_cursor_point.x +textShiftwhendown(currentLargeValue), line_totop_width - 20, maxnumber_paint);
            canvas.drawText(currentSmallValue + "", min_cursor_point.x +textShiftwhendown(currentSmallValue), line_totop_width - 20, minnumber_paint);
        }
        else if(min_cursor_status==UP)
        {
            canvas.drawText(currentLargeValue + "", max_cursor_point.x +textshiftwhenup(currentLargeValue), line_totop_width - 20, maxnumber_paint);
            canvas.drawText(currentSmallValue + "", min_cursor_point.x +textshiftwhenup(currentSmallValue), line_totop_width - 20, minnumber_paint);
        }

    }
    private int textShiftwhendown(int value)  //当游标在下时，根据数值位数调整位移，始终保持数值字体居中
    {
        int shift;
            if (value < 10)
                shift = -10;
            else if (value > 9 && value < 100)
                shift = -20;
            else
                shift = -30;
        return shift;
    }
    private int textshiftwhenup(int value) //当游标在下时，根据数值调整位移，使数值与游标保持适当距离
    {
        int shift;
        if(value==currentSmallValue)
        {
            if (value < 10)
                shift = -50;
            else if (value > 9 && value < 100)
                shift = -70;
            else
                shift = -90;
        }
        else
        {

                shift = 30;
        }
        return shift;
    }
    /*绘制游标图形*/
    private void drawCursor(Canvas canvas)
    {

            canvas.drawPath(doDrawCorser(min_cursor_point, MIN_CURSOR), curser_paint);
            canvas.drawPath(doDrawCorser(max_cursor_point, MAX_CURSOR), curser_paint);

    }
    private Path doDrawCorser(Point point,int TorB)
    {
        if(TorB==MIN_CURSOR) {
            Path path = new Path();
            if(isCoincide()) {
                min_cursor_status=UP;
                path.moveTo(point.x, point.y);
                path.lineTo(point.x + 17, point.y - 17);
                path.lineTo(point.x + 17, point.y - 45);
                path.lineTo(point.x - 17, point.y - 45);
                path.lineTo(point.x - 17, point.y - 17);
                path.lineTo(point.x, point.y);
            }
            else
            {
                min_cursor_status=DOWN;
                path.moveTo(point.x, point.y);
                path.lineTo(point.x + 17, point.y + 17);
                path.lineTo(point.x + 17, point.y + 45);
                path.lineTo(point.x - 17, point.y+ 45);
                path.lineTo(point.x - 17, point.y+ 17);
                path.lineTo(point.x, point.y);
            }
            return path;
        }
        else
        {
           if(isCoincide())
               min_cursor_status=UP;
           else
               min_cursor_status=DOWN;
            Path path = new Path();
            path.moveTo(point.x, point.y);
            path.lineTo(point.x + 20, point.y + 20);
            path.lineTo(point.x + 20, point.y + 50);
            path.lineTo(point.x - 20, point.y + 50);
            path.lineTo(point.x - 20, point.y + 20);
            path.lineTo(point.x, point.y);
            return path;
        }
    }
    /*判断游标是否重合*/
    private boolean  isCoincide()
    {
        if((max_cursor_point.x-min_cursor_point.x)<70)
            return true;
        else return false;
    }
    /*计算游标位置的值*/
    private int getValue(int x)
    {
        int value=0;
        value=(((x-line_toleft_width)*(maxNumber-minNumber)/line_width))+minNumber;
        return value;
    }
    /*重写ViewonDraw方法*/
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvas.drawLine(line_toleft_width, line_totop_width,line_toleft_width+line_width,line_totop_width, unselectedline_paint);
        canvas.drawLine(min_cursor_point.x,line_totop_width,max_cursor_point.x,line_totop_width,selectedline_paint);
        drawCursor(canvas);
        drawValue(canvas);
    }
    /*设置最大(小)值*/
    public void setMaxValue(int value)
    {
        this.maxNumber=value;
    }
    public void setMinValue(int value)
    {
        this.minNumber=value;
    }
    /*获取当前值*/
    public int getLargeValue()
    {
     return currentLargeValue;
    }
    public int getSmallValue()
    {
        return currentSmallValue;
    }

}
