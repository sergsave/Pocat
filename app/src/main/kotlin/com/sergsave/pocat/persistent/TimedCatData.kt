package com.sergsave.pocat.persistent

import com.sergsave.pocat.models.CatData
import java.util.*

data class TimedCatData(val timestamp: Date, val data: CatData)
