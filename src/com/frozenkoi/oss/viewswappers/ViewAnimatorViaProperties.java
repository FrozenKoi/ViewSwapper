/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.frozenkoi.oss.viewswappers;

import java.util.WeakHashMap;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Base class for a {@link FrameLayout} container that will perform animations
 * when switching between its views.
 *
 * @attr ref android.R.styleable#ViewAnimator_inAnimation
 * @attr ref android.R.styleable#ViewAnimator_outAnimation
 * @attr ref android.R.styleable#ViewAnimator_animateFirstView
 */
public class ViewAnimatorViaProperties extends android.widget.FrameLayout {

    int mWhichChild = 0;
    boolean mFirstTime = true;

    boolean mAnimateFirstTime = true;

    Animator mInAnimator;
    Animator mOutAnimator;

    private final String TAG = "ViewAnimatorViaProperties";

    protected WeakHashMap<View, Animator> mCurrentAnimators=new WeakHashMap<View, Animator>();

    public ViewAnimatorViaProperties(Context context) {
        super(context);
        initViewAnimator(context, null);
    }

    public ViewAnimatorViaProperties(Context context, AttributeSet attrs) {
        super(context, attrs);

        //[dk]  //TypedArray a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.ViewAnimator);
        //[dk]  //int resource = a.getResourceId(com.android.internal.R.styleable.ViewAnimator_inAnimation, 0);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewAnimatorViaProperties);
        int resource = a.getResourceId(R.styleable.ViewAnimatorViaProperties_inAnimator, 0);
        if (resource > 0) {
            setInAnimation(context, resource);
        }

        //[dk]  //resource = a.getResourceId(com.android.internal.R.styleable.ViewAnimator_outAnimation, 0);
        resource = a.getResourceId(R.styleable.ViewAnimatorViaProperties_outAnimator, 0);
        if (resource > 0) {
            setOutAnimation(context, resource);
        }

        //[dk]  //boolean flag = a.getBoolean(com.android.internal.R.styleable.ViewAnimator_animateFirstView, true);
        boolean flag = a.getBoolean(R.styleable.ViewAnimatorViaProperties_animateFirstView, true);
        setAnimateFirstView(flag);

        a.recycle();

        initViewAnimator(context, attrs);
    }

    /**
     * Initialize this {@link ViewAnimator}, possibly setting
     * {@link #setMeasureAllChildren(boolean)} based on {@link FrameLayout} flags.
     */
    private void initViewAnimator(Context context, AttributeSet attrs) {
        if (attrs == null) {
            // For compatibility, always measure children when undefined.
            setMeasureAllChildren(true);    //[dk]  //mMeasureAllChildren = true;
            return;
        }

        //since this inherits FrameLayout, shouldn't we let base class do this with it's own attributes?
        //Oh My Chtulu! no we can't since the default is different

        // For compatibility, default to measure children, but allow XML
        // attribute to override.
        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ViewAnimatorViaProperties);
              //com.android.internal.R.styleable.FrameLayout);
        final boolean measureAllChildren = a.getBoolean(
                R.styleable.ViewAnimatorViaProperties_measureAllChildren, true);
              //com.android.internal.R.styleable.FrameLayout_measureAllChildren, true);
        setMeasureAllChildren(measureAllChildren);
        a.recycle();
    }

    /**
     * Sets which child view will be displayed.
     *
     * @param whichChild the index of the child view to display
     */
    //[dk]  //@android.view.RemotableViewMethod //TODO: figure out what to do with this annotation
    public void setDisplayedChild(int whichChild) {
        mWhichChild = whichChild;
        if (whichChild >= getChildCount()) {
            mWhichChild = 0;
        } else if (whichChild < 0) {
            mWhichChild = getChildCount() - 1;
        }
        boolean hasFocus = getFocusedChild() != null;
        // This will clear old focus if we had it
        showOnly(mWhichChild);
        if (hasFocus) {
            // Try to retake focus if we had it
            requestFocus(FOCUS_FORWARD);
        }
    }

    /**
     * Returns the index of the currently displayed child view.
     */
    public int getDisplayedChild() {
        return mWhichChild;
    }

    /**
     * Manually shows the next child.
     */
    //[dk]  //@android.view.RemotableViewMethod //TODO: figure out what to do with this annotation
    public void showNext() {
        setDisplayedChild(mWhichChild + 1);
    }

    /**
     * Manually shows the previous child.
     */
    //[dk]  //@android.view.RemotableViewMethod //TODO: figure out what to do with this annotation
    public void showPrevious() {
        setDisplayedChild(mWhichChild - 1);
    }

    /**
     * Shows only the specified child. The other displays Views exit the screen,
     * optionally with the with the {@link #getOutAnimation() out animation} and
     * the specified child enters the screen, optionally with the
     * {@link #getInAnimation() in animation}.
     *
     * @param childIndex The index of the child to be shown.
     * @param animate Whether or not to use the in and out animations, defaults
     *            to true.
     */
    void showOnly(int childIndex, boolean animate) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (i == childIndex) {
                if (animate && mInAnimator != null) {
                    //[dk]  //child.startAnimation(mInAnimator);
                    //TODO: consider canceling previous animation stored in mCurrentAnimators
                    final Animator inAnimator = mInAnimator.clone();
                    inAnimator.setTarget(child);
                    inAnimator.start();
                    mCurrentAnimators.put(child, inAnimator);   //keep track of what animator is being used for this view
                }
                else if (mInAnimator != null)
                {
                    final Animator inAnimator = mInAnimator.clone();
                    inAnimator.setTarget(child);
                    inAnimator.start();
                    mCurrentAnimators.put(child, inAnimator);
                    inAnimator.end();//we want the view to jump to final position.
                }
                child.setVisibility(View.VISIBLE);
                mFirstTime = false;
            } else {
                if (animate && mOutAnimator != null && child.getVisibility() == View.VISIBLE) {
                    //[dk]  //child.startAnimation(mOutAnimator);
                    final Animator outAnimator = mOutAnimator.clone();
                    outAnimator.setTarget(child);
                    outAnimator.start();
                    mCurrentAnimators.put(child, outAnimator);   //keep track of what animator is being used for this view
                //} else if (child.getAnimation() == mInAnimator) //TODO: how to find out if the animator is the same as the one running
                } else if (mCurrentAnimators.get(child) == mInAnimator) //this will fail. Since the child's animator is a clone of mInAnimator
                    //[dk]  //child.clearAnimation(); //TODO: stop animation. Since this is for tween, don't do it. Check if we need it for property animation
                child.setVisibility(View.GONE);
            }
        }
    }
    /**
     * Shows only the specified child. The other displays Views exit the screen
     * with the {@link #getOutAnimation() out animation} and the specified child
     * enters the screen with the {@link #getInAnimation() in animation}.
     *
     * @param childIndex The index of the child to be shown.
     */
    void showOnly(int childIndex) {
        final boolean animate = (!mFirstTime || mAnimateFirstTime);
        showOnly(childIndex, animate);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        if (getChildCount() == 1) {
            child.setVisibility(View.VISIBLE);
        } else {
            child.setVisibility(View.GONE);
        }
        if (index >= 0 && mWhichChild >= index) {
            // Added item above current one, increment the index of the displayed child
            setDisplayedChild(mWhichChild + 1);
        }
    }

    @Override
    public void removeAllViews() {
        super.removeAllViews();
        mWhichChild = 0;
        mFirstTime = true;
    }

    @Override
    public void removeView(View view) {
        final int index = indexOfChild(view);
        if (index >= 0) {
            removeViewAt(index);
        }
    }

    @Override
    public void removeViewAt(int index) {
        super.removeViewAt(index);
        final int childCount = getChildCount();
        if (childCount == 0) {
            mWhichChild = 0;
            mFirstTime = true;
        } else if (mWhichChild >= childCount) {
            // Displayed is above child count, so float down to top of stack
            setDisplayedChild(childCount - 1);
        } else if (mWhichChild == index) {
            // Displayed was removed, so show the new child living in its place
            setDisplayedChild(mWhichChild);
        }
    }

    public void removeViewInLayout(View view) {
        removeView(view);
    }

    public void removeViews(int start, int count) {
        super.removeViews(start, count);
        if (getChildCount() == 0) {
            mWhichChild = 0;
            mFirstTime = true;
        } else if (mWhichChild >= start && mWhichChild < start + count) {
            // Try showing new displayed child, wrapping if needed
            setDisplayedChild(mWhichChild);
        }
    }

    public void removeViewsInLayout(int start, int count) {
        removeViews(start, count);
    }

    /**
     * Returns the View corresponding to the currently displayed child.
     *
     * @return The View currently displayed.
     *
     * @see #getDisplayedChild()
     */
    public View getCurrentView() {
        return getChildAt(mWhichChild);
    }

    /**
     * Returns the current animation used to animate a View that enters the screen.
     *
     * @return An Animation or null if none is set.
     *
     * @see #setInAnimation(android.view.animation.Animation)
     * @see #setInAnimation(android.content.Context, int)
     */
    public Animator getInAnimation() {
        return mInAnimator;
    }

    /**
     * Specifies the animation used to animate a View that enters the screen.
     *
     * @param inAnimation The animation started when a View enters the screen.
     *
     * @see #getInAnimation()
     * @see #setInAnimation(android.content.Context, int)
     */
    public void setInAnimation(Animator inAnimation) {
        mInAnimator = inAnimation;
        mInAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.v(TAG, "swapper  in start "+animation);
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
                Log.v(TAG, "swapper  in repeat "+animation);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                Log.v(TAG, "swapper  in end "+animation);
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                Log.v(TAG, "swapper  in cancel "+animation);
            }
        });
    }

    /**
     * Returns the current animation used to animate a View that exits the screen.
     *
     * @return An Animation or null if none is set.
     *
     * @see #setOutAnimation(android.view.animation.Animation)
     * @see #setOutAnimation(android.content.Context, int)
     */
    public Animator getOutAnimation() {
        return mOutAnimator;
    }

    /**
     * Specifies the animation used to animate a View that exit the screen.
     *
     * @param outAnimation The animation started when a View exit the screen.
     *
     * @see #getOutAnimation()
     * @see #setOutAnimation(android.content.Context, int)
     */
    public void setOutAnimation(Animator outAnimator) {
        mOutAnimator = outAnimator;
        mOutAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.v(TAG, "swapper out start "+animation);
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
                Log.v(TAG, "swapper out repeat "+animation);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                Log.v(TAG, "swapper out end "+animation);
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                Log.v(TAG, "swapper out cancel "+animation);
            }
        });
    }

    /**
     * Specifies the animation used to animate a View that enters the screen.
     *
     * @param context The application's environment.
     * @param resourceID The resource id of the animation.
     *
     * @see #getInAnimation()
     * @see #setInAnimation(android.view.animation.Animation)
     */
    public void setInAnimation(Context context, int resourceID) {
        //[dk]  //setInAnimation(AnimationUtils.loadAnimation(context, resourceID));
        final Animator inAnimator = AnimatorInflater.loadAnimator(context, resourceID);
        setInAnimation(inAnimator);
    }

    /**
     * Specifies the animation used to animate a View that exit the screen.
     *
     * @param context The application's environment.
     * @param resourceID The resource id of the animation.
     *
     * @see #getOutAnimation()
     * @see #setOutAnimation(android.view.animation.Animation)
     */
    public void setOutAnimation(Context context, int resourceID) {
        //[dk]  //setOutAnimation(AnimationUtils.loadAnimation(context, resourceID));
        final Animator outAnimator = AnimatorInflater.loadAnimator(context, resourceID);
        setOutAnimation(outAnimator);
    }

    /**
     * Returns whether the current View should be animated the first time the ViewAnimator
     * is displayed.
     *
     * @return true if the current View will be animated the first time it is displayed,
     * false otherwise.
     *
     * @see #setAnimateFirstView(boolean)
     */
    public boolean getAnimateFirstView() {
        return mAnimateFirstTime;
    }

    /**
     * Indicates whether the current View should be animated the first time
     * the ViewAnimator is displayed.
     *
     * @param animate True to animate the current View the first time it is displayed,
     *                false otherwise.
     */
    public void setAnimateFirstView(boolean animate) {
        mAnimateFirstTime = animate;
    }

    @Override
    public int getBaseline() {
        return (getCurrentView() != null) ? getCurrentView().getBaseline() : super.getBaseline();
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(ViewAnimatorViaProperties.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(ViewAnimatorViaProperties.class.getName());
    }
}
