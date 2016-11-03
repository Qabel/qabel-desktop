package de.qabel.desktop.ui

import com.google.gson.Gson
import de.qabel.desktop.crashReports.CrashReportHandler
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import org.slf4j.LoggerFactory
import java.io.*
import java.text.MessageFormat
import java.util.*
import javax.inject.Inject


open class AbstractController {
    @Inject
    private val reportHandler: CrashReportHandler? = null

    protected var gson: Gson? = null

    protected fun alert(e: Throwable) {
        alert(e.message ?: "", e)
    }

    protected fun tryOrAlert(runnable: CheckedRunnable) {
        try {
            runnable.run()
        } catch (e: Exception) {
            alert(e)
        }

    }

    protected fun getString(resources: ResourceBundle, message: String, vararg params: Any): String {
        return MessageFormat.format(resources.getString(message), *params)
    }

    var confirmDialog: Alert? = null
        internal set

    @Throws(Exception::class)
    protected fun confirm(title: String, text: String, onConfirm: CheckedRunnable) {
        confirmDialog = Alert(
                Alert.AlertType.CONFIRMATION,
                "",
                ButtonType.YES,
                ButtonType.CANCEL)
        confirmDialog!!.headerText = null
        val content = Label(text)
        content.isWrapText = true
        confirmDialog!!.dialogPane.content = content
        confirmDialog!!.title = title

        val result = confirmDialog!!.showAndWait()
        if (result.isPresent && result.get() == ButtonType.YES) {
            onConfirm.run()
        }
        confirmDialog = null
    }

    @FunctionalInterface
    interface CheckedRunnable {
        @Throws(Exception::class)
        fun run()
    }

    var alert: CrashReportAlert? = null

    fun alert(message: String, e: Throwable) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater { alert(message, e) }
            return
        }
        LoggerFactory.getLogger(javaClass).error(message, e)
        e.printStackTrace()

        val alert = CrashReportAlert(reportHandler, message, e)
        this.alert = alert
        alert.showAndWait()
    }

    @Throws(IOException::class)
    protected fun writeStringInFile(json: String, dir: File) {
        val targetFile = File(dir.path)
        targetFile.createNewFile()
        val outStream = FileOutputStream(targetFile)
        outStream.write(json.toByteArray())
    }

    @Throws(IOException::class)
    fun readFile(f: File): String {
        val fileReader = FileReader(f)
        val br = BufferedReader(fileReader)

        try {
            val sb = StringBuilder()
            var line: String? = br.readLine()

            while (line != null) {
                sb.append(line)
                line = br.readLine()
                if (line != null) {
                    sb.append("\n")
                }
            }
            return sb.toString()
        } finally {
            br.close()
        }
    }
}
