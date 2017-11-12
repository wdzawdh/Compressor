package com.cw.compressor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cw.library.compressor.CompressConfig;
import com.cw.library.compressor.ImageCompressor;

public class MainActivity extends AppCompatActivity {

    private static final int RESULT_PICK_IMAGE = 200;//选择图片后的结果码

    private ImageCompressor mImageCompressor;
    private ImageView iv_content;
    private ProgressBar pb_progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv_content = (ImageView) findViewById(R.id.iv_content);
        pb_progress = (ProgressBar) findViewById(R.id.pb_progress);

        mImageCompressor = new ImageCompressor.Builder(getApplication())
                //WebP是google开发的一种旨在加快图片加载速度的图片格式
                .setCompressFormat(CompressConfig.COMPRESS_WEBP)
                .setMaxWidth(1028)
                .setMaxHeight(1028)
                .setMaxSize(700)
                .setDestinationDir(Environment.getExternalStorageDirectory().getPath() + "/cw/image/")
                //开启压缩进程后需要在合适的地方通过stopCompressProcess关闭进程
                .startCompressProcess()
                .build();

        //选择图片
        findViewById(R.id.bt_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickPhotoBySystem(MainActivity.this);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //关闭进程(如果没有开启不需要关闭)
        mImageCompressor.stopCompressProcess();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        String pickPhotoPath = getPickPhotoPath(this, data);
        mImageCompressor.compress(pickPhotoPath, new ImageCompressor.CompressImageListener() {

            @Override
            public void onCompressStart(String basePath) {
                pb_progress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCompressFinish(String basePath) {
                pb_progress.setVisibility(View.GONE);
            }

            @Override
            public void onCompressSuccess(String basePath, String imgPath) {
                iv_content.setImageURI(Uri.parse("file://" + imgPath));
            }

            @Override
            public void onCompressFailed(String basePath) {
                //压缩失败
            }
        });
    }

    /**
     * 打开系统图库
     */
    private void pickPhotoBySystem(Activity act) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(act, "SD卡未挂载", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        act.startActivityForResult(i, RESULT_PICK_IMAGE);
    }

    /**
     * 获取图库中选择的图片的路径
     *
     * @param data 图库返回的Intent
     * @return 选择的图片路径
     */
    private String getPickPhotoPath(Context context, Intent data) {
        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            return picturePath;
        }
        return "";
    }

}
