package com.scrolltax.intervention

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterventionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
}
