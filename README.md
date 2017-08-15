## 我为什么要使用多进程压缩图片？
#### 由于压缩图片需要加载和操作Bitmap，这需要的内存非常大，有的低配手机就非常容易出现OOM的情况。由于多进程可以扩展应用内存，于是就有了这个方案。这样的话就算压缩进程崩溃也不会影响主进程的运行。在这个库中对压缩进程出现异常情况也进行了处理，如果出现奔溃或者通讯异常都会把压缩任务交还到主进程处理，这样就减少了一些不可预期的问题。


### 使用方法
> 使用时把Demo中的compressor库粘贴到自己项目即可，当然可以打成arr或者jar包使用。需要使用的类只有一个ImageCompressor。

#### 1.初始化
```
ImageCompressor imageCompressor = new ImageCompressor.Builder(context)
        .setCompressFormat(CompressConfig.COMPRESS_WEBP)//压缩格式jpeg等
        .setMaxX(1028)//压缩后最大宽度（px）
        .setMaxY(1028)//压缩后最大高度（px）
        .setMaxSize(700)//压缩后最大大小（k）
        .setOpenProcess(true)//是否开启压缩进程
        .build();
```
#### 2.压缩图片
```
imageCompressor.compress(pickPhotoPath, compressPath
                    , new ImageCompressor.CompressImageListener() {
    @Override
    public void onCompressSuccess(String basePath, String imgPath) {
        //压缩成功
        imageView.setImageURI(Uri.parse("file://" + imgPath));
    }

    @Override
    public void onCompressFailed(String basePath) {
        //压缩失败
    }
});
```
#### 3.释放资源
>这里的释放资源其实是解绑压缩图片的服务进程，如果没有开启setOpenProcess(true)则不需要释放。如果不解绑服务进程也不会影响程序运行。

```
@Override
protected void onDestroy() {
    super.onDestroy();
    imageCompressor.recycle();
}
```
