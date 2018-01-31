
## Update

My major problem seems to resolved in Android System WebView v64. However, I
still get an intermitent blank page with "Blank + reload" button. That is not
an issue for my production app, but still deserves mention. I firmly believe
that `WebView.reload()` should make sure that it reloads the right resource.

Trying this app on a variety of AVDs with different API levels (my only way to
try different versions of WebView) yields different outcomes. Try pressing the
"Blank + reload" button multiple times and you might get the blank page some
times and the child page other times. This leads me to believe that it's
probably a thread management issue in WebView after it decides to go ahead and
load about:blank.

The most serious issue, the "Google" button resulting in a spinlock, appeared
in WebView v61 and looks solved in v64. To see it, create an AVD with Oreo. It
will have WevView v61.

I'm still afraid that the issue is not completely solved, so I'm resubmitting
this as a bug.

## Original issue

This demo app was requested in response to [Chromium bug 770015][1].

Starting with WebView 61 (Sep 17), some issues prevented some child window
pages from loading. My app browses an internal web site, so I can't use it to
demonstrate the problem. I also don't control the web server, so any strange
behavior in the web pages is not my fault and can't be fixed. The Android
Chrome app, however, loads the pages just fine. The sequence happens like this:

- Parent window uses javascript to open a child window and uses it as the
  target for a form.

    window.open( "//Pages/Please wait..." , win_name , features );
    form.target = win_name;
    form.submit();

- My app sees a series of calls:

    WebChromeClient.createWindow()
    WebViewClient.onPageStart() url is form.target as expected
    WebViewClient.onPageStart() url is about:blank 
    WebViewClient.onPageFinished() url is about:blank
    WebViewClient.onPageStart() url is form.target (again)

- After that, the app's CPU usage rises and no page loads.

I found that sending a delayed `WebView.reload()` caused the form to resubmit
and get the WebView unclogged and added that to the app with _most_ users
satisfied.

The demo app has a parent WebView at the top of the screen, the child window
WebView below that and a scrollable message console at the bottom.

The parent web page has buttons that trigger different child window opening
behaviors:

- open the child with form.target = "\_blank"

- open the child with `window.open( "about:blank" )` and then call `form.submit()`


- open the child with `window.opet( "https://www.google.com/" )` and then call
  `form.submit()`

  This usually results in a blank child window and high CPU usage. **Fixed in
  v64**

- open the child with `window.open( "about:blank" )`, call `form.submit()` and
  then call WebView.reload()

  This often results in a blank child window. ** NOT fixed in v64 **

- open the child with `window.opet( "https://www.google.com/" )` and then call
  `form.submit()` and then call WebView.reload()

  This usually "fixes" the problem in my real-world app.

If you don't want to build, look for the AVD in [build/output/avd/debug][2]

[1]: https://bugs.chromium.org/p/chromium/issues/detail?id=770015
[2]: https://github.com/mikehat/demo-webview/tree/master/build/outputs/apk/debug
