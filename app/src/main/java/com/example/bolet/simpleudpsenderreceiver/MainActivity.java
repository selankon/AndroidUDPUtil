package com.example.bolet.simpleudpsenderreceiver;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;


public class MainActivity extends Activity {


    private UDPUtils udpUtils;

    private Button btSend;
    private TextView tvServerLabel;
    private EditText etIp, etRxPort, etTxPort, etMessage;
    private CheckBox cbBroadcast;
    private LinearLayout llLog;

    private String serverLabelMessage = "UDP server listening at port ";

    private String testServer = "192.168.1.91"; //127.0.0.1
    private String udpMsg = "HELLO!\r\n\0";
    private int defaultServerPort = 45450;

    /**
     * Pattern for validate ip well writted
     */
    private static final Pattern PARTIAl_IP_ADDRESS =
            Pattern.compile("^((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])\\.){0,3}"+
                    "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])){0,1}$");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etIp = (EditText) findViewById(R.id.etIp);
        etRxPort = (EditText) findViewById(R.id.etRxPort);
        etTxPort = (EditText) findViewById(R.id.etTxPort);
        etMessage = (EditText) findViewById(R.id.etMessage);
        tvServerLabel = (TextView) findViewById(R.id.tvServerLabel);
        btSend = (Button) findViewById(R.id.btSend);
        llLog = (LinearLayout) findViewById(R.id.llLog);
        cbBroadcast = (CheckBox) findViewById(R.id.cbBroadcast);

        etRxPort.setText(String.valueOf(defaultServerPort), TextView.BufferType.EDITABLE);
        etIp.setText(String.valueOf(testServer), TextView.BufferType.EDITABLE);


        //Starting udp server
        udpUtils = new UDPUtils(this);
        udpUtils.setServerPort(defaultServerPort);
        udpUtils.startReceiveUdp();

        //Label with the terminal ip and listening port, by default 45450
        tvServerLabel.setText(serverLabelMessage + getIPAddress(true) + ":" +String.valueOf(udpUtils.getServerPort()) );

        //Listener for the Reception port Edit text for change the server label text view and the listening port of udp server
        etRxPort.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after) {}

            private String mPreviousText = "";
            @Override
            public void afterTextChanged(Editable s) {
                if ( !(etRxPort.getText().toString().matches("")) ) {
                    udpUtils.restartReceiveUdp( Integer.parseInt(etRxPort.getText().toString()) );

                    tvServerLabel.setText(serverLabelMessage + getIPAddress(true) + ":" +String.valueOf(udpUtils.getServerPort()) );
                }
            }
        });


        //Listener for the Ip Edit text that makes it an edit text that only accepts valid ip numbers
        etIp.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after) {}

            private String mPreviousText = "";
            @Override
            public void afterTextChanged(Editable s) {
                if(isValidIp(s.toString())) {
                    mPreviousText = s.toString();
                    etIp.setTextColor(Color.BLACK);
                    //Log.v("IP" , "" + etIp.getText());
                } else {
                    Toast.makeText(getApplicationContext(), "Not valid IP" , Toast.LENGTH_SHORT).show();
                    //etIp.setTextColor(Color.RED);
                    s.replace(0, s.length(), mPreviousText);
                }
            }
        });


        //Listener on send button for send an UDP msg datagram to specific ip:port
        btSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                if ( (isValidIp(etIp.getText().toString())) && !(etTxPort.getText().toString().matches("")) ){

                    HashMap<String,Object> params = new HashMap<String, Object>( );
//                    params.put("ip", testServer);
//                    params.put("msg", udpMsg);
//                    params.put("port", defaultServerPort);

                    if ( cbBroadcast.isChecked() ) {

                    }
                    params.put("ip", etIp.getText().toString() );
                    params.put("msg", etMessage.getText().toString() );
                    params.put("port", Integer.parseInt(etTxPort.getText().toString()) );
                    params.put("bdc", cbBroadcast.isChecked() );

                    //new UDPSender().execute(params);
                    udpUtils.sendUDP(params);

                } else {
                    Toast.makeText(getApplicationContext(), "Parameters not corrects" , Toast.LENGTH_SHORT).show();
                }

            }
        });


//        UDPListenerService xx = new UDPListenerService();
//        xx.startListenForUDPBroadcast();
        //Toast.makeText(getApplicationContext(), "" + tv.getText(), LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        if ( udpUtils != null ) udpUtils.stopReceiveUdp();
        super.onDestroy();
        Toast.makeText(getApplicationContext(),"Stopped UDP Server", Toast.LENGTH_SHORT).show();
    }

    private boolean isValidIp (String s){
        return PARTIAl_IP_ADDRESS.matcher(s).matches();
    }


    /**
     * Get IP address from first non-localhost interface
     * @param ipv4  true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }



}
