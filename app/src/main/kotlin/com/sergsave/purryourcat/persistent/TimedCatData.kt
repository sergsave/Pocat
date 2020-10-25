package com.sergsave.purryourcat.persistent

import com.sergsave.purryourcat.models.CatData
import java.util.*

data class TimedCatData(val timestamp: Date, val data: CatData)
