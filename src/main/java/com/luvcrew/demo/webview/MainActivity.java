package com.luvcrew.demo.webview;


import java.io.InputStream;
import java.io.ByteArrayInputStream;

import java.lang.Runnable;

import android.net.Uri;
import android.graphics.Bitmap;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Build;
import android.os.HardwarePropertiesManager;
import android.os.CpuUsageInfo;
import android.os.SystemClock;

import android.content.Context;

import android.app.Activity;

import android.view.View;
import android.view.ViewGroup;

import android.widget.FrameLayout;
import android.widget.TextView;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.ConsoleMessage;


public class MainActivity extends Activity
{

    void log( final String msg )
    {
        android.util.Log.d( "LUVCREW" , msg );

        new Handler( getMainLooper() ).post(
            new Runnable() {
                public void run()
                {
                    TextView messages = (TextView) findViewById( R.id.messages );
                    String text = messages.getText().toString();
                    messages.setText( text + msg + "\n" );
                }
            }
        );
    }

    public void clearMessages()
    {
        new Handler( getMainLooper() ).post(
            new Runnable() {
                public void run()
                {
                    TextView messages = (TextView) findViewById( R.id.messages );
                    messages.setText( new String() );
                }
            }
        );
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        setContentView( R.layout.main );

        WebView parent_webview = (WebView) findViewById( R.id.parent_webview );

        WebSettings web_settings = parent_webview.getSettings();
        web_settings.setJavaScriptEnabled( true );
        web_settings.setJavaScriptCanOpenWindowsAutomatically( true );
        web_settings.setSupportMultipleWindows( true );

        parent_webview.setWebViewClient( new DemoWebViewClient() );

        parent_webview.setWebChromeClient(
            new WebChromeClient()
            {
                @Override
                public boolean onCreateWindow(
                      WebView view
                    , boolean isDialog
                    , boolean isUserGesture
                    , Message resultMsg
                )
                {
                    FrameLayout child_frame = (FrameLayout) findViewById( R.id.child_frame );
                    child_frame.removeAllViews();

                    WebView child_webview = new WebView( MainActivity.this );
                    child_webview.setLayoutParams(
                        new ViewGroup.LayoutParams(
                              ViewGroup.LayoutParams.MATCH_PARENT
                            , ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    );
                    child_webview.setWebViewClient( new DemoWebViewClient() );
                    child_frame.addView( child_webview );

                    ((WebView.WebViewTransport) resultMsg.obj).setWebView( child_webview );
                    resultMsg.sendToTarget();

                    new Handler( MainActivity.this.getMainLooper() ).post(
                        new Runnable() { public void run() { clearMessages(); } }
                    );
                    return true;
                }

                @Override
                public boolean onConsoleMessage( ConsoleMessage message )
                {
                    log( "console: " + message.message() + " (" + message.lineNumber() + ")" );
                    return true;
                }

            }
        );

        parent_webview.loadUrl( "http://localhost/parent" );
    }


    class DemoWebViewClient extends WebViewClient
    {

        public DemoWebViewClient()
        {
            super();
            log( "new DemoWebViewClient" );
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(
              WebView view
            , WebResourceRequest request
        )
        {
            return handleRequest( request );
        }

        boolean do_once = true;

        @Override
        public void onPageStarted( WebView view , String url , Bitmap favicon )
        {
            log( "onPageStarted: " + url );

            if( do_once && url.contains( "do-reload=true" ) )
            {
                log( "reloading" );
                view.reload();
                do_once = false;
            }
        }

        @Override
        public void onPageFinished( WebView view , String url )
        {
            log( "onPageFinished: " + url );
        }

        WebResourceResponse handleRequest( WebResourceRequest request )
        {
            log( "handleRequest: " + request.getUrl() );

            Uri url = request.getUrl();

            if( ! url.getScheme().equals( "http" ) ) return null;
            if( ! url.getHost().equals( "localhost" ) ) return null;

            int resource_id = 0;
            switch( url.getPath() )
            {
                case "/parent":
                    resource_id = R.raw.parent;
                    break;
                case "/already-opened":
                    resource_id = R.raw.opened;
                    break;
                case "/traditional":
                    resource_id = R.raw.traditional;
                    break;
                default:
                    return new WebResourceResponse( null , null , 404 , "Not Found" , null , null );
            }

            InputStream body = MainActivity.this.getResources().openRawResource( resource_id );

            return new WebResourceResponse( "text/html" , null , body );
        }
    }

}


