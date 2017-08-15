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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.cw.library.compressor.CompressConfig;
import com.cw.library.compressor.ImageCompressor;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int RESULT_PICK_IMAGE = 200;//选择图片后的结果码
    //总文件夹路径
    private static String sDir = Environment.getExternalStorageDirectory().getPath() + "/takePhotoUtils/img/";
    //文件夹路径
    private String mPath = sDir + System.currentTimeMillis() + "/";

    private ImageCompressor mImageCompressor;
    private ImageView iv_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageCompressor = new ImageCompressor.Builder(getApplication())
                //WebP是google开发的一种旨在加快图片加载速度的图片格式
                .setCompressFormat(CompressConfig.COMPRESS_WEBP)
                .setMaxX(1028)
                .setMaxY(1028)
                .setMaxSize(700)
                .setOpenProcess(false)
                .build();

        iv_content = (ImageView) findViewById(R.id.iv_content);
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
        mImageCompressor.recycle();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (!makeDirs(mPath)) {
            Log.i("TakePhotoUtils", mPath + "Directory not find");
            return;
        }

        String pickPhotoPath = getPickPhotoPath(this, data);
        String compressPath = mPath + System.currentTimeMillis() + "compress.webp";

        mImageCompressor.compress(pickPhotoPath, compressPath, new ImageCompressor.CompressImageListener() {
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

    private static boolean makeDirs(String filePath) {
        String folderName = getFolderName(filePath);
        if (folderName == null || folderName.length() == 0) {
            return false;
        }

        File folder = new File(folderName);
        return (folder.exists() && folder.isDirectory()) || folder.mkdirs();
    }

    private static String getFolderName(String filePath) {
        if (filePath == null || filePath.length() == 0) {
            return filePath;
        }
        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? "" : filePath.substring(0, filePosi);
    }
}
