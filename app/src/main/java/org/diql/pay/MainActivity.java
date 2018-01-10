package org.diql.pay;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;

import java.util.List;

import static com.android.billingclient.api.BillingClient.SkuType;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Billing";
    private BillingClient mBillingClient;

    private PurchasesUpdatedListener mPurchasesUpdatedListener;

    private ConsumeResponseListener mConsumeResponseListener;

    /**
     * 与 google play 连接状态。
     * <p>如果与 google play 断开连接，下次使用之前需求先建立连接。</p>
     */
    private boolean mBillConnected;

    private TextView mTvDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTvDesc = findViewById(R.id.tv_desc);

        mPurchasesUpdatedListener = new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
                String msg = "onPurchasesUpdated() called with: responseCode = [" + responseCode + "], purchases = [" + purchases + "]";
                Log.d(TAG, msg);
                mTvDesc.setText(msg);
                // TODO: 2018/1/10 update uprchases.
                if (responseCode == BillingClient.BillingResponse.OK
                        && purchases != null) {
                    for (Purchase purchase : purchases) {
                        handlePurchase(purchase);
                    }
                } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
                    Log.i(TAG, "onPurchasesUpdated() - user cancelled the purchase flow - skipping");
                } else {
                    Log.w(TAG, "onPurchasesUpdated() got unknown resultCode: " + responseCode);
                }
            }

            private void handlePurchase(Purchase purchase) {
                Log.d(TAG, "handlePurchase() called with: purchase = [" + purchase + "]");
                if (purchase == null) {
                    return;
                }
                Log.d(TAG, "handlePurchase: " + purchase.toString());
            }
        };

        mConsumeResponseListener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@BillingClient.BillingResponse int responseCode, String outToken) {
                Log.d(TAG, "onConsumeResponse() called with: responseCode = [" + responseCode + "], outToken = [" + outToken + "]");
                if (responseCode == BillingClient.BillingResponse.OK) {
                    // Handle the success of the consume operation.
                    // For example, increase the number of coins inside the user's basket.
                }
            }
        };

        mBillingClient = BillingClient
                .newBuilder(getApplicationContext())
                .setListener(mPurchasesUpdatedListener)
                .build();

        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {
                Log.d(TAG, "onBillingSetupFinished() called with: billingResponseCode = [" + billingResponseCode + "]");
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    // The billing client is ready. You can query purchases here.
                    mBillConnected = true;

                   mBillingClient.queryPurchases(SkuType.INAPP);
                   mBillingClient.queryPurchases(SkuType.SUBS);
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                Log.d(TAG, "onBillingServiceDisconnected() called");
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.

                // It's strongly recommended that you implement your own connection retry policy
                // and override the onBillingServiceDisconnected() method.
                // Make sure you maintain the billing client connection when executing any methods.
                // You can see an example of a basic retry policy inside the executeServiceRequest method
                // in the BillingManager class from the Trivial Drive v2 sample app.
                mBillConnected = false;
            }
        });
    }

    private void purchased(String skuId) {
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSku(skuId)
                .setType(SkuType.INAPP)
                .build();
        int responseCode = mBillingClient.launchBillingFlow(this, flowParams);
        if (responseCode == BillingClient.BillingResponse.OK) {
            Log.d(TAG, "purchased: launchBillingFlow：ok");
        } else {
            Log.e(TAG, "purchased: launchBillingFlow：failed. code:" + responseCode);
        }
    }

    public void purchased() {
        // 付款.
        String skuId = "android.test.purchased";
        purchased(skuId);
    }

    public void canceled() {
        // 用户取消.
        String skuId = "android.test.canceled";
        purchased(skuId);
    }

    public void refunded() {
        // 退款.
        String skuId = "android.test.refunded";
        purchased(skuId);
    }

    public void itemUnavailable() {
        // 商品不存在.
        String skuId = "android.test.item_unavailable";
        purchased(skuId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBillConnected && mBillingClient != null) {
            // 断开与 google play 连接
            mBillingClient.endConnection();
        }
    }

    public void onPurchased(View view) {
        purchased();
    }

    public void onCancel(View view) {
        canceled();
    }

    public void onRefunded(View view) {
        refunded();
    }

    public void onItemUnavailable(View view) {
        itemUnavailable();
    }

    public void getConn(View view) {
        mTvDesc.setText("连接状态：" + mBillConnected + "\n ready:" + mBillingClient.isReady());
    }
}
