package com.example.bolet.simpleudpsenderreceiver;

import android.os.AsyncTask;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by bolet on 18/01/16.
 */

public class UDPServer extends AsyncTask<String, String, String> {

    private int port;
    public AsyncResponse delegate = null;
    DatagramSocket clientsocket;



    protected void cancelClientSocket (){
        try {
            clientsocket.close();
            clientsocket = new DatagramSocket(null);
            clientsocket.setReuseAddress(true);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String doInBackground(String... params) {
        while (true) {
            try {
                publishProgress(receiveMessage());
                if(isCancelled()) {
                    Log.v("CANCELL SOCKET: " , "CANCELLED");
                    break;
                }
            } catch (Exception e) {
                //
            }

        }
        return "";
    }

    public String[] receiveMessage(){

        String[] rec_arr = null;
        try {
            if (clientsocket == null){
                clientsocket=new DatagramSocket(port);
            }

            byte[] receivedata = new byte[30];

            DatagramPacket recv_packet = new DatagramPacket(receivedata, receivedata.length);
            //Log.d("UDP", "S: Receiving...");
            clientsocket.receive(recv_packet);
            String str = new String(recv_packet.getData()); //stringa con mesasggio ricevuto

            InetAddress ipaddress = recv_packet.getAddress();
            int portR = recv_packet.getPort();
            Log.d("IPAddress : ",ipaddress.toString());
            Log.d(" Port : ",Integer.toString(portR));
            String rec_str = ipaddress.toString() + ":" + Integer.toString(portR) + "|";
            rec_str += str.replace(Character.toString ((char) 0), "");
            Log.d(" Received String ",rec_str);

            rec_arr=rec_str.split("\\|");


            return rec_arr;

        } catch (Exception e) {
            Log.e("UDP", "S: Error", e);
        }
        return rec_arr;
    }


    @Override
    protected void onPostExecute(String result) {
        //
    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onProgressUpdate(String... rec_arr) {
        //ricevi la stringa,
        //splittala
        //esegui l'azione richiesta sulla GUI



        if (rec_arr.length>1){
            String ip=rec_arr[0].substring(1);
            String command=rec_arr[1];

            String uniqueString = "RX|" + command +"<-" + ip ;
            Log.v("UNIQUE String" , uniqueString);

            delegate.processFinish(uniqueString);


               /* if(command.contentEquals("go")){
                    //press button go
                    //startAction(null);
                }*/


        }
//            Log.v("RECEIVED UDP", "onProgressUpdate: " + rec_arr[0]);

    }
    // you may separate this or combined to caller class.





    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }


}