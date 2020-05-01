package net.xzos.upgradeall.ui.activity.file_pref

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import net.xzos.upgradeall.R
import net.xzos.upgradeall.core.data_manager.utils.wait
import net.xzos.upgradeall.utils.FileUtil

abstract class FilePrefActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blank_wait)
        checkPermissionAndSelectFile()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        when (resultCode) {
            Activity.RESULT_CANCELED -> {
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 运行完成，解锁
        if (mutex.isLocked) mutex.unlock()
    }

    private fun checkPermissionAndSelectFile() {
        if (FileUtil.requestPermission(this, PERMISSIONS_REQUEST_WRITE_CONTACTS)) {
            selectFile()
        }
    }

    abstract fun selectFile()

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_WRITE_CONTACTS) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.file_permission_request, Toast.LENGTH_LONG).show()
                finish()
            } else {
                checkPermissionAndSelectFile()
            }
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_WRITE_CONTACTS = 1

        private val mutex = Mutex()

        suspend fun startActivity(context: Context, cls: Class<*>) {
            withContext(Dispatchers.Default) {
                mutex.lock()
                context.startActivity(Intent(context, cls))
                mutex.wait()
            }
        }
    }
}
