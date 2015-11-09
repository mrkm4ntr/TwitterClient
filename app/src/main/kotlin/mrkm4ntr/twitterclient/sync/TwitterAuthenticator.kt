package mrkm4ntr.twitterclient.sync

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.accounts.NetworkErrorException
import android.content.Context
import android.content.Intent
import android.os.Bundle

import mrkm4ntr.twitterclient.activities.OAuthActivity

class TwitterAuthenticator(private val mContext: Context) : AbstractAccountAuthenticator(mContext) {

    override fun editProperties(
            accountAuthenticatorResponse: AccountAuthenticatorResponse, s: String): Bundle? {
        return null
    }

    @Throws(NetworkErrorException::class)
    override fun addAccount(accountAuthenticatorResponse: AccountAuthenticatorResponse, s: String,
                            s1: String, strings: Array<String>, options: Bundle): Bundle {
        return Bundle().apply {
            val intent = Intent(mContext, OAuthActivity::class.java)
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse)
            putParcelable(AccountManager.KEY_INTENT, intent)
        }
    }

    @Throws(NetworkErrorException::class)
    override fun confirmCredentials(accountAuthenticatorResponse: AccountAuthenticatorResponse,
                                    account: Account, bundle: Bundle): Bundle? {
        return null
    }

    @Throws(NetworkErrorException::class)
    override fun getAuthToken(accountAuthenticatorResponse: AccountAuthenticatorResponse,
                              account: Account, s: String, bundle: Bundle): Bundle {
        val accountManager = AccountManager.get(mContext)
        val authToken = accountManager.peekAuthToken(account, s)
        authToken?.let {
            if (it.isEmpty()) {
                return Bundle().apply {
                    putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
                    putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
                    putString(AccountManager.KEY_AUTHTOKEN, authToken)
                }
            }
        }
        return Bundle().apply {
            val intent = Intent(mContext, OAuthActivity::class.java)
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse)
            putParcelable(AccountManager.KEY_INTENT, intent)
        }
    }

    override fun getAuthTokenLabel(s: String): String? {
        return null
    }

    @Throws(NetworkErrorException::class)
    override fun updateCredentials(
            accountAuthenticatorResponse: AccountAuthenticatorResponse, account: Account, s: String,
            bundle: Bundle): Bundle? {
        return null
    }

    @Throws(NetworkErrorException::class)
    override fun hasFeatures(accountAuthenticatorResponse: AccountAuthenticatorResponse, account: Account, strings: Array<String>): Bundle? {
        return null
    }
}
