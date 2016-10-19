package com.cbruegg.mensaupb

import com.cbruegg.mensaupb.activity.MainActivity
import com.cbruegg.mensaupb.downloader.Downloader
import com.cbruegg.mensaupb.fragment.DishesFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class, NetModule::class))
interface NetComponent {
    fun inject(downloader: Downloader)
    fun inject(app: MensaApplication)
    fun inject(mainActivity: MainActivity)
    fun inject(dishesFragment: DishesFragment)
}