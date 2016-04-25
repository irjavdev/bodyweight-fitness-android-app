package com.bodyweight.fitness.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.TextView

import com.bodyweight.fitness.R
import com.bodyweight.fitness.extension.debug
import com.bodyweight.fitness.isRoutineLogged
import com.bodyweight.fitness.isToday
import com.bodyweight.fitness.model.CalendarDayChanged
import com.bodyweight.fitness.stream.CalendarStream
import com.bodyweight.fitness.utils.ViewUtils
import com.trello.rxlifecycle.kotlin.bindToLifecycle

import kotlinx.android.synthetic.main.view_calendar_page.view.*

class CalendarPagePresenter : AbstractPresenter() {
    var mViewPagerPosition = 0
    var mIsTodaysWeek = false
    var mIsTodaysDate = 3

    override fun updateView() {
        super.updateView()

        val view = (mView as CalendarPageView)

        val firstDayOfTheWeek = CalendarDayChanged(0, mViewPagerPosition).date

        for (index in 0..6) {
            val currentDayOfTheWeek = firstDayOfTheWeek.plusDays(index)
            if (currentDayOfTheWeek.isToday()) {
                mIsTodaysWeek = true
                mIsTodaysDate = index

                view.setActive(index)

                if (CalendarStream.getInstance().calendarPage == mViewPagerPosition) {
                    view.select(index)
                    clickedAt(index)
                }
            }

            view.setListener(index)
            view.setIsToday(index, currentDayOfTheWeek.isToday())
            view.showDot(index, currentDayOfTheWeek.isRoutineLogged())
            view.setText(index, currentDayOfTheWeek.dayOfMonth().asText)
        }

        CalendarStream.getInstance()
                .calendarPageObservable
                .bindToLifecycle(view)
                .doOnSubscribe { debug(this.javaClass.simpleName + " = doOnSubscribe") }
                .doOnUnsubscribe { debug(this.javaClass.simpleName + " = doOnUnsubscribe") }
                .filter { it == mViewPagerPosition }
                .subscribe {
                    if (mIsTodaysWeek) {
                        view.select(mIsTodaysDate)
                        clickedAt(mIsTodaysDate)
                    } else {
                        view.select(3)
                        clickedAt(3)
                    }
                }

        CalendarStream.getInstance()
                .calendarDayChangedObservable
                .bindToLifecycle(view)
                .doOnSubscribe { debug(this.javaClass.simpleName + " = doOnSubscribe") }
                .doOnUnsubscribe { debug(this.javaClass.simpleName + " = doOnUnsubscribe") }
                .subscribe {
                    if (it.presenterSelected != mViewPagerPosition) {
                        view.unselect(mIsTodaysDate)
                    } else {
                        view.select(it.daySelected)
                    }
                }
    }

    fun clickedAt(dayView: Int) {
        CalendarStream.getInstance().setCalendarDay(
                CalendarDayChanged(dayView, mViewPagerPosition)
        )
    }
}

open class CalendarPageView : AbstractView {
    override var mPresenter: AbstractPresenter = CalendarPagePresenter()

    var mClickedDay: Int = 3

    val mDayViews: List<TextView> by lazy {
        listOf(this.day_1, this.day_2, this.day_3, this.day_4, this.day_5, this.day_6, this.day_7)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onCreateView() { }

    override fun updateView() {
        super.updateView()
    }

    fun setListener(dayView: Int) {
        val view: TextView? = mDayViews[dayView]

        view?.setOnClickListener {
            select(dayView)

            (mPresenter as CalendarPagePresenter).clickedAt(dayView)
        }
    }

    fun select(dayView: Int) {
        val view: TextView? = mDayViews[dayView]

        unselect(mClickedDay)

        view?.let {
            view.setTextColor(Color.parseColor("#ffffff"))

            ViewUtils.setBackgroundResourceWithPadding(view, R.drawable.rounded_corner_today)
        }

        mClickedDay = dayView
    }

    fun unselect(dayView: Int) {
        val view: TextView? = mDayViews[dayView]
        val isToday = view?.tag as? Boolean ?: false

        val clickedView: TextView? = mDayViews[mClickedDay]

        clickedView?.let {
            if (isToday) {
                clickedView.setTextColor(Color.parseColor("#00453E"))

                ViewUtils.setBackgroundResourceWithPadding(clickedView, R.drawable.rounded_corner_active)
            } else {
                clickedView.setTextColor(Color.parseColor("#00453E"))
                clickedView.setBackgroundResource(0)
            }
        }
    }

    fun setActive(dayView: Int) {
        val view: TextView = mDayViews[dayView]

        ViewUtils.setBackgroundResourceWithPadding(view, R.drawable.rounded_corner_active)
    }

    fun setIsToday(dayView: Int, tag: Boolean) {
        val view: TextView = mDayViews[dayView]

        view.tag = tag
    }

    fun showDot(dayView: Int, show: Boolean) {
        val view: TextView = mDayViews[dayView]

        if (show) {
            view.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.dot)
        } else {
            view.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.dot_invisible)
        }
    }

    fun setText(dayView: Int, text: String) {
        val view: TextView = mDayViews[dayView]

        view.text = text
    }
}