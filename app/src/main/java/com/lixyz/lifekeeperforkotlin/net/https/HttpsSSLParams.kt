package com.lixyz.lifekeeperforkotlin.net.https

import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

class HttpsSSLParams {
    var sSLSocketFactory: SSLSocketFactory? = null
    var trustManager: X509TrustManager? = null
}