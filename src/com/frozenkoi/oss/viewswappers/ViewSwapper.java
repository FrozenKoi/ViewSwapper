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

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * {@link ViewAnimatorViaProperties} that switches between two views, and has a factory
 * from which these views are created.  You can either use the factory to
 * create the views, or add them yourself.  A ViewSwapper can only have two
 * child views, of which only one is shown at a time.
 */
public class ViewSwapper extends ViewAnimatorViaProperties {
    /**
     * The factory used to create the two children.
     */
    ViewFactory mFactory;

    /**
     * Creates a new empty ViewSwapper.
     *
     * @param context the application's environment
     */
    public ViewSwapper(Context context) {
        super(context);
    }

    /**
     * Creates a new empty ViewSwapper for the given context and with the
     * specified set attributes.
     *
     * @param context the application environment
     * @param attrs a collection of attributes
     */
    public ViewSwapper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if this switcher already contains two children
     */
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() >= 2) {
            throw new IllegalStateException("Can't add more than 2 views to a ViewSwapper");
        }
        super.addView(child, index, params);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(ViewSwapper.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(ViewSwapper.class.getName());
    }

    /**
     * Returns the next view to be displayed.
     *
     * @return the view that will be displayed after the next views flip.
     */
    public View getNextView() {
        int which = mWhichChild == 0 ? 1 : 0;
        return getChildAt(which);
    }

    private View obtainView() {
        View child = mFactory.makeView();
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
        addView(child, lp);
        return child;
    }

    /**
     * Sets the factory used to create the two views between which the
     * ViewSwapper will flip. Instead of using a factory, you can call
     * {@link #addView(android.view.View, int, android.view.ViewGroup.LayoutParams)}
     * twice.
     *
     * @param factory the view factory used to generate the switcher's content
     */
    public void setFactory(ViewFactory factory) {
        mFactory = factory;
        obtainView();
        obtainView();
    }

    /**
     * Reset the ViewSwapper to hide all of the existing views and to make it
     * think that the first time animation has not yet played.
     */
    public void reset() {
        mFirstTime = true;
        View v;
        v = getChildAt(0);
        if (v != null) {
            Animator vA = mCurrentAnimators.get(v);
            if (vA != null) {
                vA.end();
            }
            v.setVisibility(View.GONE);
        }
        v = getChildAt(1);
        if (v != null) {
            Animator vA = mCurrentAnimators.get(v);
            if (vA != null) {
                vA.end();
            }
            v.setVisibility(View.GONE);
        }
    }

    /**
     * Creates views in a ViewSwapper.
     */
    public interface ViewFactory {
        /**
         * Creates a new {@link android.view.View} to be added in a
         * {@link android.widget.ViewSwitcher}.
         *
         * @return a {@link android.view.View}
         */
        View makeView();
    }
}

