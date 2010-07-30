/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.gallery3d.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;

import com.android.gallery3d.R;
import com.android.gallery3d.data.MediaSet;

public class GalleryView extends TapListenerView {
    private static final int CHANGE_BACKGROUND = 1;

    private static final int MARGIN_HUD_SLOTVIEW = 5;
    private static final int HORIZONTAL_GAP_SLOTS = 5;
    private static final int VERTICAL_GAP_SLOTS = 5;

    private AdaptiveBackground mBackground;
    private SlotView mSlotView;
    private HeadUpDisplay mHud;
    private SynchronizedHandler mHandler;

    private Bitmap mBgImages[];
    private int mBgIndex = 0;

    public GalleryView() {}

    @Override
    public void onStart(Bundle data) {
        initializeViews();
        intializeData();
        mHandler = new SynchronizedHandler(mContext.getUiMonitor()) {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case CHANGE_BACKGROUND:
                        changeBackground();
                        mHandler.sendEmptyMessageDelayed(
                                CHANGE_BACKGROUND, 3000);
                        break;
                }
            }
        };
        mHandler.sendEmptyMessageDelayed(CHANGE_BACKGROUND, 3000);
    }

    @Override
    public void onPause() {
        mHandler.removeMessages(CHANGE_BACKGROUND);
    }

    @Override
    public void onResume() {
        mHandler.sendEmptyMessage(CHANGE_BACKGROUND);
    }

    private void intializeData() {
        MediaSet mediaSet = mContext.getDataManager().getRootSet();
        mSlotView.setModel(new MediaSetSlotAdapter(
                mContext.getAndroidContext(), mediaSet, mSlotView, mSelectionManager));
    }

    private void initializeViews() {
        mBackground = new AdaptiveBackground();
        addComponent(mBackground);
        mSlotView = new SlotView(mContext.getAndroidContext());
        mSelectionManager = new SelectionManager(mContext.getAndroidContext(), mSlotView);
        addComponent(mSlotView);
        mHud = new HeadUpDisplay(mContext.getAndroidContext());
        addComponent(mHud);
        mSlotView.setGaps(HORIZONTAL_GAP_SLOTS, VERTICAL_GAP_SLOTS);
        mSlotView.setSlotTapListener(this);

        loadBackgroundBitmap(R.drawable.square,
                R.drawable.potrait, R.drawable.landscape);
        mBackground.setImage(mBgImages[mBgIndex]);
    }

    public SlotView getSlotView() {
        return mSlotView;
    }

    @Override
    protected void onLayout(
            boolean changed, int left, int top, int right, int bottom) {
        mBackground.layout(0, 0, right - left, bottom - top);
        mHud.layout(0, 0, right - left, bottom - top);

        int slotViewTop = mHud.getTopBarBottomPosition() + MARGIN_HUD_SLOTVIEW;
        int slotViewBottom = mHud.getBottomBarTopPosition()
                - MARGIN_HUD_SLOTVIEW;

        mSlotView.layout(0, slotViewTop, right - left, slotViewBottom);
    }

    public void changeBackground() {
        mBackground.setImage(mBgImages[mBgIndex]);
        if (++mBgIndex == mBgImages.length) mBgIndex = 0;
    }

    private void loadBackgroundBitmap(int ... ids) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        mBgImages = new Bitmap[ids.length];
        Resources res = mContext.getResources();
        for (int i = 0, n = ids.length; i < n; ++i) {
            Bitmap bitmap = BitmapFactory.decodeResource(res, ids[i], options);
            mBgImages[i] = mBackground.getAdaptiveBitmap(bitmap);
            bitmap.recycle();
        }
    }

    @Override
    public void startStateView(int slotIndex) {
        Bundle data = new Bundle();
        data.putInt(AlbumView.KEY_BUCKET_INDEX, slotIndex);
        mContext.getStateManager().startStateView(AlbumView.class, data);
    }
}