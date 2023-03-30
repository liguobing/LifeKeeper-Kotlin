package com.lixyz.lifekeeperforkotlin.net.https

import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class HttpsTrustManager(localTrustManager: X509TrustManager) : X509TrustManager {
    private var defaultTrustManager: X509TrustManager? = null
    private var localTrustManager: X509TrustManager? = localTrustManager

    init {
        val var4 = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        var4.init(null as KeyStore?)
        defaultTrustManager = chooseTrustManager(var4.trustManagers)
    }

    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        try {
            defaultTrustManager!!.checkServerTrusted(chain, authType)
        } catch (ce: CertificateException) {
            localTrustManager!!.checkServerTrusted(chain, authType)
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate?> {
        return arrayOfNulls(0)
    }

    private fun chooseTrustManager(trustManagers: Array<TrustManager>): X509TrustManager? {
        for (trustManager in trustManagers) {
            if (trustManager is X509TrustManager) {
                return trustManager
            }
        }
        return null
    }
}