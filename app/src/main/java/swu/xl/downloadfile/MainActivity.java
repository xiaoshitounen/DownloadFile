package swu.xl.downloadfile;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    private Button download;
    private ProgressBar progressBar;
    private TextView progressText;
    private EditText editText;
    private DownloadFileTask downloadFileTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.edit_url);
        download = findViewById(R.id.download);
        progressBar = findViewById(R.id.progress_bar);
        progressText = findViewById(R.id.progress_text);

        download.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                downloadFileTask = new DownloadFileTask();
                downloadFileTask.execute(editText.getText().toString());
                v.setClickable(false);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        downloadFileTask.cancel(false);
    }

    //下载文件的异步类
    private class DownloadFileTask extends AsyncTask<String,Integer,String> {
        //任务开始前-主线程
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //下载前初始化
            progressBar.setProgress(0);
            progressText.setText("开始下载");
        }

        //任务开始-工作线程
        @Override
        protected String doInBackground(String... strings) {

            try {
                //获得URL
                URL url = new URL(strings[0]);
                //打开连接
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                //连接
                connection.connect();
                //获取文件的总长度
                int contentLength = connection.getContentLength();
                //获得响应流
                InputStream is = connection.getInputStream();

                //获取该应用的外部存储
                File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

                //获取输出流
                File file = new File(externalFilesDir, "text.apk");
                OutputStream os = new FileOutputStream(file);

                //写入
                int downloadSize = 0;
                int len;
                byte[] bytes = new byte[1024];
                while ((len = is.read(bytes)) != -1){
                    os.write(bytes,0,len);

                    downloadSize += len;
                    int progress = (int) ((downloadSize / (contentLength * 1.0)) * 100);
                    publishProgress(progress);
                    System.out.println("下载进度："+progress);
                    //System.out.println("已经下载的大小："+downloadSize+" "+"总的大小："+contentLength);
                }

                //关闭
                os.flush();
                is.close();
                os.close();

                return is.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        //回调进度-主线程
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            progressBar.setProgress(values[0]);
            progressText.setText("下载进度："+values[0]+"%");
        }

        //执行完毕-主线程
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setProgress(100);
            progressText.setText("下载完毕");
        }
    }
}