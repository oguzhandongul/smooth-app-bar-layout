/*
 * Copyright 2015 "Henry Tao <hi@henrytao.me>"
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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.henrytao.smoothappbarlayout.base.Utils;

/**
 * Created by henrytao on 9/24/15.
 */
public class SmoothCollapsingToolbarLayout extends RelativeLayout {

  private int mAvatarId;

  private int mCollapsedAvatarSize;

  private float mCollapsedOffsetX;

  private float mCollapsedOffsetY;

  private float mCollapsedSubTitleTextSize;

  private float mCollapsedTitleTextSize;

  private float mCurrentRatio;

  private int mExpandedAvatarSize;

  private float mExpandedOffsetX;

  private float mExpandedOffsetY;

  private float mExpandedSubtitleTextSize;

  private float mExpandedTitleTextSize;

  private AppBarLayout.OnOffsetChangedListener mOnAppBarLayoutOffsetChangedListener;

  private int mSubtitleId;

  private int mTitleId;

  private AppBarLayout vAppBarLayout;

  private View vAvatar;

  private TextView vSubtitle;

  private TextView vTitle;

  private Toolbar vToolbar;

  public SmoothCollapsingToolbarLayout(Context context) {
    super(context);
    init(null);
  }

  public SmoothCollapsingToolbarLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs);
  }

  public SmoothCollapsingToolbarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public SmoothCollapsingToolbarLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init(attrs);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    initViews();
    if (!isInEditMode()) {
      mOnAppBarLayoutOffsetChangedListener = new AppBarLayout.OnOffsetChangedListener() {

        @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
          SmoothCollapsingToolbarLayout.this.onOffsetChanged(i);
        }
      };
      getAppBarLayout().addOnOffsetChangedListener(mOnAppBarLayoutOffsetChangedListener);
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    if (mOnAppBarLayoutOffsetChangedListener != null) {
      getAppBarLayout().removeOnOffsetChangedListener(mOnAppBarLayoutOffsetChangedListener);
    }
    super.onDetachedFromWindow();
  }

  public void setAvatar(@IdRes int id) {
    vAvatar = findViewById(id);
    if (vAvatar != null) {
      ViewGroup.LayoutParams params = vAvatar.getLayoutParams();
      params.height = params.width = mExpandedAvatarSize;
      ViewCompat.setPivotX(vAvatar, 0);
      ViewCompat.setPivotY(vAvatar, mExpandedAvatarSize / 2);
    }
  }

  public void setSubtitle(@IdRes int id) {
    vSubtitle = (TextView) findViewById(id);
    if (vSubtitle != null) {
      vSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mExpandedSubtitleTextSize);
    }
  }

  public void setTitle(@IdRes int id) {
    vTitle = (TextView) findViewById(id);
    if (vTitle != null) {
      vTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mExpandedTitleTextSize);
    }
  }

  private AppBarLayout getAppBarLayout() {
    if (vAppBarLayout == null) {
      if (getParent() instanceof CollapsingToolbarLayout && getParent().getParent() instanceof AppBarLayout) {
        vAppBarLayout = (AppBarLayout) getParent().getParent();
      } else {
        throw new IllegalStateException("Must be inside a CollapsingToolbarLayout and AppBarLayout");
      }
    }
    return vAppBarLayout;
  }

  private float getScaleOffset(float expandedOffset, float collapsedOffset, float ratio) {
    return getTranslationOffset(expandedOffset, collapsedOffset, ratio) / expandedOffset;
  }

  private Toolbar getToolbar() {
    if (vToolbar == null) {
      int i = 0;
      ViewGroup parent = (ViewGroup) getParent();
      View child;
      for (int z = parent.getChildCount(); i < z; i++) {
        child = parent.getChildAt(i);
        if (child instanceof Toolbar) {
          vToolbar = (Toolbar) child;
          break;
        }
      }
      if (vToolbar == null) {
        throw new IllegalStateException("Must have Toolbar");
      }
    }
    return vToolbar;
  }

  private float getTranslationOffset(float expandedOffset, float collapsedOffset, float ratio) {
    return expandedOffset - ratio * (expandedOffset - collapsedOffset);
  }

  private void init(AttributeSet attrs) {
    TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SmoothCollapsingToolbarLayout, 0, 0);
    try {
      mCollapsedOffsetX = a.getDimension(R.styleable.SmoothCollapsingToolbarLayout_sctl_collapsed_offsetX, 0);
      mCollapsedOffsetY = a.getDimension(R.styleable.SmoothCollapsingToolbarLayout_sctl_collapsed_offsetY, 0);
      mCollapsedAvatarSize = a.getDimensionPixelSize(R.styleable.SmoothCollapsingToolbarLayout_sctl_collapsed_avatarSize, -1);
      mCollapsedTitleTextSize = a.getDimension(R.styleable.SmoothCollapsingToolbarLayout_sctl_collapsed_titleTextSize, -1);
      mCollapsedSubTitleTextSize = a.getDimension(R.styleable.SmoothCollapsingToolbarLayout_sctl_collapsed_subtitleTextSize, -1);

      mExpandedOffsetX = a.getDimension(R.styleable.SmoothCollapsingToolbarLayout_sctl_expanded_offsetX, 0);
      mExpandedOffsetY = a.getDimension(R.styleable.SmoothCollapsingToolbarLayout_sctl_expanded_offsetY, 0);
      mExpandedAvatarSize = a.getDimensionPixelSize(R.styleable.SmoothCollapsingToolbarLayout_sctl_expanded_avatarSize, -1);
      mExpandedTitleTextSize = a.getDimension(R.styleable.SmoothCollapsingToolbarLayout_sctl_expanded_titleTextSize, -1);
      mExpandedSubtitleTextSize = a.getDimension(R.styleable.SmoothCollapsingToolbarLayout_sctl_expanded_subtitleTextSize, -1);

      mAvatarId = a.getResourceId(R.styleable.SmoothCollapsingToolbarLayout_sctl_avatar_id, 0);
      mTitleId = a.getResourceId(R.styleable.SmoothCollapsingToolbarLayout_sctl_title_id, 0);
      mSubtitleId = a.getResourceId(R.styleable.SmoothCollapsingToolbarLayout_sctl_subtitle_id, 0);
    } finally {
      a.recycle();
    }
  }

  private void initViews() {
    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
    if (params.gravity < 0) {
      params.gravity = Gravity.BOTTOM;
    }
    setAvatar(mAvatarId);
    setTitle(mTitleId);
    setSubtitle(mSubtitleId);
    updateViews();
  }

  private void onOffsetChanged(int verticalOffset) {
    int maxOffset = getAppBarLayout().getMeasuredHeight() - getToolbar().getMeasuredHeight();
    float ratio = Math.min(Math.abs(verticalOffset) * 1.0f / maxOffset, 1.0f);
    Utils.log("SmoothCollapsingToolbarLayout | onOffsetChanged | %f", ratio);
    updateViews(ratio);
  }

  private void updateViews() {
    updateViews(mCurrentRatio);
  }

  private void updateViews(float ratio) {
    mCurrentRatio = ratio;

    if (vAvatar != null) {
      float mAvatarScaleOffset = getScaleOffset(mExpandedAvatarSize, mCollapsedAvatarSize, ratio);
      ViewCompat.setScaleX(vAvatar, mAvatarScaleOffset);
      ViewCompat.setScaleY(vAvatar, mAvatarScaleOffset);
    }
  }
}
