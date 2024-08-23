CREATE TABLE performance_reviews (
    reviewee uuid NOT NULL,
    reviewer uuid NOT NULL,
    comment varchar(2000),
    performance_score smallint,
    soft_skills_score smallint,
    independence_score smallint,
    aspiration_for_growth_score smallint,
    created_at date default now(),
    PRIMARY KEY(reviewee, created_at)
);
