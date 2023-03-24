package cn.kuaicode.gensignature;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.math.BigInteger;
import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity {

    private EditText etInput;
    private TextView tvShow;
    private Button btnCopy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etInput = findViewById(R.id.et);
        tvShow = findViewById(R.id.tv);
        btnCopy = findViewById(R.id.btn_copy);
        TextView github = findViewById(R.id.github);

        github.setOnClickListener(v -> {
            String url = "https://github.com/flutterbest/GenSignature.git"; // 替换为您想要打开的网址
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        //接受传值
        Intent intent = getIntent();//获得上个界面传过来的数据
        if (intent.hasExtra("packageName")) {
            etInput.setText(intent.getStringExtra("packageName"));
            genSign();
        }
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_select:
                Intent intent = new Intent(MainActivity.this, AppActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_gen:
                genSign();
                break;
            case R.id.btn_copy:
                copyStr(tvShow.getText().toString());
                break;

            default:

        }
    }

    /**
     * 获取签名
     */
    private void genSign() {
        String input = etInput.getText().toString().trim();
        if (TextUtils.isEmpty(input)) {
            Toast.makeText(MainActivity.this, "请填写正确包名!", Toast.LENGTH_SHORT).show();
            return;
        }
        String sign = gen(input);
        if (TextUtils.isEmpty(sign)) {
            btnCopy.setVisibility(View.INVISIBLE);
            return;
        }
        tvShow.setText(sign);
        btnCopy.setVisibility(View.VISIBLE);
    }

    /**
     * 获取签名
     */
    private String gen(String packageName) {
        String sign = "";
        try {
            @SuppressLint("PackageManagerGetSignatures") Signature[] signatures = getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures;
            if (signatures.length > 0) {
                MessageDigest digest = MessageDigest.getInstance("MD5");
                digest.update(signatures[0].toByteArray());
                BigInteger bigInteger = new BigInteger(1, digest.digest());
                sign = bigInteger.toString(16);
                Log.d("msg", "获取到包名 " + packageName + " 下的签名为: " + sign);
                Toast.makeText(MainActivity.this, "签名获取成功", Toast.LENGTH_LONG).show();

            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "签名获取失败", Toast.LENGTH_LONG).show();
        }
        return sign;
    }

    /**
     * copy
     */
    private void copyStr(String str) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("", str));
        Toast.makeText(MainActivity.this, "签名复制成功", Toast.LENGTH_SHORT).show();
    }
}
