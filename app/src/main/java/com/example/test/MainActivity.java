package com.example.test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    private Handler mHandler;
    Socket socket;

    private Context mContext;
    ListView listView;
    private ArrayList<String> listInterface;
//    private String[] listInterface;
    private ArrayAdapter adapter;
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

        Button connectButton_ = (Button) findViewById(R.id.button1);
        Button getListButton_ = (Button) findViewById(R.id.button2);


        listInterface = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listInterface) ;
        listView = (ListView) findViewById(R.id.listView) ;
        listView.setAdapter(adapter);


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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView,
                                    View view, int position, long id) {

                // 8. 클릭한 아이템의 문자열을 가져와서
                String selectedItem = (String)adapterView.getItemAtPosition(position);
                mSocketManager.sendData(selectedItem);




                // 10. 어댑터 객체에 변경 내용을 반영시켜줘야 에러가 발생하지 않습니다.
                adapter.notifyDataSetChanged();
            }
        });

    }

//    class msgUpdate implements Runnable{
//        private String msg;
//        public msgUpdate(String str) {
//            this.msg = str;
//        }
//        public void run() {
//            textView.append(msg);
//        }
//    };


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
//                    mHandler.post(new msgUpdate("connect OK\n"));
                } catch (Exception e) {
                    e.printStackTrace();
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

//                    mHandler.post(new msgUpdate("sendData\n"));

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
//                        mHandler.post(new msgUpdate("read error\n"));
                    }
                    String buf = new String(readBytes, 0, readByteCount, "UTF-8");

                    StringTokenizer s = new StringTokenizer(buf);

                    while(s.hasMoreTokens()) {
                        listInterface.add(s.nextToken(","));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}