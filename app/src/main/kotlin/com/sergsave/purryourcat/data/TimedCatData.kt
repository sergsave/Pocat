package com.sergsave.purryourcat.persistent

import java.util.Date
import com.sergsave.purryourcat.models.Cat

data class TimedCat(val timestamp: Date, val cat: Cat)
