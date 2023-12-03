package com.example.bletutorial.di
import com.example.bletutorial.model.repository.WIFIRepository
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.example.bletutorial.model.repository.BLERepository
import com.example.bletutorial.model.repository.JoystickRepository
import com.example.bletutorial.model.service.BLEService
import com.example.bletutorial.model.service.JoystickService
import com.example.bletutorial.model.service.WIFIService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBluetoothAdapter(@ApplicationContext context: Context):BluetoothAdapter{
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return manager.adapter
    }

    @Singleton
    @Provides
    fun provideBLEService(
        bluetoothAdapter: BluetoothAdapter,
        @ApplicationContext context: Context
    ): BLEService {
        return BLERepository(bluetoothAdapter, context)
    }

    @Singleton
    @Provides
    fun provideJoystickService(
        @ApplicationContext context: Context
    ): JoystickService {
        return JoystickRepository(context)
    }

    @Singleton
    @Provides
    fun provideWIFIService(
        @ApplicationContext context: Context
    ): WIFIService {
        return WIFIRepository(context)
    }

}