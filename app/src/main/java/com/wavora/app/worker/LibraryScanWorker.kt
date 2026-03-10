package com.wavora.app.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.wavora.app.core.utils.Constants
import com.wavora.app.data.repository.MusicRepositoryImpl
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * @author Muhamed Amin Hassan on 10,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
@HiltWorker
class LibraryScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val musicRepository: MusicRepositoryImpl,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result =
        try {
            val scanResult = musicRepository.scanLibrary()
            Log.i(
                TAG,
                "Scan done — added:${scanResult.added} updated:${scanResult.updated} removed:${scanResult.removed}"
            )
            Result.success()
        } catch (e: SecurityException) {
            Log.w(TAG, "Scan failed — storage permission missing", e)
            Result.failure()
        } catch (e: Exception) {
            Log.e(TAG, "Scan failed — will retry", e)
            if (runAttemptCount < MAX_RETRIES)
                Result.retry()
            else
                Result.failure()
        }

    companion object {
        private const val TAG = "LibraryScanWorker"
        private const val MAX_RETRIES = 3

        // ── One-time scan (first launch / manual rescan) ──────────────────────

        /**
         * Enqueues an immediate, expedited scan.
         * [ExistingWorkPolicy.KEEP] prevents queuing a second scan if one is
         * already running — avoids double-scans on rapid permission grant.
         */

        fun enqueueOneTimeScan(workManager: WorkManager) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false) // Run even on low battery for first-launch
                .build()

            val request = OneTimeWorkRequestBuilder<LibraryScanWorker>()
                .setConstraints(constraints)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .addTag(Constants.WORK_TAG_LIBRARY_SCAN)
                .build()

            workManager.enqueueUniqueWork(
                Constants.WORK_TAG_LIBRARY_SCAN,
                ExistingWorkPolicy.KEEP,
                request
            )
        }

        // ── Periodic scan (e.g. daily, battery-constrained) ───────────────────

        /**
         * Schedules a periodic rescan that respects battery constraints.
         * Runs at most once per day, only when battery and storage are healthy.
         *
         * [ExistingPeriodicWorkPolicy.KEEP] — if a periodic scan is already
         * scheduled, don't replace it (would reset the interval timer).
         */

        fun schedulePeriodicScan(workManager: WorkManager) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<LibraryScanWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
                flexTimeInterval = 4, // can run anytime in a 4-hour window
                flexTimeIntervalUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .addTag(Constants.WORK_TAG_LIBRARY_SCAN)
                .build()

            workManager.enqueueUniquePeriodicWork(
                "${Constants.WORK_TAG_LIBRARY_SCAN}_periodic",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}