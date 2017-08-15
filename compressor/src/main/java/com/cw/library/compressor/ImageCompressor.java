/**
 *   @function:$
 *   @description: $
 *   @param:$
 *   @return:$
 *   @history:
 * 1.date:$ $
 *           author:$
 *           modification:
 */

package com.cw.library.compressor;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.cw.library.compressor.utils.ThreadPoolManager;
import com.cw.library.compressor.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.cw.library.compressor.CompressConfig.COMPRESS_JPEG;
import static com.cw.library.compressor.CompressConfig.COMPRESS_PNG;
import static com.cw.library.compressor.CompressConfig.COMPRESS_WEBP;

/**
 * @author Cw
 * @date 17/8/15
 */
public class ImageCompressor {

    private static final String COMPRESS_SERVICE_ACTION = "com.cw.library.compressor.COMPRESS_ACTION";

    private static boolean sHasBind;

    private Context mContext;
    private CompressConfig mConfig;
    private ICompressService mService;
    private Handler UIHandle = new Handler(Looper.getMainLooper());
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = ICompressService.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
        }
    };

    public ImageCompressor(Context context) {
        this.mContext = context.getApplicationContext();
        this.mConfig = new CompressConfig();
    }


    public static class Builder {

        private ImageCompressor mImageCompressor;

        public Builder(Context context) {
            mImageCompressor = new ImageCompressor(context);
        }

        /**
         * 设置图片最大大小（k）
         */
        public Builder setMaxSize(int maxSize) {
            mImageCompressor.setMaxSize(maxSize);
            return this;
        }

        /**
         * 设置图片最大高度（px）
         */
        public Builder setMaxX(int maxX) {
            mImageCompressor.setMaxX(maxX);
            return this;
        }

        /**
         * 设置图片最大宽度（px）
         */
        public Builder setMaxY(int maxY) {
            mImageCompressor.setMaxY(maxY);
            return this;
        }

        /**
         * 压缩格式
         * CompressConfig.COMPRESS_JPEG
         * CompressConfig.COMPRESS_PNG
         * CompressConfig.COMPRESS_WEBP
         */
        public Builder setCompressFormat(int compressFormat) {
            mImageCompressor.setCompressFormat(compressFormat);
            return this;
        }

        /**
         * 是否使用多进程压缩
         */
        public Builder setOpenProcess(boolean openProcess) {
            mImageCompressor.setOpenProcess(openProcess);
            return this;
        }

        public ImageCompressor build() {
            if (mImageCompressor.getConfig().isOpenProcess()) {
                mImageCompressor.bindCompressService();
            }
            return mImageCompressor;
        }
    }

    public void setMaxSize(int maxSize) {
        if (mConfig != null) {
            mConfig.setMaxSize(maxSize);
        }
    }

    public void setMaxX(int maxX) {
        if (mConfig != null) {
            mConfig.setMaxX(maxX);
        }
    }

    public void setMaxY(int maxY) {
        if (mConfig != null) {
            mConfig.setMaxY(maxY);
        }
    }

    public void setCompressFormat(int compressFormat) {
        if (mConfig != null) {
            mConfig.setCompressFormat(compressFormat);
        }
    }

    public void setOpenProcess(boolean openProcess) {
        if (mConfig != null) {
            mConfig.setOpenProcess(openProcess);
        }
    }

    public CompressConfig getConfig() {
        return mConfig;
    }

    public void recycle() {
        unBindCompressService();
        mServiceConnection = null;
    }

    public void compress(final String basePath, final String compressPath, final CompressImageListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("CompressImageListener can not be null");
        }
        if (mConfig == null) {
            throw new IllegalArgumentException("CompressConfig can not be null");
        }
        if (mConfig.isOpenProcess() && mService != null && isServiceRunning(CompressorService.class)) {
            try {
                mService.compress(basePath, compressPath
                        , mConfig.getMaxSize(), mConfig.getMaxX()
                        , mConfig.getMaxY(), mConfig.getCompressFormat()
                        , new ICompressCallback.Stub() {
                            @Override
                            public void onCallBack(final String basePath, final String compress) throws RemoteException {
                                UIHandle.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!TextUtils.isEmpty(compress)) {
                                            Log.d("ImageCompressor", "Service:" + compress);
                                            listener.onCompressSuccess(basePath, compress);
                                        } else {
                                            listener.onCompressFailed(basePath);
                                        }
                                    }
                                });
                            }
                        });
                return;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        //如果压缩服务不在运行就开线程进行压缩
        ThreadPoolManager.getThreadProxyPool(1, 1, 0L).excute(new Runnable() {
            @Override
            public void run() {
                final String path = compressImage(basePath, compressPath
                        , mConfig.getMaxSize(), mConfig.getMaxX(), mConfig.getMaxY(), mConfig.getCompressFormat());
                UIHandle.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!TextUtils.isEmpty(path)) {
                            Log.d("ImageCompressor", "Thread:" + path);
                            listener.onCompressSuccess(basePath, path);
                        } else {
                            listener.onCompressFailed(basePath);
                        }
                    }
                });
            }
        });
    }

    private boolean isServiceRunning(Class clz) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (clz.getCanonicalName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void bindCompressService() {
        if (!sHasBind && mServiceConnection != null) {
            Intent intent = new Intent();
            intent.setAction(COMPRESS_SERVICE_ACTION);
            intent.setPackage(Utils.getCurrentPkgName(mContext));
            mContext.bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
            sHasBind = true;
        }
    }

    private void unBindCompressService() {
        if (sHasBind && mServiceConnection != null) {
            mContext.unbindService(mServiceConnection);
            sHasBind = false;
        }
    }

    /**
     * 图片压缩
     *
     * @param basePath       原图路径
     * @param compressPath   压缩后存放路径
     * @param maxSize        压缩后图片最大大小（k）
     * @param maxX           压缩后图片宽最大尺寸（px）
     * @param maxY           压缩后图片高最大尺寸（px）
     * @param compressFormat 压缩后图片格式
     * @return 压缩后存放路径（压缩失败返回为空字符串）
     */
    public static String compressImage(String basePath, String compressPath
            , int maxSize, int maxX, int maxY, int compressFormat) {

        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        switch (compressFormat) {
            case COMPRESS_JPEG:
                format = Bitmap.CompressFormat.JPEG;
                break;
            case COMPRESS_PNG:
                format = Bitmap.CompressFormat.PNG;
                break;
            case COMPRESS_WEBP:
                format = Bitmap.CompressFormat.WEBP;
                break;
        }

        int quality = 100;
        Bitmap bitmap = null;
        ByteArrayOutputStream baos = null;
        try {
            //压缩好比例大小后再进行质量压缩
            bitmap = getSmallBitmap(basePath, maxX, maxY);
            baos = new ByteArrayOutputStream();
            bitmap.compress(format, quality, baos);
            while (baos.toByteArray().length > maxSize * 1024) {
                //每超出200k质量减1
                int i = (baos.toByteArray().length / 1024 - maxSize) / 200;
                quality -= i > 0 ? i : 1;
                baos.reset();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            }
            FileOutputStream out = new FileOutputStream(compressPath);
            baos.writeTo(out);
        } catch (OutOfMemoryError oom) {
            oom.printStackTrace();
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            if (baos != null) {
                try {
                    baos.flush();
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bitmap != null) {
                bitmap.recycle();
                bitmap = null;
            }
        }
        return compressPath;
    }

    /**
     * 根据路径获得图片并压缩返回bitmap
     */
    private static Bitmap getSmallBitmap(String filePath, int maxX, int maxY) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, maxX, maxY);
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;//5.0之前有用
        options.inInputShareable = true;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 计算图片的缩放值
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    /**
     * 压缩监听
     */
    public interface CompressImageListener {
        /**
         * 压缩成功
         *
         * @param imgPath 压缩图片的路径
         */
        void onCompressSuccess(String basePath, String imgPath);

        /**
         * 压缩失败
         *
         * @param basePath 压缩失败的原图
         */
        void onCompressFailed(String basePath);
    }
}