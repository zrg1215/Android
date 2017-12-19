package com.hp.choosephoto.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileUtil {

    /**
     * <p>
     * 将文件转成base64 字符串
     * </p>
     *
     * @param path 文件路径
     * @return
     * @throws Exception
     */
    public static String encodeBase64File(String path) throws Exception {
        if (path == null) {
            return "";
        }
        File file = new File(path);
        FileInputStream inputFile = new FileInputStream(file);
        byte[] buffer = new byte[(int) file.length()];
        inputFile.read(buffer);
        inputFile.close();
        return Base64.encodeToString(buffer, Base64.NO_WRAP);
    }

    /**
     * bitmap转为base64
     *
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    //资源图片转换为bitmap
    public static Bitmap getBitMapByResId(Context mContext, int res_id,
                                          int... inSampleSize) {
        Bitmap result = null;
        int _inSampleSize = 1;
        if (inSampleSize != null && inSampleSize.length > 0) {
            _inSampleSize = inSampleSize[0];
        }

        InputStream is = null;
        is = mContext.getResources().openRawResource(res_id);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = _inSampleSize; // width，hight设为原来的_inSampleSize分一
        result = BitmapFactory.decodeStream(is, null, options);
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                // e.printStackTrace();
            } finally {
                is = null;
            }
        }
        return result;
    }

    /**
     * 网络图片转成Bitmap
     *
     * @param src 图片路径
     *            注意：需要在子线程调用，否则报NetworkOnMainThreadException异常
     */
    public static Bitmap netPicToBmp(String src) {
        if (TextUtils.isEmpty(src)) {
            return null;
        }
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream is = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(is);
            is.close();
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 图片sd地址 上传服务器时把图片调用下面方法压缩后 保存到临时文件夹 图片压缩后小于100KB，失真度不明显
     **/
    public static Bitmap revitionImageSize(String path, String fname)
            throws IOException {
        Bitmap bitmap = null;
        BufferedInputStream in = null;
        FileInputStream fin = null;
        File file = null;

        try {
            makeRootDirectory(path);
            file = new File(path + fname);
            fin = new FileInputStream(file);
            in = new BufferedInputStream(fin);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            in.close();
            fin.close();
            fin = null;
            in = null;
            int i = 0;
            while (true) {
                if ((options.outWidth >> i <= 1000)
                        && (options.outHeight >> i <= 1000)) {
                    fin = new FileInputStream(new File(path + fname));
                    in = new BufferedInputStream(fin);
                    options.inSampleSize = (int) Math.pow(2.0D, i);
                    options.inJustDecodeBounds = false;
                    bitmap = BitmapFactory.decodeStream(in, null, options);
                    break;
                }
                i += 1;
            }
        } catch (Exception ex) {
            //L.e("uploadImg", ex.getMessage());
        } finally {
            if (in != null) {
                in.close();
            }
            if (fin != null) {
                fin.close();
            }
        }

        return bitmap;
    }

    public static Bitmap getBitmap(String fpath) {
        if (checkSDCardExist()) {
            try {
                File file = new File(fpath);
                if (file.exists()) {
                    Bitmap bm = BitmapFactory.decodeFile(fpath);
                    return bm;
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static Bitmap getBitmapBySampleSize(String fpath, int inSampleSize) {
        if (checkSDCardExist()) {
            try {
                File file = new File(fpath);
                if (file.exists()) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = inSampleSize;// 图片宽高都为原来的二分之一，即图片为原来的四分之一
                    Bitmap bm = BitmapFactory.decodeFile(fpath, options);
                    return bm;
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    /**
     * 保存图片，compressVal压缩至可选，默认90
     **/
    public static void saveBitmap(Bitmap bm, String f_dir, String fname, int picType,
                                  int... compressVal) {
        if (bm == null)
            return;

        int comp_val = 100;
        if (compressVal != null && compressVal.length > 0) {
            comp_val = compressVal[0];
        }

        FileOutputStream out = null;
        try {
            // 目录不存在则创建
            makeRootDirectory(f_dir);
            File fullPath = new File(f_dir + fname);
            fullPath.createNewFile();
            out = new FileOutputStream(fullPath);
            if (picType == 0) {//png
                bm.compress(Bitmap.CompressFormat.PNG, comp_val, out);
            } else {
                bm.compress(Bitmap.CompressFormat.JPEG, comp_val, out);
            }
            out.flush();
            out.close();
            out = null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                out = null;
            }
        }
    }

    /**
     * 保存图片，compressVal压缩至可选，默认90
     **/
    public static void saveBitmap(Bitmap bm, String fpath, int... compressVal) {
        if (bm == null)
            return;

        int comp_val = 100;
        if (compressVal != null && compressVal.length > 0) {
            comp_val = compressVal[0];
        }

        FileOutputStream out = null;
        try {
            // 目录不存在则创建
            String f_dir_name = fpath.substring(0, fpath.lastIndexOf("/") + 1);
            String f_name = fpath.substring(fpath.lastIndexOf("/") + 1);
            File f = makeFile(f_dir_name, f_name);
            if (f == null) {
                return;
            }

            out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, comp_val, out);

            out.flush();
            out.close();
            out = null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                out = null;
            }
        }
    }

    /*
     * 保存图片，且返回保存图片的路径
     */
    public static String saveBitmapToSD(String targetPath, Bitmap mBitmap) {
        ByteArrayOutputStream stream = null;
        FileOutputStream os = null;
        File f;
        try {
            // 目录不存在则创建
            String f_dir_name = targetPath.substring(0, targetPath.lastIndexOf("/") + 1);
            String f_name = targetPath.substring(targetPath.lastIndexOf("/") + 1);
            f = makeFile(f_dir_name, f_name);
            if (f == null) {
                return "";
            }

            stream = new ByteArrayOutputStream();
            mBitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
            os = new FileOutputStream(f);
            os.write(stream.toByteArray());
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } catch (Exception e) {
        } finally {
            f = null;
            try {
                if (stream != null) {
                    stream.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return targetPath;
    }

    public static Bitmap compressImageFromFile(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;// 只读边,不读内容
        BitmapFactory.decodeFile(srcPath, newOpts);//初始化 newOpts 的属性，不能删掉
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float hh = 800f;//
        float ww = 480f;//
        int be = 1;
        if (w > h && w > ww) {
            be = (int) (w / ww);
        } else if (w < h && h > hh) {
            be = (int) (h / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置采样率

        newOpts.inPreferredConfig = Config.ARGB_8888;// 该模式是默认的,可不设
        newOpts.inPurgeable = true;// 同时设置才会有效
        newOpts.inInputShareable = true;// 。当系统内存不够时候图片自动被回收

        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        } catch (OutOfMemoryError e) {
        }

        int degree = readPictureDegree(srcPath);//获取相片拍摄角度
        if (degree != 0) {//旋转照片角度，防止头像横着显示
            bitmap = rotateBitmap(bitmap, degree);
        }
        return bitmap;
    }

    //region 压缩图片
    public static Bitmap compressImage(String filePath) {
        Bitmap bitmap = getSmallBitmap(filePath);//获取一定尺寸的图片
        int degree = readPictureDegree(filePath);//获取相片拍摄角度
        if (degree != 0) {//旋转照片角度，防止头像横着显示
            bitmap = rotateBitmap(bitmap, degree);
        }
        return bitmap;
    }

    private static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//只解析图片边沿，获取宽高
        BitmapFactory.decodeFile(filePath, options);
        // 计算缩放比
        options.inSampleSize = calculateInSampleSize(options, 480, 800);
        // 完整解析图片返回bitmap
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 获取照片角度
     *
     * @param path
     * @return
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 旋转照片
     *
     * @param bitmap
     * @param degress
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int degress) {
        if (bitmap != null) {
            Matrix m = new Matrix();
            m.postRotate(degress);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), m, true);
            return bitmap;
        }
        return bitmap;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
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
    //endregion

    public static Bitmap getBitmapFrmSD(String fpath) {
        if (checkSDCardExist()) {
            try {
                File file = new File(fpath);
                if (file.exists()) {
                    Bitmap bm = BitmapFactory.decodeFile(fpath);
                    return bm;
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static File getFile(String fpath) {
        if (checkSDCardExist()) {
            try {
                File file = new File(fpath);
                if (file.exists()) {
                    return file;
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static boolean isFileExists(String fpath) {
        if (checkSDCardExist()) {
            try {
                File file = new File(fpath);
                if (file.exists()) {
                    return true;
                }
            } catch (Exception e) {
            }
        }
        return false;
    }

    /**
     * mkdir()：只能创建一层目录 ;mkdirs():创建多层目录
     **/
    public static boolean makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                return file.mkdirs();
            }
        } catch (Exception e) {
            Log.e("makeRootDirectory:Error", e.getMessage());
        }
        return false;
    }

    /**
     * 创建多层目录
     **/
    public static File makeFile(String filePath, String fName) {
        makeRootDirectory(filePath);
        return new File(filePath, fName);
    }

    /**
     * <p>
     * 将base64字符解码保存文件
     * </p>
     *
     * @param base64Code
     * @param targetPath
     * @throws Exception
     */
    public static String decoderBase64File(String base64Code, String targetPath)
            throws Exception {
        byte[] buffer = Base64.decode(base64Code, Base64.NO_WRAP);
        FileOutputStream out = new FileOutputStream(targetPath);
        out.write(buffer);
        out.close();
        return targetPath;
    }

    /**
     * 删除文件
     *
     * @param fname 文件名
     */
    public static boolean delFile(String fname) {
        try {
            File file = new File(fname);
            if (file.exists() && file.isFile()) {
                return file.delete();
            }
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 删除文件夹所有文件，并删除文件夹
     *
     * @param fpath 文件名路径
     */
    public static void deleteDir(String fpath) {
        if (TextUtils.isEmpty(fpath)) return;
        try {
            File dir = new File(fpath);
            if (dir == null || !dir.exists() || !dir.isDirectory()) return;

            for (File file : dir.listFiles()) {
                if (file.isFile())
                    file.delete(); // 删除所有文件
                else if (file.isDirectory())
                    deleteDir(fpath); // 递规的方式删除文件夹
            }
            dir.delete();// 删除目录本身
        } catch (Exception e) {
        }
    }

    /**
     * 根据路径获取文件名称
     **/
    public static String getFNameByFPath(String fpath) {
        if (TextUtils.isEmpty(fpath))
            return "";

        int start = 0, end = fpath.length();
        int first_char_index = 0, last_char_index;

        first_char_index = fpath.lastIndexOf("/");
        // last_char_index = fpath.lastIndexOf(".");

        if (first_char_index >= 0) {
            start = first_char_index;
        }
        // if (last_char_index >= 2) {
        // end = last_char_index;
        // }
        // String fname = fpath.substring(start, end - 1);

        String fname = fpath.substring(start + 1);

        return fname;
    }

    /**
     * 根据web url获取删除域名后文件夹名称
     **/
    public static String getFilePathRemoveDomain(String webURL) {
        if (TextUtils.isEmpty(webURL))
            return "";

        int start = 0;
        String fname = "";

        start = webURL.indexOf("com/");
        if (start >= 0) {
            start = webURL.indexOf("com/") + "com/".length() - 1;
        } else {
            start = webURL.indexOf("/");
        }

        if (start >= 0 && (start + 1) < webURL.length()) {
            fname = webURL.substring(start + 1);
            fname = fname.replace("/", "_");
        } else {
            fname = webURL.substring(0);
            fname = fname.replace("/", "_");
        }

        return fname;
    }

    /**
     * 去除文件扩展名称
     **/
    public static String removeExtByFPath(String fpathWithName) {
        if (TextUtils.isEmpty(fpathWithName))
            return "";

        int last_char_index;
        last_char_index = fpathWithName.lastIndexOf(".");
        if (last_char_index > 1) {
            return fpathWithName.substring(0, last_char_index - 1);
        }
        return fpathWithName;
    }

    /**
     * 获取sd卡是否有空间存储图片
     **/
    public static boolean checkSDFreeSizeIsFull(Bitmap bitmap) {
        return getSDFreeSizeToByte() > (bitmap.getRowBytes() * bitmap.getHeight());
    }

    public static boolean checkSDFreeSizeIsFull(long byteSize) {
        return getSDFreeSizeToByte() < byteSize;
    }

    /**
     * 获取sd卡是否存在
     **/
    public static boolean checkSDCardExist() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD的可用空间（单位Byte）
     **/
    public static long getSDFreeSizeToByte() {
        if (checkSDCardExist()) {
            //取得SD卡文件路径
            File path = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(path.getPath());
            //获取单个数据块的大小(Byte)
            long blockSize = sf.getBlockSize();
            //空闲的数据块的数量
            long freeBlocks = sf.getAvailableBlocks();
            //返回SD卡空闲大小
            return freeBlocks * blockSize; //单位Byte
        }
        return -1;
    }

}
