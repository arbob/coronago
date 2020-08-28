package app.solocoin.solocoin.ui.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import app.solocoin.solocoin.R
import app.solocoin.solocoin.model.Reward
import app.solocoin.solocoin.util.GlobalUtils
import com.google.android.material.button.MaterialButton
import java.util.*

/**
 * Created by Saurav Gupta on 14/5/2020
 */
class RewardRedeemAdapter(
    private val context: Activity,
    private val rewardArrayList: ArrayList<Reward>
) :
    RecyclerView.Adapter<RewardRedeemAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder =
        ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_reward_redeem, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setUpView(context, rewardArrayList[position])
    }

    override fun getItemCount() = rewardArrayList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var rewardImage: ImageView
        private var companyLogo: ImageView
        private var rewardName: TextView
        private var coinsAmt: TextView
        private var tnc: LinearLayout
        private var copyBtn: MaterialButton
        private var rewardCouponCode: TextView
        private var rewardCouponCodeRl: RelativeLayout

        init {
            with(itemView) {
                rewardImage = findViewById(R.id.reward_image)
                companyLogo = findViewById(R.id.company_logo)
                rewardName = findViewById(R.id.reward_name)
                coinsAmt = findViewById(R.id.coins_amt)
                tnc = findViewById(R.id.tnc)
                copyBtn = findViewById(R.id.copy_button)
                rewardCouponCode = findViewById(R.id.redeem_code)
                rewardCouponCodeRl = findViewById(R.id.redeem_code_container)
            }

            rewardImage.visibility = View.GONE
            companyLogo.visibility = View.GONE
        }

        @SuppressLint("DefaultLocale")
        fun setUpView(context: Activity, reward: Reward) {
            reward.let {
                //updateImage(it)
                coinsAmt.text = ("${it.costCoins} coins")
                rewardName.text = it.rewardName.capitalize()
                updateRewardTnc(it)
                rewardCouponCode.text = it.couponCode
                copyBtn.setOnClickListener {
                    (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).apply {
                        setPrimaryClip(ClipData.newPlainText("Code", rewardCouponCode.text))
                    }
                    Toast.makeText(
                        context,
                        "Coupon code copied successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                when (it.isClaimed) {
                    false -> rewardCouponCodeRl.visibility = View.GONE
                    true -> rewardCouponCodeRl.visibility = View.VISIBLE
                }
            }
        }

//        private fun updateImage(reward: Reward) {
//            GlobalUtils.loadImageNetworkCacheVisibility(reward.rewardImageUrl, rewardImage)
//            GlobalUtils.loadImageNetworkCacheVisibility(reward.companyLogoUrl, companyLogo)
//        }

        private fun updateRewardTnc(reward: Reward) {
            reward.rewardTermsAndConditions?.let {
                val rewardTncTV = TextView(tnc.context).apply {
                    text = ("\u2022 " + it.trim().replace("[\n\r]", "\n\u2022 ").trimIndent())
                    setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    typeface = ResourcesCompat.getFont(context, R.font.poppins)
                }
                tnc.addView(rewardTncTV)
            }
        }
    }


}