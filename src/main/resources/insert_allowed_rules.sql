INSERT INTO individual_allowed_roles (allowed_roles, individual_id)
VALUES ('user', 'EU323');
INSERT INTO biosample_allowed_roles(allowed_roles, biosample_id)
VALUES ('user', 'MB20188');
INSERT INTO run_allowed_roles(allowed_roles, run_id)
VALUES ('user', 'EU323');
INSERT INTO analysis_allowed_roles(allowed_roles, analysis_id)
VALUES ('user', 'EU323');
INSERT INTO case_level_data_allowed_roles(case_level_data_id, allowed_roles)
VALUES (5, 'user');
INSERT INTO case_level_data_allowed_roles_for_aggregation(case_level_data_id, allowed_roles_for_aggregation)
VALUES (5, 'user');


INSERT INTO individual_allowed_roles (allowed_roles, individual_id)
VALUES ('user', 'EU181');
INSERT INTO biosample_allowed_roles(allowed_roles, biosample_id)
VALUES ('user', 'MB21573');
INSERT INTO run_allowed_roles(allowed_roles, run_id)
VALUES ('user', 'EU181');
INSERT INTO analysis_allowed_roles(allowed_roles, analysis_id)
VALUES ('user', 'EU181');
INSERT INTO case_level_data_allowed_roles_for_aggregation(case_level_data_id, allowed_roles_for_aggregation)
VALUES (1, 'user');