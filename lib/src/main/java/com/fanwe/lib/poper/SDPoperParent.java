/*
 * Copyright (C) 2017 zhengjun, fanwe (http://www.fanwe.com)
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
package com.fanwe.lib.poper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

/**
 * Created by zhengjun on 2017/9/5.
 */
class SDPoperParent extends FrameLayout
{
    public SDPoperParent(Context context)
    {
        super(context);
        init();
    }

    private void init()
    {
        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        final int count = getChildCount();
        for (int i = 0; i < count; i++)
        {
            View child = getChildAt(i);

            final int left = child.getLeft();
            final int top = child.getTop();
            final int right = left + child.getMeasuredWidth();
            final int bottom = top + child.getMeasuredHeight();
            child.layout(left, top, right, bottom);
        }
    }

    @Override
    public void onViewAdded(View child)
    {
        super.onViewAdded(child);

        if (getChildCount() > 1)
        {
            throw new IllegalArgumentException("SDPoperParent can only add one child");
        }
    }

    @Override
    public void onViewRemoved(View child)
    {
        super.onViewRemoved(child);

        if (getChildCount() <= 0)
        {
            removeSelf();
        }
    }

    private Activity getActivity()
    {
        return (Activity) getContext();
    }

    public void removeSelf()
    {
        if (!getActivity().isFinishing())
        {
            ViewParent parent = getParent();
            if (parent instanceof ViewGroup)
            {
                ViewGroup viewGroup = (ViewGroup) parent;
                viewGroup.removeView(this);
            }
        }
    }
}
