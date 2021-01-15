package com.example.aqrc.ui.generate;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import com.example.aqrc.R;
import com.example.aqrc.databinding.FragmentGeneratedCodeBinding;
import com.example.aqrc.helpers.constant.AppConstants;
import com.example.aqrc.helpers.constant.IntentKey;
import com.example.aqrc.helpers.model.Code;
import com.example.aqrc.helpers.model.CodeGenerated;
import com.example.aqrc.helpers.util.FileUtil;
import com.example.aqrc.helpers.util.ProgressDialogUtil;
import com.example.aqrc.helpers.util.database.DatabaseUtil;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

//import com.example.aqrc.databinding.ActivityGeneratedCodeBinding;
//mport com.example.aqrc.ui.settings.SettingsActivity;

public class GeneratedCodeFragment extends androidx.fragment.app.Fragment implements View.OnClickListener {

    private final int REQUEST_CODE_TO_SHARE = 1;
    private final int REQUEST_CODE_TO_SAVE = 2;
    private final int REQUEST_CODE_TO_PRINT = 3;

    private FragmentGeneratedCodeBinding mBinding;
    private Menu mToolbarMenu;
    private Code mCurrentCode;
    private CodeGenerated mCodeGenerated;
    private Bitmap mCurrentGeneratedCodeBitmap;
    private File mCurrentCodeFile, mCurrentPrintedFile;
    private CompositeDisposable mCompositeDisposable;







    public CompositeDisposable getCompositeDisposable() {
        return mCompositeDisposable;
    }

    public CodeGenerated getCodeGenerated() {
        return mCodeGenerated;
    }

    public void setCompositeDisposable(CompositeDisposable compositeDisposable) {
        mCompositeDisposable = compositeDisposable;
    }

    public File getCurrentPrintedFile() {
        return mCurrentPrintedFile;
    }

    public void setCurrentPrintedFile(File currentPrintedFile) {
        mCurrentPrintedFile = currentPrintedFile;
    }

    public File getCurrentCodeFile() {
        return mCurrentCodeFile;
    }

    public void setCurrentCodeFile(File currentCodeFile) {
        mCurrentCodeFile = currentCodeFile;
    }

    public Code getCurrentCode() {
        return mCurrentCode;
    }

    public void setCurrentCode(Code currentCode) {
        mCurrentCode = currentCode;
    }

    public Menu getToolbarMenu() {
        return mToolbarMenu;
    }

    public void setToolbarMenu(Menu toolbarMenu) {
        mToolbarMenu = toolbarMenu;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(getActivity(), R.layout.fragment_generated_code);
        setCompositeDisposable(new CompositeDisposable());

        getActivity().getWindow().setBackgroundDrawable(null);
        initializeToolbar();
        loadQRCode();
        setListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getCompositeDisposable().dispose();
    }

    private void setListeners() {
        mBinding.imageViewSave.setOnClickListener(this);
        mBinding.imageViewShare.setOnClickListener(this);
        mBinding.imageViewPrint.setOnClickListener(this);
    }

    private void loadQRCode() {
        Intent intent = getActivity().getIntent();
       Bundle bundle = getArguments();
        if (bundle != null) {
           // Bundle bundle = intent.getExtras();

            if (bundle != null && bundle.containsKey(IntentKey.MODEL)) {
                setCurrentCode(bundle.getParcelable(IntentKey.MODEL));
                if (getActivity().getIntent().getBooleanExtra(IntentKey.IS_GENERATED, false)) {
                    Gson gson=new Gson();
                    String retrievedString= getActivity().getIntent().getStringExtra("abc");
                    CodeGenerated codeGenerated=gson.fromJson(retrievedString,CodeGenerated.class);
                    setCodeGenerated(codeGenerated);
                }
            }
        }

        if (getCurrentCode() != null) {
            ProgressDialogUtil.on().showProgressDialog(getContext());

            mBinding.textViewContent.setText(String.format(Locale.ENGLISH,
                    getString(R.string.content), getCurrentCode().getContent()));

            mBinding.textViewType.setText(String.format(Locale.ENGLISH, getString(R.string.code_type),
                    getResources().getStringArray(R.array.code_types)[getCurrentCode().getType()]));

            BarcodeFormat barcodeFormat;
            switch (getCurrentCode().getType()) {
                case Code.AQR_CODE:
                    barcodeFormat = BarcodeFormat.QR_CODE;
                    break;

                case Code.QR_CODE:
                    barcodeFormat = BarcodeFormat.QR_CODE;
                    break;

                default:
                    barcodeFormat = null;
                    break;
            }

            if (barcodeFormat != null) {
                try {
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.encodeBitmap(getCurrentCode().getContent(),
                            barcodeFormat, 1000, 1000);
                    mBinding.imageViewGeneratedCode.setImageBitmap(bitmap);
                    mCurrentGeneratedCodeBitmap = bitmap;
                } catch (Exception e) {
                    if (!TextUtils.isEmpty(e.getMessage())) {
                        Log.e(getClass().getSimpleName(), e.getMessage());
                    }
                }
            }

            ProgressDialogUtil.on().hideProgressDialog();
        }
    }

    private void initializeToolbar() {
//        setSupportActionBar(mBinding.toolbar);
//
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setDisplayShowHomeEnabled(true);
//        }
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                onBackPressed();
//                break;
//
//            case R.id.action_settings:
//                startActivity(new Intent(this, SettingsActivity.class));
//                return true;
//
//            default:
//                break;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.home_toolbar_menu, menu);
//        setToolbarMenu(menu);
//        return true;
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.image_view_print:
//                if (PermissionUtil.on().requestPermission(this,
//                        REQUEST_CODE_TO_PRINT, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                    if (getCurrentPrintedFile() == null) {
//                        storeCodeDocument();
//                    } else {
//                        Toast.makeText(this,
//                                getString(R.string.generated_qr_code_already_exists),
//                                Toast.LENGTH_SHORT).show();
//                    }
//                }
//                break;
//
//            case R.id.image_view_save:
//                if (PermissionUtil.on().requestPermission(this,
//                        REQUEST_CODE_TO_SAVE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                    if (getCurrentCodeFile() == null) {
//                        storeCodeImage(true);
//                    } else {
//                        Toast.makeText(this,
//                                getString(R.string.generated_qr_code_already_exists),
//                                Toast.LENGTH_SHORT).show();
//                    }
//                }
//                break;
//
//            case R.id.image_view_share:
//                if (PermissionUtil.on().requestPermission(this,
//                        REQUEST_CODE_TO_SHARE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                    if (getCurrentCodeFile() == null) {
//                        storeCodeImage(false);
//                    } else {
//                        shareCode(getCurrentCodeFile());
//                    }
//                }
//                break;

            default:
                break;
        }
    }

    private void storeCodeImage(boolean justSave) {
        ProgressDialogUtil.on().showProgressDialog(getContext());

        getCompositeDisposable().add(
                Completable.create(emitter -> {
                    String type = getResources().getStringArray(R.array.code_types)[getCurrentCode().getType()];
                    File codeImageFile = FileUtil.getEmptyFile(getContext(), AppConstants.PREFIX_IMAGE,
                            String.format(Locale.ENGLISH, getString(R.string.file_name_body),
                                    type.substring(0, type.indexOf(" Code")),
                                    String.valueOf(System.currentTimeMillis())),
                            AppConstants.SUFFIX_IMAGE,
                            Environment.DIRECTORY_PICTURES);

                    if (codeImageFile != null && mCurrentGeneratedCodeBitmap != null) {
                        try (FileOutputStream out = new FileOutputStream(codeImageFile)) {
                            mCurrentGeneratedCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                            MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), codeImageFile.getAbsolutePath(), codeImageFile.getName(), ""); //display image on gallary
                            insertCode(codeImageFile.getAbsolutePath());
                            setCurrentCodeFile(codeImageFile);

                            if (!emitter.isDisposed()) {
                                emitter.onComplete();
                            }
                        } catch (IOException e) {
                            if (!emitter.isDisposed()) {
                                emitter.onError(e);
                            }
                        }
                    } else {
                        if (!emitter.isDisposed()) {
                            emitter.onError(new NullPointerException());
                        }
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribeWith(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                ProgressDialogUtil.on().hideProgressDialog();
                                if (justSave) {
                                    Toast.makeText(getContext(),
                                            getString(R.string.saved_the_code_successfully),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    shareCode(getCurrentCodeFile());
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (e != null && !TextUtils.isEmpty(e.getMessage())) {
                                    Log.e(getClass().getSimpleName(), e.getMessage());
                                }

                                ProgressDialogUtil.on().hideProgressDialog();
                                if (justSave) {
                                    Toast.makeText(getContext(),
                                            getString(R.string.failed_to_save_the_code),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(),
                                            getString(R.string.failed_to_share_the_code), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }));
    }

    private void insertCode(String imagepath) {



        Code codeGenerated=mCurrentCode;
        codeGenerated.setCodeImagePath(imagepath);
        codeGenerated.setTimeStamp(System.currentTimeMillis());
        codeGenerated.setIsFromGenerated(1);
        if (codeGenerated != null) {
            getCompositeDisposable().add(DatabaseUtil.on().insertCode(codeGenerated)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribeWith(new DisposableCompletableObserver() {
                        @Override
                        public void onComplete() {
                            Log.d("codeinsertstatus", "status code: success!");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("codeinsertstatus", "status code: failed!");
                        }
                    }));
        }
    }

    public void setCodeGenerated(CodeGenerated mCodeGenerated) {
        this.mCodeGenerated = mCodeGenerated;
    }

    private void storeCodeDocument() {
        ProgressDialogUtil.on().showProgressDialog(getContext());

        getCompositeDisposable().add(
                Completable.create(emitter -> {
                    String type = getResources().getStringArray(R.array.code_types)[getCurrentCode().getType()];
                    File codeDocumentFile = FileUtil.getEmptyFile(getContext(), AppConstants.PREFIX_CODE,
                            String.format(Locale.ENGLISH, getString(R.string.file_name_body),
                                    type.substring(0, type.indexOf(" Code")),
                                    String.valueOf(System.currentTimeMillis())),
                            AppConstants.SUFFIX_CODE,
                            Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT ?
                                    Environment.DIRECTORY_PICTURES : Environment.DIRECTORY_DOCUMENTS);

                    if (codeDocumentFile != null && mCurrentGeneratedCodeBitmap != null && getCurrentCode() != null) {
                        try {
                            Document document = new Document();

                            PdfWriter.getInstance(document, new FileOutputStream(codeDocumentFile));

                            document.open();
                            document.setPageSize(PageSize.A4);
                            document.addCreationDate();
                            document.addAuthor(getString(R.string.app_name));
                            document.addCreator(getString(R.string.app_name));

                            BaseColor colorAccent = new BaseColor(0, 153, 204, 255);
                            float headingFontSize = 20.0f;
                            float valueFontSize = 26.0f;

                            BaseFont baseFont = BaseFont.createFont("res/font/opensans_regular.ttf", "UTF-8", BaseFont.EMBEDDED);

                            LineSeparator lineSeparator = new LineSeparator();
                            lineSeparator.setLineColor(new BaseColor(0, 0, 0, 68));

                            // Adding Title....
                            Font mOrderDetailsTitleFont = new Font(baseFont, 36.0f, Font.NORMAL, BaseColor.BLACK);
                            Chunk mOrderDetailsTitleChunk = new Chunk("Code Details", mOrderDetailsTitleFont);
                            Paragraph mOrderDetailsTitleParagraph = new Paragraph(mOrderDetailsTitleChunk);
                            mOrderDetailsTitleParagraph.setAlignment(Element.ALIGN_CENTER);
                            document.add(mOrderDetailsTitleParagraph);

                            document.add(new Paragraph(AppConstants.EMPTY_STRING));
                            document.add(Chunk.NEWLINE);
                            document.add(new Paragraph(AppConstants.EMPTY_STRING));
                            document.add(new Paragraph(AppConstants.EMPTY_STRING));
                            document.add(Chunk.NEWLINE);
                            document.add(new Paragraph(AppConstants.EMPTY_STRING));

                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            mCurrentGeneratedCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            Image codeImage = Image.getInstance(stream.toByteArray());
                            codeImage.setAlignment(Image.ALIGN_CENTER);
                            codeImage.scalePercent(40);
                            Paragraph imageParagraph = new Paragraph();
                            imageParagraph.add(codeImage);
                            document.add(imageParagraph);

                            document.add(new Paragraph(AppConstants.EMPTY_STRING));
                            document.add(Chunk.NEWLINE);
                            document.add(new Paragraph(AppConstants.EMPTY_STRING));

                            // Adding Chunks for Title and value
                            Font mOrderIdFont = new Font(baseFont, headingFontSize, Font.NORMAL, colorAccent);
                            Chunk mOrderIdChunk = new Chunk("Content:", mOrderIdFont);
                            Paragraph mOrderIdParagraph = new Paragraph(mOrderIdChunk);
                            document.add(mOrderIdParagraph);

                            Font mOrderIdValueFont = new Font(baseFont, valueFontSize, Font.NORMAL, BaseColor.BLACK);
                            Chunk mOrderIdValueChunk = new Chunk(getCurrentCode().getContent(), mOrderIdValueFont);
                            Paragraph mOrderIdValueParagraph = new Paragraph(mOrderIdValueChunk);
                            document.add(mOrderIdValueParagraph);

                            document.add(new Paragraph(AppConstants.EMPTY_STRING));
                            document.add(Chunk.NEWLINE);
                            document.add(new Paragraph(AppConstants.EMPTY_STRING));

                            // Fields of Order Details...
                            Font mOrderDateFont = new Font(baseFont, headingFontSize, Font.NORMAL, colorAccent);
                            Chunk mOrderDateChunk = new Chunk("Type:", mOrderDateFont);
                            Paragraph mOrderDateParagraph = new Paragraph(mOrderDateChunk);
                            document.add(mOrderDateParagraph);

                            Font mOrderDateValueFont = new Font(baseFont, valueFontSize, Font.NORMAL, BaseColor.BLACK);
                            Chunk mOrderDateValueChunk = new Chunk(type, mOrderDateValueFont);
                            Paragraph mOrderDateValueParagraph = new Paragraph(mOrderDateValueChunk);
                            document.add(mOrderDateValueParagraph);

                            document.close();

                            setCurrentPrintedFile(codeDocumentFile);
                            if (!emitter.isDisposed()) {
                                emitter.onComplete();
                            }
                        } catch (IOException | DocumentException ie) {
                            if (!emitter.isDisposed()) {
                                emitter.onError(ie);
                            }
                        } catch (ActivityNotFoundException ae) {
                            if (!emitter.isDisposed()) {
                                emitter.onError(ae);
                            }
                        }
                    } else {
                        if (!emitter.isDisposed()) {
                            emitter.onError(new NullPointerException());
                        }
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribeWith(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                ProgressDialogUtil.on().hideProgressDialog();
                                Toast.makeText(getContext(),
                                        getString(R.string.saved_the_code_successfully),
                                        Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (e != null && !TextUtils.isEmpty(e.getMessage())) {
                                    Log.e(getClass().getSimpleName(), e.getMessage());
                                }

                                ProgressDialogUtil.on().hideProgressDialog();
                                Toast.makeText(getContext(),
                                        getString(R.string.failed_to_save_the_code),
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
        );
    }

    private void shareCode(File codeImageFile) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(getContext(),
                    getString(R.string.file_provider_authority), codeImageFile));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(codeImageFile));
        }

        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_code_using)));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean isValid = true;

        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                isValid = false;
            }
        }



        switch (requestCode) {
//            case REQUEST_CODE_TO_SAVE:
//                if (isValid) {
//                    if (getCurrentCodeFile() == null) {
//                        storeCodeImage(true);
//                    } else {
//                        Toast.makeText(this,
//                                getString(R.string.generated_qr_code_already_exists),
//                                Toast.LENGTH_SHORT).show();
//                    }
//                }
//                break;
//
//            case REQUEST_CODE_TO_PRINT:
//                if (isValid) {
//                    if (getCurrentPrintedFile() == null) {
//                        storeCodeDocument();
//                    } else {
//                        Toast.makeText(this,
//                                getString(R.string.generated_qr_code_already_exists),
//                                Toast.LENGTH_SHORT).show();
//                    }
//                }
//                break;
//
//            case REQUEST_CODE_TO_SHARE:
//                if (isValid) {
//                    if (getCurrentCodeFile() == null) {
//                        storeCodeImage(false);
//
//                        if (getCurrentCodeFile() != null) {
//                            shareCode(getCurrentCodeFile());
//                        } else {
//                            Toast.makeText(this,
//                                    getString(R.string.failed_to_share_the_code), Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        shareCode(getCurrentCodeFile());
//                    }
//                }
//                break;

            default:
                break;
        }
    }
}

//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.database.Cursor;
//import android.graphics.Bitmap;
//import android.graphics.drawable.Drawable;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Environment;
//import android.provider.MediaStore;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.core.content.ContextCompat;
//import androidx.fragment.app.Fragment;
//import androidx.lifecycle.Observer;
//import androidx.lifecycle.ViewModelProvider;
//import androidx.loader.content.CursorLoader;
//
//import com.example.aqrc.R;
//import com.example.aqrc.helpers.constant.AppConstants;
//import com.example.aqrc.helpers.constant.IntentKey;
//import com.example.aqrc.helpers.constant.PreferenceKey;
//import com.example.aqrc.helpers.model.Code;
//import com.example.aqrc.helpers.util.FileUtil;
//import com.example.aqrc.helpers.util.ProgressDialogUtil;
//import com.example.aqrc.helpers.util.SharedPrefUtil;
//import com.example.aqrc.helpers.util.image.ImageInfo;
//import com.example.aqrc.helpers.util.image.ImagePicker;
//import com.example.aqrc.ui.pickedfromgallery.PickedFromGalleryActivity;
//import com.example.aqrc.ui.scanresult.ScanResultActivity;
//import com.google.android.gms.vision.barcode.Barcode;
//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.BinaryBitmap;
//import com.google.zxing.DecodeHintType;
//import com.google.zxing.LuminanceSource;
//import com.google.zxing.MultiFormatReader;
//import com.google.zxing.MultiFormatWriter;
//import com.google.zxing.RGBLuminanceSource;
//import com.google.zxing.Reader;
//import com.google.zxing.Result;
//import com.google.zxing.ResultPoint;
//import com.google.zxing.WriterException;
//import com.google.zxing.client.android.BeepManager;
//import com.google.zxing.common.BitMatrix;
//import com.google.zxing.common.HybridBinarizer;
//import com.google.zxing.integration.android.IntentIntegrator;
//import com.journeyapps.barcodescanner.BarcodeCallback;
//import com.journeyapps.barcodescanner.BarcodeResult;
//import com.journeyapps.barcodescanner.DecoratedBarcodeView;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.Hashtable;
//import java.util.List;
//import java.util.Locale;
//



//public class GenerateCodeFragment extends Fragment {
//
//    private DashboardViewModel dashboardViewModel;
//    private Barcode barcodeResult;
//    public final static int QRcodeWidth = 350 ;
//        ImageView imageView;
//        Button button;
//        Button btnScan;
//        EditText editText;
//        String EditTextValue ;
//        Thread thread ;
//        Bitmap bitmap ;
//
//        TextView tv_qr_readTxt;
//
//
//
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//
//
//        dashboardViewModel =
//                new ViewModelProvider(this).get(DashboardViewModel.class);
//        View root = inflater.inflate(R.layout.fragment_generate, container, false);
//       // final TextView textView = root.findViewById(R.id.text_dashboard);
//        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//            //    textView.setText(s);
//            }
//        });
//
//        imageView = (ImageView) root.findViewById(R.id.imageView);
//        editText = (EditText) root.findViewById(R.id.editText);
//        button = (Button) root.findViewById(R.id.button);
//        btnScan = (Button) root.findViewById(R.id.btnScan);
//        tv_qr_readTxt = (TextView) root.findViewById(R.id.tv_qr_readTxt);
//
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//
//                if(!editText.getText().toString().isEmpty()){
//                    EditTextValue = editText.getText().toString();
//
//                    try {
//                        bitmap = TextToImageEncode(EditTextValue);
//
//                        imageView.setImageBitmap(bitmap);
//
//                    } catch (WriterException e) {
//                        e.printStackTrace();
//                    }
//                }
//                else{
//                    editText.requestFocus();
//                    Toast.makeText(getActivity(), "Please Enter Your Scanned Test" , Toast.LENGTH_LONG).show();
//                }
//
//            }
//        });
//
//
//        btnScan.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                IntentIntegrator integrator = new IntentIntegrator(getActivity());
//                integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
//                integrator.setPrompt("Scan");
//                integrator.setOrientationLocked(false);
//                integrator.setCameraId(0);
//                integrator.setBeepEnabled(false);
//                integrator.setBarcodeImageEnabled(false);
//                integrator.initiateScan();
//
//            }
//        });
//        return root;
//    }
//
//    Bitmap TextToImageEncode(String Value) throws WriterException {
//        BitMatrix bitMatrix;
//        try {
//            bitMatrix = new MultiFormatWriter().encode(
//                    Value,
//                    BarcodeFormat.DATA_MATRIX.QR_CODE,
//                    QRcodeWidth, QRcodeWidth, null
//            );
//
//        } catch (IllegalArgumentException Illegalargumentexception) {
//
//            return null;
//        }
//        int bitMatrixWidth = bitMatrix.getWidth();
//
//        int bitMatrixHeight = bitMatrix.getHeight();
//
//        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];
//
//        for (int y = 0; y < bitMatrixHeight; y++) {
//            int offset = y * bitMatrixWidth;
//
//            for (int x = 0; x < bitMatrixWidth; x++) {
//
//                pixels[offset + x] = bitMatrix.get(x, y) ?
//                        getResources().getColor(R.color.black):getResources().getColor(R.color.white);
//            }
//        }
//        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);
//
//        bitmap.setPixels(pixels, 0, 350, 0, 0, bitMatrixWidth, bitMatrixHeight);
//        return bitmap;
//    }
//
//    private String formatToEncode(String value){
//        String domain = "https://aqrc.app/";
//        String uuid = "";
//        String hash = "";
//        String timestamp = "";
//
//                return "";
//    }
//
////    @Override
////    public void onActivityResult(int requestCode, int resultCode, Intent data) {
////        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
////        if(result != null) {
////            if(result.getContents() == null) {
////                Log.e("Scan*******", "Cancelled scan");
////
////            } else {
////                Log.e("Scan", "Scanned");
////
////                tv_qr_readTxt.setText(result.getContents());
////                Toast.makeText(getContext(), "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
////            }
////        } else {
////            // This is important, otherwise the result will not be passed to the fragment
////            super.onActivityResult(requestCode, resultCode, data);
////        }
////    }
//
//
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//    }
//}

