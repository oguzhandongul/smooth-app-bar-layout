/*
 * Copyright 2016 "Henry Tao <hi@henrytao.me>"
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.henrytao.smoothappbarlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.henrytao.smoothappbarlayout.base.ScrollFlag;
import me.henrytao.smoothappbarlayout.base.ScrollTargetCallback;
import me.henrytao.smoothappbarlayout.base.Utils;

/**
 * Created by henrytao on 2/1/16.
 */
@CoordinatorLayout.DefaultBehavior(SmoothAppBarLayout.Behavior.class)
public class SmoothAppBarLayout extends AppBarLayout {

  public static boolean DEBUG = false;

  protected final List<WeakReference<OnOffsetChangedListener>> mOffsetChangedListeners = new ArrayList<>();

  protected boolean mHaveChildWithInterpolator;

  private ScrollTargetCallback mScrollTargetCallback;

  private int mViewPagerId;

  private ViewPager vViewPager;

  public SmoothAppBarLayout(Context context) {
    super(context);
    init(null);
  }

  public SmoothAppBarLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(null);
  }

  @Override
  public void addOnOffsetChangedListener(OnOffsetChangedListener listener) {
    super.addOnOffsetChangedListener(listener);
    int i = 0;
    for (int z = this.mOffsetChangedListeners.size(); i < z; ++i) {
      WeakReference ref = (WeakReference) this.mOffsetChangedListeners.get(i);
      if (ref != null && ref.get() == listener) {
        return;
      }
    }
    this.mOffsetChangedListeners.add(new WeakReference(listener));
  }

  @Override
  public void removeOnOffsetChangedListener(OnOffsetChangedListener listener) {
    super.removeOnOffsetChangedListener(listener);
    Iterator i = mOffsetChangedListeners.iterator();
    while (true) {
      OnOffsetChangedListener item;
      do {
        if (!i.hasNext()) {
          return;
        }
        WeakReference ref = (WeakReference) i.next();
        item = (OnOffsetChangedListener) ref.get();
      } while (item != listener && item != null);
      i.remove();
    }
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    initViews();
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    int i = 0;
    for (int z = this.getChildCount(); i < z; ++i) {
      View child = this.getChildAt(i);
      LayoutParams childLp = (LayoutParams) child.getLayoutParams();
      Interpolator interpolator = childLp.getScrollInterpolator();
      if (interpolator != null) {
        mHaveChildWithInterpolator = true;
        break;
      }
    }
  }

  public void setScrollTargetCallback(ScrollTargetCallback scrollTargetCallback) {
    mScrollTargetCallback = scrollTargetCallback;
  }

  protected ViewPager getViewPager() {
    return vViewPager;
  }

  private void init(AttributeSet attrs) {
    TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SmoothAppBarLayout, 0, 0);
    try {
      mViewPagerId = a.getResourceId(R.styleable.SmoothAppBarLayout_sabl_view_pager_id, 0);
    } finally {
      a.recycle();
    }
  }

  private void initViews() {
    if (mViewPagerId > 0) {
      vViewPager = (ViewPager) getRootView().findViewById(mViewPagerId);
    } else {
      int i = 0;
      ViewGroup parent = (ViewGroup) getParent();
      View child;
      for (int z = parent.getChildCount(); i < z; i++) {
        child = parent.getChildAt(i);
        if (child instanceof ViewPager) {
          vViewPager = (ViewPager) child;
          break;
        }
      }
    }
  }

  public static class Behavior extends BaseBehavior {

    protected ScrollFlag mScrollFlag;

    private int mLastTransitionOffset;

    private int mStatusBarSize;

    @Override
    protected void onInit(CoordinatorLayout coordinatorLayout, AppBarLayout child) {
      Utils.log("widget | onInit");
      if (mScrollFlag == null) {
        mScrollFlag = new ScrollFlag(child);
      }
    }

    @Override
    protected void onScrollChanged(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int y, int dy, boolean accuracy) {
      if (!mScrollFlag.isFlagScrollEnabled()) {
        return;
      }

      int minOffset = getMinOffset(child);
      int maxOffset = getMaxOffset(child);
      int translationOffset = accuracy ? Math.min(Math.max(minOffset, -y), maxOffset) : minOffset;

      dy = dy != 0 ? dy : y + mLastTransitionOffset;

      if (mScrollFlag.isQuickReturnEnabled()) {
        translationOffset = mLastTransitionOffset - dy;
        translationOffset = Math.min(Math.max(minOffset, translationOffset), maxOffset);
        int minHeight = ViewCompat.getMinimumHeight(child);
        minHeight = minHeight > 0 ? minHeight : ViewCompat.getMinimumHeight(mScrollFlag.getView());
        int breakPoint = minOffset + minHeight;
        if (dy <= 0 && !(accuracy && y <= Math.abs(breakPoint))) {
          translationOffset = Math.min(translationOffset, breakPoint);
        }
      } else if (mScrollFlag.isFlagEnterAlwaysEnabled()) {
        translationOffset = mLastTransitionOffset - dy;
        translationOffset = Math.min(Math.max(minOffset, translationOffset), maxOffset);
      } else if (mScrollFlag.isFlagEnterAlwaysCollapsedEnabled()) {
        // do nothing
      } else if (mScrollFlag.isFlagExitUntilCollapsedEnabled()) {
        // do nothing
      }

      Utils.log("widget | onScrollChanged | %d | %d | %d | %d | %b | %d", minOffset, maxOffset, y, dy, accuracy, translationOffset);
      mLastTransitionOffset = translationOffset;
      scrolling(coordinatorLayout, child, target, translationOffset);
    }

    protected int getMaxOffset(AppBarLayout layout) {
      return 0;
    }

    protected int getMinOffset(AppBarLayout layout) {
      int minOffset = layout.getMeasuredHeight();
      if (mScrollFlag != null) {
        if (mScrollFlag.isFlagScrollEnabled()) {
          minOffset = layout.getMeasuredHeight();
          int minHeight = ViewCompat.getMinimumHeight(layout);
          if (mScrollFlag.isFlagExitUntilCollapsedEnabled() || (minHeight > 0 && !mScrollFlag.isQuickReturnEnabled())) {
            minOffset -= minHeight > 0 ? minHeight : ViewCompat.getMinimumHeight(mScrollFlag.getView());
          }
        }
      }
      if (ViewCompat.getFitsSystemWindows(layout)) {
        if (mStatusBarSize == 0) {
          mStatusBarSize = Utils.getStatusBarSize(layout.getContext());
        }
        minOffset -= mStatusBarSize;
      }
      return -Math.max(minOffset, 0);
    }
  }
}
