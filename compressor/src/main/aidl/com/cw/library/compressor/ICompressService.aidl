// ICompressService.aidl
package com.cw.library.compressor;

// Declare any non-default types here with import statements
import com.cw.library.compressor.ICompressCallback;

interface ICompressService {
    void compress(String basePath, String compressPath
    ,int maxSize,int maxX,int maxY,int compressFormat,ICompressCallback callback);
}
