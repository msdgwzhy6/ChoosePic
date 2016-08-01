package com.openxu.choosepic;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.openxu.choosepic.util.BitmapCache;
import com.openxu.choosepic.util.Constant;
import com.openxu.choosepic.util.FileUtils;
import com.openxu.choosepic.util.ImageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";


    public static final int REQUEST_PICS = 1;
    private GridView gridView;
    private GridAdapter adapter;

    private String picDir;
    private ArrayList<String> choosed;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridView = (GridView)findViewById(R.id.gridView);

        choosed = new ArrayList<>();
        //创建缓存选中的图片的文件夹（需要一级一级创建）
        picDir = Constant.LEGALD_AILY_SD;
        File file =  new File(picDir);
        if(!file.exists()||!file.isDirectory()){
            file.mkdir();
        }
        picDir = Constant.PICS_DIR;
        file =  new File(picDir);
        if(!file.exists()||!file.isDirectory()){
            file.mkdir();
        }

        adapter = new GridAdapter();
        gridView.setAdapter(adapter);

    }

    public void choose(View v){
        //删除缓存文件中的照片
        FileUtils.delAllFile(picDir);
        FileUtils.delAllFile(Constant.UPLOAD_DIR);
        Intent intent = new Intent(this, ChoosePhotoActivity.class);
        intent.putStringArrayListExtra("choosed", choosed);
        startActivityForResult(intent, REQUEST_PICS);
    }
    private ProgressDialog mDirDialog;
    public void upload(View v){

        mDirDialog = ProgressDialog.show(MainActivity.this, null, "正在加载...");
        CompressFileTask task = new CompressFileTask();
        task.execute();
    }


    /****************************展示选择的照片↓↓↓↓↓*****************************/
    // 拍照返回
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (null == data) {
                return;
            }

            // 判断返回的数据
            choosed = data.getExtras().getStringArrayList("pic_paths");
            Log.v(TAG, "选择了" + choosed.size() + "张图片");
            adapter.setData(choosed);
            if (!choosed.isEmpty()) {
                int pic_size = choosed.size();
                if (pic_size > 1) {
                    // 肯定是从相册来的，直接copy到到临时文件夹
                    FileUtils.copyFiletoFile(choosed, picDir);
                } else {
                    File file_tem = new File(choosed.get(0));
                    try {
                        if (null != file_tem) {
                            String name = file_tem.getParentFile().getAbsolutePath();
                            if (!name.contains(picDir)) {
                                Log.v(TAG, "不是拍照返回的图片");
                                // 不是相机来的，测copy进去
                                FileUtils.copyFiletoFile(choosed, picDir);
                            } else {
                                choosed.clear();
                                Log.v(TAG, "拍照返回的图片");
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    class GridAdapter extends BaseAdapter {
        LayoutInflater inflater;
        private List<String> picPaths;

        public GridAdapter() {
            picPaths = new ArrayList<String>();
            inflater = LayoutInflater.from(MainActivity.this);
        }
        BitmapCache.ImageCallback callback = new BitmapCache.ImageCallback() {
            @Override
            public void imageLoad(ImageView imageView, Bitmap bitmap, Object... params) {
                if (imageView != null && bitmap != null) {
                    String url = (String) params[0];
                    if (url != null && url.equals((String) imageView.getTag())) {
                        ((ImageView) imageView).setImageBitmap(bitmap);
                    } else {
                        Log.e(TAG, "callback, bmp not match");
                    }
                } else {
                    Log.e(TAG, "callback, bmp null");
                }
            }
        };

        public void setData(ArrayList<String> strs) {
            if (null != strs) {
                picPaths.clear();
                picPaths.addAll(strs);
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            return picPaths.size();
        }

        @Override
        public String getItem(int position) {
            return picPaths.get(position);
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup arg2) {
            convertView = inflater.inflate(R.layout.main_grid_item, null);
            ImageView grid_image = (ImageView)convertView.findViewById(R.id.grid_image);
            ImageLoader.getInstance().displayImage("file://" + picPaths.get(position),grid_image);

            return convertView;
        }
    }

    /****************************展示选择的照片↓↓↓↓↓↑↑↑↑↑*****************************/

    /****************************压缩上传照片↓↓↓↓↓*****************************/
    private List<File> picFiles = new ArrayList<>();
    class CompressFileTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            //创建wgh根目录
            File wghFile = new File(Constant.UPLOAD_DIR);
            if (!wghFile.exists() && !wghFile.isDirectory()) {
                wghFile.mkdir();
            }
            int file_size = FileUtils.getlistSize(picDir);
            Log.d(TAG, "一共有"+file_size+"张图片");
            if (file_size > 0) {
                picFiles.clear();
                ArrayList<String> picList = FileUtils.getFileList(Constant.PICS_DIR);
                //遍历压缩
                if (null != picList && picList.size() > 0) {
                    for(String picPath : picList){
                        Log.d(TAG, "开始压缩"+picPath);
                        File pic = ImageUtils.getimage(picPath, Constant.UPLOAD_DIR);
                        picFiles.add(pic);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            uploadBitFile();
        }

    }

    //上传压缩后的图片
    private void uploadBitFile() {
        mDirDialog.dismiss();
        for(File file : picFiles){
            //遍历添加文件上传

        }
    }

    /****************************压缩上传照片↑↑↑↑↑*****************************/

    @Override
    protected void onDestroy() {
        //删除缓存文件中的照片
        FileUtils.delAllFile(picDir);
        FileUtils.delAllFile(Constant.UPLOAD_DIR);
        super.onDestroy();
    }
}
