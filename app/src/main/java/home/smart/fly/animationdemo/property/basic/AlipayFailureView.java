package home.smart.fly.animationdemo.property.basic;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by rookie on 2016/10/19.
 */

public class AlipayFailureView extends View {

    private String TAG = AlipayFailureView.class.getSimpleName();


    private Paint mCirclePanit;
    private Paint mLinePaint;
    private float mStrokeWidth = 12;
    private float factor = 0.8f;
    private float temp;
    private float mCenterX, mCenterY;
    private float mRadius = 250;
    private final RectF mRectF = new RectF();
    private int mDegree;
    private Float mOffsetValue = 0f;
    private Float mOffsetRightValue = 0f;
    private AnimatorSet mAnimatorSet = new AnimatorSet();
    private static final float PADDING = 10;
    private ValueAnimator mCircleAnim;
    private ValueAnimator mLineLeftAnimator;
    private ValueAnimator mLineRightAnimator;

    private boolean mIsCanHide;
    private PathMeasure pathLeftMeasure;
    private PathMeasure pathRightMeasure;
    private float[] mLeftPos = new float[2];
    private float[] mRightPos = new float[2];

    public AlipayFailureView(Context context) {
        this(context, null);
    }

    public AlipayFailureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlipayFailureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mCirclePanit = new Paint();
        mCirclePanit.setAntiAlias(true);
        mCirclePanit.setStrokeJoin(Paint.Join.ROUND);
        mCirclePanit.setStrokeWidth(mStrokeWidth);
        mCirclePanit.setColor(Color.RED);
        mCirclePanit.setStyle(Paint.Style.STROKE);

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeJoin(Paint.Join.ROUND);
        mLinePaint.setStrokeWidth(mStrokeWidth);
        mLinePaint.setColor(Color.WHITE);
        mLinePaint.setStyle(Paint.Style.STROKE);

        initDegreeAndOffset();
        lodingCircleMeasure();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mRectF.left = mCenterX - mRadius;
        mRectF.top = mCenterY - mRadius;
        mRectF.right = mCenterX + mRadius;
        mRectF.bottom = mCenterY + mRadius;
        canvas.drawArc(mRectF, 0, mDegree, false, mCirclePanit);
        if (mLeftPos[1] > (mCenterY - temp) && mRightPos[1] > (mCenterY - temp)) {
            canvas.drawLine(mCenterX - temp, mCenterY - temp, mLeftPos[0], mLeftPos[1], mLinePaint);
            canvas.drawLine(mCenterX + temp, mCenterY - temp, mRightPos[0], mRightPos[1], mLinePaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        lodingCircleMeasure();
    }

    private void lodingCircleMeasure() {
        int mViewWidth = getWidth();
        int mViewHeight = getHeight();
        mCenterX = mViewWidth / 2;
        mCenterY = mViewHeight / 2;

        temp = mRadius / 2.0f * factor;
        Path path = new Path();
        path.moveTo(mCenterX - temp, mCenterY - temp);
        path.lineTo(mCenterX + temp, mCenterY + temp);
        pathLeftMeasure = new PathMeasure(path, false);

        path = new Path();
        path.moveTo(mCenterX + temp, mCenterY - temp);
        path.lineTo(mCenterX - temp, mCenterY + temp);
        pathRightMeasure = new PathMeasure(path, false);


    }

    public void loadCircle() {
        if (null != mAnimatorSet && mAnimatorSet.isRunning()) {
            return;
        }

        mCircleAnim = ValueAnimator.ofInt(0, 360);
        mLineLeftAnimator = ValueAnimator.ofFloat(0, pathLeftMeasure.getLength());
        mLineRightAnimator = ValueAnimator.ofFloat(0, pathRightMeasure.getLength());
        mCircleAnim.setDuration(700);
        mLineLeftAnimator.setDuration(350);
        mLineRightAnimator.setDuration(350);
        mCircleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDegree = (Integer) animation.getAnimatedValue();
                invalidate();
            }
        });
        mLineLeftAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mOffsetValue = (Float) valueAnimator.getAnimatedValue();
                Log.e("left", "-------->" + mOffsetValue);
                pathLeftMeasure.getPosTan(mOffsetValue, mLeftPos, null);
                invalidate();
            }
        });
        mLineRightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mOffsetRightValue = (Float) animation.getAnimatedValue();
                pathRightMeasure.getPosTan(mOffsetRightValue, mRightPos, null);
                invalidate();
            }
        });
        mAnimatorSet.play(mCircleAnim).before(mLineLeftAnimator);
        mAnimatorSet.play(mLineRightAnimator).after(mLineLeftAnimator);
        mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                stop();
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mEndListner != null) {
                            mEndListner.onCircleDone();
                        }
                    }
                }, 800);
            }
        });
        mAnimatorSet.start();
    }

    public void stop() {
        if (null != mCircleAnim) {
            mCircleAnim.end();
        }
        if (null != mLineLeftAnimator) {
            mLineLeftAnimator.end();
        }
        if (null != mLineRightAnimator) {
            mLineRightAnimator.end();
        }
        clearAnimation();
    }

    public boolean isStarted() {
        if (null != mAnimatorSet) {
            return mAnimatorSet.isStarted();
        }
        return false;
    }

    public void initDegreeAndOffset() {
        mDegree = 0;
        mOffsetValue = 0f;
        mOffsetRightValue = 0f;
    }

    public boolean IsCanHide() {
        return mIsCanHide;
    }

    public void setCanHide(boolean mCanHide) {
        this.mIsCanHide = mCanHide;
    }

    private OnDoneCircleAnimListner mEndListner;

    public void addCircleAnimatorEndListner(OnDoneCircleAnimListner endListenr) {
        if (null == mEndListner) {
            this.mEndListner = endListenr;
        }
    }

    public interface OnDoneCircleAnimListner {
        void onCircleDone();
    }

    public void removeCircleAnimatorEndListner() {
        mEndListner = null;
    }
}
