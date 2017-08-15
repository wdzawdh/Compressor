// ICompressService.aidl
package com.cw.library.compressor;

// Declare any non-default types here with import statements

interface ICompressCallback {
    void onCallBack(String basePath,String compress);
}
