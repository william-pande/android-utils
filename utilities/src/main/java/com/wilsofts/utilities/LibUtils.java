package com.wilsofts.utilities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.wilsofts.utilities.dialogs.ReturnResponse;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class LibUtils {
    private static final String TAG = "LIB UTILS";
    public static boolean SHOW_LOG = true;

    public static int CONNECT_TIMEOUT = 10;
    public static int READ_TIMEOUT = 10;
    public static int WRITE_TIMEOUT = 10;


    public static String URL_LINK = "";
    public static String AUTHORIZATION_BEARER = "";

    public static void setUrlLink(String link) {
        LibUtils.URL_LINK = link;
    }

    public static boolean noInternetConnection(@NonNull Context context, CoordinatorLayout coordinatorLayout) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }

        if (activeNetworkInfo == null) {
            LibUtils.showError(coordinatorLayout, "No network connection found");
        }
        return activeNetworkInfo == null;
    }

    public static boolean noInternetConnection(@NonNull Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo == null;
    }

    public static void showError(CoordinatorLayout coordinatorLayout, String error) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, error, Snackbar.LENGTH_LONG);

        View view = snackbar.getView();
        view.setBackgroundColor(Color.RED);

        TextView textView = view.findViewById(R.id.snackbar_text);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        textView.setTextColor(Color.WHITE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        } else {
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
        }

        snackbar.show();
    }

    public static void logE(String s) {
        if (LibUtils.SHOW_LOG) {
            Log.e(LibUtils.TAG, s);
        }
    }

    public static void logE(Throwable throwable) {
        if (LibUtils.SHOW_LOG) {
            Log.e(LibUtils.TAG, throwable.getMessage(), throwable);
        }
    }

    public static void showToast(FragmentActivity activity, String toast) {
        Toast.makeText(activity, toast, Toast.LENGTH_LONG).show();
    }

    public static void showErrorToast(FragmentActivity activity) {
        LibUtils.showToast(activity, activity.getString(R.string.request_unsuccessful));
    }

    public static void restart(@NonNull Intent intent, @NonNull Context context) {
        // Closing all the Activities from stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Add new Flag to start new Activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Staring Login Activity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        context.startActivity(intent);
    }

    public static boolean invalidEmail(@NonNull String email_address) {
        return email_address.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email_address).matches();
    }

    public static boolean invalidPassword(@NonNull String password) {
        String regex = "^[a-zA-Z0-9@\\\\#$%&*()_+\\]\\[';:?.,!^-]{6,}$";
        return !password.matches(regex);
    }

    public static boolean invalidName(@NonNull String name) {
        String expression = "^[a-zA-Z\\s]+";
        return !name.matches(expression);
    }

    public static String formatPhoneNumber(String country_code, String phone_number) {
        phone_number = phone_number.replaceAll("\\D", "");

        if (phone_number.startsWith("0")) {
            phone_number = phone_number.substring(1);
        } else if (phone_number.startsWith(country_code)) {
            phone_number = phone_number.substring(country_code.length());
        }
        return country_code + phone_number;
    }

    public static boolean invalidPhoneNumber(@NonNull String number) {
        return number.length() < 12 || number.length() > 16;
    }

    public static boolean invalidDouble(String number) {
        String DOUBLE_PATTERN = "^[0-9]+(\\.)?[0-9]*";
        String INTEGER_PATTERN = "\\d+";

        if (number.isEmpty()) {
            return true;
        }

        if (Pattern.matches(INTEGER_PATTERN, number)) {
            return false;
        } else {
            return !Pattern.matches(DOUBLE_PATTERN, number);
        }
    }

    public static void dialogWindow(Window window) {
        if (window != null) {
            Point point = new Point();
            Display display = window.getWindowManager().getDefaultDisplay();
            display.getSize(point);
            window.setLayout((int) (point.x * 0.9), ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
        }
    }

    public static String getDate(long timeStamp) {
        try {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat format = new SimpleDateFormat("dd MMMM, yyyy");
            Date netDate = (new Date(timeStamp * 1000));
            return format.format(netDate);
        } catch (Exception ex) {
            return "";
        }
    }

    @NotNull
    public static String shortDateTime(long timestamp) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("dd EEE MMM, yyyy hh:mm:ss a");
        Date date = new Date(timestamp * 1000);
        return format.format(date);
    }

    @NotNull
    public static String shortDate(long timestamp) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("dd EEE MMM, yyyy");
        Date date = new Date(timestamp * 1000);
        return format.format(date);
    }

    public static String getTime(long timestamp) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss a");
        Date date = new Date(timestamp * 1000);
        return format.format(date);
    }

    public static long dateLongToMillis(String string_date) {
        if (string_date == null) {
            return 0;
        }

        try {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = simpleDateFormat.parse(string_date);
            return date.getTime() / 1000;
        } catch (ParseException e) {
            LibUtils.logE(e);
        }
        return 0;
    }

    public static String dateLongToString(String date) {
        long timestamp = LibUtils.dateLongToMillis(date);
        return LibUtils.shortDate(timestamp);
    }

    public static long dateShortToMillis(String string_date) {
        if (string_date == null) {
            return 0;
        }

        try {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = simpleDateFormat.parse(string_date);
            return date.getTime() / 1000;
        } catch (ParseException e) {
            LibUtils.logE(e);
        }
        return 0;
    }

    public static String dateShortToString(String date) {
        long timestamp = LibUtils.dateShortToMillis(date);
        return LibUtils.shortDate(timestamp);
    }

    public static String formatDouble(double number) {
        return NumberFormat.getNumberInstance(Locale.getDefault()).format(number);
    }

    public static String formatNumber(double number) {
        @SuppressLint("DefaultLocale")
        String formatted = String.format("%,.2f", number);
        if (formatted.endsWith(".00"))
            return formatted.substring(0, formatted.length() - 3);
        else if (formatted.contains(".") && formatted.endsWith("0"))
            return formatted.substring(0, formatted.length() - 2);
        return formatted;
    }

    public static void confirmaAlert(Context context, String title, String message, int code) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyDialogTheme);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());

        builder.setPositiveButton("Proceed",
                (dialog, which) -> {
                    dialog.dismiss();
                    Intent intent = new Intent("app_receiver");
                    intent.putExtra("code", code);
                    intent.putExtra("proceed", true);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                });

        AlertDialog dialog = builder.create();
        LibUtils.dialogWindow(dialog.getWindow());
        dialog.show();
    }

    public static void extractDatabase(Context context, String database_name) {
        try {
            File storage_file = Environment.getExternalStorageDirectory();

            if (storage_file.canWrite()) {
                String database_path = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ?
                        context.getFilesDir().getAbsolutePath().replace("files", "databases") + File.separator :
                        context.getFilesDir().getPath() + context.getPackageName() + "/databases/";

                File current_database_file = new File(database_path, database_name);
                File backup_database_file = new File(storage_file, database_name + ".db");

                if (current_database_file.exists()) {
                    FileChannel source_file_channel = new FileInputStream(current_database_file).getChannel();
                    FileChannel destination_file_channel = new FileOutputStream(backup_database_file).getChannel();
                    destination_file_channel.transferFrom(source_file_channel, 0, source_file_channel.size());
                    source_file_channel.close();
                    destination_file_channel.close();
                }
            }
        } catch (IOException e) {
            LibUtils.logE(e);
        }
    }

    public static void listBundle(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                Object value = bundle.get(key);
                LibUtils.logE(String.format("%s %s (%s)", key, Objects.requireNonNull(value).toString(), value.getClass().getName()));
            }
        }
    }

    public static void listIntentData(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                Object value = bundle.get(key);
                LibUtils.logE(String.format("%s %s (%s)", key, Objects.requireNonNull(value).toString(), value.getClass().getName()));
            }
        }
    }

    public static void writeDBToSD(Context context, String db_name) {
        try {
            File sd = Environment.getExternalStorageDirectory();

            if (sd.canWrite()) {
                String DB_PATH;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    DB_PATH = context.getFilesDir().getAbsolutePath().replace("files", "databases") + File.separator;
                } else {
                    DB_PATH = context.getFilesDir().getPath() + context.getPackageName() + "/databases/";
                }

                File currentDB = new File(DB_PATH, db_name);
                File backupDB = new File(sd, db_name);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (IOException e) {
            LibUtils.logE(e);
        }
    }

    public static class RecyclerViewSpacing extends RecyclerView.ItemDecoration {
        private final int mItemOffset;

        public RecyclerViewSpacing(int itemOffset) {
            this.mItemOffset = itemOffset;
        }

        public RecyclerViewSpacing(@NonNull Context context, @DimenRes int itemOffsetId) {
            this(context.getResources().getDimensionPixelSize(itemOffsetId));
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(this.mItemOffset, this.mItemOffset, this.mItemOffset, this.mItemOffset);
        }
    }

    public static class ConfirmationDialog extends DialogFragment {

        public static ConfirmationDialog newInstance(Context context, String title, String message, ReturnResponse returnResponse) {
            return ConfirmationDialog.newInstance(title, message, context.getString(R.string.proceed), context.getString(R.string.cancel), Gravity.CENTER,
                    0, 0, returnResponse);
        }

        public static ConfirmationDialog newInstance(Context context, String title, String message, int gravity, int offset_x, int offset_y,
                                                     ReturnResponse returnResponse) {
            return ConfirmationDialog.newInstance(title, message, context.getString(R.string.proceed),
                    context.getString(R.string.cancel), gravity, offset_x, offset_y, returnResponse);
        }

        public static ConfirmationDialog newInstance(String title, String message, String ok, String cancel, int gravity, int offset_x,
                                                     int offset_y, ReturnResponse returnResponse) {
            ConfirmationDialog dialog = new ConfirmationDialog();

            Bundle extras = new Bundle();
            extras.putString("title", title);
            extras.putString("message", message);
            extras.putString("ok", ok);
            extras.putString("cancel", cancel);
            extras.putInt("gravity", gravity);
            extras.putInt("offset_y", offset_y);
            extras.putInt("offset_x", offset_x);
            extras.putSerializable("returnResponse", returnResponse);
            dialog.setArguments(extras);

            return dialog;
        }

        private Bundle extras;

        @NotNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            FragmentActivity activity = this.getActivity();
            this.extras = this.getArguments();
            assert this.extras != null;
            assert activity != null;

            ReturnResponse returnResponse = (ReturnResponse) this.extras.getSerializable("returnResponse");
            assert returnResponse != null;

            LayoutInflater inflater = this.requireActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_layout, null);
            ((TextView) view.findViewById(R.id.message_text)).setText(Html.fromHtml(this.extras.getString("message")));

            AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.MyDialogTheme)
                    .setView(view)
                    .setTitle(this.extras.getString("title"))
                    .setPositiveButton(this.extras.getString("ok"), (dialog, id) -> {
                        dialog.dismiss();
                        returnResponse.response(true);
                    })
                    .setNegativeButton(R.string.cancel, (dialog, id) -> {
                        dialog.dismiss();
                        returnResponse.response(false);
                    });


            return builder.create();
        }

        @Override
        public void onResume() {
            super.onResume();
            Window window = this.getDialog().getWindow();
            if (window != null) {
                Point point = new Point();
                Display display = window.getWindowManager().getDefaultDisplay();
                display.getSize(point);
                window.setLayout((int) (point.x * 0.9), ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setGravity(this.extras.getInt("gravity"));

                WindowManager.LayoutParams params = window.getAttributes();
                params.x = this.extras.getInt("offset_x");
                params.y = this.extras.getInt("offset_y");
                window.setAttributes(params);
            }
        }
    }

}
