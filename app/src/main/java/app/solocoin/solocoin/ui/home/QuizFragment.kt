package app.solocoin.solocoin.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import app.solocoin.solocoin.R
import app.solocoin.solocoin.app.SolocoinApp.Companion.sharedPrefs
import app.solocoin.solocoin.repo.NoConnectivityException
import app.solocoin.solocoin.util.AppDialog
import app.solocoin.solocoin.util.GlobalUtils
import app.solocoin.solocoin.util.enums.Status
import com.google.android.material.card.MaterialCardView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.fragment_quiz.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.math.abs

/**
 * Created by Ankur Kumar on 14/05/20
 */
@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class QuizFragment(position: Int) : Fragment(), View.OnClickListener {
    private val DAILY_QUIZ: Int = 0
    private val WEEKLY_QUIZ: Int = 1
    private val CURRENT_QUIZ: Int = position

    private val WEEKLY_QUIZ_QUESTION_LIMIT: Int = 10

    private val TWO_HOURS: Long = 7200000

    private var IS_DEVICE_TIME_CORRECT = GlobalUtils.verifyDeviceTimeConfig()

    private val NOCONNECTIVITY_EXCEPTION: Int = 11
    private val AFTER_TWO_HOURS: Int = 12
    private val DEVICE_TIME_INCORRECT: Int = 13
    private val AVAILABLE_SUNDAY: Int = 14
    private val QUIZ_UNAVAILABLE: Int = 15

    private val viewModel: QuizViewModel by viewModel()

    private lateinit var answers: JsonArray
    private var question_id: Int = 0

    private var mView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_quiz, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        IS_DEVICE_TIME_CORRECT = GlobalUtils.verifyDeviceTimeConfig()

        if (CURRENT_QUIZ == DAILY_QUIZ)
            getDailyQuiz()
        else if (CURRENT_QUIZ == WEEKLY_QUIZ)
            getWeeklyQuiz()

        option1_card.setOnClickListener(this)
        option2_card.setOnClickListener(this)
        option3_card.setOnClickListener(this)
        option4_card.setOnClickListener(this)

    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.option1_card -> verifyAnswer(option1_card, 0)
            R.id.option2_card -> verifyAnswer(option2_card, 1)
            R.id.option3_card -> verifyAnswer(option3_card, 2)
            R.id.option4_card -> verifyAnswer(option4_card, 3)
        }
    }

    private fun verifyAnswer(optionCard: MaterialCardView, optionNumber: Int) {
//        optionCard.setBackgroundColor(resources.getColor(R.color.green_stroke_color));

        optionCard.strokeColor =
            ResourcesCompat.getColor(resources, R.color.green_stroke_color, null)

        option1_card.apply {
            setOnClickListener(null)
            isClickable = false
        }
        option2_card.apply {
            setOnClickListener(null)
            isClickable = false
        }
        option3_card.apply {
            setOnClickListener(null)
            isClickable = false
        }
        option4_card.apply {
            setOnClickListener(null)
            isClickable = false
        }

        submitQuizAnswer(optionNumber)
    }

    private fun getDailyQuiz() {
        if (IS_DEVICE_TIME_CORRECT) {

            if (sharedPrefs!!.dailyQuizTime > 0L) {

                if (abs(sharedPrefs!!.dailyQuizTime - Calendar.getInstance().timeInMillis) > TWO_HOURS) {
                    getDailyQuizQuestion()

                } else {
                    showFallbackText(AFTER_TWO_HOURS)
                }
            } else {
                getDailyQuizQuestion()
            }
        } else {
            showFallbackText(DEVICE_TIME_INCORRECT)
        }
    }

    private fun getWeeklyQuiz() {
        if (IS_DEVICE_TIME_CORRECT) {

            if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {

                if (sharedPrefs!!.countAnsweredWeeklyQuiz < WEEKLY_QUIZ_QUESTION_LIMIT) {
                    getWeeklyQuizQuestion()

                } else {
                    showFallbackText(AVAILABLE_SUNDAY)
                }

            } else {
                showFallbackText(AVAILABLE_SUNDAY)
                sharedPrefs!!.countAnsweredWeeklyQuiz = 0
            }
        } else {
            showFallbackText(DEVICE_TIME_INCORRECT)
        }
    }

    private fun getDailyQuizQuestion() {
        quiz_container.visibility = View.GONE
        quiz_message.visibility = View.GONE
        quiz_placeholder.visibility = View.VISIBLE
        viewModel.getDailyQuiz().observe(viewLifecycleOwner, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        if (resource.code == 200) {
                            quiz_placeholder.visibility = View.GONE
                            quiz_message.visibility = View.GONE
                            quiz_container.visibility = View.VISIBLE

                            answers = resource.data!!.getAsJsonArray("answers")
                            question_id = resource.data.get("id").asInt
                            val question=resource.data.get("name").toString().substring(1)
                            question_textview.text = question.substring(0,question.length-1)
                            val option1 ="A) "+answers[0].asJsonObject.get("name").toString().substring(1)
                            val option2 ="B) "+answers[1].asJsonObject.get("name").toString().substring(1)
                            val option3 ="C) "+answers[2].asJsonObject.get("name").toString().substring(1)
                            val option4 ="D) "+answers[3].asJsonObject.get("name").toString().substring(1)

                            option1_textview.text = option1.substring(0,option1.length-1)
                            option2_textview.text = option2.substring(0,option2.length-1)
                            option3_textview.text = option3.substring(0,option3.length-1)
                            option4_textview.text = option4.substring(0,option4.length-1)
                        }
                    }

                    Status.ERROR -> {
                        if (resource.exception is NoConnectivityException) {
                            showFallbackText(NOCONNECTIVITY_EXCEPTION)

                        } else {
                            showFallbackText(QUIZ_UNAVAILABLE)
                        }
                    }

                    Status.LOADING -> {
                    }
                }
            }
        })
    }

    private fun getWeeklyQuizQuestion() {
        quiz_message.visibility = View.GONE
        quiz_container.visibility = View.GONE
        quiz_placeholder.visibility = View.VISIBLE
        viewModel.getWeeklyQuiz().observe(viewLifecycleOwner, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        if (resource.code == 200) {
                            quiz_placeholder.visibility = View.GONE
                            quiz_message.visibility = View.GONE
                            quiz_container.visibility = View.VISIBLE

                            answers = resource.data!!.getAsJsonArray("answers")
                            question_id = resource.data.get("id").asInt
                            question_textview.text = resource.data.get("name").toString()
                            val option1 ="A) "+answers[0].asJsonObject.get("name").toString().substring(1)
                            val option2 ="B) "+answers[1].asJsonObject.get("name").toString().substring(1)
                            val option3 ="C) "+answers[2].asJsonObject.get("name").toString().substring(1)
                            val option4 ="d) "+answers[3].asJsonObject.get("name").toString().substring(1)

                            option1_textview.text = option1.substring(0,option1.length-1)
                            option2_textview.text = option2.substring(0,option2.length-1)
                            option3_textview.text = option3.substring(0,option3.length-1)
                            option4_textview.text = option4.substring(0,option4.length-1)
                        }
                    }

                    Status.ERROR -> {
                        if (resource.exception is NoConnectivityException) {
                            showFallbackText(NOCONNECTIVITY_EXCEPTION)

                        } else {
                            showFallbackText(QUIZ_UNAVAILABLE)
                        }
                    }

                    Status.LOADING -> {
                    }
                }
            }
        })
    }

    private fun submitQuizAnswer(optionId: Int) {
        val body = JsonObject()
        body.addProperty("question_id", question_id)
        body.addProperty("answer_id", answers[optionId].asJsonObject.get("id").asInt)
//        quiz_container.visibility = View.GONE
//        quiz_message.visibility = View.GONE
//        quiz_placeholder.visibility = View.VISIBLE
        val isCorrect = answers[optionId].asJsonObject.get("correct").asBoolean
        Log.i("karan","jsonlookslikethis"+body);
        viewModel.submitQuizAnswer(body).observe(this, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        if (resource.code == 201) {
                            if (isCorrect) {
                                showInfoDialog(
                                    "Correct Answer",
                                    "You get 50 coins.\n\nPlease wait 2-3 minutes for wallet to update."
                                )
                            } else {
                                showInfoDialog(
                                    "Wrong Answer",
                                    "Better luck next time."
                                )
                            }
//                          Log.d("Question submit SUCESS", question_id.toString())
                            if (CURRENT_QUIZ == DAILY_QUIZ) {
                                sharedPrefs?.dailyQuizTime = Calendar.getInstance().timeInMillis
                                showFallbackText(AFTER_TWO_HOURS)
                            } else if (CURRENT_QUIZ == WEEKLY_QUIZ) {
                                var countAnswered = sharedPrefs!!.countAnsweredWeeklyQuiz
                                countAnswered += 1
                                sharedPrefs!!.countAnsweredWeeklyQuiz = countAnswered
                                getWeeklyQuiz()
                            }
                        } else {
                            quiz_placeholder.visibility = View.GONE
                            quiz_container.visibility = View.GONE
                            quiz_message.visibility = View.VISIBLE
                            quiz_message_text.text = getString(R.string.error_msg)
                        }
                    }

                    Status.ERROR -> {
                        if (resource.exception is NoConnectivityException) {
                            showFallbackText(NOCONNECTIVITY_EXCEPTION)

                        } else {
                            quiz_placeholder.visibility = View.GONE
                            quiz_container.visibility = View.GONE
                            quiz_message.visibility = View.VISIBLE
                            quiz_message_text.text = getString(R.string.error_msg)
                        }
                    }

                    Status.LOADING -> {
                    }
                }
            }
        })
    }

    val showInfoDialog: (String, String) -> Unit = { title: String, message: String ->
        val infoDialog = AppDialog.instance(
            title,
            message,
            object : AppDialog.AppDialogListener {
                override fun onClickConfirm() {
                    onClickCancel()
                }

                override fun onClickCancel() {}
            },
            getString(R.string.okay)
        )
        infoDialog.show(requireFragmentManager(), infoDialog.tag)
    }

    private fun showFallbackText(category: Int) {
        quiz_placeholder.visibility = View.GONE
        quiz_container.visibility = View.GONE
        quiz_message.visibility = View.VISIBLE

        when (category) {
            NOCONNECTIVITY_EXCEPTION -> quiz_message_text.text =
                getString(R.string.noconnectivity_error_msg)

            AFTER_TWO_HOURS -> {
                val millisecLeft =
                    TWO_HOURS - abs(sharedPrefs!!.dailyQuizTime - Calendar.getInstance().timeInMillis)
                val minsLeft = ((millisecLeft / 60000) % 60).toInt()
                val hrsLeft = (millisecLeft / 3600000).toInt()
                quiz_message_text.text =
                    String.format(getString(R.string.after_two_hours_msg), hrsLeft, minsLeft)
            }

            DEVICE_TIME_INCORRECT -> quiz_message_text.text =
                getString(R.string.device_time_incorrect_msg)

            AVAILABLE_SUNDAY -> quiz_message_text.text = getString(R.string.available_sunday_msg)

            QUIZ_UNAVAILABLE -> quiz_message_text.text = getString(R.string.quiz_unavailable_msg)
        }
    }

}