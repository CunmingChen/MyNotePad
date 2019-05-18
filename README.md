# MyNotePad应用
拓展功能如下：
* 添加时间戳 
* 查询框
* UI美化
* 修改背景色 
* 闹钟提示
* 保存图片
## 一.添加时间戳 
### 1.修改noteslist_item.xml中的样式，增加要显示时间戳的TextView。
```javascript
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
    
    <TextView xmlns:android="http://schemas.android.com/apk/res/android"
        android:id="@android:id/text1"
        android:layout_width="match_parent"
        android:layout_height="40dp"
        android:textAppearance="?android:attr/textAppearanceLarge"
        android:gravity="center_vertical"
        android:paddingLeft="5dip"
        android:textColor="@color/colorPrimaryDark"
        android:singleLine="true"
        android:textSize="25dp"
        />
    <!--添加 显示时间 的TextView-->
    <TextView
        android:id="@+id/times"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:textAppearance="?android:attr/textAppearanceSmall"
        android:paddingLeft="5dip"
        android:textColor="@color/OrangeRed"
        android:textSize="15dp"/>d

</LinearLayout>
```
### <br/>2.进入NotesList.java PROJECTION 契约类的变量值加一列。
```javascript
private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE,// 1
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, // 2 时间
    };
```
### <br/>3.修改SimpleCursorAdapter的dataColumns和viewIDs的相关值。
```javascript
    private String[] dataColumns = { NotePad.Notes.COLUMN_NAME_TITLE ,  NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE } ;
    private int[] viewIDs = { android.R.id.text1 , R.id.times };
```
### <br/>4.发现这样显示的时间戳格式不对，因此要把时间戳改为以时间格式存入，改动地方分别为<u>NotePadProvider中的insert方法</u>和<u>NoteEditor中的updateNote</u>方法。前者为创建笔记时产生的时间，后者为修改笔记时产生的时间。
```javascript
		//修改时间
        Long now = Long.valueOf(System.currentTimeMillis());
        Date date = new Date(now);
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String dateTime = format.format(date);
        values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, dateTime)
```
 
### <br/>效果
![Alt](https://github.com/CunmingChen/MyNotePad/blob/master/picture/time.png)
## <br/><br/>二.查询框
### 1.找到菜单的list_options_menu.xml文件，添加一个搜索的item
```java
       <item
        android:id="@+id/menu_search"
        android:title="Search"
        android:icon="@android:drawable/ic_search_category_default"
        android:showAsAction="always"
       />
```
### <br/>2.在NotesList中找到onOptionsItemSelected方法，在switch中添加搜索的case语句
```java
            //添加搜索
            case R.id.menu_search:
                Intent intent = new Intent();
                intent.setClass(NotesList.this,NoteSearch.class);
                NotesList.this.startActivity(intent);
                return true;
```
### <br/>3.新建一个名为NoteSearch的activity用来显示跳转的搜索界面的内容和功能。在安卓中有个用于搜索控件：SearchView，可以把SearchView跟ListView相结合，动态地显示搜索结果.<br/>
NoteSearch.java文件的布局文件note_search_list.xml
```java
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical" android:layout_width="match_parent"
    android:layout_height="match_parent">

    <SearchView
        android:id="@+id/search_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:iconifiedByDefault="false"
        android:queryHint="输入搜索内容..."
        android:layout_alignParentTop="true">
    </SearchView>

    <ListView
        android:id="@android:id/list"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
    </ListView>

</LinearLayout>
```
NoteSearch.java
```java
    
package com.example.mynotepad;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;

public class NoteSearch extends ListActivity  implements SearchView.OnQueryTextListener {

    private static final String[] PROJECTION = new String[]{
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, // 时间
            NotePad.Notes.COLUMN_NAME_BACK_COLOR //颜色
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_search_list);
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }
        SearchView searchview = (SearchView) findViewById(R.id.search_view);
        searchview.setOnQueryTextListener(NoteSearch.this);  //为查询文本框注册监听器
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        String selection = NotePad.Notes.COLUMN_NAME_TITLE + " Like ? ";

        String[] selectionArgs = {"%" + newText + "%"};

        Cursor cursor = managedQuery(
                getIntent().getData(),            // Use the default content URI for the provider.
                PROJECTION,                       // Return the note ID and title for each note. and modifcation date
                selection,                        // 条件左边
                selectionArgs,                    // 条件右边
                NotePad.Notes.DEFAULT_SORT_ORDER  // Use the default sort order.
        );

        String[] dataColumns = {NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE};
        int[] viewIDs = {android.R.id.text1, R.id.text2};

        MyCursorAdapter adapter = new MyCursorAdapter(
                this,
                R.layout.noteslist_item,
                cursor,
                dataColumns,
                viewIDs
        );
        setListAdapter(adapter);
        return true;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        // Constructs a new URI from the incoming URI and the row ID
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);

        // Gets the action from the incoming Intent
        String action = getIntent().getAction();

        // Handles requests for note data
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {

            // Sets the result to return to the component that called this Activity. The
            // result contains the new URI
            setResult(RESULT_OK, new Intent().setData(uri));
        } else {

            // Sends out an Intent to start an Activity that can handle ACTION_EDIT. The
            // Intent's data is the note ID URI. The effect is to call NoteEdit.
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
        }
    }
}
```
###  <br/>4.最后要在AndroidManifest.xml注册NoteSearch：
```javascript
<!--添加搜索activity-->
        <activity
            android:name="NoteSearch"
            android:label="Search">
        </activity>
```
### <br/> 效果
![Alt](https://github.com/CunmingChen/MyNotePad/blob/master/picture/search_1.png)
---
![Alt](https://github.com/CunmingChen/MyNotePad/blob/master/picture/search_2.png)
---
![Alt](https://github.com/CunmingChen/MyNotePad/blob/master/picture/search_3.png)
## <br/><br/>三.UI美化（此功能要和跟换背景功能相结合）
### 1.先给NotesList换个主题，把黑色换成白色，在AndroidManifest.xml中NotesList的Activity中添加：
```javascript
<activity android:name=".NotesList" android:label="@string/title_notes_list"
            android:theme="@android:style/Theme.Holo.Light">
```
### <br/>2.让NotesList和NoteSearch每条笔记都有背景色，并且能保存，因此要在数据库中新添加一个颜色字段，并在数据库中进行相应处理。
在NotePadProvider.java创建数据表的地方添加颜色字段
```javascript
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + NotePad.Notes.TABLE_NAME + " ("
                    + NotePad.Notes._ID + " INTEGER PRIMARY KEY,"
                    + NotePad.Notes.COLUMN_NAME_TITLE + " TEXT,"
                    + NotePad.Notes.COLUMN_NAME_NOTE + " TEXT,"
                    + NotePad.Notes.COLUMN_NAME_CREATE_DATE + " INTEGER,"
                    + NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE + " INTEGER,"
                    + NotePad.Notes.COLUMN_NAME_BACK_COLOR + " INTEGER" //颜色数据
                    + ");");
        }
```
在契约类NotePad.java中添加颜色字段、以及定义好背景的五种颜色。
```javascript
        public static final String COLUMN_NAME_BACK_COLOR = "color";

        /**
         * background color
         */
        public static final int DEFAULT_COLOR = 0; 
        public static final int YELLOW_COLOR = 1;
        public static final int BLUE_COLOR = 2;
        public static final int GREEN_COLOR = 3;
        public static final int RED_COLOR = 4;
```
### <br/>3.用SimpleCursorAdapter中的bindView方法将颜色填充到ListView
自定义一个MyCursorAdapter继承SimpleCursorAdapter，既能完成cursor读取的数据库内容填充到item，又能将颜色填充
```javascript
package com.example.mynotepad;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.View;
import android.widget.SimpleCursorAdapter;
public class MyCursorAdapter extends SimpleCursorAdapter {


    public MyCursorAdapter(Context context, int layout, Cursor c,
                           String[] from, int[] to) {
        super(context, layout, c, from, to);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor){
        super.bindView(view, context, cursor);
        //从数据库中读取的cursor中获取笔记列表对应的颜色数据，并设置笔记颜色
        int x = cursor.getInt(cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_BACK_COLOR));
        
        switch (x){
            case NotePad.Notes.DEFAULT_COLOR:
                view.setBackgroundColor(Color.rgb(255, 255, 255));
                break;
            case NotePad.Notes.YELLOW_COLOR:
                view.setBackgroundColor(Color.rgb(247, 216, 133));
                break;
            case NotePad.Notes.BLUE_COLOR:
                view.setBackgroundColor(Color.rgb(165, 202, 237));
                break;
            case NotePad.Notes.GREEN_COLOR:
                view.setBackgroundColor(Color.rgb(161, 214, 174));
                break;
            case NotePad.Notes.RED_COLOR:
                view.setBackgroundColor(Color.rgb(244, 149, 133));
                break;
            default:
                view.setBackgroundColor(Color.rgb(255, 255, 255));
                break;
        }
    }
}

```
### <br/> 效果
![Alt](https://github.com/CunmingChen/MyNotePad/blob/master/picture/UI.png)


## <br/><br/>四.修改背景色
###  1.背景更换指的是编辑笔记时的背景色更换。编辑笔记的Activity为NoteEditor.java
在NoteEditor.java的onResume方法中从数据中读取颜色字段并且设置背景色
```javascript
//读取颜色数据做准备
        int x = mCursor.getInt(mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_BACK_COLOR));
            /**
             * 白 255 255 255
             * 黄 247 216 133
             * 蓝 165 202 237
             * 绿 161 214 174
             * 红 244 149 133
             */
            switch (x){
                case NotePad.Notes.DEFAULT_COLOR:
                    mText.setBackgroundColor(Color.rgb(255, 255, 255));
                    break;
                case NotePad.Notes.YELLOW_COLOR:
                    mText.setBackgroundColor(Color.rgb(247, 216, 133));
                    break;
                case NotePad.Notes.BLUE_COLOR:
                    mText.setBackgroundColor(Color.rgb(165, 202, 237));
                    break;
                case NotePad.Notes.GREEN_COLOR:
                    mText.setBackgroundColor(Color.rgb(161, 214, 174));
                    break;
                case NotePad.Notes.RED_COLOR:
                    mText.setBackgroundColor(Color.rgb(244, 149, 133));
                    break;
                default:
                    mText.setBackgroundColor(Color.rgb(255, 255, 255));
                    break;
            }
```
###  <br/>2.在editor_options_menu.xml中添加一个更改背景的选项
```java
    <item android:id="@+id/menu_color"
        android:title="background"
        android:icon="@drawable/background"
        android:showAsAction="always"/>
```
### <br/>3.在NoteEditor.java中找到onOptionsItemSelected方法，在switch中添加搜索的case语句
```java
            //更换背景颜色
            case R.id.menu_color:
                changeColor();
                break;
```
changeColor的代码为
```javascript
    //跳转改变颜色的activity，将uri信息传到新的activity
    private final void changeColor() {
        Intent intent = new Intent(null,mUri);
        intent.setClass(NoteEditor.this,NoteColor.class);
        NoteEditor.this.startActivity(intent);

    }
```
### <br/>4.新建一个名为NoteColor的activity用来对背景色进行选择<br/>
NoteColor.java的布局文件note_color.xml，垂直线性布局放置5个ImageButton用来选择颜色
```java
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="horizontal" android:layout_width="match_parent"
    android:layout_height="match_parent">

    <ImageButton
        android:id="@+id/color_white"
        android:layout_width="0dp"
        android:layout_height="50dp"
        android:layout_weight="1"
        android:background="@color/colorWhite"
        android:onClick="white"/>

    <ImageButton
        android:id="@+id/color_yellow"
        android:layout_width="0dp"
        android:layout_height="50dp"
        android:layout_weight="1"
        android:background="@color/colorYellow"
        android:onClick="yellow"/>

    <ImageButton
        android:id="@+id/color_blue"
        android:layout_width="0dp"
        android:layout_height="50dp"
        android:layout_weight="1"
        android:background="@color/colorBlue"
        android:onClick="blue"/>

    <ImageButton
        android:id="@+id/color_green"
        android:layout_width="0dp"
        android:layout_height="50dp"
        android:layout_weight="1"
        android:background="@color/colorGreen"
        android:onClick="green"/>

    <ImageButton
        android:id="@+id/color_red"
        android:layout_width="0dp"
        android:layout_height="50dp"
        android:layout_weight="1"
        android:background="@color/colorRed"
        android:onClick="red"/>

</LinearLayout>
```
NoteColor.java
```java
    
package com.example.mynotepad;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;


public class NoteColor extends Activity {

    private Cursor mCursor;
    private Uri mUri;
    private int color;
    private static final int COLUMN_INDEX_TITLE = 1;

    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_BACK_COLOR,
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_color);
        mUri = getIntent().getData();
        mCursor = managedQuery(
                mUri,        // The URI for the note that is to be retrieved.
                PROJECTION,  // The columns to retrieve
                null,        // No selection criteria are used, so no where columns are needed.
                null,        // No where columns are used, so no where values are needed.
                null         // No sort order is needed.
        );

    }

    @Override
    protected void onResume(){
        if (mCursor != null) {
            mCursor.moveToFirst();
            color = mCursor.getInt(COLUMN_INDEX_TITLE);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ContentValues values = new ContentValues();
        values.put(NotePad.Notes.COLUMN_NAME_BACK_COLOR, color);
        getContentResolver().update(mUri, values, null, null);

    }

    public void white(View view){
        color = NotePad.Notes.DEFAULT_COLOR;
        finish();
    }

    public void yellow(View view){
        color = NotePad.Notes.YELLOW_COLOR;
        finish();
    }

    public void blue(View view){
        color = NotePad.Notes.BLUE_COLOR;
        finish();
    }

    public void green(View view){
        color = NotePad.Notes.GREEN_COLOR;
        finish();
    }

    public void red(View view){
        color = NotePad.Notes.RED_COLOR;
        finish();
    }

}

```
###  <br/>5.最后要在AndroidManifest.xml注册NoteColor并且将主题定义为对话框样式：
```javascript
        <!--换背景色-->
        <activity android:name="NoteColor"
            android:theme="@android:style/Theme.Holo.Light.Dialog"
            android:label="ChangeColor"
            android:windowSoftInputMode="stateVisible"/>
```
### <br/> 效果
![Alt](https://github.com/CunmingChen/MyNotePad/blob/master/picture/changeBackground_1.png)
---
![Alt](https://github.com/CunmingChen/MyNotePad/blob/master/picture/changeBackground_2.png)
---
![Alt](https://github.com/CunmingChen/MyNotePad/blob/master/picture/changeBackground_3.png)

## <br/><br/>五.闹钟提醒
###  1.找到菜单的list_options_menu.xml文件，添加一个搜索的item
```javascript
    <item
        android:id="@+id/clock"
        android:title="clocks"
        android:icon="@drawable/clocks"
        android:showAsAction="always"
        />
```
###  <br/>2.在NotesList中找到onOptionsItemSelected方法，在switch中添加闹钟的case语句
```java
            //闹钟
            case R.id.clock:
                alarm();
                // Returns to the caller and skips further processing.
                return true;
```
### <br/> 3.编写闹钟启动的方法alarm
```java
    /**
     * 设置闹钟
     */
    private final void alarm() {
        Calendar currentTime = Calendar.getInstance();
        new TimePickerDialog(NotesList.this, 0,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view,
                                          int hourOfDay, int minute) {
                        Intent alarmIntent = new Intent(NotesList.this, AlarmActivity.class);
                        //alarmIntent.putExtra(AlarmActivity.ITEM_CONTENT,itemContent);
                        PendingIntent pi = PendingIntent.getActivity(NotesList.this, 0, alarmIntent, 0);
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(System.currentTimeMillis());
                        c.set(Calendar.HOUR, hourOfDay);
                        c.set(Calendar.MINUTE, minute);
                        //启动Activity
                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
                        Log.e("HEHE", c.getTimeInMillis() + "");
                        Toast.makeText(NotesList.this, "闹钟设置完毕~", Toast.LENGTH_SHORT).show();
                    }
                }, currentTime.get(Calendar.HOUR_OF_DAY), currentTime.get(Calendar.MINUTE), false).show();
    }
```
###  <br/>4.编写闹钟响应的Activity
```java
package com.example.mynotepad;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class AlarmActivity extends AppCompatActivity {

    public static String ITEM_CONTENT;
    MediaPlayer alarmMusic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alarmMusic=MediaPlayer.create(AlarmActivity.this,R.raw.alarm);
        alarmMusic.setLooping(true);
        alarmMusic.start();
        new AlertDialog.Builder(AlarmActivity.this)
                .setTitle("闹钟").
                setMessage("闹钟响了").
                setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alarmMusic.stop();
                        AlarmActivity.this.finish();
                    }
                }).show();
    }
}

```
### <br/>效果
![Alt](https://github.com/CunmingChen/MyNotePad/blob/master/picture/clock_1.png)
---
![Alt](https://github.com/CunmingChen/MyNotePad/blob/master/picture/clock_2.png)
---
![Alt](https://github.com/CunmingChen/MyNotePad/blob/master/picture/clock_3.png)

## <br/><br/>六.在文本中插入图片
### 1.在NodeEditor.java的optionmenu中添加选项     
```javascript
            //获取照片
            case R.id.insert_album:
                getPhoto();
                break;
            //拍照
            case R.id.insert_camera:
                takeCamera();
                break;

</LinearLayout>
```
### <br/>2.拍照要在AndroidManifest.xml中设置权限,同时还要设置动态权限和写入权限
AndroidManifest.xml
```javascript
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```
在NoteList.java中添加权限的方法并将它放入onCreate中
```javascript
    public void requestPower() {
        //判断是否已经赋予权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                //申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,}, 1);
            }
        }
    }
```

### <br/>3.编写从相册中取图片的<u>getPhoto</u>方法和拍照取照片的<u>takeCamera</u>方法
```javascript
    //从相册取图片
    public void getPhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(intent, PHOTO_FROM_GALLERY);
    }
    //拍照取照片
    public void takeCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( this, new String[] { Manifest.permission.CAMERA }, PHOTO_FROM_CAMERA);
        }
        else {
            File file=new File(Environment.getExternalStorageDirectory(),System.currentTimeMillis()+".jpg");
            try {
                if(file.exists()){
                    file.delete();
                }
                file.createNewFile();
            }catch (IOException e){
                e.printStackTrace();
            }
            imageUri=Uri.fromFile(file);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
            startActivityForResult(intent, PHOTO_FROM_CAMERA);
        }
    }
```
### <br/>4.定义两个正则表达式，提取文本的图片路径，用SpannableString进行图片文字替换，从而实现图片的插入
```javascript
private static final String regex="content://com.android.providers.media.documents/"
    +"document/image%\\w{4}";
private static final String reg="file:///storage/emulated/0/\\d+.jpg";
```
在onResumn( )和cancelNote( )中同时添加
```javascript
            //photo

            ArrayList<String> contentList=new ArrayList<>();
            ArrayList<Integer> startList=new ArrayList<>();
            ArrayList<Integer> endList=new ArrayList<>();
            Pattern p=Pattern.compile(regex);
            Matcher m=p.matcher(note);

            while(m.find()){
                contentList.add(m.group());
                startList.add(m.start());
                endList.add(m.end());
                flag=true;
            }
            p=Pattern.compile(reg);
            m=p.matcher(note);
            while(m.find()){
                contentList.add(m.group());
                startList.add(m.start());
                endList.add(m.end());
                flag=true;
            }

            if(!flag){
                mText.setText(note);
            }else{
                pushPicture(note,contentList,startList,endList);
            }
```
### <br/>5.对图片进行处理
```javascript
    private void pushPicture(String note,ArrayList<String> contentList,ArrayList<Integer> startList,ArrayList<Integer> endList) {
        //创建一个SpannableString对象，以便插入用ImageSpan对象封装的图像
        SpannableString spannableString = new SpannableString(note);
        for(int i=0;i<contentList.size();i++) {
            Uri uri = Uri.parse(contentList.get(i));
            Bitmap bitmap = null;
            try {
                Bitmap originalBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                bitmap = resizeImage(originalBitmap, 200, 200);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (bitmap != null) {
                //根据Bitmap对象创建ImageSpan对象
                ImageSpan imageSpan = new ImageSpan(NoteEditor.this, bitmap);

                //  用ImageSpan对象替换face
                spannableString.setSpan(imageSpan, startList.get(i), endList.get(i), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        mText.setText("");
        Editable edit_text = mText.getEditableText();
        edit_text.append(spannableString);
    }
```
### <br/>效果
![Alt](https://github.com/CunmingChen/MyNotePad/blob/master/picture/photo_1.png)
---
从相册中取图片

![Alt](https://github.com/CunmingChen/MyNotePad/blob/master/picture/photo_2.png)
---
![Alt](https://github.com/CunmingChen/MyNotePad/blob/master/picture/photo_3.png)

拍照取图片

![Alt](https://github.com/CunmingChen/MyNotePad/blob/master/picture/photo_4.png)
---
![Alt](https://github.com/CunmingChen/MyNotePad/blob/master/picture/photo5.png)


