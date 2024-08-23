package org.mycompany.hris.configuration.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import java.time.LocalDate

object PerformanceReviewsTable : Table("performance_reviews") {
    val reviewee = uuid("reviewee")
    val reviewer = uuid("reviewer")
    val comment = varchar("comment", 2000).nullable()
    val performanceScore = short("performance_score")
    val softSkillsScore = short("soft_skills_score")
    val independenceScore = short("independence_score")
    val aspirationForGrowthScore = short("aspiration_for_growth_score")
    val createdAt = date("created_at").default(LocalDate.now())
    override val primaryKey: PrimaryKey = PrimaryKey(reviewee, createdAt)
}
