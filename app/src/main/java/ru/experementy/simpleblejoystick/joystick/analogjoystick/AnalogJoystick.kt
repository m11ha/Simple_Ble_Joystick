package ru.experementy.simpleblejoystick.joystick.analogjoystick

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import ru.experementy.customview.R
import java.lang.Math.toDegrees
import kotlin.math.pow
import kotlin.math.sqrt

class AnalogJoystick(ctx: Context, attrs: AttributeSet): View(ctx, attrs) ,Runnable{
    //Paint values
    private var pJButton = Paint()
    private var pJBorder = Paint()
    private var pJBackground= Paint()

    private var btnColor: Int
    private var brdColor: Int
    private var bkgrColor: Int

    var isEnable=false

// Joystick size

    private var butRadius: Float=0f
    private var brdRadius: Float=0f
    private var bkgrRadius: Float=0f

    private var butMultipler: Float=1f
    private var bkgrdMultipler: Float=1f

    private var centerX: Int=0
    private var centerY: Int=0

    private var butX: Int=0
    private var butY: Int=0

    private var mThread: Thread?=null

    companion object{
        //Default paint values
        const val DEF_BTN_COLOR= Color.BLACK
        const val DEF_BRD_COLOR= Color.BLACK
        const val DEF_BKGR_COLOR= Color.WHITE
        //Default size
        const val DEFAULT_SIZE=200

        const val DEFAULT_BUT_MULTIPLER=0.25f
        const val DEFAULT_BKGRD_MULTIPLER=0.75f
    }

    interface JoystickListener{
        fun onPush(angle: Int, srength: Int)
    }
    private var joystickListener: JoystickListener?=null

    fun setJoystickListener(listener: JoystickListener){
        joystickListener=listener
    }

    init {
        ctx.theme.obtainStyledAttributes(attrs, R.styleable.myJoistik,
                0, 0).apply {
            try {
                brdColor=getColor(R.styleable.myJoistik_butColor, DEF_BRD_COLOR)
                btnColor=getColor(R.styleable.myJoistik_butColor, DEF_BTN_COLOR)
                bkgrColor=getColor(R.styleable.myJoistik_bkgrColor, DEF_BKGR_COLOR)

                butMultipler=getFraction(R.styleable.myJoistik_butSizeMultipler, 1, 1, DEFAULT_BUT_MULTIPLER)
                bkgrdMultipler=getFraction(R.styleable.myJoistik_bkgrdSizeMultipler, 1, 1, DEFAULT_BKGRD_MULTIPLER)
                isEnable=getBoolean(R.styleable.myJoistik_enable,false)
            } finally {
                recycle()
            }
            pJButton.style= Paint.Style.FILL
            pJButton.color=btnColor

            pJBorder.style= Paint.Style.STROKE
            pJBorder.color=brdColor
            pJBorder.strokeWidth=5f

            pJBackground.style= Paint.Style.FILL
            pJBackground.color=bkgrColor

        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX=w/2
        centerY=h/2
        butX=centerX
        butY=centerY
        val d= minOf(w, h).toFloat()
        butRadius=d/2*butMultipler
        brdRadius=d/2*bkgrdMultipler
        bkgrRadius=brdRadius-pJBorder.strokeWidth/2

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w=if(MeasureSpec.getMode(widthMeasureSpec)== MeasureSpec.UNSPECIFIED)
            DEFAULT_SIZE
        else
            MeasureSpec.getSize(widthMeasureSpec)
        val h=if(MeasureSpec.getMode(heightMeasureSpec)== MeasureSpec.UNSPECIFIED)
            DEFAULT_SIZE
        else
            MeasureSpec.getSize(heightMeasureSpec)
        val d=minOf(w, h)
        setMeasuredDimension(d, d)

    }



    override fun onDraw(canvas: Canvas?) {
        canvas?.drawCircle(centerX.toFloat(), centerY.toFloat(), brdRadius, pJBorder)
        canvas?.drawCircle(centerX.toFloat(), centerY.toFloat(), bkgrRadius, pJBackground)
        canvas?.drawCircle(butX.toFloat(), butY.toFloat(), butRadius, pJButton)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        //Выход, если джойстик недоступен
        if(!isEnable) return true

        val x=event?.x?.toInt()
        val y= event?.y?.toInt()
        if(x==null ||y==null) return false
        val dx=(x-centerX).toDouble()
        val dy=(y-centerY).toDouble()

        val abs=sqrt(dx.pow(2) + dy.pow(2))


        butY=if(abs<brdRadius.toDouble()) y else {
            ((dy*brdRadius/abs)+centerY.toDouble()).toInt()
        }
        butX=if(abs<brdRadius.toInt()) x else {
            ((dx*brdRadius/abs)+centerX.toDouble()).toInt()
        }


        when(event.action){
            MotionEvent.ACTION_UP -> {
                mThread?.interrupt()
                butX = centerX
                butY = centerY
                joystickListener?.onPush(getAngle(), getStrength())

            }
            MotionEvent.ACTION_DOWN -> {

                 if(mThread?.isAlive==true && mThread!=null){
                    mThread?.interrupt()
                }
                mThread= Thread(this)
                mThread?.start()
                joystickListener?.onPush(getAngle(), getStrength())

            }

        }

        invalidate()

       // return super.onTouchEvent(event)
        return true
    }

    private fun getAngle(): Int {
        var angle=toDegrees(kotlin.math.atan2((centerY - butY).toDouble(), (butX - centerX).toDouble()))
        if(angle<0)angle+=360
        return angle.toInt()
    }

    private fun getStrength(): Int{
        val dx=(butX-centerX).toDouble()
        val dy=(centerY-butY).toDouble()
        return((sqrt(dx.pow(2) + dy.pow(2))/brdRadius*255).toInt())
    }



    // Runnable
    override fun run() {
        while (!Thread.interrupted()) {
            this.post { if (joystickListener!= null) joystickListener?.onPush(getAngle(), getStrength()) }
            try {
                Thread.sleep(10)
                //TimeUnit.MILLISECONDS.sleep(10)
            } catch (e: InterruptedException) {
                break
            }

        }
    }
}