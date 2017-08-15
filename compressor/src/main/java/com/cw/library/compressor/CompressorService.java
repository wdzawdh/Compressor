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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.cw.library.compressor.utils.ThreadPoolManager;


/**
 * @author Cw
 * @date 17/8/15
 */
public class CompressorService extends Service {

    private final ICompressService.Stub mBinder = new ICompressService.Stub() {
        @Override
        public void compress(final String basePath, final String compressPath
                , final int maxSize, final int maxX, final int maxY, final int CompressFormat
                , final ICompressCallback callback) throws RemoteException {
            if (callback == null) {
                throw new IllegalArgumentException("ICompressCallback con not be null");
            }
            ThreadPoolManager.getThreadProxyPool().excute(new Runnable() {
                @Override
                public void run() {
                    String path = ImageCompressor.compressImage(basePath, compressPath, maxSize, maxX, maxY, CompressFormat);
                    try {
                        callback.onCallBack(basePath, path);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        //出现通讯异常就自杀
                        stopSelf();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                }
            });
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

}