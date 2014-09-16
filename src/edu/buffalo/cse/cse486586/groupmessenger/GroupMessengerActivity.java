package edu.buffalo.cse.cse486586.groupmessenger;


import android.content.ContentResolver;

import android.net.Uri;

import android.content.ContentValues;

import java.io.ObjectOutputStream;

import java.util.LinkedList;

import java.util.ArrayList;

import java.util.List;

import java.util.Queue;

import java.util.concurrent.Executor;

import android.widget.EditText;

import android.view.View.OnClickListener;

import android.view.View;

import android.view.View;

import android.widget.Button;

import java.io.ObjectInputStream;

import java.io.DataOutputStream;

import java.net.InetAddress;

import java.net.UnknownHostException;

import android.telephony.TelephonyManager;

import android.content.Context;

import android.util.Log;

import java.io.FileOutputStream;

import java.io.IOException;

import java.io.BufferedReader;

import java.io.InputStreamReader;

import java.net.Socket;

import java.net.ServerSocket;

import android.os.AsyncTask;

import android.os.Bundle;
import android.app.Activity;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.widget.TextView;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author pratik
 *
 */
public class GroupMessengerActivity extends Activity {

    static final String TAG = GroupMessengerActivity.class.getSimpleName();

    static final String SEQUENCER = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";

    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";
    private final Uri  mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger.provider");;
    static int count = 0;



    static final String [] ports = {"11108","11112","11116","11120","11124"};
    boolean isSequencer = false;
    static final int SERVER_PORT = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        final EditText editText = (EditText) findViewById(R.id.editText1);

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        final String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        Log.d("port String", portStr);
        Log.d("Line number",tel.getLine1Number());
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));


        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             * 
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {

            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        Button send = (Button)findViewById(R.id.button4);
        send.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String msg = editText.getText().toString();
                editText.setText("");

                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg,portStr);

            }
        });
        
    }

    public void multicast(String msg)
    {
    
        for(String s : ports)
        {
            Log.d("Port no", s);
    
            // Message orderedMsg = new Message("1", msg, "ordered");
            new Sequencer().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,msg,s,String.valueOf(count));
        }
    
        count++;
    }

    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }


    
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];

            try{

                while(true)
                {
                    Socket s = serverSocket.accept();
                    // BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String line = "";

                    Message msg = null;
                    ObjectInputStream objin = new ObjectInputStream(s.getInputStream());

                    // Log.d("Server", "Server started");
                    TelephonyManager tel = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
                    String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
                    final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));



                    try {
                        msg =(Message)objin.readObject();
                        //                        while(msg!=null)
                        //                        {
                        // Log.d("Server", br.readLine().toString());

                        if(msg.getType().equals("ordered"))
                        {
                            ContentValues cv = new ContentValues();
                            cv.put(KEY_FIELD, msg.getKey());
                            cv.put(VALUE_FIELD, msg.getMessage());
                            getContentResolver().insert(mUri, cv);
                        }
                        publishProgress(msg.getMessage(),msg.getType(),msg.getKey());

                        //                         }

                        if(myPort.equals(SEQUENCER) && msg.getType().equals("unordered"))
                        {
                            Log.d(SEQUENCER,"multicast");

                            multicast(msg.getMessage());

                        }

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    //br.close();

                    objin.close();
                    s.close();
                }


            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 


 
            return null;
        }

        protected void onProgressUpdate(String...strings) {
          
            if(strings[1].equals("ordered"))
            {
                TextView tv = (TextView)findViewById(R.id.textView1);
                tv.append(strings[2].trim()+strings[0].trim()+"\t\n");
            }

            return;
        }
    }

    //
    /***
     * ClientTask is an AsyncTask that should send a string over the network.
     * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
     * an enter key press event.
     * 
     * @author pratik
     *
     */
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String remotePort = SEQUENCER;

                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(remotePort));

                String msgToSend = msgs[0];


                ObjectOutputStream objout = new ObjectOutputStream(socket.getOutputStream());
                // DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                Message msg = new Message("null", msgToSend, "unordered");
                objout.writeObject(msg);
                // dos.writeUTF(msgToSend);
                //  q.add(msgToSend);

                Log.d("Client", msgToSend);
                //  dos.close();

             
                socket.close();


            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }

            return null;
        }
    }

    private class Sequencer extends AsyncTask<String, Void, Void>
    {


        protected Void doInBackground(String... msgs) {
            try {
                String remotePort = msgs[1];


                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(remotePort));

                String msgToSend = msgs[0];


                ObjectOutputStream objout = new ObjectOutputStream(socket.getOutputStream());
                // DataOutputStream dos = new DataOutputStream(socket.getOutputStream());


                Message msg = new Message(msgs[2], msgToSend, "ordered");
                objout.writeObject(msg);
                //   DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                //   dos.writeUTF(msgToSend);

                Log.d("Client", msgToSend);
                //   dos.close();

      

                objout.close();
                socket.close();


            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }

            return null;
        }

    }

}
