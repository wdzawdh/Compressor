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

/**
 * @author Cw
 * @date 17/8/15
 */
public class CompressConfig {

    public static final int COMPRESS_JPEG = 0;
    public static final int COMPRESS_PNG = 1;
    public static final int COMPRESS_WEBP = 2;

    /**
     * 长或宽不超过的最大像素,单位px
     */
    private int mMaxX = 1280;
    private int mMaxY = 1280;

    /**
     * 压缩到的最大大小，单位KB
     */
    private int mMaxSize = 700;

    /**
     * 压缩格式
     */
    private int mCompressFormat = COMPRESS_JPEG;

    /**
     * 是否开启多进程
     */
    private boolean mIsOpenProcess = false;

    public int getMaxX() {
        return mMaxX;
    }

    public void setMaxX(int maxX) {
        mMaxX = maxX;
    }

    public int getMaxY() {
        return mMaxY;
    }

    public void setMaxY(int maxY) {
        mMaxY = maxY;
    }

    public int getMaxSize() {
        return mMaxSize;
    }

    public void setMaxSize(int maxSize) {
        mMaxSize = maxSize;
    }

    public int getCompressFormat() {
        return mCompressFormat;
    }

    public void setCompressFormat(int compressFormat) {
        if (compressFormat == COMPRESS_JPEG || compressFormat == COMPRESS_PNG || compressFormat == COMPRESS_WEBP) {
            mCompressFormat = compressFormat;
        }
    }

    public boolean isOpenProcess() {
        return mIsOpenProcess;
    }

    public void setOpenProcess(boolean openProcess) {
        mIsOpenProcess = openProcess;
    }

}