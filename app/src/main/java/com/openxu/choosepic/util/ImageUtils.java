package com.openxu.choosepic.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * author : openXu
 * create at : 2016/8/1 13:13
 * blog : http://blog.csdn.net/xmxkf
 * gitHub : https://github.com/openXu
 * project : ChoosePic
 * class name : ImageUtils
 * version : 1.0
 * class describe：
 */
public class ImageUtils {

    private static String TAG ="ImageUtils";

    /**
     * 压缩好比例大小后再进行质量压缩
     * @param srcPath 原图片路径
     * @param upDir 压缩之后的缓存路径
     * @return
     */
    public static File getimage(String srcPath, String upDir) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;
        float ww = 480f;
        // 缩放比，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (h > hh || w > ww) {
            final int heightRatio = Math.round((float) h/ (float) hh);
            final int widthRatio = Math.round((float) w / (float) ww);
            be = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

       /* if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;*/
        Log.d(TAG, "比例压缩之前图片大小为："+w+  "*" + h +",比例缩放比："+be);
        newOpts.inSampleSize = be;// 设置缩放比例
        newOpts.inJustDecodeBounds = false;
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        Log.d(TAG, "比例压缩之后图片大小为："+bitmap.getWidth()+  "*" + bitmap.getHeight());
        bitmap = compressImage(bitmap);
        File bitFile = saveBitMaptoSdcard(bitmap, upDir);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int options = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);// 质量压缩方法，这里100表示不
            Log.d(TAG, "压缩之后图片大小为："+  ",size=" + (baos.size()/1024)+"kb");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitFile;
    }

    /**
     * 将图片压缩到200kb以内
     * @param image
     * @return
     */
    private static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 100;
        image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        while (baos.toByteArray().length / 1024 > 200) { // 循环判断如果压缩后图片是否大于500kb,大于继续压缩
            Log.i(TAG, "压缩后图片大小："+(baos.toByteArray().length / 1024)+",太大，继续压缩");
            baos.reset();// 重置baos即清空baos
            options -= 10;// 每次都减少10
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    /**
     * 将图片存储到指定文件夹
     * @param bitmap
     * @param dir
     * @return
     */
    private static File saveBitMaptoSdcard(Bitmap bitmap, String dir) {
        // 得到外部存储卡的路径
        // ff.png是将要存储的图片的名称
        File dir_file = new File(dir);
        if (!dir_file.exists() && !dir_file.isDirectory()) {
            // 文件夹不存在，则创建文件夹
            dir_file.mkdir();
        }
        File file = new File(dir, "/"+ System.currentTimeMillis() + ".png");
        Log.v(TAG, "svaeBitmapSdacrd===" + file.getAbsolutePath());
        // 从资源文件中选择一张图片作为将要写入的源文件
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

}
