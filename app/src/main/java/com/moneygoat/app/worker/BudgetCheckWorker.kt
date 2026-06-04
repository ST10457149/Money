package com.moneygoat.app.worker

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.moneygoat.app.R
import com.moneygoat.app.data.database.AppDatabase
import java.util.*

class BudgetCheckWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val userId = inputData.getLong("USER_ID", -1L)
        if (userId == -1L) return Result.failure()

        val db = AppDatabase.getDatabase(applicationContext)
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)

        // Find the goal for the current month
        val goal = db.budgetGoalDao().getGoalDirect(userId, month, year) ?: return Result.success()

        // Calculate current spending
        val startDate = "%04d-%02d-01".format(year, month)
        val endDate = "%04d-%02d-%02d".format(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val spent = db.expenseDao().getTotalSpentDirect(userId, startDate, endDate) ?: 0.0

        if (spent >= goal.maximumGoal) {
            sendNotification(1, "Budget Exceeded!", "You have spent R$spent, which is over your limit of R${goal.maximumGoal}!")
        } else if (spent >= goal.maximumGoal * 0.9) {
            sendNotification(1, "Budget Warning", "You are at 90% of your monthly budget!")
        }

        // Inactivity Check
        val lastExpense = db.expenseDao().getLastExpense(userId)
        if (lastExpense != null) {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val lastDate = sdf.parse(lastExpense.date)
            if (lastDate != null) {
                val diff = System.currentTimeMillis() - lastDate.time
                val days = diff / (1000 * 60 * 60 * 24)
                if (days >= 2) {
                    sendNotification(2, "Goat Alert! 🐐", "You haven't logged an expense in $days days. Stay on track!")
                }
            }
        }

        return Result.success()
    }

    private fun sendNotification(id: Int, title: String, message: String) {
        val builder = NotificationCompat.Builder(applicationContext, "BUDGET_ALERTS")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(applicationContext)) {
                notify(id, builder.build())
            }
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}
