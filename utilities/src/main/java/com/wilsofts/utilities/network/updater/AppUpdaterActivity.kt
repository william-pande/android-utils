package com.wilsofts.utilities.network.updater

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.wilsofts.utilities.LibUtils
import com.wilsofts.utilities.R
import com.wilsofts.utilities.dialogs.ReturnResponse

class AppUpdaterActivity : AppCompatActivity(), InstallStateUpdatedListener {
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var editor: SharedPreferences.Editor

    companion object {
        const val IMMEDIATE = 1
        const val FLEXIBLE = 2

        fun isUpdateAvailable(context: Context, returnResponse: ReturnResponse) {
            val editor: SharedPreferences.Editor = context.getSharedPreferences("updater_prefs", Context.MODE_PRIVATE).edit()
            val prefs = context.getSharedPreferences("updater_prefs", Context.MODE_PRIVATE)
            // Creates instance of the manager.
            val appUpdateManager = AppUpdateManagerFactory.create(context)
            // Returns an intent object that you use to check for an update.
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo
            // Checks that the platform will allow the specified type of update.
            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    val version = appUpdateInfo.availableVersionCode()
                    val current = prefs.getInt("current_version", 0)
                    val last_checked = prefs.getLong("last_checked", 0L)
                    val ignored = prefs.getBoolean("ignored", false)

                    when {
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> {
                            returnResponse.response(true)
                        }

                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                            if (current < version) {
                                continueUpdate(returnResponse, editor, appUpdateInfo, version)

                            } else {
                                if ((System.currentTimeMillis() - last_checked) < (3600 * 24) || ignored) {
                                    returnResponse.response(false)
                                } else {
                                    continueUpdate(returnResponse, editor, appUpdateInfo, version)
                                }
                            }
                        }
                    }
                } else {
                    returnResponse.response(false)
                }
            }
        }

        private fun continueUpdate(returnResponse: ReturnResponse, editor: SharedPreferences.Editor, appUpdateInfo: AppUpdateInfo, version: Int) {
            editor.putInt("current", version)
            editor.putLong("last_checked", System.currentTimeMillis())
            editor.apply()

            when {
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> {
                    returnResponse.response(proceed = true, ignored = false)
                }
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                    returnResponse.response(true, ignored = false)
                }
                else -> {
                    returnResponse.response(false)
                }
            }
        }
    }

    override fun onStateUpdate(installState: InstallState?) {
        if (installState?.installStatus() == InstallStatus.DOWNLOADED) {
            notifyUser()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_updater)

        editor = getSharedPreferences("updater_prefs", Context.MODE_PRIVATE).edit()


        appUpdateManager = AppUpdateManagerFactory.create(this)

        appUpdateManager.registerListener(this)

        // Returns an intent object that you use to check for an update.
        // Checks that the platform will allow the specified type of update.
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                when {
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> {
                        val dialog = LibUtils.ConfirmationDialog.newInstance(this,
                                "New Update", "A new update has been released, do you wish to continue updating your app?",
                                object : ReturnResponse {
                                    override fun response(proceed: Boolean, ignored: Boolean) {
                                        editor.putBoolean("ignored", ignored)
                                        editor.apply()

                                        if (proceed) {
                                            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE,
                                                    this@AppUpdaterActivity, IMMEDIATE)
                                        }
                                    }
                                },
                                "Ignore this app update")
                        dialog.show(this.supportFragmentManager, "confirm_update")
                    }
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                        appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE, this, FLEXIBLE)
                    }
                }
            }else{
                this.finish()
            }
        }
    }

    private fun notifyUser() {
        appUpdateManager.completeUpdate()
        appUpdateManager.unregisterListener(this)
        LibUtils.showToast(this, "Update finished, restarting application")
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager
                .appUpdateInfo
                .addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                        // If an in-app update is already running, resume the update.
                        appUpdateManager.startUpdateFlowForResult(appUpdateInfo, IMMEDIATE, this, IMMEDIATE)

                    } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                        notifyUser()
                    }
                }
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMMEDIATE || requestCode == FLEXIBLE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    //The user has accepted the update.
                    // For immediate updates, you might not receive this callback because the update should already be completed by
                    // Google Play by the time the control is given back to your app
                }
                Activity.RESULT_CANCELED -> {
                    LibUtils.showToast(this, "Update rejected")
                    this.finish()
                }
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                    //Some other error prevented either the user from providing consent or the update to proceed.
                    LibUtils.showToast(this, "Updated failed")
                    this.finish()
                }
            }
        } else {
            this.finish()
        }
    }
}
