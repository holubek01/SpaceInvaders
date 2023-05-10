package com.example.spaceinvaders

import dagger.Component

@Component(modules = [DatabaseModule::class])
interface AppComponent {
    fun inject(activity: MainActivity)
    fun inject(activity: ShowResultsActivity)
    fun inject(activity: StartActivity)
}
