package com.example.test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity implements ListViewBtnAdapter.ListBtnClickListener{

    private Handler mHandler;
    Socket socket;

    private Context mContext;
    ListView listView;
    TextView textView;
//    private ArrayList<String> listInterface;
    private ArrayList<ListViewBtnItem> items;
    private ListViewBtnAdapter adapter;
//    private ArrayAdapter adapter;
    private SocketManager mSocketManager;


    @Override
    protected void onStop() {
        super.onStop();
        try {
            socket.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();

        Button connectButton_ = (Button) findViewById(R.id.mbutton1);
        Button getListButton_ = (Button) findViewById(R.id.mbutton2);
        Button stopButton_ = (Button) findViewById(R.id.mbutton3);
        textView = (TextView) findViewById(R.id.mtextView1) ;

        items = new ArrayList<ListViewBtnItem>() ;
        adapter = new ListViewBtnAdapter(this, R.layout.listview_btn_item, items, (ListViewBtnAdapter.ListBtnClickListener) this);

        listView = (ListView) findViewById(R.id.mlistView);
        listView.setAdapter(adapter);

        //
//        try {
//            Process process = Runtime.getRuntime().exec("su -c \"/data/local/tmp/gilgil/pcap_socket\"");
//        } catch (IOException e1) {
//            e1.printStackTrace();
//        }

        connectButton_.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mSocketManager = new SocketManager(socket, "127.0.0.1", 25164);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        getListButton_.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    mSocketManager.sendData("1");
                    mSocketManager.getList();

                    Thread.sleep(200);
                    adapter.notifyDataSetChanged();
                } catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

        stopButton_.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    mSocketManager.sendData("2");
                    Thread.sleep(200);
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                // TODO : item click
                textView.append("item click\n");
            }
        });

//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView,
//                                    View view, int position, long id) {
//
//                String selectedItem = (String)adapterView.getItemAtPosition(position);
//                mSocketManager.sendData(selectedItem);
//
//                adapter.notifyDataSetChanged();
//            }
//        });

    }


    @Override
    public void onListBtnClick1(int position) {
        Toast.makeText(this, items.get(position).getText() + " -> Attack start", Toast.LENGTH_SHORT).show() ;
        items.get(position).switchButton();

        String selectedItem = items.get(position).getText();
        mSocketManager.sendData(selectedItem);

    }

    @Override
    public void onListBtnClick2(int position) {
        Toast.makeText(this, items.get(position).getText() + " -> Attack stop", Toast.LENGTH_SHORT).show() ;
        items.get(position).switchButton();

        String selectedItem = items.get(position).getText();
        mSocketManager.sendData(selectedItem);

    }

    class msgUpdate implements Runnable{
        private String msg;
        public msgUpdate(String str) {
            this.msg = str;
        }
        public void run() {
            textView.append(msg);
        }
    };


    public class SocketManager {

        private Socket socket;
        private InputStream is;
        private OutputStream os;
        private String ip;
        private int port;
        private connectThread ct;


        public SocketManager(Socket socket, String ip, int port) throws IOException {
            this.socket = socket;
            this.ip = ip;
            this.port = port;

            ct = new connectThread();
            ct.start();
        }

        public void sendData(String data){
            sendDataThread st = new sendDataThread(data);
            st.start();
        }

        public void getList(){
            getListThread rt = new getListThread();
            rt.start();
        }

        class connectThread extends Thread {
            public void run() {
                try {
                    socket = new Socket(ip, port);
                    is = socket.getInputStream();
                    os = socket.getOutputStream();

                    mHandler.post(new msgUpdate("connect ok\n"));
                } catch (IOException e) {
                    mHandler.post(new msgUpdate("connect error!!!\n"));
                }
            }
        };

        class sendDataThread extends Thread {

            private String data;
            byte[] dataBytes = new byte[1024];

            sendDataThread(String data)
            {
                this.data = data;
            }
            public void run() {
                try {

                    dataBytes = data.getBytes();
                    os.write(dataBytes);
                    os.flush();

                    mHandler.post(new msgUpdate("send data = " + data + "\n"));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        class getListThread extends Thread {

            byte[] readBytes = new byte[1024];
            int readByteCount;

            public void run() {
                try {

                    if ((readByteCount = is.read(readBytes)) < 0) {
                        mHandler.post(new msgUpdate("read error\n"));
                    }
                    String buf = new String(readBytes, 0, readByteCount, "UTF-8");

                    StringTokenizer s = new StringTokenizer(buf);

                    while(s.hasMoreTokens()) {
                        ListViewBtnItem item = new ListViewBtnItem();
                        item.setText(s.nextToken(","));
                        items.add(item);

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}