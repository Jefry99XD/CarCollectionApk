// presentation/navigation/NavRoutes.kt
package com.example.carcollection.presentation.navigation

object NavRoutes {
    const val MENU = "menu"
    const val COLLECTION = "collection_view" // Colección
    const val DATA = "data"
    const val ADD_EDIT_CAR = "add_edit_car"
    const val ADD_EDIT_CAR_WITH_ID = "add_edit_car?carId={carId}"
    const val ADD_EDIT_TAG = "add_edit_tag"
    const val VIEW_TAGS = "view_tags"
    const val CONSULTAS = "consultas_menu"
    const val VIEW_STH = "view_sth"
    const val VIEW_TH = "view_th"
    const val STATISTICS = "statistics"
    const val CONFIG = "config"
    const val ABOUT = "about"
    const val LIBRARY = "library"
    const val CAR_MODEL_LIBRARY = "car_model_library"
    const val WISHLIST = "wishlist"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val EDIT_TAG = "edit_tag/{tagId}"
    const val DETAIL = "car_detail"
    const val ACHIEVEMENTS = "achievements"
    const val ADD_ACHIEVEMENT = "add_achievement"
    const val USER_LIST = "user_list"
    const val STATS_MAIN = "stats_main"
    const val EASTER_EGG = "easter_egg_secret"

    fun editTag(tagId: String) = "edit_tag/$tagId"

    const val PUBLIC_PROFILE = "public_profile/{uid}"
    fun publicProfile(uid: String) = "public_profile/$uid"

}


