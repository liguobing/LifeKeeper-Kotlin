package com.lixyz.lifekeeperforkotlin.view.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.github.chrisbanes.photoview.PhotoView
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.bean.netdisk.image.ImageBean
import com.lixyz.lifekeeperforkotlin.net.https.HttpsSSLParams
import com.lixyz.lifekeeperforkotlin.net.https.HttpsUtil
import com.lixyz.lifekeeperforkotlin.utils.Constant
import com.lixyz.lifekeeperforkotlin.view.customview.CustomDialog
import kotlinx.coroutines.*
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier

class ImageDetailFragment(
    private val bean: ImageBean,
    private val categoryId: String,
    private val view: IShowImageDetailView
) :
    Fragment() {

    private var loadImageDialog: CustomDialog? = null
    private var image: PhotoView? = null
    private var progress: ProgressBar? = null
    private var factory: HttpsSSLParams? = null
    private var client: OkHttpClient? = null
    private var loadImageCall: Call? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        factory = HttpsUtil.getSslSocketFactory(
            arrayOf(this.requireContext().assets.open(Constant.SERVER_CER)),
            this.requireContext().assets.open(Constant.CLIENT_CER),
            Constant.CLOUD_ADDRESS_CERTIFICATE_PASSWORD
        )
        client = OkHttpClient.Builder().retryOnConnectionFailure(false)
            .connectTimeout(15 * 1000.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(15 * 1000.toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout(15 * 1000.toLong(), TimeUnit.MILLISECONDS)
//            .sslSocketFactory(
//                factory!!.sSLSocketFactory!!, factory!!.trustManager!!
//            )
//            .hostnameVerifier(HostnameVerifier
//            { hostname, _ ->
//                Constant.HOST_NAME == hostname
//            })
            .cache(null)
            .build()
        loadImageDialog = CustomDialog(this.requireContext(), this.activity, "图片载入中")
        val view = inflater.inflate(
            R.layout.view___show_photo_detail___viewpager_item,
            container,
            false
        )
        image = view.findViewById(R.id.photo_view)
        image!!.setOnClickListener {
            (this.activity as ShowImageDetailActivity).photoViewClick()
        }
        progress = view.findViewById(R.id.progress)
        return view
    }

    fun rotateImage() {
        var degree = image!!.rotation
        degree += 90
        image!!.rotation = degree
    }

    private var bitmap: Bitmap? = null
    private var job: Job? = null

    override fun onResume() {
        super.onResume()
        if (bitmap != null && !bitmap!!.isRecycled) {
            image!!.setImageBitmap(bitmap)
        } else {
            if (progress!!.visibility == View.GONE) {
                progress!!.visibility = View.VISIBLE
            }
            job = CoroutineScope(Dispatchers.Main).async {
                val webpFileName = bean.sourceFileName!!.split(".")[0] + ".webp"
                val url = "${Constant.PHOTO_ADDRESS}/${bean.imageUser}/cover/$webpFileName"
                bitmap = getBitmap(url)
                image!!.setImageBitmap(bitmap)
                if (progress!!.visibility == View.VISIBLE) {
                    progress!!.visibility = View.GONE
                }
            }
        }
    }

    fun showHDImage() {
        view.showWaitDialog()
        job = CoroutineScope(Dispatchers.Main).async {
            val url = "${Constant.PHOTO_ADDRESS}/${bean.imageUser}/${bean.sourceFileName}"
            bitmap = getBitmap(url)
            image!!.setImageBitmap(bitmap)
            view.hideWaitDialog()
        }
    }

    override fun onPause() {
        super.onPause()
        if (loadImageCall != null && !loadImageCall!!.isExecuted()) {
            loadImageCall!!.cancel()
        }
        if (progress!!.visibility == View.VISIBLE) {
            progress!!.visibility = View.GONE
        }
        job?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bitmap != null && !bitmap!!.isRecycled) {
            bitmap!!.recycle()
        }
    }


    private suspend fun getBitmap(url: String) = withContext(Dispatchers.IO) {
        val factory = HttpsUtil.getSslSocketFactory(
            arrayOf(requireContext().assets.open(Constant.SERVER_CER)),
            requireContext().assets.open(Constant.CLIENT_CER),
            Constant.CLOUD_ADDRESS_CERTIFICATE_PASSWORD
        )
        val client: OkHttpClient = OkHttpClient.Builder()
            .retryOnConnectionFailure(false)
            .connectTimeout(15 * 1000.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(15 * 1000.toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout(15 * 1000.toLong(), TimeUnit.MILLISECONDS)
//            .sslSocketFactory(
//                factory.sSLSocketFactory!!, factory.trustManager!!
//            )
//            .hostnameVerifier(HostnameVerifier { hostname, _ ->
//                Constant.HOST_NAME == hostname
//            })
            .cache(null)
            .build()
        val request: Request = Request.Builder()
            .url(url)
            .build()
        val response = client.newCall(request).execute()
        val inputStream = response.body!!.byteStream()
        BitmapFactory.decodeStream(inputStream)
    }
}