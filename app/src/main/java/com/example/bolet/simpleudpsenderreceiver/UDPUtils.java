package com.example.bolet.simpleudpsenderreceiver;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

/**
 * Created by bolet on 18/01/16.
 */
public class UDPUtils implements AsyncResponse {

    private UDPServer udpServer;
    private UDPSender udpSender;
    protected MainActivity context;
    private int serverPort = 45450;



    /**
     * Used for get the context of main activity for modify the response
     *
     * @param context
     */
    public UDPUtils(Context context) {
        this.context = (MainActivity) context;
    }

    /**
     * Get the param of the async task nad add a text view to the main activity context.
     *
     * @param output
     */
    @Override
    public void processFinish(final String output) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                LinearLayout llLog = (LinearLayout) ((MainActivity) context).findViewById(R.id.llLog);
                TextView tvToAdd = new TextView(context);
                tvToAdd.setText( output.substring( output.indexOf("|")+1 ) );
                tvToAdd.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                if ( output.indexOf("RX|") >= 0 ) {
                    tvToAdd.setTextColor(Color.RED);
                } else if ( output.indexOf("TX|") >= 0 ) {
                    tvToAdd.setTextColor(Color.GREEN);
                } else {
                    tvToAdd.setTextColor(Color.BLACK);
                }



                    llLog.addView(tvToAdd);
            }
        });

    }

    /**
     * Start UDP server as asynctask
     */
    void startReceiveUdp() {
        if (udpServer == null) {

            udpServer = new UDPServer();
            udpServer.setPort(serverPort);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                udpServer.delegate = this;
                udpServer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            } else {
                udpServer.execute();
            }
//            UDPServer=new UDPServer();
//            UDPServer.execute("");
        }
    }

    /**
     * Stop UDP server
     */
    void stopReceiveUdp() {

        if (udpServer != null) {
            udpServer.cancelClientSocket();
            udpServer.cancel(true);
            udpServer = null;
        }
    }

    /**
     * Restart UDP server
     * @param port
     */
    void restartReceiveUdp(int port) {
        stopReceiveUdp();
        serverPort = port;
        startReceiveUdp();
    }
    
    
    public void sendUDP (HashMap... params){
        udpSender = new UDPSender();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            udpSender.delegate = this;
            udpSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,params);

        } else {
            udpSender.execute(params);
        }
        //new UDPSender().execute(params);
    }


    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int port) {
        this.serverPort = port;
    }
}

/**
 * This interface is used for gestion the response of the AsyncTasks
 */
interface AsyncResponse {
    void processFinish(String output);
}
