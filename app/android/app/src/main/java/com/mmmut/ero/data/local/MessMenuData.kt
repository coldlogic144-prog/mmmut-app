package com.mmmut.ero.data.local

import com.mmmut.ero.data.model.MessDay
import com.mmmut.ero.data.model.MessWeeklyMenu
import java.util.Calendar

object MessMenuData {
    val DAYS = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    const val BREAKFAST_TIMING = "07:30 AM – 09:00 AM"
    const val LUNCH_TIMING = "12:30 PM – 02:00 PM"
    const val SNACKS_TIMING = "04:45 PM – 05:30 PM"
    const val DINNER_TIMING = "07:30 PM – 09:00 PM"

    val DEFAULT_WEEKLY_MENU = MessWeeklyMenu(
        days = listOf(
            MessDay(
                day = "Monday",
                breakfast = listOf(
                    "Stuffed Paratha",
                    "Veg Soya",
                    "Tea/Coffee",
                    "Milk",
                    "Any seasonal fruit"
                ),
                lunch = listOf(
                    "Chana dal",
                    "Mixed seasonal vegetables",
                    "Salad",
                    "Rice",
                    "Chapati",
                    "Pickle"
                ),
                snacks = listOf(
                    "Sandwich (2 Pc.)",
                    "Aloo? / vegetable paneer & sauce",
                    "Tea",
                    "Sharbat"
                ),
                dinner = listOf(
                    "Mix Dal (Moong/Masoor)",
                    "Seasonal vegetables",
                    "Salad",
                    "Chutney",
                    "Jeera rice",
                    "Chapati",
                    "Pickle"
                )
            ),
            MessDay(
                day = "Tuesday",
                breakfast = listOf(
                    "Poha",
                    "Veg Upma",
                    "Dal Chilla",
                    "Milk",
                    "Tea/Coffee",
                    "Any seasonal fruit"
                ),
                lunch = listOf(
                    "Arhar dal",
                    "Kofta",
                    "Salad",
                    "Rice",
                    "Chapati",
                    "Custard",
                    "Pickle"
                ),
                snacks = listOf(
                    "Patties (1 Pc.)",
                    "Sauce",
                    "Tea/Coffee/Lassi"
                ),
                dinner = listOf(
                    "Dal Makhani",
                    "Seasonal vegetables",
                    "Rice",
                    "Chapati",
                    "Salad"
                )
            ),
            MessDay(
                day = "Wednesday",
                breakfast = listOf(
                    "Methi Palak Sabji",
                    "Paratha",
                    "Boiled Egg",
                    "Curd",
                    "Oats",
                    "Tea/Coffee",
                    "Milk",
                    "Any seasonal fruit"
                ),
                lunch = listOf(
                    "Dal",
                    "White Chana",
                    "Boondi/Veg Raita",
                    "Chapati",
                    "Salad",
                    "Pickle"
                ),
                snacks = listOf(
                    "Macaroni/Pasta",
                    "Sauce",
                    "Coffee/Aam Panna"
                ),
                dinner = listOf(
                    "Palak Paneer",
                    "Paneer Chilli",
                    "Masoor Dal",
                    "Suji Gajar Halwa",
                    "Rice",
                    "Chapati",
                    "Salad",
                    "Pickle"
                )
            ),
            MessDay(
                day = "Thursday",
                breakfast = listOf(
                    "Puri",
                    "Aloo Sabji",
                    "Aloo Chura",
                    "Veg Upma",
                    "Tea/Coffee",
                    "Milk",
                    "Any seasonal fruit"
                ),
                lunch = listOf(
                    "Rajma",
                    "Seasonal mix vegetables",
                    "Curd (1 Katori)",
                    "Rice",
                    "Chapati",
                    "Salad",
                    "Pickle"
                ),
                snacks = listOf(
                    "Dal Kachori (2 Pc.)",
                    "Sauce/Chutney",
                    "Tea/Veg Soup",
                    "Sharbat"
                ),
                dinner = listOf(
                    "Arhar Dal",
                    "Mix Vegetable",
                    "Chapati",
                    "Rice",
                    "Salad",
                    "Pickle",
                    "Ice Cream"
                )
            ),
            MessDay(
                day = "Friday",
                breakfast = listOf(
                    "Veg Besan Cheela with green chutney",
                    "Vada-?",
                    "Sambhar with coconut chutney",
                    "Tea/Coffee",
                    "Milk",
                    "Any seasonal fruit"
                ),
                lunch = listOf(
                    "Aloo Gobhi Matar",
                    "Yellow Dal",
                    "Chapati",
                    "Jeera Rice",
                    "Raita",
                    "Salad",
                    "Pickle"
                ),
                snacks = listOf(
                    "Chhola Kulcha",
                    "Tea/Shikanji"
                ),
                dinner = listOf(
                    "Kadhi Pakora",
                    "Egg Curry",
                    "Seasonal vegetables",
                    "Rice",
                    "Chapati",
                    "Salad",
                    "Kheer/Boondi Halwa"
                )
            ),
            MessDay(
                day = "Saturday",
                breakfast = listOf(
                    "Choley Bhatura with lassi",
                    "Veg Sevaiya",
                    "Tea/Coffee",
                    "Milk",
                    "Any seasonal fruit"
                ),
                lunch = listOf(
                    "Dal Tadka",
                    "Matar Paneer",
                    "Curd",
                    "Papad",
                    "Rice",
                    "Chapati",
                    "Salad",
                    "Pickle"
                ),
                snacks = listOf(
                    "Mix Bread Pakora (1 Pc.)",
                    "Sauce/Chutney",
                    "Tea/Coffee/Lemon Juice"
                ),
                dinner = listOf(
                    "Moong Dal",
                    "Mix seasonal vegetables",
                    "Rice",
                    "Chapati",
                    "Salad"
                )
            ),
            MessDay(
                day = "Sunday",
                breakfast = listOf(
                    "Paratha (Onion/Lauki)",
                    "Curd",
                    "Butter",
                    "Tea/Coffee",
                    "Milk",
                    "Any seasonal fruit"
                ),
                lunch = listOf(
                    "Rajma",
                    "Bhindi Fry",
                    "Saag Sabji",
                    "Rice",
                    "Chapati",
                    "Salad",
                    "Pickle"
                ),
                snacks = listOf(
                    "Samosa (1 Pc.)",
                    "White Matar",
                    "Sauce/Chutney",
                    "Tea/Sharbat"
                ),
                dinner = listOf(
                    "Chhole/Kadhi",
                    "Paneer",
                    "Aloo ...",
                    "Rice",
                    "Papad",
                    "Green ...",
                    "Kheer/Sevai"
                )
            )
        ),
        lastUpdated = System.currentTimeMillis(),
        note = "Official MMMUT Mess Menu"
    )

    fun getDay(dayName: String): MessDay {
        return DEFAULT_WEEKLY_MENU.days.firstOrNull { it.day.equals(dayName, ignoreCase = true) }
            ?: DEFAULT_WEEKLY_MENU.days.first()
    }

    fun getTodayDayName(): String {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            Calendar.SUNDAY -> "Sunday"
            else -> "Monday"
        }
    }
}
