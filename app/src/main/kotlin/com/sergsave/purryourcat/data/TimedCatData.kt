package com.sergsave.purryourcat.data

import com.sergsave.purryourcat.models.CatData

data class TimedCatData(val timeOfCreateMillis: Long, val data: CatData)
