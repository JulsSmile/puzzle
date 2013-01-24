package me.elvishew.puzzle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateManager {

    public interface UpdateCheckingListener {
        void onStartChecking();
        void onCheckingFinish(boolean hasNewVersion);
    }

    private UpdateCheckingListener mListener;
    private static final String UPDATE_XML_VERSION = "version";
    private static final String UPDATE_XML_NAME = "name";
    private static final String UPDATE_XML_SIZE = "size";
    private static final String UPDATE_XML_URL = "url";

    /* 下载中 */
    private static final int DOWNLOAD = 1;
    /* 下载结束 */
    private static final int DOWNLOAD_FINISH = 2;
    /* Download failed */
    private static final int DOWNLOAD_FAILED = 3;
    /* 保存解析的XML信息 */
    HashMap<String, String> mHashMap;
    /* 下载保存路径 */
    private String mSavePath;
    /* 记录进度条数量 */
    private int progress;
    private String totalSize;
    private long downloadedSize;

    /* 是否取消更新 */
    private boolean cancelUpdate = false;

    private CheckUpdateTask mCheckUpdateTask;
    private Context mContext;
    /* 更新进度条 */
    private ProgressBar mProgressBar;
    private TextView mProgressSize;
    private TextView mProgressPercent;
    private Dialog mDownloadDialog;

    private enum CheckResult {AVAILABLE, UNAVAILABLE, FAIL}

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            // 正在下载
            case DOWNLOAD:
                // 设置进度条位置
                Log.i("-----------", "progress " + progress);
                mProgressBar.setProgress(progress);
                mProgressPercent.setText(progress + "%");
                mProgressSize.setText(Utils.formetFileSize(downloadedSize) + '/' + totalSize);
                break;
            case DOWNLOAD_FINISH:
                // 安装文件
                installApk();
                break;
            case DOWNLOAD_FAILED:
                Toast.makeText(mContext, R.string.download_fail, Toast.LENGTH_SHORT).show();
            default:
                break;
            }
        };
    };

    public UpdateManager(Context context, UpdateCheckingListener listener) {
        this.mContext = context;
        this.mListener = listener;
    }

    /**
     * 检测软件更新
     */
    public void checkUpdate() {
        mCheckUpdateTask = new CheckUpdateTask();
        mCheckUpdateTask.execute((Void) null);
    }

    public void cancelCheckUpdate() {
        if (mCheckUpdateTask != null) {
            mCheckUpdateTask.cancel(true);
            mCheckUpdateTask = null;
        }
    }

    /**
     * 检查软件是否有更新版本
     * 
     * @return
     */
    private CheckResult isUpdate() {
        // 获取当前软件版本
        int versionCode = getVersionCode(mContext);
        // 把update.xml放到网络上，然后获取文件信息
        // 解析XML文件。 由于XML文件比较小，因此使用DOM方式进行解析
        try {
            String path = "http://elvishew.me/game/puzzle/update.xml"; // 地址是服务器上update.xml链接地址
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5 * 1000);
            conn.setRequestMethod("GET");
            InputStream inStream = conn.getInputStream();
            mHashMap = parseXml(inStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != mHashMap && mHashMap.get(UPDATE_XML_VERSION) != null) {
            int serviceCode = Integer.valueOf(mHashMap.get(UPDATE_XML_VERSION));
            // 版本判断
            if (serviceCode > versionCode) {
                return CheckResult.AVAILABLE;
            } else {
                return CheckResult.UNAVAILABLE;
            }
        }
        return CheckResult.FAIL;
    }

    /**
     * 获取软件版本号
     * 
     * @param context
     * @return
     */
    private int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            // 获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = context.getPackageManager().getPackageInfo(
                    Constants.PACKAGE_NAME, 0).versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 显示软件更新对话框
     */
    private void showNoticeDialog() {
        // 构造对话框
        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle(R.string.update_title);
        builder.setMessage(String.format(mContext.getString(R.string.update_msg), mHashMap.get(UPDATE_XML_SIZE)));
        // 更新
        builder.setPositiveButton(R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // 显示下载对话框
                showDownloadDialog();
            }
        });
        // 稍后更新
        builder.setNegativeButton(R.string.update_later, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }

    /**
     * 显示软件下载对话框
     */
    private void showDownloadDialog() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            // 构造软件下载对话框
            AlertDialog.Builder builder = new Builder(mContext);
            builder.setTitle(R.string.update_updating);
            // 给下载对话框增加进度条
            final LayoutInflater inflater = LayoutInflater.from(mContext);
            View v = inflater.inflate(R.layout.update_progress, null);
            mProgressBar = (ProgressBar) v.findViewById(R.id.update_progress);
            mProgressSize = (TextView) v.findViewById(R.id.progress_size);
            mProgressPercent = (TextView) v.findViewById(R.id.progress_percent);

            mProgressSize.setText("0B/0B");
            mProgressPercent.setText("0%");

            builder.setView(v);
            // 取消更新
            builder.setNegativeButton(R.string.cancel, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    // 设置取消状态
                    cancelUpdate = true;
                }
            });
            mDownloadDialog = builder.create();
            mDownloadDialog.show();
        }

        downloadApk();
    }

    /**
     * 下载apk文件
     */
    private void downloadApk() {
        // 启动新线程下载软件
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            new downloadApkThread().start();
        } else {
            downloadApkAfterGingerbread();
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void downloadApkAfterGingerbread() {
        // Let's use system download manager instead. From 2.3
        String url = mHashMap.get(UPDATE_XML_URL);
        String name = mHashMap.get(UPDATE_XML_NAME);
        Uri resource = Uri.parse(url);
        Request request = new DownloadManager.Request(resource);
        request.setAllowedNetworkTypes(Request.NETWORK_MOBILE
                | Request.NETWORK_WIFI);
        request.setAllowedOverRoaming(false);
        // 设置文件类型
        request.setMimeType("application/vnd.android.package-archive"); //apk

        //sdcard的目录下的download文件夹
        request.setDestinationInExternalPublicDir("/Download/", name);
        // 在通知栏中显示
        request.setShowRunningNotification(true);
        request.setVisibleInDownloadsUi(true);

        request.setTitle(name);
        request.setDescription(mContext.getString(R.string.update_downloading_description));

        DownloadManager downloadManager = (DownloadManager) mContext
                .getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
    }

    /**
     * 下载文件线程
     */
    private class downloadApkThread extends Thread {
        @Override
        public void run() {
            try {
                // 判断SD卡是否存在，并且是否具有读写权限
                if (Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
                    // 获得存储卡的路径
                    String sdpath = Environment.getExternalStorageDirectory()
                            + "/";
                    mSavePath = sdpath + "Download";
                    URL url = new URL(mHashMap.get(UPDATE_XML_URL));
                    // 创建连接
                    HttpURLConnection conn = (HttpURLConnection) url
                            .openConnection();
                    conn.setRequestProperty("Accept-Encoding", "identity");
                    conn.connect();
                    // 获取文件大小
                    long length = conn.getContentLength();
                    Log.i("-----------", "length " + length);
                    totalSize = Utils.formetFileSize(length);
                    mHandler.sendEmptyMessage(DOWNLOAD);
                    // 创建输入流
                    InputStream is = conn.getInputStream();

                    File file = new File(mSavePath);
                    // 判断文件目录是否存在
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    File apkFile = new File(mSavePath, mHashMap.get(UPDATE_XML_NAME));
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    long count = 0;
                    // 缓存
                    byte buf[] = new byte[1024];
                    // 写入到文件中
                    do {
                        int numread = is.read(buf);
                        count += numread;
                        downloadedSize = count;
                        // 计算进度条位置
                        progress = (int) (((float) count / length) * 100);
                        // 更新进度
                        mHandler.sendEmptyMessage(DOWNLOAD);
                        if (numread <= 0) {
                            // 下载完成
                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                            break;
                        }
                        // 写入文件
                        fos.write(buf, 0, numread);
                    } while (!cancelUpdate);// 点击取消就停止下载.
                    fos.close();
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                mHandler.sendEmptyMessage(DOWNLOAD_FAILED);
            }
            // 取消下载对话框显示
            mDownloadDialog.dismiss();
        }
    };

    /**
     * 安装APK文件
     */
    private void installApk() {
        File apkfile = new File(mSavePath, mHashMap.get(UPDATE_XML_NAME));
        if (!apkfile.exists()) {
            return;
        }
        // 通过Intent安装APK文件
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                "application/vnd.android.package-archive");
        mContext.startActivity(i);
    }

    private class CheckUpdateTask extends AsyncTask<Void, Integer, CheckResult> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mListener.onStartChecking();
        }

        @Override
        protected CheckResult doInBackground(Void... params) {
            return isUpdate();
        }

        @Override
        protected void onPostExecute(CheckResult result) {
            super.onPostExecute(result);
            mListener.onCheckingFinish(true);
            if (CheckResult.AVAILABLE.equals(result)) {
                // 显示提示对话框
                showNoticeDialog();
            } else if (CheckResult.UNAVAILABLE.equals(result)) {
                Toast.makeText(mContext, R.string.update_unavailable, Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(mContext, R.string.update_checking_fail, Toast.LENGTH_SHORT)
                .show();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    public HashMap<String, String> parseXml(InputStream inStream)
            throws Exception {
        HashMap<String, String> hashMap = new HashMap<String, String>();

        // 实例化一个文档构建器工厂
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // 通过文档构建器工厂获取一个文档构建器
        DocumentBuilder builder = factory.newDocumentBuilder();
        // 通过文档通过文档构建器构建一个文档实例
        Document document = builder.parse(inStream);
        // 获取XML文件根节点
        Element root = document.getDocumentElement();
        // 获得所有子节点
        NodeList childNodes = root.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); j++) {
            // 遍历子节点
            Node childNode = (Node) childNodes.item(j);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) childNode;
                // 版本号
                if (UPDATE_XML_VERSION.equals(childElement.getNodeName())) {
                    hashMap.put(UPDATE_XML_VERSION, childElement.getFirstChild()
                            .getNodeValue());
                }
                // 软件名称
                else if ((UPDATE_XML_NAME.equals(childElement.getNodeName()))) {
                    hashMap.put(UPDATE_XML_NAME, childElement.getFirstChild()
                            .getNodeValue());
                }
                // 软件大小
                else if ((UPDATE_XML_SIZE.equals(childElement.getNodeName()))) {
                    hashMap.put(UPDATE_XML_SIZE, childElement.getFirstChild()
                            .getNodeValue());
                }
                // 下载地址
                else if ((UPDATE_XML_URL.equals(childElement.getNodeName()))) {
                    hashMap.put(UPDATE_XML_URL, childElement.getFirstChild()
                            .getNodeValue());
                }
            }
        }
        return hashMap;
    }
}