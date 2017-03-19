package com.cbruegg.mensaupb

import com.cbruegg.mensaupb.cache.ModelCache
import com.cbruegg.mensaupb.dishes.DishesFragment
import com.cbruegg.mensaupb.downloader.Downloader
import com.cbruegg.mensaupb.main.MainActivity
import com.cbruegg.mensaupb.service.DishRemoteViewsService
import com.cbruegg.mensaupb.service.DishesWidgetUpdateService
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class, NetModule::class))
interface AppComponent {
    fun inject(downloader: Downloader)
    fun inject(app: MensaApplication)
    fun inject(mainActivity: MainActivity)
    fun inject(dishesFragment: DishesFragment)
    fun inject(modelCache: ModelCache)
    fun inject(dishesWidgetUpdateService: DishesWidgetUpdateService)
    fun inject(dishRemoteViewsFactory: DishRemoteViewsService.DishRemoteViewsFactory)
}