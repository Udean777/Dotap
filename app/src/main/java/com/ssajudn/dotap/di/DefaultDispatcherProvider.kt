package com.ssajudn.dotap.di

import com.ssajudn.dotap.domain.port.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class DefaultDispatcherProvider(
    override val main: CoroutineDispatcher = Dispatchers.Main,
    override val io: CoroutineDispatcher = Dispatchers.IO,
    override val default: CoroutineDispatcher = Dispatchers.Default,
) : DispatcherProvider
