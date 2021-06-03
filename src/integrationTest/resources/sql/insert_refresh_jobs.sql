DROP TABLE IF EXISTS refresh_jobs;

create table refresh_jobs(
	job_id bigint not null,
	role_category text not null,
	jurisdiction text not null,
	status text not null,
	comments text,
	user_ids _text NULL,
	log text,
	linked_job_id bigint,
	created timestamp,
	constraint refresh_jobs_pkey PRIMARY KEY (job_id)
);

INSERT INTO public.refresh_jobs
(job_id, role_category, jurisdiction, status, "comments", user_ids, log, linked_job_id, created)
VALUES(1, 'LEGAL_OPERATIONS', 'IA', 'NEW', 'drool rule for IA version 1.0.0', '{}', '', 0, timezone('utc', now()));

INSERT INTO public.refresh_jobs
(job_id, role_category, jurisdiction, status, "comments", user_ids, log, linked_job_id, created)
VALUES(2, 'LEGAL_OPERATIONS', 'CMC', 'COMPLETED', 'drool rule for IA version 1.0.0', '{}', '', 0, timezone('utc', now()));

INSERT INTO public.refresh_jobs
(job_id, role_category, jurisdiction, status, "comments", user_ids, log, linked_job_id, created)
VALUES(3, 'LEGAL_OPERATIONS', 'CMC', 'ABORTED', 'drool rule for IA version 1.0.0', '{778520f2-1f10-4270-b5d6-9404e274a3f2, 778520f2-1f10-4270-b5d6-9404e274a3f1}', '', 0, timezone('utc', now()));

INSERT INTO public.refresh_jobs
(job_id, role_category, jurisdiction, status, "comments", user_ids, log, linked_job_id, created)
VALUES(4, 'LEGAL_OPERATIONS', 'CMC', 'NEW', 'drool rule for IA version 1.0.0', '{}', '', 3, timezone('utc', now()));