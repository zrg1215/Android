#
![image](https://github.com/zrg1215/SelectImage/blob/master/gif/test.gif)

<p>此demo用于选择本地照片、拍照。
<p>demo结构：app->demo入口，choosephoto->移植好的module
<p>demo特点：
    <p>1、6.0以上权限判断，封装在CheckPermissionUtils工具类，使用放在BaseActivity中
    <p>2、使用线程池加载本地图片列表、压缩
    <p>3、本地图片的展示
    <p>4、图片预览手势操作，单击可全屏展示
<p>demo使用注意事项：
    <p>1、CheckPermissionUtils没权限弹窗暂时没有实现
    <p>2、页面传值使用了startForResult()，所以在得到压缩图片地址后，需要重写onActivityResult，建议封装在主module的BaseActivity
    <p>3、在使用startForResult()时，CHOOSE_MULTIPLE_PHOTO_CODE、CHOOSE_TAKE_PICTURE_CODE等值就不要使用了
    <p>4、依赖module使用的Theme可定制哈
<p>本地图片展示遇到的问题和解决方法：
    看AlbumActivity->getImages()代码注释吧

