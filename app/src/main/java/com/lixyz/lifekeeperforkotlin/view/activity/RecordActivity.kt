package com.lixyz.lifekeeperforkotlin.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModelProvider
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.lixyz.lifekeeperforkotlin.R
import com.lixyz.lifekeeperforkotlin.bean.phonerecord.ContactBean
import com.lixyz.lifekeeperforkotlin.bean.phonerecord.PlayerStateValue
import com.lixyz.lifekeeperforkotlin.bean.phonerecord.RecordBean
import com.lixyz.lifekeeperforkotlin.presenter.RecordViewModel
import com.lixyz.lifekeeperforkotlin.utils.PlayVideoDataSource
import kotlinx.android.synthetic.main.activity___net_disk.*
import kotlinx.android.synthetic.main.dialog_paste.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class RecordActivity : AppCompatActivity(), IRecordView {

    private var viewModel: RecordViewModel? = null

    private var player: SimpleExoPlayer? = null

    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModelProvider = ViewModelProvider(this)

        val isHasStoragePermission = Environment.isExternalStorageManager()
        if (!isHasStoragePermission) {
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            startActivity(intent)
        }

        viewModel = viewModelProvider[RecordViewModel::class.java]
        viewModel!!.fileToSQLite(this)
        viewModel!!.getContactNames(this)
        setContent {
            MaterialTheme {
                MainCard()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel!!.checkNeedUpload()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }

    override fun onBackPressed() {
        setResult(RESULT_OK)
        finish()
    }


    //底部上传模态框显示状态
    @OptIn(ExperimentalMaterialApi::class)
    private var uploadModalVisibleState: ModalBottomSheetState? = null

    @OptIn(ExperimentalMaterialApi::class)
    private var playerModalState: ModalBottomSheetState? = null

    private var playingContactNameState: MutableState<String>? = null
    private var callTimeState: MutableState<String>? = null
    private var playerState: MutableState<PlayerStateValue>? = null
    private var playerSpeedState: MutableState<Float>? = null
    private var recordList: MutableList<RecordItem>? = null

    private var editableState: MutableState<Boolean>? = null
    private var contactItemList: MutableList<ContactItem>? = null
    private var waitDialogState: MutableState<Boolean>? = null


    @ExperimentalAnimationApi
    @OptIn(
        ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class,
        ExperimentalFoundationApi::class
    )
    @Composable
    fun MainCard() {
        val scope = rememberCoroutineScope()
        //等候 Dialog
        waitDialogState = remember {
            mutableStateOf(false)
        }
        viewModel!!.waitDialogStateLiveData!!.observe(this) {
            waitDialogState!!.value = it
        }
        //上传 Dialog
        val uploadDialogState = remember {
            mutableStateOf(false)
        }
        viewModel!!.uploadDialogStateLiveData!!.observe(this) {
            uploadDialogState.value = it
        }
        //联系人
        contactItemList = remember {
            mutableStateListOf()
        }
        viewModel!!.contactLiveData!!.observe(this) {
            contactItemList!!.clear()
            it.forEachIndexed { _, contactBean ->
                contactItemList!!.add(ContactItem(contactBean, mutableStateOf(false)))
            }
        }
        //录音
        recordList = remember {
            mutableStateListOf()
        }
        viewModel!!.recordLiveData!!.observe(this) {
            recordList!!.clear()
            it.forEachIndexed { _, recordBean ->
                recordList!!.add(RecordItem(recordBean, false))
            }
            scope.launch { playerModalState!!.show() }
            waitDialogState!!.value = false
        }
        //FloatingActionButton
        val floatingActionButtonVisibleState = remember {
            mutableStateOf(false)
        }
        viewModel!!.needUploadLiveData!!.observe(this) {
            floatingActionButtonVisibleState.value = it
        }
        //本地文件
        val localFileList = remember {
            mutableStateListOf<NeedUploadItem>()
        }
        viewModel!!.localFileLiveData!!.observe(this) { it ->
            localFileList.clear()
            it.forEach {
                localFileList.add(NeedUploadItem(it, mutableStateOf(false)))
            }
            scope.launch { uploadModalVisibleState!!.show() }
            waitDialogState!!.value = false

        }
        //上传文件进度
        val uploadDialogProgressState = remember {
            mutableStateOf(0f)
        }
        viewModel!!.uploadDialogProgressLiveData!!.observe(this) {
            uploadDialogProgressState.value = it
        }
        //上传文件名
        val uploadDialogFileNameState = remember {
            mutableStateOf("")
        }
        viewModel!!.uploadDialogFileNameLiveData!!.observe(this) {
            uploadDialogFileNameState.value = it
        }

        playerState = remember {
            mutableStateOf(PlayerStateValue.STOP)
        }

        playerSpeedState = remember {
            mutableStateOf(1f)
        }

        playingContactNameState = remember {
            mutableStateOf("")
        }
        callTimeState = remember {
            mutableStateOf("未播放")
        }

        editableState = remember {
            mutableStateOf(false)
        }
        viewModel!!.editableStateLiveData!!.observe(this) {
            editableState!!.value = it
        }

        //等待 Dialog 动画
        val infiniteTransition = rememberInfiniteTransition()
        val angle by infiniteTransition.animateFloat(
            initialValue = 0f, // 初始值
            targetValue = 360f, // 最终值
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing), // 一个动画值的转换持续 1 秒，缓和方式为 LinearEasing
                repeatMode = RepeatMode.Restart
            )
        )

        uploadModalVisibleState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
        playerModalState =
            rememberModalBottomSheetState(ModalBottomSheetValue.Hidden, confirmStateChange = {
                if (it == ModalBottomSheetValue.Hidden) {
                    if (player != null) {
                        //停止播放
                        player!!.stop()
                    }
                    //回复播放按钮状态
                    playerState!!.value = PlayerStateValue.STOP
                    playerSpeedState!!.value = 1f
                    callTimeState!!.value = "未播放"
                }
                true
            })

        Scaffold(
            topBar = {
                TopAppBar(
                    backgroundColor = colorResource(id = R.color.RecordActivityTopBarBackgroundColor),//背景色
                    contentColor = Color.White,
                    title = {
                        if (editableState!!.value) {
                            Text(
                                "编辑",
                                color = Color.White,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                        } else {
                            Text(
                                "通话录音",
                                color = Color.White,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                        }
                    },
                    actions = {
                        if (editableState!!.value) {
                            Icon(
                                Icons.Filled.Delete,
                                null,
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .clickable {
                                        val checkList = mutableListOf<String>()
                                        contactItemList!!.forEachIndexed { _, contactItem ->
                                            if (contactItem.checked.value) {
                                                checkList.add(contactItem.contactBean.contactId)
                                            }
                                        }
                                        waitDialogState!!.value = true
                                        viewModel!!.deleteContact(this@RecordActivity, checkList)
                                    }
                            )
                            Icon(
                                Icons.Filled.Done,
                                null,
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .clickable {
                                        editableState!!.value = false
                                        val tmpList = contactItemList!!.map { it.copy() }
                                        tmpList.forEachIndexed { index, contactItem ->
                                            contactItemList!![index] =
                                                contactItemList!![index].copy(
                                                    contactBean = contactItem.contactBean,
                                                    checked = mutableStateOf(false)
                                                )
                                        }
                                    }
                            )
                        } else {
                            Icon(
                                Icons.Filled.Edit,
                                null,
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .clickable { editableState!!.value = true }
                            )
                        }
                    }
                )
            },
            content = { it ->
                LazyColumn(
                    modifier = Modifier
                        .padding(it)
                        .background(Color.White)
                ) {
                    items(contactItemList!!) {
                        Box(modifier = Modifier
                            .padding(start = 10.dp, end = 10.dp, top = 5.dp)
                            .clip(RoundedCornerShape(10))
                            .clickable {
                                if (!editableState!!.value) {
                                    playingContactNameState!!.value = it.contactBean.contactName
                                    waitDialogState!!.value = true
                                    viewModel!!.getRecords(
                                        this@RecordActivity,
                                        it.contactBean.contactId
                                    )
                                }
                            }) {
                            ContactNameCard(it)
                        }
                    }
                }
                //上传Dialog
                if (uploadDialogState.value) {
                    Dialog(
                        onDismissRequest = { uploadDialogState.value = false },
                        properties = DialogProperties(
                            dismissOnClickOutside = false,
                            dismissOnBackPress = false
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(shape = RoundedCornerShape(10))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                LinearProgressIndicator(
                                    color = Color.Green,
                                    backgroundColor = Color.Gray,
                                    progress = uploadDialogProgressState.value,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp, 0.dp, 10.dp, 0.dp)
                                )
                                Text(
                                    uploadDialogFileNameState.value,
                                    modifier = Modifier.padding(10.dp, 20.dp, 10.dp, 0.dp),
                                    maxLines = 1
                                )
                                Text(
                                    "上传中...",
                                    modifier = Modifier.padding(0.dp, 20.dp, 0.dp, 0.dp)
                                )
                            }
                        }
                    }
                }
                //等候Dialog
                if (waitDialogState!!.value) {
                    Dialog(
                        onDismissRequest = { waitDialogState!!.value = false },
                        properties = DialogProperties(
                            dismissOnClickOutside = false,
                            dismissOnBackPress = false
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(shape = RoundedCornerShape(10))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painterResource(id = R.drawable.record_loading),
                                    "",
                                    modifier = Modifier
                                        .rotate(angle)
                                        .size(50.dp)
                                )
                                Text(
                                    "请稍候...",
                                    modifier = Modifier.padding(0.dp, 20.dp, 0.dp, 0.dp)
                                )
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                if (floatingActionButtonVisibleState.value) {
                    FloatingActionButton(
                        onClick = {
                            waitDialogState!!.value = true
                            viewModel!!.getLocalFileList()
                        },
                    ) {
                        Icon(Icons.Filled.Cloud, contentDescription = "上传云端")
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End
        )
        //底部上传/删除弹窗
        ModalBottomSheetLayout(
            sheetState = uploadModalVisibleState!!,
            sheetContent = {
                Column {
                    //上传/删除 按钮组
                    Row {
                        //上传本地文件
                        Button(modifier = Modifier.weight(1f), onClick = {
                            uploadDialogState.value = true
                            scope.launch { uploadModalVisibleState!!.hide() }
                            val checkedList = ArrayList<String>()
                            localFileList.forEachIndexed { _, needUploadItem ->
                                if (needUploadItem.check.value) {
                                    checkedList.add(needUploadItem.fileName)
                                }
                            }
                            viewModel!!.uploadRecordFile(this@RecordActivity, checkedList)
                        }) {
                            Text(text = "上传")
                        }
                        //删除本地文件
                        Button(modifier = Modifier.weight(1f), onClick = {
                            waitDialogState!!.value = true
                            scope.launch { uploadModalVisibleState!!.hide() }
                            val checkedList = ArrayList<String>()
                            localFileList.forEachIndexed { _, needUploadItem ->
                                if (needUploadItem.check.value) {
                                    checkedList.add(needUploadItem.fileName)
                                }
                            }
                            viewModel!!.deleteRecordFile(this@RecordActivity, checkedList)
                        }) {
                            Text(text = "删除")
                        }
                    }
                    //本地录音文件
                    LazyColumn(
                        modifier = Modifier.padding(0.dp,50.dp),
                        content = {
                        items(localFileList) {
                            Log.d("TTT", "MainCard: ${localFileList.size}")
                            Row(
                                horizontalArrangement = Arrangement.Center,//设置水平居中对齐
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = it.check.value, onCheckedChange = { result ->
                                    it.check.value = result
                                })
                                Text(text = it.fileName, maxLines = 1)
                            }
                        }
                    })
                }
            }
        ) {

        }
        //播放器/播放列表
        ModalBottomSheetLayout(
            sheetState = playerModalState!!,
            sheetContent = {
                Column {
                    LazyColumn(modifier = Modifier
                        .padding(0.dp, 0.dp, 0.dp, 30.dp),
                        content = {
                            stickyHeader {
                                PlayerHeader()
                            }

                            items(recordList!!, key = {
                                it.record.objectId!!
                            }) {
                                RecordItemCard(it, recordList!!)
                                Divider(
                                    thickness = 3.dp,
                                    color = Color.White,
                                    startIndent = 10.dp

                                )
                            }
                        })

                }
            }
        ) {

        }
    }

    //联系人姓名
    @Composable
    fun ContactNameCard(contactItem: ContactItem) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(colorResource(id = R.color.RecordActivityContactListItemColor))
                .height(60.dp)
        ) {
            if (editableState!!.value) {
                Checkbox(
                    checked = contactItem.checked.value,
                    onCheckedChange = {
                        contactItem.checked.value = it
                    })
            }
            Text(
                text = contactItem.contactBean.contactName,
                softWrap = false,
                maxLines = 1,
                fontSize = 30.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp),
            )
        }
    }

    @Composable
    fun PlayerHeader() {
        Column {
            //联系人姓名和通话日期
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, bottom = 0.dp, top = 10.dp)
                    .clip(shape = RoundedCornerShape(10))
                    .background(color = colorResource(id = R.color.RecordActivityContactNameAndCallTime))
            ) {
                Text(
                    text = playingContactNameState!!.value,
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    color = Color.White
                )
                Text(
                    text = callTimeState!!.value,
                    modifier = Modifier.padding(top = 10.dp),
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
            //播放控制面板
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .height(80.dp)
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, bottom = 0.dp, top = 10.dp)
                    .clip(shape = RoundedCornerShape(10))
                    .background(color = colorResource(id = R.color.RecordActivityPlayControlPanel))
            ) {
                if (playerSpeedState!!.value == 1f) {
                    Image(
                        modifier = Modifier
                            .weight(1f)
                            .size(30.dp)
                            .clickable {
                                playerSpeedState!!.value = 2f
                                if (player != null) {
                                    val param = PlaybackParameters(2f)
                                    player!!.setPlaybackParameters(param)
                                }
                            },
                        painter = painterResource(id = R.drawable.record_x1),
                        contentDescription = "正常速度播放"
                    )
                } else {
                    Image(
                        modifier = Modifier
                            .weight(1f)
                            .size(30.dp)
                            .clickable {
                                playerSpeedState!!.value = 1f
                                if (player != null) {
                                    val param = PlaybackParameters(1f)
                                    player!!.setPlaybackParameters(param)
                                }
                            },
                        painter = painterResource(id = R.drawable.record_x2),
                        contentDescription = "正常速度播放"
                    )
                }

                if (playerState!!.value == PlayerStateValue.PLAYING) {
                    Image(
                        modifier = Modifier
                            .weight(1f)
                            .size(30.dp)
                            .clickable {
                                player!!.playWhenReady = false
                                playerState!!.value = PlayerStateValue.PAUSE
                            },
                        painter = painterResource(id = R.drawable.record_pause),
                        contentDescription = "播放/暂停"
                    )
                } else {
                    Image(
                        modifier = Modifier
                            .weight(1f)
                            .size(30.dp)
                            .clickable {
                                if (playerState!!.value == PlayerStateValue.PAUSE) {
                                    player!!.playWhenReady = true
                                    playerState!!.value = PlayerStateValue.PLAYING
                                } else {
                                    //更新 item 背景色
                                    val tmpList = recordList!!.map { it.copy() }
                                    tmpList.forEachIndexed { index, recordItem ->
                                        if (index == 0) {
                                            recordList!![index] = recordList!![index].copy(
                                                record = recordItem.record,
                                                checked = true
                                            )
                                        } else {
                                            recordList!![index] = recordList!![index].copy(
                                                record = recordItem.record,
                                                checked = false
                                            )
                                        }
                                    }
                                    callTimeState!!.value =
                                        "${recordList!![0].record.callTime!!.substring(0, 4)}-${
                                            recordList!![0].record.callTime!!.substring(
                                                4,
                                                6
                                            )
                                        }-${
                                            recordList!![0].record.callTime!!.substring(
                                                6,
                                                8
                                            )
                                        } ${
                                            recordList!![0].record.callTime!!.substring(
                                                8,
                                                10
                                            )
                                        }:${
                                            recordList!![0].record.callTime!!.substring(
                                                10,
                                                12
                                            )
                                        }:${
                                            recordList!![0].record.callTime!!.substring(
                                                12,
                                                14
                                            )
                                        }"
                                    playerState!!.value = PlayerStateValue.PLAYING
                                    playerPlayingIndex = 0
                                    playerSpeedState!!.value = 1f

                                    val url =
                                        "https://www.li-xyz.com/LifeKeeper/resource/LifeKeeperCallRecord/${recordList!![0].record.recordUser}/${recordList!![0].record.sourceFileName}"
                                    player = getPlayer()
                                    val dataSourceFactory =
                                        PlayVideoDataSource(this@RecordActivity).dataSourceFactory
                                    val source = ProgressiveMediaSource
                                        .Factory(dataSourceFactory)
                                        .createMediaSource(Uri.parse(url))
                                    val param = PlaybackParameters(1f)
                                    player!!.setPlaybackParameters(param)
                                    player!!.prepare(source)
                                    player!!.playWhenReady = true

                                }
                            },
                        painter = painterResource(id = R.drawable.record_play),
                        contentDescription = "播放/暂停"
                    )
                }
                Image(
                    modifier = Modifier
                        .weight(1f)
                        .size(30.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                // 点击事件
                                onTap = {
                                    val tmpList = recordList!!.map { it.copy() }
                                    tmpList.forEachIndexed { index, recordItem ->
                                        recordList!![index] = recordList!![index].copy(
                                            record = recordItem.record,
                                            checked = false
                                        )
                                    }
                                    callTimeState!!.value = "未播放"
                                    if (player != null) {
                                        player!!.stop()
                                        playerState!!.value = PlayerStateValue.STOP
                                    }
                                })
                        },
                    painter = painterResource(id = R.drawable.record_stop),
                    contentDescription = "停止"
                )
            }
        }
    }

    private var playerPlayingIndex = 0


    //电话录音
    @Composable
    fun RecordItemCard(
        recordItem: RecordItem,
        recordList: MutableList<RecordItem>
    ) {
        if (recordItem.checked) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(50.dp)
                    .padding(start = 10.dp, end = 10.dp)
                    .clip(shape = RoundedCornerShape(10))
                    .background(color = colorResource(id = R.color.RecordActivityPlayListChecked))
                    .clickable {
                        Log.d("TTT", "RecordItemCard: 1")
                    }
            ) {
                Text(
                    text = recordItem.record.callTime!!,
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(50.dp)
                    .padding(start = 10.dp, end = 10.dp)
                    .clip(shape = RoundedCornerShape(10))
                    .background(color = colorResource(id = R.color.RecordActivityPlayList))
                    .clickable {
                        //更新 item 背景色
                        val controlRecordIndex = recordList.indexOf(recordItem)
                        val tmpList = recordList.map { it.copy() }
                        tmpList.forEachIndexed { index, recordItem ->
                            if (index == controlRecordIndex) {
                                recordList[index] = recordList[index].copy(
                                    record = recordItem.record,
                                    checked = true
                                )
                            } else {
                                recordList[index] = recordList[index].copy(
                                    record = recordItem.record,
                                    checked = false
                                )
                            }
                        }
                        //修改表头通话时间
                        callTimeState!!.value =
                            "${recordItem.record.callTime!!.substring(0, 4)}-${
                                recordItem.record.callTime!!.substring(
                                    4,
                                    6
                                )
                            }-${
                                recordItem.record.callTime!!.substring(
                                    6,
                                    8
                                )
                            } ${
                                recordItem.record.callTime!!.substring(
                                    8,
                                    10
                                )
                            }:${
                                recordItem.record.callTime!!.substring(
                                    10,
                                    12
                                )
                            }:${
                                recordItem.record.callTime!!.substring(
                                    12,
                                    14
                                )
                            }"
                        playerState!!.value = PlayerStateValue.PLAYING
                        playerPlayingIndex = controlRecordIndex
                        playerSpeedState!!.value = 1f

                        val url =
                            "https://www.li-xyz.com/LifeKeeper/resource/LifeKeeperCallRecord/${recordItem.record.recordUser}/${recordItem.record.sourceFileName}"
                        player = getPlayer()
                        val dataSourceFactory =
                            PlayVideoDataSource(this@RecordActivity).dataSourceFactory
                        val source = ProgressiveMediaSource
                            .Factory(dataSourceFactory)
                            .createMediaSource(Uri.parse(url))
                        val param = PlaybackParameters(1f)
                        player!!.setPlaybackParameters(param)
                        player!!.prepare(source)
                        player!!.playWhenReady = true
                    }

            ) {
                Text(
                    text = recordItem.record.callTime!!,
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }


    private fun getPlayer(): SimpleExoPlayer {
        if (player == null) {
            player = SimpleExoPlayer.Builder(this).build()
        }
        return player as SimpleExoPlayer
    }

    data class NeedUploadItem(var fileName: String, var check: MutableState<Boolean>)
    data class RecordItem(var record: RecordBean, var checked: Boolean)
    data class ContactItem(var contactBean: ContactBean, var checked: MutableState<Boolean>)
}