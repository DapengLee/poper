package com.fanwe.library.poper;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2017/4/27.
 */

public class SDPoper
{
    private View mPopView;
    private Position mPosition;
    private boolean mDynamicUpdate;

    private int mConsumeHeight;
    private int mScreenWidth;
    private int mScreenHeight;

    private FrameLayout mRootLayout;
    private FrameLayout.LayoutParams mParams;
    private int mMarginLeft;
    private int mMarginTop;
    private int mMarginRight;
    private int mMarginBottom;
    private int mGravity;

    private int mMarginX;
    private int mMarginY;

    private WeakReference<View> mTarget;
    private Rect mTargetRect = new Rect();

    private WeakReference<Activity> mActivity;

    public SDPoper(Activity activity)
    {
        if (activity == null)
        {
            throw new NullPointerException("activity is null");
        }
        mActivity = new WeakReference<>(activity);
    }

    private Activity getActivity()
    {
        if (mActivity != null)
        {
            return mActivity.get();
        } else
        {
            return null;
        }
    }

    private void init(Activity activity)
    {
        if (activity == null)
        {
            return;
        }

        final Resources resources = activity.getResources();

        mScreenWidth = resources.getDisplayMetrics().widthPixels;
        mScreenHeight = resources.getDisplayMetrics().heightPixels;

        mConsumeHeight = 0;

        final boolean isStatusBarVisible = ((activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == 0);
        if (isStatusBarVisible)
        {
            try
            {
                int resId = resources.getIdentifier("status_bar_height", "dimen", "android");
                int statusBarHeight = resources.getDimensionPixelSize(resId);
                mConsumeHeight += statusBarHeight;
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        FrameLayout frameLayout = (FrameLayout) activity.findViewById(android.R.id.content);
        mConsumeHeight += frameLayout.getTop();

        if (mRootLayout == null)
        {
            mRootLayout = frameLayout;
        }
    }

    /**
     * 设置根部局
     *
     * @param frameLayout
     */
    public SDPoper setRootLayout(FrameLayout frameLayout)
    {
        if (mRootLayout != frameLayout)
        {
            final boolean isAttached = isAttached();
            if (isAttached)
            {
                removePopViewFromRoot();
            }
            this.mRootLayout = frameLayout;
        }
        return this;
    }

    /**
     * 设置要Pop的view
     *
     * @param popView
     * @return
     */
    public SDPoper setPopView(View popView)
    {
        if (mPopView != popView)
        {
            releasePopView();
            mPopView = popView;
        }
        return this;
    }

    private void releasePopView()
    {
        mPopView = null;
        mPosition = null;
    }

    /**
     * 返回Target
     *
     * @return
     */
    public View getTarget()
    {
        if (mTarget != null)
        {
            return mTarget.get();
        } else
        {
            return null;
        }
    }

    /**
     * 设置目标view
     *
     * @param target
     */
    public SDPoper setTarget(View target)
    {
        if (getTarget() != target)
        {
            releaseTarget();
            if (target != null)
            {
                mTarget = new WeakReference<>(target);
            } else
            {
                mTarget = null;
            }
            initTarget();
        }
        return this;
    }

    /**
     * 初始化Target
     */
    private void initTarget()
    {
        if (getTarget() != null)
        {
            addTargetOnGlobalLayoutListenerIfNeed();
        }
    }

    /**
     * 释放Target
     */
    private void releaseTarget()
    {
        if (getTarget() != null)
        {
            getTarget().getViewTreeObserver().removeGlobalOnLayoutListener(mOnGlobalLayoutListenerTarget);
            mTarget = null;
        }
    }

    /**
     * 根据设置是否添加Target的OnGlobalLayoutListener回调
     */
    private void addTargetOnGlobalLayoutListenerIfNeed()
    {
        if (getTarget() != null)
        {
            if (mDynamicUpdate)
            {
                getTarget().getViewTreeObserver().removeGlobalOnLayoutListener(mOnGlobalLayoutListenerTarget);
                getTarget().getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListenerTarget);
            } else
            {
                getTarget().getViewTreeObserver().removeGlobalOnLayoutListener(mOnGlobalLayoutListenerTarget);
            }
        }
    }

    /**
     * 设置是否动态更新popview的位置，默认false
     *
     * @param dynamicUpdate true-当target大小或者位置发生变化的时候会动态更新popview的位置
     */
    public SDPoper setDynamicUpdate(boolean dynamicUpdate)
    {
        mDynamicUpdate = dynamicUpdate;
        addTargetOnGlobalLayoutListenerIfNeed();
        return this;
    }

    /**
     * 设置显示的位置
     *
     * @param position
     */
    public SDPoper setPosition(Position position)
    {
        if (position != null)
        {
            mPosition = position;
        }
        return this;
    }

    /**
     * 设置x轴方向margin
     *
     * @param marginX
     */
    public SDPoper setMarginX(int marginX)
    {
        mMarginX = marginX;
        return this;
    }

    /**
     * 设置y轴方向margin
     *
     * @param marginY
     */
    public SDPoper setMarginY(int marginY)
    {
        mMarginY = marginY;
        return this;
    }

    private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListenerTarget = new ViewTreeObserver.OnGlobalLayoutListener()
    {
        @Override
        public void onGlobalLayout()
        {
            if (mDynamicUpdate && isAttached())
            {
                updatePosition();
            }
        }
    };

    /**
     * 保存target的信息
     */
    private void saveTargetInfo()
    {
        if (getTarget() != null)
        {
            getTarget().getGlobalVisibleRect(mTargetRect);
        }
    }

    /**
     * 把PopView添加到Parent
     *
     * @param attach
     */
    public void attach(boolean attach)
    {
        if (attach)
        {
            updatePosition();
        } else
        {
            removePopViewFromRoot();
        }
    }

    /**
     * 当前PopView是否已经被添加到Parent
     *
     * @return
     */
    public boolean isAttached()
    {
        return mPopView != null && mPopView.getParent() != null && mPopView.getParent() == mRootLayout;
    }

    private void removePopViewFromRoot()
    {
        if (isAttached())
        {
            mRootLayout.removeView(mPopView);
        }
    }

    /**
     * 刷新popview的位置
     */
    private void updatePosition()
    {
        if (mPopView == null)
        {
            return;
        }
        if (mPosition == null)
        {
            return;
        }
        init(getActivity());

        saveTargetInfo();
        addToRoot();
        switch (mPosition)
        {
            case TopLeft:
                alignTopLeft();
                break;
            case TopCenter:
                alignTopCenter();
                break;
            case TopRight:
                alignTopRight();
                break;

            case LeftCenter:
                alignLeftCenter();
                break;
            case Center:
                alignCenter();
                break;
            case RightCenter:
                alignRightCenter();
                break;

            case BottomLeft:
                alignBottomLeft();
                break;
            case BottomCenter:
                alignBottomCenter();
                break;
            case BottomRight:
                alignBottomRight();
                break;
            default:
                break;
        }
        updateParamsIfNeed();
    }

    //---------- position start----------

    private void alignTopLeft()
    {
        mGravity = Gravity.TOP | Gravity.LEFT;
        if (getTarget() != null)
        {
            mMarginLeft = mTargetRect.left;
            mMarginTop = mTargetRect.top - mConsumeHeight;
        } else
        {
            mMarginLeft = 0;
            mMarginTop = 0;
        }
        mMarginLeft += mMarginX;
        mMarginTop += mMarginY;
        mMarginRight = 0;
        mMarginBottom = 0;
    }

    private void alignTopCenter()
    {
        mGravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        if (getTarget() != null)
        {
            mMarginLeft = mTargetRect.left - mScreenWidth / 2 + mTargetRect.width() / 2;
            mMarginTop = mTargetRect.top - mConsumeHeight;
        } else
        {
            mMarginLeft = 0;
            mMarginTop = 0;
        }
        mMarginLeft += mMarginX;
        mMarginTop += mMarginY;
        mMarginRight = 0;
        mMarginBottom = 0;
    }

    private void alignTopRight()
    {
        mGravity = Gravity.TOP | Gravity.RIGHT;
        if (getTarget() != null)
        {
            mMarginTop = mTargetRect.top - mConsumeHeight;
            mMarginRight = mScreenWidth - mTargetRect.left - mTargetRect.width();
        } else
        {
            mMarginTop = 0;
            mMarginRight = 0;
        }
        mMarginLeft = 0;
        mMarginTop += mMarginY;
        mMarginRight -= mMarginX;
        mMarginBottom = 0;
    }

    private void alignLeftCenter()
    {
        mGravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        if (getTarget() != null)
        {
            mMarginLeft = mTargetRect.left;
            mMarginTop = mTargetRect.top - mScreenHeight / 2 + mTargetRect.height() / 2 - mConsumeHeight / 2;
        } else
        {
            mMarginLeft = 0;
            mMarginTop = 0;
        }
        mMarginLeft += mMarginX;
        mMarginTop += mMarginY;
        mMarginRight = 0;
        mMarginBottom = 0;
    }

    private void alignCenter()
    {
        mGravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
        if (getTarget() != null)
        {
            mMarginLeft = mTargetRect.left - mScreenWidth / 2 + mTargetRect.width() / 2;
            mMarginTop = mTargetRect.top - mScreenHeight / 2 + mTargetRect.height() / 2 - mConsumeHeight / 2;
        } else
        {
            mMarginLeft = 0;
            mMarginTop = 0;
        }
        mMarginLeft += mMarginX;
        mMarginTop += mMarginY;
        mMarginRight = 0;
        mMarginBottom = 0;
    }

    private void alignRightCenter()
    {
        mGravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        if (getTarget() != null)
        {
            mMarginTop = mTargetRect.top - mScreenHeight / 2 + mTargetRect.height() / 2 - mConsumeHeight / 2;
            mMarginRight = mScreenWidth - mTargetRect.left - mTargetRect.width();
        } else
        {
            mMarginTop = 0;
            mMarginRight = 0;
        }
        mMarginLeft = 0;
        mMarginTop += mMarginY;
        mMarginRight -= mMarginX;
        mMarginBottom = 0;
    }

    private void alignBottomLeft()
    {
        mGravity = Gravity.BOTTOM | Gravity.LEFT;
        if (getTarget() != null)
        {
            mMarginLeft = mTargetRect.left;
            mMarginBottom = mScreenHeight - mTargetRect.top - mTargetRect.height();
        } else
        {
            mMarginLeft = 0;
            mMarginBottom = 0;
        }
        mMarginLeft += mMarginX;
        mMarginTop = 0;
        mMarginRight = 0;
        mMarginBottom -= mMarginY;
    }

    private void alignBottomCenter()
    {
        mGravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        if (getTarget() != null)
        {
            mMarginLeft = mTargetRect.left - mScreenWidth / 2 + mTargetRect.width() / 2;
            mMarginBottom = mScreenHeight - mTargetRect.top - mTargetRect.height();
        } else
        {
            mMarginLeft = 0;
            mMarginBottom = 0;
        }
        mMarginLeft += mMarginX;
        mMarginTop = 0;
        mMarginRight = 0;
        mMarginBottom -= mMarginY;
    }

    private void alignBottomRight()
    {
        mGravity = Gravity.BOTTOM | Gravity.RIGHT;
        if (getTarget() != null)
        {
            mMarginRight = mScreenWidth - mTargetRect.left - mTargetRect.width();
            mMarginBottom = mScreenHeight - mTargetRect.top - mTargetRect.height();
        } else
        {
            mMarginRight = 0;
            mMarginBottom = 0;
        }
        mMarginLeft = 0;
        mMarginTop = 0;
        mMarginRight -= mMarginX;
        mMarginBottom -= mMarginY;
    }

    //---------- position end----------

    private void addToRoot()
    {
        final ViewParent parent = mPopView.getParent();

        if (parent != mRootLayout)
        {
            if (parent != null)
            {
                ((ViewGroup) parent).removeView(mPopView);
            }

            ViewGroup.LayoutParams params = mPopView.getLayoutParams();
            FrameLayout.LayoutParams p = null;
            if (params != null)
            {
                p = new FrameLayout.LayoutParams(params.width, params.height);
            } else
            {
                p = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            }

            mRootLayout.addView(mPopView, p);
        }
        mParams = (FrameLayout.LayoutParams) mPopView.getLayoutParams();
    }

    private void updateParamsIfNeed()
    {
        boolean needUpdate = false;

        if (mParams.leftMargin != mMarginLeft)
        {
            mParams.leftMargin = mMarginLeft;
            needUpdate = true;
        }
        if (mParams.leftMargin != mMarginLeft)
        {
            mParams.leftMargin = mMarginLeft;
            needUpdate = true;
        }
        if (mParams.topMargin != mMarginTop)
        {
            mParams.topMargin = mMarginTop;
            needUpdate = true;
        }
        if (mParams.rightMargin != mMarginRight)
        {
            mParams.rightMargin = mMarginRight;
            needUpdate = true;
        }
        if (mParams.bottomMargin != mMarginBottom)
        {
            mParams.bottomMargin = mMarginBottom;
            needUpdate = true;
        }
        if (mParams.gravity != mGravity)
        {
            mParams.gravity = mGravity;
            needUpdate = true;
        }

        if (needUpdate)
        {
            mPopView.setLayoutParams(mParams);
        }
    }

    public enum Position
    {
        TopLeft,
        TopCenter,
        TopRight,

        LeftCenter,
        Center,
        RightCenter,

        BottomLeft,
        BottomCenter,
        BottomRight,
    }
}
