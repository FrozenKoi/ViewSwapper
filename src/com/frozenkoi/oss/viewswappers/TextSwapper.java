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


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Specialized {@link android.widget.ViewSwitcher} that contains
 * only children of type {@link android.widget.TextView}.
 *
 * A TextSwapper is useful to animate a label on screen. Whenever
 * {@link #setText(CharSequence)} is called, TextSwapper animates the current text
 * out and animates the new text in. 
 */
public class TextSwapper extends ViewSwapper {
    /**
     * Creates a new empty TextSwapper.
     *
     * @param context the application's environment
     */
    public TextSwapper(Context context) {
        super(context);
    }

    /**
     * Creates a new empty TextSwapper for the given context and with the
     * specified set attributes.
     *
     * @param context the application environment
     * @param attrs a collection of attributes
     */
    public TextSwapper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if child is not an instance of
     *         {@link android.widget.TextView}
     */
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (!(child instanceof TextView)) {
            throw new IllegalArgumentException(
                    "TextSwapper children must be instances of TextView");
        }

        super.addView(child, index, params);
    }

    /**
     * Sets the text of the next view and switches to the next view. This can
     * be used to animate the old text out and animate the next text in.
     *
     * @param text the new text to display
     */
    public void setText(CharSequence text) {
        final TextView t = (TextView) getNextView();
        t.setText(text);
        showNext();
    }

    /**
     * Sets the text of the text view that is currently showing.  This does
     * not perform the animations.
     *
     * @param text the new text to display
     */
    public void setCurrentText(CharSequence text) {
        ((TextView)getCurrentView()).setText(text);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(TextSwapper.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(TextSwapper.class.getName());
    }
}
