package com.example.aqrc.ui.generate;



import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aqrc.MainActivity;
import com.example.aqrc.databinding.FragmentGenerateCodeBinding;
//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.InterstitialAd;
import com.example.aqrc.databinding.FragmentGenerateCodeBindingImpl;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.aqrc.R;
import com.example.aqrc.databinding.FragmentGenerateCodeBinding;
import com.example.aqrc.helpers.constant.IntentKey;
import com.example.aqrc.helpers.model.Code;
import com.example.aqrc.helpers.model.CodeGenerated;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Time;
import java.util.Date;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
//import com.example.aqrc.ui.generatedcode.GeneratedCodeActivity;


public class GenerateCodeFragment extends androidx.fragment.app.Fragment implements View.OnClickListener {

    private FragmentGenerateCodeBinding mBinding;
    private Context mContext;
    private FirebaseAuth mAuth;
    //  private InterstitialAd mInterstitialAd;
    private static final String TAG = GeneratedCodeFragment.class.getSimpleName();


    public GenerateCodeFragment() {

    }

    public static GenerateCodeFragment newInstance() {
        return new GenerateCodeFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_generate_code, container, false);
        initializeAd();
        setListeners();
        initializeCodeTypesSpinner();


        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        // mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    private void initializeAd() {
        if (mContext == null) {
            return;
        }
//        mInterstitialAd = new InterstitialAd(mContext);
//        mInterstitialAd.setAdUnitId(getString(R.string.admob_test_interstitial_ad_unit_id));
    }

    private void setListeners() {
        mBinding.spinnerTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getSelectedView()).setTextColor(ContextCompat.getColor(mContext,
                        position == 0 ? R.color.text_hint : R.color.text_regular));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mBinding.textViewGenerate.setOnClickListener(this);

//        mInterstitialAd.setAdListener(new AdListener() {
//            @Override
//            public void onAdLoaded() {
//                // Code to be executed when an ad finishes loading.
//            }
//
//            @Override
//            public void onAdFailedToLoad(int errorCode) {
//                // Code to be executed when an ad request fails.
//            }
//
//            @Override
//            public void onAdOpened() {
//                // Code to be executed when the ad is displayed.
//            }
//
//            @Override
//            public void onAdLeftApplication() {
//                // Code to be executed when the user has left the app.
//            }
//
//            @Override
//            public void onAdClosed() {
//                // Code to be executed when when the interstitial ad is closed.
//                generateCode();
//            }
//        });
    }

    private void initializeCodeTypesSpinner() {
        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(mContext,
                R.array.code_types, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(R.layout.item_spinner);
        mBinding.spinnerTypes.setAdapter(arrayAdapter);
    }

    @Override
    public void onClick(View view) {
        if (mContext == null) {
            return;
        }

        switch (view.getId()) {
            case R.id.text_view_generate:
//
//                if (mInterstitialAd.isLoaded()) {
//                    mInterstitialAd.show();
//                } else {
                generateCode();
                //}
                break;

            default:
                break;
        }
    }

    private boolean onGenerateClick;

    private void generateCode() {
        Intent intent = new Intent(mContext, GeneratedCodeFragment.class);
        Fragment nextFrag = new GeneratedCodeFragment();

        if (mBinding.editTextContent.getText() != null) {
            String content = mBinding.editTextContent.getText().toString().trim();
            int type = mBinding.spinnerTypes.getSelectedItemPosition();

            if (!TextUtils.isEmpty(content) && type != 0) {

                boolean isValid = true;

                switch (type) {
                    case Code.AQR_CODE:
                        if (content.length() > 80) {
                            Toast.makeText(mContext,
                                    getString(R.string.error_barcode_content_limit),
                                    Toast.LENGTH_SHORT).show();
                            isValid = false;
                        }
                        content = encodeQRText(content);
                        break;

                    case Code.QR_CODE:
                        if (content.length() > 1000) {
                            Toast.makeText(mContext,
                                    getString(R.string.error_qrcode_content_limit),
                                    Toast.LENGTH_SHORT).show();
                            isValid = false;
                        }
                        break;

                    default:
                        isValid = false;
                        break;
                }

                if (isValid) {
                    Code code = new Code(content, type);
                    CodeGenerated codeGenerated = new CodeGenerated(content, type);
                    // intent.putExtra(IntentKey.MODEL, code);
                    Gson gson = new Gson();
                    String classmodel = gson.toJson(codeGenerated);
                    // intent.putExtra("abc",""+classmodel);
                    //intent.putExtra(IntentKey.IS_GENERATED,true);
                    // startActivity(intent);
                    //    AppCompatActivity activity = (MainActivity) this.getActivity();
                    Bundle args = new Bundle();
                    args.putParcelable(IntentKey.MODEL, code);
                    args.putString("abc", "" + classmodel);
                    args.putBoolean(IntentKey.IS_GENERATED, true);
                    nextFrag.setArguments(args);
//                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentManager manager = getChildFragmentManager();
                    manager.beginTransaction()
                            .replace(R.id.generate_code_body_container, nextFrag, "generatedFragment")
                            // .add(nextFrag, "")
                            .addToBackStack(null)
                            .commit();
//                    activity.getSupportFragmentManager().beginTransaction()
//                            .replace(R.id.fragment_generate_container, nextFrag, "generatedFragment")
//                            .addToBackStack(null)
//                            .commit();
                }
            } else {
                Toast.makeText(mContext,
                        getString(R.string.error_provide_proper_content_and_type),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String encodeQRText(String text) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
        } else {
            // No user is signed in
            return text;
        }
        String site = "https://aqrc.app";
        String uuid = user.getUid();
        String providerId = user.getProviderId();
        String email = user.getEmail();
        String nameOnly = email.substring(0,email.indexOf('@'));
        System.out.println(nameOnly);
        long timeNow = System.currentTimeMillis();
        String hashText = sha256(text);
        String concat = site + "?creator=" + email + "&provider=" + providerId + "&at=" + timeNow + "&value=" + hashText + "&code=";

        long code = concat.hashCode(); //create an id for the QR code using object hashCode function.
        concat+=code; //add code at the end of AQRC content

        Log.d(TAG, "encodeQRText: " + concat);
        String secretHash = sha256(concat + "#_" + uuid);
        Log.d(TAG, "encodeQRText: secret hash " + secretHash);
        return concat;

        //return  "";
    }

    public static String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}