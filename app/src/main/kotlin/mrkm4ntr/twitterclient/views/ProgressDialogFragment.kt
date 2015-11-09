package mrkm4ntr.twitterclient.views

import android.app.Dialog
import android.app.DialogFragment
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle

class ProgressDialogFragment : DialogFragment() {

    override fun getDialog(): Dialog {
        return sDialog!!
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message = arguments.getString("message")
        sDialog = ProgressDialog(activity)
        sDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        sDialog!!.setMessage(message)
        return sDialog!!
    }

    override fun onCancel(dialog: DialogInterface) {
        activity.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        sDialog = null
    }

    companion object {

        private var sDialog: ProgressDialog? = null

        fun newInstance(message: String): ProgressDialogFragment {
            val fragment = ProgressDialogFragment()
            val args = Bundle()
            args.putString("message", message)
            fragment.arguments = args
            return fragment
        }
    }
}
