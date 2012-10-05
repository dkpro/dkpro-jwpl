CREATE TABLE IF NOT EXISTS templates (templateId VARCHAR(15) NOT NULL ,templateName MEDIUMTEXT NOT NULL,PRIMARY KEY(templateId));
CREATE TABLE IF NOT EXISTS templateId_revisionId(templateId VARCHAR(15) NOT NULL,revisionId INTEGER UNSIGNED NOT NULL, UNIQUE(templateId, revisionId));
