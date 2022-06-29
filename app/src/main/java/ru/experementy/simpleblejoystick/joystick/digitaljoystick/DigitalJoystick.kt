package ru.experementy.simpleblejoystick.joystick.digitaljoystick

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import ru.experementy.customview.R
import java.lang.Math.toDegrees
import kotlin.math.atan2
import kotlin.math.sqrt

class DigitalJoystick(ctx: Context,attrs: AttributeSet): View(ctx,attrs),Runnable {
    private var pJButton = Paint()
    private var pJBorder = Paint()
    private var pJBackground= Paint()

    private var btnPressedColor: Int
    private var btnReleasedColor: Int
    private var brdColor: Int
    private var bkgrColor: Int

    var isEnable=false
    private var brdRadius: Float=0f
    private var bkgrRadius: Float=0f
    private var outerRadius: Float=0f
    private var innerRadius: Float=0f

    private var bkgrdMultipler: Float=0.75f
    private var innerMultipler: Float=0.3f
    private var outerMultipler: Float=0.65f


    private var centerX: Int=0
    private var centerY: Int=0

    private var mThread: Thread?=null

    interface JoystickListener{
        fun onPress(pressedButton: Int)
    }
    private var joystickListener: JoystickListener?=null


    fun setJoystickListener(listener: JoystickListener){
        joystickListener=listener
    }

    companion object{
      const val DEF_BTN_PRESSED_COLOR=Color.BLACK
      const val DEF_BTN_RELEASED_COLOR=Color.BLACK
      const val DEF_BRD_COLOR=Color.BLACK
      const val DEF_BKGR_COLOR=Color.WHITE
        //val DEF_BKGR_COLOR=Color.parseColor("#CDD2D3")

      const val DEF_SIZE=200

      const val DEF_BKGR_MULTIPLER=0.75F
      const val DEF_OUTER_MULTIPLER=0.65f
      const val DEF_INNER_MULTIPLER=0.3f
    }
    private val anglesForButtons= arrayOf(-50f,-140f,-230f,-320f)
    private val sweepAngleForButton: Float =-85f
    private var pressedButton=0

    init {
        ctx.theme.obtainStyledAttributes(attrs, R.styleable.myJoistik,
                0, 0).apply {
            try {
                btnPressedColor = getColor(R.styleable.myJoistik_butPressedColor,
                        DEF_BTN_PRESSED_COLOR
                )
                btnReleasedColor = getColor(R.styleable.myJoistik_butReleasedColor,
                        DEF_BTN_RELEASED_COLOR
                )
                brdColor = getColor(R.styleable.myJoistik_brdColor, DEF_BRD_COLOR)
                bkgrColor = getColor(R.styleable.myJoistik_bkgrColor, DEF_BKGR_COLOR)

                bkgrdMultipler=getFraction(R.styleable.myJoistik_bkgrdSizeMultipler,
                        1,1, DEF_BKGR_MULTIPLER
                )
                outerMultipler=getFraction(R.styleable.myJoistik_outerSizeMultipler,
                        1,1, DEF_OUTER_MULTIPLER
                )
                innerMultipler=getFraction(R.styleable.myJoistik_innerSizeMultipler,
                        1,1, DEF_INNER_MULTIPLER
                )
                isEnable=getBoolean(R.styleable.myJoistik_enable,false)


            } finally {
                recycle()
            }
           // pJButton.style=Paint.Style.FILL
            //pJButton.color=btnReleasedColor

            pJBackground.style=Paint.Style.FILL
            pJBackground.color=bkgrColor

            pJBorder.style=Paint.Style.STROKE
            pJBorder.color=brdColor
            pJBorder.strokeWidth=5f

        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX=w/2
        centerY=h/2
        val d= minOf(w, h).toFloat()
        brdRadius=d/2*bkgrdMultipler
        bkgrRadius=brdRadius-pJBorder.strokeWidth/2
        outerRadius=d/2*outerMultipler
        innerRadius=d/2*innerMultipler

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w=if(MeasureSpec.getMode(widthMeasureSpec)== MeasureSpec.UNSPECIFIED)
            DEF_SIZE
        else
            MeasureSpec.getSize(widthMeasureSpec)
        val h=if(MeasureSpec.getMode(heightMeasureSpec)== MeasureSpec.UNSPECIFIED)
            DEF_SIZE
        else
            MeasureSpec.getSize(heightMeasureSpec)
        val d=minOf(w, h)
        setMeasuredDimension(d, d)
    }

    private fun drawButtons(canvas: Canvas?,nButPressed: Int){
        val ovalForBut=RectF(centerX.toFloat()-outerRadius,centerY.toFloat()-outerRadius,
                centerX.toFloat()+outerRadius,centerY.toFloat()+outerRadius)

        pJButton.style=Paint.Style.FILL
        // pJButton.strokeWidth=5f
        pJButton.color=btnReleasedColor
        canvas?.drawArc(ovalForBut,anglesForButtons[0],sweepAngleForButton,true,pJButton)
        canvas?.drawArc(ovalForBut,anglesForButtons[1],sweepAngleForButton,true,pJButton)
        canvas?.drawArc(ovalForBut,anglesForButtons[2],sweepAngleForButton,true,pJButton)
        canvas?.drawArc(ovalForBut,anglesForButtons[3],sweepAngleForButton,true,pJButton)
        if(nButPressed>0){
            pJButton.color=btnPressedColor
            canvas?.drawArc(ovalForBut,anglesForButtons[nButPressed-1],sweepAngleForButton,true,pJButton)
        }
        pJButton.color=bkgrColor
        canvas?.drawCircle(centerX.toFloat(),centerY.toFloat(),innerRadius,pJButton)
    }
    override fun onDraw(canvas: Canvas?) {
        canvas?.drawCircle(centerX.toFloat(), centerY.toFloat(), brdRadius, pJBorder)
        canvas?.drawCircle(centerX.toFloat(), centerY.toFloat(), bkgrRadius, pJBackground)

        drawButtons(canvas,pressedButton)
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //return super.onTouchEvent(event)

        if(!isEnable)
            return true

        val x=event?.x
        val y=event?.y
        if(x==null||y==null) return true
        val dx=x-centerX
        val dy=y-centerY
        val abs= sqrt(dx*dx+dy*dy)
        if(abs>outerRadius||abs<innerRadius ) return true

        when(event.action){
            MotionEvent.ACTION_UP-> {
                mThread?.interrupt()
                pressedButton=0
                joystickListener?.onPress(pressedButton)
            }

            MotionEvent.ACTION_DOWN->{

                if(mThread?.isAlive==true && mThread!=null){
                    mThread?.interrupt()
                }

                var angle= toDegrees(atan2(dy,dx).toDouble())
                if(angle>0) angle -= 360
                pressedButton = when (angle) {
                    in anglesForButtons[1]..anglesForButtons[0] -> 1
                    in anglesForButtons[2]..anglesForButtons[1] -> 2
                    in anglesForButtons[3]..anglesForButtons[2] -> 3
                    else -> 4
                }

                mThread= Thread(this)
                mThread?.start()
                joystickListener?.onPress(pressedButton)
            }
        }

        invalidate()
        return true
    }


    override fun run() {
        while (!Thread.interrupted()) {
            this.post { if (joystickListener!= null) joystickListener?.onPress(pressedButton)}
            try {
                Thread.sleep(10)
                //TimeUnit.MILLISECONDS.sleep(10)
            } catch (e: InterruptedException) {
                break
            }

        }
    }
}