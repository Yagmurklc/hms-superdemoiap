package com.superdemo.iap.huawei;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapApiException;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseReq;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseResult;
import com.huawei.hms.iap.entity.InAppPurchaseData;
import com.huawei.hms.iap.entity.IsEnvReadyResult;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.OwnedPurchasesReq;
import com.huawei.hms.iap.entity.OwnedPurchasesResult;
import com.huawei.hms.iap.entity.PurchaseIntentReq;
import com.huawei.hms.iap.entity.PurchaseIntentResult;
import com.huawei.hms.iap.entity.PurchaseResultInfo;
import com.huawei.hms.support.api.client.Status;
import com.superdemo.iap.huawei.common.Constant;
import com.superdemo.iap.huawei.util.SecurityUtil;

import org.json.JSONException;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    TextView msg;

    private static final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhq5dfS3JJkugYdnasvQyDqwx9RKwySDmCLItVsSPcSAaxtioas2Fobdba8CleeE4vBRRqxHEUVnpa+JBo9xnzgEx0yX4Mo++wi7LERCO4WJLsuDgogPjPVFBw8W0VZgENNkjI7MiuCQwAXhCEurE4wxUudbKLdy15dphNba0BMQy6U3VJSmfLQOqoAFI5mz2TwzFuoCDVY3LlAzWZMtl/baNfHsZa4XKT7C3wSnrvXL+AoBUWfRtt3+JBtLDchhbcJT8KHa/7lXaXTx0yZIUq9J3gLKbPP+Z8EPxT0N/XLiXsWiDr+iKqZjpatys0TIgoWn547+1V2yneWxPRxc+0QIDAQAB";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        msg = findViewById(R.id.text);
        deliverConsumablePurchases();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final String TAG = "onActivityResult";
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null){
            return;
        }

        PurchaseResultInfo purchaseResultInfo = Iap.getIapClient(this).parsePurchaseResultInfoFromIntent(data);
        if (purchaseResultInfo == null) {
            Log.e(TAG, "purchaseResultInfo can't be null");
            return;
        }

        if (requestCode == Constant.REQ_CODE_BUY_CONSUMABLE) {
            if (purchaseResultInfo.getReturnCode() == OrderStatusCode.ORDER_STATE_SUCCESS) {
                String inAppPurchaseDataSignature = purchaseResultInfo.getInAppDataSignature();
                String inAppPurchaseData = purchaseResultInfo.getInAppPurchaseData();

                if ((inAppPurchaseData == null) || (inAppPurchaseDataSignature == null)) {
                    Log.e(TAG, "inAppPurchaseData or inAppPurchaseDataSignature can't be null");
                    return;
                }

                msg.setText(purchaseResultInfo.getErrMsg());
                Log.e(TAG, "onActivityResult: " + purchaseResultInfo.getErrMsg());
                deliverProduct(inAppPurchaseData, inAppPurchaseDataSignature);

            }
            else  { msg.setText(purchaseResultInfo.getErrMsg()); }


        } else if(requestCode == Constant.REQ_CODE_BUY_NON_CONSUMABLE){
            if(purchaseResultInfo.getReturnCode() == OrderStatusCode.ORDER_STATE_SUCCESS){
                String inAppPurchaseDataSignature = purchaseResultInfo.getInAppDataSignature();
                String inAppPurchaseData = purchaseResultInfo.getInAppPurchaseData();
                if ((inAppPurchaseData == null) || (inAppPurchaseDataSignature == null)) {
                    Log.e(TAG, "inAppPurchaseData or inAppPurchaseDataSignature can't be null");
                    return;
                }
                msg.setText(purchaseResultInfo.getErrMsg());
                deliverConsumablePurchases();
                return;
            }

            msg.setText(purchaseResultInfo.getErrMsg());

        }else if(requestCode == Constant.REQ_CODE_BUY_SUPSCRIPTION){
            if(purchaseResultInfo.getReturnCode() == OrderStatusCode.ORDER_STATE_SUCCESS) {
                String inAppPurchaseDataSignature = purchaseResultInfo.getInAppDataSignature();
                String inAppPurchaseData = purchaseResultInfo.getInAppPurchaseData();
                deliverProduct(inAppPurchaseData, inAppPurchaseDataSignature);
                msg.setText(purchaseResultInfo.getErrMsg());
                return;
            }

            msg.setText(purchaseResultInfo.getErrMsg());
        }

    }

    private void isEnvReadyResult(){
        Task<IsEnvReadyResult> task = Iap.getIapClient(MainActivity.this).isEnvReady();
        task.addOnSuccessListener(new OnSuccessListener<IsEnvReadyResult>() {
            @Override
            public void onSuccess(IsEnvReadyResult result) { }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException) e;
                    Status status = apiException.getStatus();
                    if (status.getStatusCode() == OrderStatusCode.ORDER_HWID_NOT_LOGIN) {
                        if (status.hasResolution()) {
                            try {
                                status.startResolutionForResult(MainActivity.this, 4444);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                        }

                    } else if (status.getStatusCode() == OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED) {
                        Toast.makeText(getApplicationContext(),"This is unavailable in your country/region.", Toast.LENGTH_LONG).show();

                    }
                }
            }
        });
    }

    private  void consumeOwnedPurchases(String inAppPurchaseData) throws JSONException {
        InAppPurchaseData inAppPurchaseDataBean = new InAppPurchaseData(inAppPurchaseData);
        String purchaseToken = inAppPurchaseDataBean.getPurchaseToken();
        if(!purchaseToken.equals(null)){
            ConsumeOwnedPurchaseReq request = new ConsumeOwnedPurchaseReq();
            request.setPurchaseToken(purchaseToken);
            Task<ConsumeOwnedPurchaseResult> task = Iap.getIapClient(MainActivity.this).consumeOwnedPurchase(request);
            task.addOnSuccessListener(new OnSuccessListener<ConsumeOwnedPurchaseResult>() {

                @Override
                public void onSuccess(ConsumeOwnedPurchaseResult result) {
                    Log.i("doConfirmConsume", result.getConsumePurchaseData());
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    if (e instanceof IapApiException) {
                        IapApiException apiException = (IapApiException) e;
                        Status status = apiException.getStatus();
                        String s = apiException.getMessage();
                        int returnCode = apiException.getStatusCode();
                        Log.e("ProductActivityResult", "status :" + status + "s" + s +  "return code:" + returnCode );
                    }
                }
            });
        }
    }

    private void deliverConsumablePurchases(){
        final String  TAG = "getConsumablePurchase";
        OwnedPurchasesReq ownedPurchasesReq  = new OwnedPurchasesReq();
        ownedPurchasesReq .setPriceType(IapClient.PriceType.IN_APP_CONSUMABLE);

        Task<OwnedPurchasesResult> task = Iap.getIapClient(MainActivity.this).obtainOwnedPurchases(ownedPurchasesReq );
        task.addOnSuccessListener(new OnSuccessListener<OwnedPurchasesResult>() {
            @Override
            public void onSuccess(OwnedPurchasesResult result) {
                if (result == null || result.getInAppPurchaseDataList() == null || result.getInAppPurchaseDataList().isEmpty()) {
                    Log.i(TAG, ":no product ");
                    return;
                }

                List<String> inAppPurchaseDataList = result.getInAppPurchaseDataList();
                List<String> inAppSignature= result.getInAppSignature();
                for (int i = 0; i < inAppPurchaseDataList.size(); i++) {
                    final String inAppPurchaseData = inAppPurchaseDataList.get(i);
                    final String inAppPurchaseDataSignature = inAppSignature.get(i);
                    deliverProduct(inAppPurchaseData, inAppPurchaseDataSignature);
                }
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "obtainOwnedPurchases fail");
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException)e;
                    int returnCode = apiException.getStatusCode();
                    Log.e(TAG, "obtainOwnedPurchases fail" + returnCode);
                }
            }
        });
    }

    public  void  purchaseConsumable(View v) throws Exception{
        String tag = "purchaseStar";

        msg.setText(" ");
        isEnvReadyResult();
        PurchaseIntentReq request =  new PurchaseIntentReq();
        request.setPriceType(IapClient.PriceType.IN_APP_CONSUMABLE);
        request.setProductId("Star1000");

        Task<PurchaseIntentResult> task = Iap.getIapClient(MainActivity.this).createPurchaseIntent(request);
        task.addOnSuccessListener(new OnSuccessListener<PurchaseIntentResult>() {

            @Override
            public void onSuccess(PurchaseIntentResult result) {
                Status status = result.getStatus();
                try {
                    if(status.hasResolution()) {
                        status.startResolutionForResult(MainActivity.this, Constant.REQ_CODE_BUY_CONSUMABLE);
                        Log.i(tag, "status :" + status.getStatusMessage());
                        msg.setText(status.getStatusMessage());

                    }
                }
                catch (IntentSender.SendIntentException e) {
                    Log.e(tag, e.getMessage() );
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException)e;
                    Status status = apiException.getStatus();
                    int returnCode = apiException.getStatusCode();
                    Log.e(tag, "status" + status + "error code " + returnCode);
                }
                Log.e(tag, "onFailure: "+ e.getMessage() );
            }
        });

    }

    public  void  purchaseNonConsumable(View v) throws Exception{
        String tag = "purchaseStar";

        isEnvReadyResult();
        PurchaseIntentReq request =  new PurchaseIntentReq();
        request.setPriceType(IapClient.PriceType.IN_APP_NONCONSUMABLE);
        request.setProductId("nonConsumable");

        Task<PurchaseIntentResult> task = Iap.getIapClient(MainActivity.this).createPurchaseIntent(request);

        task.addOnSuccessListener(new OnSuccessListener<PurchaseIntentResult>() {

            @Override
            public void onSuccess(PurchaseIntentResult result) {
                Status status = result.getStatus();
                try {
                    if(status.hasResolution()) {
                        status.startResolutionForResult(MainActivity.this, Constant.REQ_CODE_BUY_NON_CONSUMABLE);
                        Log.i(tag, "status :" + status.getStatusMessage());


                    }
                }
                catch (IntentSender.SendIntentException e) {
                    Log.e(tag, e.getMessage() );
                    msg.setText(e.getMessage());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException)e;
                    Status status = apiException.getStatus();
                    int returnCode = apiException.getStatusCode();
                    Log.e(tag, "status" + status + "error code " + returnCode);
                }
                Log.e(tag, "onFailure: "+ e.getMessage() );
            }
        });
    }

    public  void  subscribe (View v) throws Exception{
        String tag = "purchaseStar";

        isEnvReadyResult();
        PurchaseIntentReq request =  new PurchaseIntentReq();
        request.setPriceType(IapClient.PriceType.IN_APP_SUBSCRIPTION);
        request.setProductId("subscribe");

        Task<PurchaseIntentResult> task = Iap.getIapClient(MainActivity.this).createPurchaseIntent(request);

        task.addOnSuccessListener(new OnSuccessListener<PurchaseIntentResult>() {

            @Override
            public void onSuccess(PurchaseIntentResult result) {
                Status status = result.getStatus();
                try {
                    if(status.hasResolution()) {
                        status.startResolutionForResult(MainActivity.this, Constant.REQ_CODE_BUY_SUPSCRIPTION);
                        Log.i(tag, "status :" + status.getStatusMessage());

                    }
                }
                catch (IntentSender.SendIntentException e) {
                    Log.e(tag, e.getMessage() );
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException)e;
                    Status status = apiException.getStatus();
                    int returnCode = apiException.getStatusCode();
                    Log.e(tag, "status" + status + "error code " + returnCode);
                }
                Log.e(tag, "onFailure: "+ e.getMessage() );
            }
        });




    }

    private void deliverProduct(final String inAppPurchaseData, final String inAppPurchaseDataSignature) {
        final String  TAG = "deliverProduct";
        if (TextUtils.isEmpty(inAppPurchaseData) || TextUtils.isEmpty(inAppPurchaseDataSignature)) {
            return;
        }

        if (SecurityUtil.doCheck(inAppPurchaseData, inAppPurchaseDataSignature, publicKey)) {
            Log.e(TAG, "delivery:verify_signature_fail");
            return;
        }

        try {
            consumeOwnedPurchases(inAppPurchaseData);

        } catch (JSONException e) {
            Log.e(TAG, "delivery:" + e.getMessage());
        }
    }

}
