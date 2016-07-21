/*
 *  Copyright 2015 Yahoo Inc. All rights reserved.
 * Copyright 2015 Clockbyte LLC. All rights reserved.
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

package co.thnki.locationalarm.ads.expressads;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;

import co.thnki.locationalarm.ads.AdMobAdapterCalculator;
import co.thnki.locationalarm.ads.AdMobAdapterWrapperInterface;
import co.thnki.locationalarm.ads.AdMobFetcherBase;

/**
 * Adapter that has common functionality for any adapters that need to show ads in-between
 * other data.
 */
public class AdMobExpressAdapterWrapper extends BaseAdapter implements AdMobFetcherBase.AdMobListener,
        AdMobAdapterWrapperInterface
{

    private final String TAG = AdMobExpressAdapterWrapper.class.getCanonicalName();

    private BaseAdapter mAdapter;

    public BaseAdapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(BaseAdapter adapter) {
        mAdapter = adapter;
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                notifyDataSetChanged();
            }

            @Override
            public void onInvalidated() {
                notifyDataSetInvalidated();
            }
        });
    }

    AdMobFetcherExpress adFetcher;
    Context mContext;
    private AdMobAdapterCalculator AdapterCalculator = new AdMobAdapterCalculator(this);
    /*
    * Gets an object which incapsulates transformation of the source and ad blocks indices
    */
    public AdMobAdapterCalculator getAdapterCalculator(){return AdapterCalculator;}
    /*
* Injects an object which incapsulates transformation of the source and ad blocks indices. You could override calculations
* by inheritance of AdmobAdapterCalculator class
*/
    public void setAdapterCalculator(AdMobAdapterCalculator adapterCalculatordmob){AdapterCalculator = adapterCalculatordmob;}

    private static final int VIEW_TYPE_COUNT = 1;
    private static final int VIEW_TYPE_AD_EXPRESS = 1;

    private final static int DEFAULT_NO_OF_DATA_BETWEEN_ADS = 10;
    private final static int DEFAULT_LIMIT_OF_ADS = 3;
    private static final AdSize DEFAULT_AD_SIZE = new AdSize(AdSize.FULL_WIDTH, 150);
    private static final String DEFAULT_AD_UNIT_ID = "ca-app-pub-3940256099942544/1072772517";

    /*
    * Gets the number of your data items between ad blocks, by default it equals to 10.
    * You should set it according to the Admob's policies and rules which says not to
    * display more than one ad block at the visible part of the screen
    * so you should choose this parameter carefully and according to your item's height and screen resolution of a target devices
    */
    public int getNoOfDataBetweenAds() {
        return AdapterCalculator.getNoOfDataBetweenAds();
    }
    /*
    * Sets the number of your data items between ad blocks, by default it equals to 10.
    * You should set it according to the Admob's policies and rules which says not to
    * display more than one ad block at the visible part of the screen
    * so you should choose this parameter carefully and according to your item's height and screen resolution of a target devices
    */
    public void setNoOfDataBetweenAds(int mNoOfDataBetweenAds) {
        AdapterCalculator.setNoOfDataBetweenAds(mNoOfDataBetweenAds);
    }

    public int getFirstAdIndex() {
        return AdapterCalculator.getFirstAdIndex();
    }
    /*
    * Sets the first ad block index (zero-based) in the adapter, by default it equals to 0
    */
    public void setFirstAdIndex(int firstAdIndex) {
        AdapterCalculator.setFirstAdIndex(firstAdIndex);
    }

    /*
    * Gets the max count of ad blocks per dataset, by default it equals to 3 (according to the Admob's policies and rules)
    */
    public int getLimitOfAds() {
        return AdapterCalculator.getLimitOfAds();
    }

    /*
    * Sets the max count of ad blocks per dataset, by default it equals to 3 (according to the Admob's policies and rules)
    */
    public void setLimitOfAds(int mLimitOfAds) {
        AdapterCalculator.setLimitOfAds(mLimitOfAds);
    }

    private String mAdsUnitId;

    /*
    * Gets the res layout id for published express ads
    */
    public String getAdsUnitId() {
        return mAdsUnitId;
    }

    /*
    * Sets the res layout id for published express ads
    */
    public void setAdsUnitId(String mAdsUnitId) {
        this.mAdsUnitId = mAdsUnitId;
    }

    /*
    *Add a test device ID.
    */
    public void addTestDeviceId(String testDeviceId) {
        adFetcher.addTestDeviceId(testDeviceId);
    }

    /*
    *Sets a test device ID. Normally you don't have to set it
    */
    @Deprecated
    public void setTestDeviceId(String testDeviceId) {
        adFetcher.addTestDeviceId(testDeviceId);
    }

    private AdSize mAdSize;

    /*
    * Gets ad size
    */
    public AdSize getAdSize() {
        return mAdSize;
    }

    /*
    * Sets ad size
    */
    public void setAdSize(AdSize mAdSize) {
        this.mAdSize = mAdSize;
    }

    public AdMobExpressAdapterWrapper(Context context) {
        setNoOfDataBetweenAds(DEFAULT_NO_OF_DATA_BETWEEN_ADS);
        setLimitOfAds(DEFAULT_LIMIT_OF_ADS);
        setAdsUnitId(DEFAULT_AD_UNIT_ID);
        setAdSize(DEFAULT_AD_SIZE);
        mContext = context;

        adFetcher = new AdMobFetcherExpress(mContext);
        adFetcher.addListener(this);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        switch (getItemViewType(position)) {
            case VIEW_TYPE_AD_EXPRESS:
                NativeExpressAdView item = null;
                if (convertView == null) {
                    item = getExpressAdView(parent);
                    adFetcher.setupAd(item);
                    adFetcher.fetchAd(item);
                } else {
                    item = (NativeExpressAdView) convertView;
                }
                return item;
            default:
                int origPos = AdapterCalculator.getOriginalContentPosition(position);
                return mAdapter.getView(origPos, convertView, parent);
        }
    }

    private NativeExpressAdView getExpressAdView(ViewGroup parent) {
        NativeExpressAdView adView = new NativeExpressAdView(mContext);
        adView.setAdSize(getAdSize());
        adView.setAdUnitId(getAdsUnitId());
        adView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                AbsListView.LayoutParams.WRAP_CONTENT));
        return adView;
    }

    /**
     * <p>Gets the count of all data, including interspersed ads.</p>
     * <p/>
     * <p>If data size is 10 and an ad is to be showed after every 5 items starting at the index 0, this method
     * will return 12.</p>
     *
     * @return the total number of items this adapter can show, including ads.
     * @see AdMobExpressAdapterWrapper#setNoOfDataBetweenAds(int)
     * @see AdMobExpressAdapterWrapper#getNoOfDataBetweenAds()
     */
    @Override
    public int getCount() {

        if (mAdapter != null) {
            /*
            No of currently fetched ads, as long as it isn't more than no of max ads that can
            fit dataset.
             */
            int noOfAds = AdapterCalculator.getAdsCountToPublish();
            return mAdapter.getCount() > 0 ? mAdapter.getCount() + noOfAds : 0;
        } else {
            return 0;
        }
    }

    /**
     * Gets the item in a given position in the dataset. If an ad is to be returned,
     * a {@link NativeExpressAdView} object is returned.
     *
     * @param position the adapter position
     * @return the object or ad contained in this adapter position
     */
    @Override
    public Object getItem(int position) {

        if (AdapterCalculator.canShowAdAtPosition(position)) {
            int adPos = AdapterCalculator.getAdIndex(position);
            return adFetcher.getAdForIndex(adPos);
        } else {
            int origPos = AdapterCalculator.getOriginalContentPosition(position);
            return mAdapter.getItem(origPos);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT + getAdapter().getViewTypeCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (AdapterCalculator.canShowAdAtPosition(position)) {
            return VIEW_TYPE_AD_EXPRESS;
        } else {
            int origPos = AdapterCalculator.getOriginalContentPosition(position);
            return mAdapter.getItemViewType(origPos);
        }
    }

    /**
     * Destroys all currently fetched ads
     */
    public void destroyAds() {
        adFetcher.destroyAllAds();
    }

    /**
     * Clears all currently displaying ads to update them
     */
    public void requestUpdateAd() {
        adFetcher.updateAds();
    }

    @Override
    public void onAdCountChanged() {

        notifyDataSetChanged();
    }

    @Override
    public int getAdapterCount() {
        return mAdapter.getCount();
    }
}
