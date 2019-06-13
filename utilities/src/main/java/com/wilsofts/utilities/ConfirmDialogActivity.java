package com.wilsofts.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ConfirmDialogActivity extends AppCompatActivity {

    public static Intent confirm(Context context, String title, String message) {
        Intent intent = new Intent(context, ConfirmDialogActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("message", message);
        bundle.putString("type", "confirm");

        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_confirm_dialog);

        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            Intent response = new Intent();

            TextView dialog_title = this.findViewById(R.id.dialog_title);
            dialog_title.setText(extras.getString("title"));

            TextView dialog_message = this.findViewById(R.id.dialog_message);
            dialog_message.setText(extras.getString("message"));

            this.findViewById(R.id.confirm_cancel).setOnClickListener(view -> {
                response.putExtra("proceed", 0);
                this.setResult(Activity.RESULT_OK, response);
                this.finish();
            });

            this.findViewById(R.id.confirm_ok).setOnClickListener(view -> {
                response.putExtra("proceed", 1);
                this.setResult(Activity.RESULT_OK, response);
                this.finish();
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LibUtils.dialogWindow(this.getWindow());
    }
}
