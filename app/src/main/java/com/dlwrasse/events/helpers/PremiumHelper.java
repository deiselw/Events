package com.dlwrasse.events.helpers;

import android.app.Activity;

import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;

import java.util.List;

public class PremiumHelper implements BillingClientStateListener, PurchasesUpdatedListener {
    private static final String SKU_PREMIUM = "android.test.purchased";

    private OnPremiumUpdateListener mListener;
    private BillingClient mBillingClient;
    private Activity mActivity;

    public PremiumHelper(Activity activity, OnPremiumUpdateListener listener) {
        mBillingClient = BillingClient.newBuilder(activity).setListener(this).build();
        mBillingClient.startConnection(this);

        mActivity = activity;
        mListener = listener;
    }

    public void launchPremiumPurchase() {
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSku(SKU_PREMIUM)
                .setType(BillingClient.SkuType.INAPP)
                .build();
        int responseCode = mBillingClient.launchBillingFlow(mActivity, flowParams);
    }

    // region BillingClientStateListener
    @Override
    public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {
        if (billingResponseCode == BillingClient.BillingResponse.OK) {
            Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(BillingClient.SkuType.INAPP);
            List<Purchase> purchases = purchasesResult.getPurchasesList();
            if (purchases != null && mListener != null) {
                mListener.onPremiumStatus(isPremium(purchases));
            }
        }
    }
    @Override
    public void onBillingServiceDisconnected() {}
    // endregion

    // region PurchasesUpdatedListener
    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        if (responseCode == BillingClient.BillingResponse.OK && purchases != null && mListener != null) {
            mListener.onPremiumUpdate(isPremium(purchases));
        }
    }
    // endregion

    private boolean isPremium(List<Purchase> purchases) {
        for (Purchase purchase : purchases) {
            if (SKU_PREMIUM.equals(purchase.getSku())) {
                return true;
            }
        }
        return false;
    }

    public interface OnPremiumUpdateListener {
        void onPremiumUpdate(boolean premium);
        void onPremiumStatus(boolean premium);
    }
}
