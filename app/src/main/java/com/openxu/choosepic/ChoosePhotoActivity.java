package com.openxu.choosepic;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.openxu.choosepic.bean.ImageBean;
import com.openxu.choosepic.bean.ImageBucket;
import com.openxu.choosepic.bean.ImageItem;
import com.openxu.choosepic.util.AlbumHelper;
import com.openxu.choosepic.util.BitmapCache;
import com.openxu.choosepic.util.BitmapCache.ImageCallback;
import com.openxu.choosepic.util.Constant;
import com.openxu.choosepic.util.FileUtils;
import com.openxu.choosepic.util.PickPhotoUtil;
import com.openxu.choosepic.util.ToastAlone;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * author : openXu
 * create at : 2016/7/18 15:13
 * project : midzs119
 * class name : ChoosePhotoActivity
 * version : 1.0
 * class describe：选择相片
 */
public class ChoosePhotoActivity extends ItotemBaseActivity {
	
	private GridView gridview;
	private TitleLayout photo_title;
	private TextView group_text, total_text;
	private ListView group_listview;

	AlbumHelper helper;
	private ProgressDialog mDirDialog;

	// 所有的图片
	private final static int SCAN_FOLDER_OK = 2;
	private RelativeLayout list_layout;
	private ListAdapter listAdapter;
	private int limit_count ;
	

	private String tempCameraPath = "";

	private Bitmap optionBtmap = null;
	
	private String picDir;       //用于存放被选中的照片，选中的照片会复制到此文件夹中

	ArrayList<String> nowStrs = new ArrayList<String>();

	ArrayList<ImageItem> nowImageItems = new ArrayList<ImageItem>();
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case SCAN_FOLDER_OK:
				try {
					mDirDialog.dismiss();
					// 获取到mAllImgs；并显示到数据中
					GridAdapter gridAdatper1 = new GridAdapter();
					gridAdatper1.setData(nowImageItems);
					gridview.setAdapter(gridAdatper1);
				} catch (Exception e) {
				}
				break;
			}
		}

	};

	@Override
	protected void initView() {
		setContentView(R.layout.activity_choose_photo);
		gridview = (GridView) findViewById(R.id.gridview);
		group_text = (TextView) findViewById(R.id.group_text);
		total_text = (TextView) findViewById(R.id.total_text);
		group_listview = (ListView) findViewById(R.id.group_listview);
		photo_title = (TitleLayout) findViewById(R.id.photo_title);
		list_layout = (RelativeLayout) findViewById(R.id.list_layout);
		
	}

	@Override
	protected void initData() {
		//用于缓存被选中的图片的原始地址，便于再次选择时标示出已选的
		ArrayList<String> choosed = getIntent().getStringArrayListExtra("choosed");


		//创建缓存选中的图片的文件夹
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

		Log.e(TAG, "picDir:"+picDir);
		Log.e(TAG, "list:"+choosed);
		
		// 初始化数据，所有图片应在281张以内
		chooseItem.add(0);
		// imageLoader配置

		cache = new BitmapCache();

		optionBtmap = BitmapFactory.decodeResource(getResources(), R.drawable.choosepic_def_pic);
		helper = AlbumHelper.getHelper();
		helper.init(getApplicationContext());
		
		addedPath = new ArrayList<String>();
		listAdapter = new ListAdapter();
		group_listview.setAdapter(listAdapter);
		addedPath.addAll(choosed);
		
		limit_count = Constant.CHOOSE_MAX_PIC-addedPath.size();
		total_text.setText(addedPath.size()+"/"+Constant.CHOOSE_MAX_PIC+"张");
		
		photo_title.setLeft1Show(true);
		photo_title.setLeft1(R.drawable.selector_btn_back);
		photo_title.setTitleName("图片");
		photo_title.setTvRight1Show(true);
		photo_title.setTvRight1("确定");
		getImages();
	}

	@Override
	protected void setListener() {
		photo_title.setLeft1Listener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				ChoosePhotoActivity.this.finish();
			}
		});
		photo_title.setTvRight1ClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(!addedPath.isEmpty()) {
					//addedPath返回给上个页面-----这里只选择相册里的
					FileUtils.delAllFile(picDir);   //清空图片文件夹
					Intent dataIntent = new Intent();
					Bundle dataBundle = new Bundle();
					dataBundle.putStringArrayList("pic_paths", addedPath);
					dataIntent.putExtras(dataBundle);
					setResult(RESULT_OK, dataIntent);
					ChoosePhotoActivity.this.finish();
				} else {
					ToastAlone.show( "请选择照片");
					return;
				}
			}
		});

		
		group_text.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (list_layout.getVisibility() == View.VISIBLE) {
//					list_layout.startAnimation(toDown);
					group_listview.setVisibility(View.GONE);
					TranslateAnimation animation = new TranslateAnimation(
			                Animation.RELATIVE_TO_SELF, 0,
			                Animation.RELATIVE_TO_SELF, 0,
			                Animation.RELATIVE_TO_SELF, 0,
			                Animation.RELATIVE_TO_SELF, 1);
			        animation.setDuration(300);
			        group_listview.startAnimation(animation);
					list_layout.setVisibility(View.GONE);
				} else {
					list_layout.setVisibility(View.VISIBLE);
					group_listview.setVisibility(View.VISIBLE);
			        TranslateAnimation animation = new TranslateAnimation(
			                Animation.RELATIVE_TO_SELF, 0,
			                Animation.RELATIVE_TO_SELF, 0,
			                Animation.RELATIVE_TO_SELF, 1,
			                Animation.RELATIVE_TO_SELF, 0);

			        animation.setDuration(300);
			        group_listview.startAnimation(animation);
//					list_layout.startAnimation(toUp);
				}
			}
		});

		group_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						// 点击刷新对应的视图
						if (chooseItem.get(0) == position) {
							// 不做操作，返回
							list_layout.setVisibility(View.GONE);
						} else {
							chooseItem.clear();
							chooseItem.add(position);
							listAdapter.notifyDataSetChanged();
							list_layout.setVisibility(View.GONE);

							// 获取到mAllImgs；并显示到数据中
							GridAdapter gridAdatper = new GridAdapter();
							gridAdatper.setData(new ArrayList<ImageItem>());
							gridview.setAdapter(gridAdatper);
							gridAdatper = null;

							// 得到当前的来刷新
							if (0 == position) {
								new ScanTask().execute();
							} else {
								// 刷新当前的GridView
								mDirDialog = ProgressDialog.show(ChoosePhotoActivity.this, null, "正在加载...");
								nowImageItems.clear();
								ImageBucket imageBucket = helper.getImagesBucketList(false).get(position - 1);
								if(null != imageBucket && !imageBucket.imageList.isEmpty()) {
									nowImageItems.addAll(imageBucket.imageList);
								}
								mHandler.sendEmptyMessageDelayed(SCAN_FOLDER_OK, 1000);
//								// 通知Handler扫描图片完成
//								getFolderImages(imageBean.getFa_filepath());
							}
						}
					}
				});
		
		gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				if(chooseItem.get(0) == 0 && 0 == position) {
					//调用系统相机
					//判断是否已经选满最大照片数
					if(addedPath.size() >= Constant.CHOOSE_MAX_PIC) {
//						ToastAlone.show(WghmidPhotoActivity.this, "最多选"+Constant.WGH_MAX_PIC+"张，请取消后再点击拍照");
						addedPath.clear();
						if(oldSelectedIndex > 0){
							View childAt = gridview.getChildAt(oldSelectedIndex- gridview.getFirstVisiblePosition());
							try{
							  ((ImageView)childAt.findViewById(R.id.grid_img)).setImageResource(R.drawable.choosepic_select_icon_unselected);
							}catch(Exception e){
								e.printStackTrace();
							}
						}
					}
					
					tempCameraPath = picDir + "/"+ System.currentTimeMillis() + ".jpg";
					Log.e("cxm", "path============"+tempCameraPath);
					try {
						//调用系统相机拍照
						PickPhotoUtil.getInstance().takePhoto(ChoosePhotoActivity.this, "tempUser", tempCameraPath);
					}catch (Exception e){
						e.printStackTrace();
						showPermissionDialog();
					}

//					PickPhotoUtil.getInstance().takePhoto(
//							WghmidPhotoActivity.this, "tempUser", tempCameraPath);
				}
			}
		});
	
	}
	/**
	 * 提示相机权限被拒绝
	 */
	private void showPermissionDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("提示")
				.setMessage("无相机使用权限，若希望继续此功能请到设置中开启相机权限。")
				.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {

					}
				}).show();

	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			mDirDialog.dismiss();
		} catch (Exception e) {
		}
	}
	
	/**
	 * 利用ContentProvider扫描手机中的图片，此方法在运行在子线程中
	 */
	private void getImages() {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			Toast.makeText(this, "暂无外部存储", Toast.LENGTH_SHORT).show();
			return;
		}
		new ScanTask().execute();
	}

	class ScanTask extends AsyncTask<Void, Void, List<ImageBucket>> {
		ProgressDialog scanDialog = new ProgressDialog(ChoosePhotoActivity.this);
		@Override
		protected void onPreExecute() {
			scanDialog = ProgressDialog.show(ChoosePhotoActivity.this, null, "正在加载...");
			super.onPreExecute();
		}
		@Override
		protected List<ImageBucket> doInBackground(Void... arg0) {
			List<ImageBucket> imagesBucketList = helper.getImagesBucketList(false);
			return imagesBucketList;
		}
		@Override
		protected void onPostExecute(List<ImageBucket> result) {
			super.onPostExecute(result);
			if(!ChoosePhotoActivity.this.isFinishing()) {
				scanDialog.dismiss();
			}
			//显示到Adapter上面
			//首先显示所有
			GridAdapter gridAdapter = new GridAdapter();
			Log.e("cxm", "helper.totalItems.size="+helper.totalItems.size());
			gridAdapter.setData(helper.totalItems);
			gridview.setAdapter(gridAdapter);
			gridAdapter = null;
			if(null != result) {
				listAdapter.setData(result);
			}
		}
	}

	/**
	 * 组装分组界面GridView的数据源，因为我们扫描手机的时候将图片信息放在HashMap中 所以需要遍历HashMap将数据组装成List
	 * 
	 * @return
	 */
	private ArrayList<ImageBean> subGroupOfImage(
			HashMap<String, ArrayList<String>> gruopMap) {
		if (gruopMap.size() == 0) {
			return null;
		}
		ArrayList<ImageBean> list = new ArrayList<ImageBean>();
		Iterator<Map.Entry<String, ArrayList<String>>> it = gruopMap.entrySet()
				.iterator();
		ImageBean ig0 = new ImageBean();
		ig0.setFolderName("所有图片");
		ig0.setImageCounts(0);
		ig0.setTopImagePath("");
		list.add(0, ig0);
		while (it.hasNext()) {
			Map.Entry<String, ArrayList<String>> entry = it.next();
			ImageBean mImageBean = new ImageBean();
			String key = entry.getKey();
			List<String> value = entry.getValue();
			File dir_file = new File(key);
			mImageBean.setFolderName(dir_file.getName());
			mImageBean.setImageCounts(value.size());
			mImageBean.setTopImagePath(value.get(0));// 获取该组的第一张图片
			mImageBean.setFa_filepath(key);
			list.add(mImageBean);
		}

		return list;

	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK) {
			switch (requestCode) {
				case PickPhotoUtil.PickPhotoCode.PICKPHOTO_TAKE:

					File fi = new File("");
					PickPhotoUtil.getInstance().takeResult(this,
							data, fi);
					//相机的图片
					ArrayList<String> camepaths = new ArrayList<String>();
					camepaths.add(tempCameraPath);
					Intent dataIntent = new Intent();
					Bundle dataBundle = new Bundle();
					dataBundle.putStringArrayList("pic_paths", camepaths);
					dataIntent.putExtras(dataBundle);
					setResult(RESULT_OK, dataIntent);
					ChoosePhotoActivity.this.finish();
					break;

				default:
					break;
			}
		}
	}

	private ArrayList<String> addedPath = null;

	private int oldSelectedIndex;      //最后一个被选中的图片的位置
	BitmapCache cache;
	// gridview的Adapter
	class GridAdapter extends BaseAdapter {
		// 根据三种不同的布局来应用
		final int VIEW_TYPE = 2;
		final int TYPE_1 = 0;
		final int TYPE_2 = 1;
		LayoutInflater inflater;
		private ArrayList<ImageItem> gridStrings;/**
		 * 用来存储图片的选中情况
		 */

		public GridAdapter() {
			gridStrings = new ArrayList<ImageItem>();
			inflater = LayoutInflater.from(ChoosePhotoActivity.this);
		}
		
		ImageCallback callback = new ImageCallback() {
			@Override
			public void imageLoad(ImageView imageView, Bitmap bitmap,
					Object... params) {
				if (imageView != null && bitmap != null) {
					String url = (String) params[0];
					if (url != null && url.equals((String) imageView.getTag())) {
						((ImageView) imageView).setImageBitmap(bitmap);
					} else {
						Log.e("cxm", "callback, bmp not match");
					}
				} else {
					Log.e("cxm", "callback, bmp null");
				}
			}
		};

		public void setData(ArrayList<ImageItem> strs) {
			if (null != strs) {
				gridStrings.clear();
				gridStrings.addAll(strs);
				notifyDataSetChanged();
			}
		}

		@Override
		public int getCount() {
			return gridStrings.size();
		}

		@Override
		public ImageItem getItem(int position) {
			if (chooseItem.get(0) == 0) {
				return gridStrings.get(position - 1);
			} else {
				Log.e("cxm", "position===="+position+",path="+gridStrings.get(position));
				return gridStrings.get(position);
			}
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public int getItemViewType(int position) {
			if (chooseItem.get(0) == 0) {
				if (position == 0) {
					return TYPE_1;
				} else {
					return TYPE_2;
				}
			} else {
				return TYPE_2;
			}
		}

		@Override
		public int getViewTypeCount() {
			if (chooseItem.get(0) == 0) {
				return VIEW_TYPE;
			} else {
				return 1;
			}
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup arg2) {
			GridHolder gridHolder = null  ;
			PhotoHolder photoHodler = null;
			int type = getItemViewType(position);
			if (convertView == null) {
				switch (type) {
				case TYPE_1:
					// 显示拍照
					photoHodler = new PhotoHolder();
					convertView = inflater.inflate(R.layout.choosepic_take_photo, null);
					convertView.setTag(photoHodler);
					break;
				case TYPE_2:
					convertView = inflater.inflate(R.layout.choosepic_grid_item, null);
					gridHolder = new GridHolder();
					gridHolder.grid_image = (ImageView) convertView.findViewById(R.id.grid_image);
					gridHolder.grid_img = (ImageView) convertView.findViewById(R.id.grid_img);
					gridHolder.grid_item_layout = (RelativeLayout) convertView.findViewById(R.id.grid_item_layout);
					convertView.setTag(gridHolder);
					break;
				default:
					break;
				}
			} else {
				switch (type) {
				case TYPE_1:
					// 显示拍照
					photoHodler = (PhotoHolder) convertView.getTag();
					break;
				case TYPE_2:
					gridHolder = (GridHolder) convertView.getTag();
					break;
				default:
					break;
				}
			}

			if (type == TYPE_2) {
				// 判断是否已经添加
				String thumb_path = getItem(position).thumbnailPath;
				String img_path = getItem(position).imagePath;
				gridHolder.grid_image.setTag(img_path);
				cache.displayBmp(gridHolder.grid_image, thumb_path, img_path,callback, optionBtmap);
				gridHolder.grid_item_layout.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if(addedPath.contains(getItem(position).imagePath)) {
							//已经包含这个path了，则干掉
							addedPath.remove(getItem(position).imagePath);
							((ImageView)view.findViewById(R.id.grid_img)).setImageResource(R.drawable.choosepic_select_icon_unselected);
						} else {
							//判断大小
							if(addedPath.size() < Constant.CHOOSE_MAX_PIC) {
								addedPath.add(getItem(position).imagePath);
								((ImageView)view.findViewById(R.id.grid_img)).setImageResource(R.drawable.choosepic_select_icon_selected);
								oldSelectedIndex = position;
							}else{
								//TODO 此处有两种处理方式
								//①.直接提示最多只能选择多少张
								ToastAlone.show( "最多选"+Constant.CHOOSE_MAX_PIC+"张图片");
								//②.移除上一张选中的

								addedPath.remove(addedPath.size()-1);   //移除掉最后一个被选中的
								addedPath.add(getItem(position).imagePath);
								((ImageView)view.findViewById(R.id.grid_img)).setImageResource(R.drawable.choosepic_select_icon_selected);
								if(oldSelectedIndex > 0){
									View childAt = gridview.getChildAt(oldSelectedIndex- gridview.getFirstVisiblePosition());
									try{
									  ((ImageView)childAt.findViewById(R.id.grid_img)).setImageResource(R.drawable.choosepic_select_icon_unselected);
									}catch(Exception e){
										e.printStackTrace();
									}
								}
								oldSelectedIndex = position;
							}
						}
						mYhandler.sendEmptyMessage(0);
					}
				});
				if (addedPath.contains(getItem(position).imagePath)) {
					// 已经添加过了
					gridHolder.grid_img.setImageResource(R.drawable.choosepic_select_icon_selected);
					oldSelectedIndex = position;
				} else {
					gridHolder.grid_img.setImageResource(R.drawable.choosepic_select_icon_unselected);
				}
			}

			return convertView;
		}

		class PhotoHolder {

		}

		class GridHolder {
			ImageView grid_image;
			public ImageView grid_img;
			RelativeLayout grid_item_layout;
		}

	}
	
	Handler mYhandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				total_text.setText(addedPath.size()+"/"+Constant.CHOOSE_MAX_PIC+"张");
				break;

			default:
				break;
			}
		}
	};

	private ArrayList<Integer> chooseItem = new ArrayList<Integer>();

	class ListAdapter extends BaseAdapter {
		private ArrayList<ImageBucket> beans = null;
		LayoutInflater inflater;

		public ListAdapter() {
			inflater = LayoutInflater.from(ChoosePhotoActivity.this);
			beans = new ArrayList<ImageBucket>();
		}
		
		ImageCallback listallback = new ImageCallback() {
			@Override
			public void imageLoad(ImageView imageView, Bitmap bitmap,
					Object... params) {
				if (imageView != null && bitmap != null) {
					String url = (String) params[0];
					if (url != null && url.equals((String) imageView.getTag())) {
						((ImageView) imageView).setImageBitmap(bitmap);
					} else {
						Log.e("cxm", "list--callback, bmp not match");
					}
				} else {
					Log.e("cxm", "list--callback, bmp null");
				}
			}
		};

		public void setData(List<ImageBucket> listBeans) {
			if (listBeans != null) {
				beans.clear();
				beans.addAll(listBeans);
				notifyDataSetChanged();
			}
		}

		@Override
		public int getCount() {
			return beans.size();
		}

		@Override
		public ImageBucket getItem(int arg0) {
			return beans.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			final ListViewHolder listHoder;
//			ImageBucket imageBean = beans.get(position);
			if (convertView == null) {
				listHoder = new ListViewHolder();
				convertView = inflater.inflate(R.layout.choosepic_list_item, null);
				listHoder.myimage_view = (ImageView) convertView
						.findViewById(R.id.myimage_view);
				listHoder.choose_img = (ImageView) convertView
						.findViewById(R.id.choose_img);
				listHoder.folder_text = (TextView) convertView
						.findViewById(R.id.folder_text);
				listHoder.count_text = (TextView) convertView
						.findViewById(R.id.count_text);
				convertView.setTag(listHoder);
			} else {
				listHoder = (ListViewHolder) convertView.getTag();
			}
			int cho_posi = chooseItem.get(0);
			if (position == cho_posi) {
				// 相等则显示
				listHoder.choose_img.setVisibility(View.VISIBLE);
			} else {
				listHoder.choose_img.setVisibility(View.GONE);
			}
//			String img_path = "";
//			if (position == 0) {
//				img_path = beans.get(1).getTopImagePath();
//				listHoder.count_text.setVisibility(View.GONE);
//			} else {
//				img_path = imageBean.getTopImagePath();
//				listHoder.count_text.setVisibility(View.VISIBLE);
//				listHoder.count_text.setText(imageBean.getImageCounts()+"张");
//			}
//			listHoder.folder_text.setText(imageBean.getFolderName());
//			imageLoader.displayImage("file://" + img_path,
//					listHoder.myimage_view, options);
			if (position == 0) {
				if(!beans.isEmpty()) {
					List<ImageItem> imageList = beans.get(0).imageList;
					if(!imageList.isEmpty()) {
						ImageItem imageItem = imageList.get(0);
						listHoder.myimage_view.setTag(imageItem.imagePath);

						cache.displayBmp(listHoder.myimage_view, imageItem.thumbnailPath, imageItem.imagePath,
								listallback, optionBtmap);
					}
				}
				listHoder.folder_text.setText("所有图片");
				listHoder.count_text.setVisibility(View.GONE);
			} else {
				if(!beans.isEmpty()) {
					List<ImageItem> imageList = beans.get(position -1).imageList;
					if(!imageList.isEmpty()) {
						ImageItem imageItem = imageList.get(0);
						listHoder.myimage_view.setTag(imageItem.imagePath);
						cache.displayBmp(listHoder.myimage_view, imageItem.thumbnailPath, imageItem.imagePath,
								listallback, optionBtmap);
					}
				}
				listHoder.count_text.setVisibility(View.VISIBLE);
				listHoder.count_text.setText(beans.get(position -1).count+"张");
				listHoder.folder_text.setText(beans.get(position -1).bucketName);
			}
			return convertView;
		}

		class ListViewHolder {
			ImageView myimage_view;
			ImageView choose_img;
			TextView folder_text, count_text;
		}

	}
}
