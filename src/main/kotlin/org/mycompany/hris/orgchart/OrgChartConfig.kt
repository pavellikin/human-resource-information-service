package org.mycompany.hris.orgchart

import java.time.Duration

data class OrgChartConfig(
    val cache: CacheConfig,
)

data class CacheConfig(
    val maxSize: Long = 10_000,
    val expireTime: Duration = Duration.ofHours(1),
)
