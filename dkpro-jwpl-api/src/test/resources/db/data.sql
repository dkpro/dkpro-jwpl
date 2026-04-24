-- Shared test fixture data for dkpro-jwpl-api.
-- Portable across HSQLDB, MariaDB, and MySQL:
--   * real UTF-8 (no \uXXXX escapes),
--   * real newlines inside string literals (no \n escape sequences),
--   * standard single-quote escaping via doubling (e.g. 'Moore''s_law').

INSERT INTO Category VALUES (1, 200, 'Disambiguation');
INSERT INTO Category VALUES (2, 15, 'Student_research_assistants_of_UKP');
INSERT INTO Category VALUES (3, 4, 'Publications_of_Telecooperation');
INSERT INTO Category VALUES (4, 8, 'People_of_UKP');
INSERT INTO Category VALUES (5, 11, 'AQUA');
INSERT INTO Category VALUES (6, 3, 'Research_at_Telecooperation');
INSERT INTO Category VALUES (7, 7, 'Projects_of_UKP');
INSERT INTO Category VALUES (8, 12, 'Group_leader_of_UKP');
INSERT INTO Category VALUES (9, 2, 'Teaching_at_Telecooperation');
INSERT INTO Category VALUES (10, 13, 'Research_Staff_of_UKP');
INSERT INTO Category VALUES (11, 9, 'Publications_of_UKP');
INSERT INTO Category VALUES (12, 6, 'UKP');
INSERT INTO Category VALUES (13, 1, 'Telecooperation');
INSERT INTO Category VALUES (14, 14, 'Diploma_Students_of_UKP');
INSERT INTO Category VALUES (15, 10, 'SIR');
INSERT INTO Category VALUES (16, 5, 'People_of_Telecooperation');
INSERT INTO Category VALUES (17, 30, 'Unconnected_category');

INSERT INTO MetaData VALUES (1, 'test', 'Disambiguation', 'Telecooperation', 36, 6, 2, 17, '1.0');

INSERT INTO Page VALUES (1, 1014, 'Wikipedia_API', 'Wikipedia API ist die wichtigste [[Software]] überhaupt.
[[JWPL|Wikipedia API]].

*Nicht zu übertreffen.

*Unglaublich

*[[http://www.ukp.tu-darmstadt.de]]

[[en:Wikipedia API]] [[fi:WikipediaAPI]]', FALSE);
INSERT INTO Page VALUES (2, 1019, 'Christoph_Mueller', 'Christoph_Mueller', FALSE);
INSERT INTO Page VALUES (3, 107, 'TK3', 'TK3', FALSE);
INSERT INTO Page VALUES (4, 1022, 'Torsten_Zesch', 'Torsten_Zesch', FALSE);
INSERT INTO Page VALUES (5, 1011, 'Semantic_Information_Retrieval', '{{Disambiguation}}_Semantic_Information_Retrieval', FALSE);
INSERT INTO Page VALUES (6, 105, 'TK2', 'TK2', FALSE);
INSERT INTO Page VALUES (7, 1016, 'Analyzing_and_Accessing_Wikipedia_as_a_Lexical_Semantic_Resource', 'Analyzing_and_Accessing_Wikipedia_as_a_Lexical_Semantic_Resource', FALSE);
INSERT INTO Page VALUES (8, 1017, 'Exploring_the_Potential_of_Semantic_Relatedness_in_Information_Retrieval', 'Exploring_the_Potential_of_Semantic_Relatedness_in_Information_Retrieval', FALSE);
INSERT INTO Page VALUES (9, 1027, 'Hossein_Rabighomi', 'Hossein_Rabighomi', FALSE);
INSERT INTO Page VALUES (10, 1024, 'Niklas_Jakob', 'Niklas_Jakob', FALSE);
INSERT INTO Page VALUES (11, 1030, 'Dimitri_Belski', 'Dimitri_Belski', FALSE);
INSERT INTO Page VALUES (12, 1010, 'Research_Focus_of_UKP', 'Research_Focus_of_UKP', FALSE);
INSERT INTO Page VALUES (13, 1023, 'Christian_Jacobi', 'Christian_Jacobi', FALSE);
INSERT INTO Page VALUES (14, 1025, 'Cigdem_Toprak', 'Cigdem_Toprak', FALSE);
INSERT INTO Page VALUES (15, 1018, 'Iryna_Gurevych', 'Iryna_Gurevych', FALSE);
INSERT INTO Page VALUES (16, 1015, 'Natural_Language_Processing_for_Ambient_Intelligence', 'Natural_Language_Processing_for_Ambient_Intelligence', FALSE);
INSERT INTO Page VALUES (17, 103, 'TK1', 'TK1', FALSE);
INSERT INTO Page VALUES (18, 108, 'Peer-to-Peer_and_Grid_Computing', 'Peer-to-Peer_and_Grid_Computing.', FALSE);
INSERT INTO Page VALUES (19, 1029, 'Atif_Azad', 'Atif_Azad', FALSE);
INSERT INTO Page VALUES (20, 1043, 'Demo_of_Wikipedia_API', 'Demo_of_Wikipedia_API', FALSE);
INSERT INTO Page VALUES (21, 1026, 'Lars_Lipecki', 'Lars_Lipecki', FALSE);
INSERT INTO Page VALUES (22, 1028, 'Anouar_Haha', 'Anouar_Haha', FALSE);
INSERT INTO Page VALUES (23, 1021, 'Markus_Weimer', 'Markus_Weimer', FALSE);
INSERT INTO Page VALUES (24, 1020, 'Juergen_Steimle', 'Juergen_Steimle', FALSE);
INSERT INTO Page VALUES (25, 1013, 'AQUA', 'AQUA', FALSE);
INSERT INTO Page VALUES (26, 1041, 'UKP', 'UKP', FALSE);
INSERT INTO Page VALUES (27, 101, 'Net_Centric_Systems', 'Net_Centric_Systems.', FALSE);
INSERT INTO Page VALUES (28, 1031, 'Max_Muehlhaeuser', 'Max_Muehlhaeuser', FALSE);
INSERT INTO Page VALUES (29, 1032, 'Semantic_information_retrieval_(computer_science)', 'This is semantic information retrieval in the sense of computer science.', FALSE);
INSERT INTO Page VALUES (30, 2000, 'Unconnected_page', 'This page is unconnected from all other pages', FALSE);
INSERT INTO Page VALUES (31, 4000, 'Discussion:Wikipedia_API', 'This page is a discussion page for Wikipedia_API.', FALSE);
INSERT INTO Page VALUES (32, 6000, 'Humanbiologie', 'Die Attraktivität der Fachrichtungen Humanbiologie beziehungsweise Biomedizin als Studienfächer ist in jüngerer Zeit deutlich gestiegen.
 {| cellspacing="5"
 !align="left"|Studiengang
!align="left"|besteht seit
!align="left"|Abschluss
!align="left"|Hochschule
|-
|Humanbiologie (Biomedical Science)
|1979
|[[Bachelor#Der Bachelor-Abschluss in Europa Bachelor]] / Master
|[[Philipps-Universitæt Marburg|Marburg (U)]]
|-
|Molekulare Biomedizin
|2014
|Bachelor
|[[Rheinische Fachhochschule Köln]]
|}
[[Kategorie:Biologie]] [[Kategorie:Medizin]] [[Kategorie:Humangenetik]] [[Kategorie:Studienfach]]', FALSE);
INSERT INTO Page VALUES (33, 6001, 'Liste_von_Materia_Medica_der_traditionellen_uigurischen_Medizin', 'Dies ist eine Liste von [[Materia Medica]] der traditionellen [[Uiguren|uigurischen]] [[Medizin]]. Die uigurische Medizin entwickelte sich aus der [[Arabische Medizin|arabischen Medizin]], der [[Medizin_des_Altertums#Medizin im Antiken Griechenland|antiken griechischen Medizin]] und der [[Traditionelle chinesische Medizin|traditionellen chinesischen Medizin]].<ref>[http://www.cintcm.com/e_cintcm/e_cmm/Uigur%20drugs.htm cintcm.com: The traditional Uigur drugs] – gefunden am 13. Juli 2010</ref> Übersicht <small><center>Quellen: [http://www.cintcm.com/e_cintcm/e_cmm/Uigur%20drugs.htm cintcm.com], [http://www.tcm-resources.com/xiangguan/zhongdianziyuan/minzuyaozhonglei.doc tcm-resources.com]</center></small>', FALSE);
INSERT INTO Page VALUES (34, 6002, 'Moore''s_law', 'Moore''s_law', FALSE);

INSERT INTO PageMapLine VALUES (1, 'Net_Centric_Systems', 101, NULL, NULL);
INSERT INTO PageMapLine VALUES (2, 'TK1', 103, NULL, NULL);
INSERT INTO PageMapLine VALUES (3, 'TK2', 105, NULL, NULL);
INSERT INTO PageMapLine VALUES (4, 'TK3', 107, NULL, NULL);
INSERT INTO PageMapLine VALUES (5, 'Peer-to-Peer_and_Grid_Computing', 108, NULL, NULL);
INSERT INTO PageMapLine VALUES (6, 'Research_Focus_of_UKP', 1010, NULL, NULL);
INSERT INTO PageMapLine VALUES (7, 'Semantic_Information_Retrieval', 1011, NULL, NULL);
INSERT INTO PageMapLine VALUES (8, 'AQUA', 1013, NULL, NULL);
INSERT INTO PageMapLine VALUES (9, 'Wikipedia_API', 1014, NULL, NULL);
INSERT INTO PageMapLine VALUES (10, 'Natural_Language_Processing_for_Ambient_Intelligence', 1015, NULL, NULL);
INSERT INTO PageMapLine VALUES (11, 'Analyzing_and_Accessing_Wikipedia_as_a_Lexical_Semantic_Resource', 1016, NULL, NULL);
INSERT INTO PageMapLine VALUES (12, 'Exploring_the_Potential_of_Semantic_Relatedness_in_Information_Retrieval', 1017, NULL, NULL);
INSERT INTO PageMapLine VALUES (13, 'Iryna_Gurevych', 1018, NULL, NULL);
INSERT INTO PageMapLine VALUES (14, 'Christoph_Mueller', 1019, NULL, NULL);
INSERT INTO PageMapLine VALUES (15, 'Juergen_Steimle', 1020, NULL, NULL);
INSERT INTO PageMapLine VALUES (16, 'Markus_Weimer', 1021, NULL, NULL);
INSERT INTO PageMapLine VALUES (17, 'Torsten_Zesch', 1022, NULL, NULL);
INSERT INTO PageMapLine VALUES (18, 'Christian_Jacobi', 1023, NULL, NULL);
INSERT INTO PageMapLine VALUES (19, 'Niklas_Jakob', 1024, NULL, NULL);
INSERT INTO PageMapLine VALUES (20, 'Cigdem_Toprak', 1025, NULL, NULL);
INSERT INTO PageMapLine VALUES (21, 'Lars_Lipecki', 1026, NULL, NULL);
INSERT INTO PageMapLine VALUES (22, 'Hossein_Rabighomi', 1027, NULL, NULL);
INSERT INTO PageMapLine VALUES (23, 'Anouar_Haha', 1028, NULL, NULL);
INSERT INTO PageMapLine VALUES (24, 'Atif_Azad', 1029, NULL, NULL);
INSERT INTO PageMapLine VALUES (25, 'Dimitri_Belski', 1030, NULL, NULL);
INSERT INTO PageMapLine VALUES (26, 'Max_Muehlhaeuser', 1031, NULL, NULL);
INSERT INTO PageMapLine VALUES (27, 'UKP', 1041, NULL, NULL);
INSERT INTO PageMapLine VALUES (28, 'Demo_of_Wikipedia_API', 1043, NULL, NULL);
INSERT INTO PageMapLine VALUES (29, 'NCS', 101, NULL, NULL);
INSERT INTO PageMapLine VALUES (30, 'Rechnernetze,_Verteilte_Systeme_und_Algorithmen', 103, NULL, NULL);
INSERT INTO PageMapLine VALUES (31, 'Web_Engineering,_Web_Cooperation_and_eLearning', 105, NULL, NULL);
INSERT INTO PageMapLine VALUES (32, 'P2P', 108, NULL, NULL);
INSERT INTO PageMapLine VALUES (33, 'SIR', 1011, NULL, NULL);
INSERT INTO PageMapLine VALUES (34, 'Ubiquitous_Knowledge_Processing', 1041, NULL, NULL);
INSERT INTO PageMapLine VALUES (35, 'Semantic_information_retrieval_(computer_science)', 1032, NULL, NULL);
INSERT INTO PageMapLine VALUES (36, 'Unconnected_page', 2000, NULL, NULL);
INSERT INTO PageMapLine VALUES (37, 'Discussion:Wikipedia_API', 4000, NULL, NULL);
INSERT INTO PageMapLine VALUES (38, 'Humanbiologie', 6000, NULL, NULL);
INSERT INTO PageMapLine VALUES (39, 'Liste_von_Materia_Medica_der_traditionellen_uigurischen_Medizin', 6001, NULL, NULL);
INSERT INTO PageMapLine VALUES (40, 'Moore''s_law', 6002, NULL, NULL);

INSERT INTO category_inlinks VALUES (2, 8);
INSERT INTO category_inlinks VALUES (3, 1);
INSERT INTO category_inlinks VALUES (4, 6);
INSERT INTO category_inlinks VALUES (4, 5);
INSERT INTO category_inlinks VALUES (5, 7);
INSERT INTO category_inlinks VALUES (6, 1);
INSERT INTO category_inlinks VALUES (7, 6);
INSERT INTO category_inlinks VALUES (8, 8);
INSERT INTO category_inlinks VALUES (9, 1);
INSERT INTO category_inlinks VALUES (10, 8);
INSERT INTO category_inlinks VALUES (11, 4);
INSERT INTO category_inlinks VALUES (11, 6);
INSERT INTO category_inlinks VALUES (12, 3);
INSERT INTO category_inlinks VALUES (14, 8);
INSERT INTO category_inlinks VALUES (15, 7);
INSERT INTO category_inlinks VALUES (16, 1);
INSERT INTO category_inlinks VALUES (1, 1);

INSERT INTO category_outlinks VALUES (3, 9);
INSERT INTO category_outlinks VALUES (4, 15);
INSERT INTO category_outlinks VALUES (4, 13);
INSERT INTO category_outlinks VALUES (4, 14);
INSERT INTO category_outlinks VALUES (4, 12);
INSERT INTO category_outlinks VALUES (6, 6);
INSERT INTO category_outlinks VALUES (7, 11);
INSERT INTO category_outlinks VALUES (7, 10);
INSERT INTO category_outlinks VALUES (12, 9);
INSERT INTO category_outlinks VALUES (12, 8);
INSERT INTO category_outlinks VALUES (12, 7);
INSERT INTO category_outlinks VALUES (13, 2);
INSERT INTO category_outlinks VALUES (13, 4);
INSERT INTO category_outlinks VALUES (13, 3);
INSERT INTO category_outlinks VALUES (13, 5);
INSERT INTO category_outlinks VALUES (16, 8);
INSERT INTO category_outlinks VALUES (13, 200);

INSERT INTO category_pages VALUES (1, 1014);
INSERT INTO category_pages VALUES (1, 1011);
INSERT INTO category_pages VALUES (2, 1028);
INSERT INTO category_pages VALUES (2, 1030);
INSERT INTO category_pages VALUES (2, 1027);
INSERT INTO category_pages VALUES (2, 1029);
INSERT INTO category_pages VALUES (5, 1021);
INSERT INTO category_pages VALUES (5, 1020);
INSERT INTO category_pages VALUES (5, 1013);
INSERT INTO category_pages VALUES (8, 1018);
INSERT INTO category_pages VALUES (9, 105);
INSERT INTO category_pages VALUES (9, 107);
INSERT INTO category_pages VALUES (9, 108);
INSERT INTO category_pages VALUES (9, 103);
INSERT INTO category_pages VALUES (9, 1031);
INSERT INTO category_pages VALUES (9, 101);
INSERT INTO category_pages VALUES (10, 1019);
INSERT INTO category_pages VALUES (10, 1021);
INSERT INTO category_pages VALUES (10, 1020);
INSERT INTO category_pages VALUES (10, 1022);
INSERT INTO category_pages VALUES (11, 1016);
INSERT INTO category_pages VALUES (11, 1017);
INSERT INTO category_pages VALUES (11, 1015);
INSERT INTO category_pages VALUES (12, 1010);
INSERT INTO category_pages VALUES (12, 1041);
INSERT INTO category_pages VALUES (14, 1026);
INSERT INTO category_pages VALUES (14, 1023);
INSERT INTO category_pages VALUES (14, 1025);
INSERT INTO category_pages VALUES (14, 1024);
INSERT INTO category_pages VALUES (15, 1014);
INSERT INTO category_pages VALUES (15, 1019);
INSERT INTO category_pages VALUES (15, 1022);
INSERT INTO category_pages VALUES (15, 1043);
INSERT INTO category_pages VALUES (15, 1011);
INSERT INTO category_pages VALUES (16, 1031);

INSERT INTO page_categories VALUES (1, 200);
INSERT INTO page_categories VALUES (1, 10);
INSERT INTO page_categories VALUES (2, 13);
INSERT INTO page_categories VALUES (2, 10);
INSERT INTO page_categories VALUES (3, 2);
INSERT INTO page_categories VALUES (4, 13);
INSERT INTO page_categories VALUES (4, 10);
INSERT INTO page_categories VALUES (5, 200);
INSERT INTO page_categories VALUES (5, 10);
INSERT INTO page_categories VALUES (6, 2);
INSERT INTO page_categories VALUES (7, 9);
INSERT INTO page_categories VALUES (8, 9);
INSERT INTO page_categories VALUES (9, 15);
INSERT INTO page_categories VALUES (10, 14);
INSERT INTO page_categories VALUES (11, 15);
INSERT INTO page_categories VALUES (12, 6);
INSERT INTO page_categories VALUES (13, 14);
INSERT INTO page_categories VALUES (14, 14);
INSERT INTO page_categories VALUES (15, 12);
INSERT INTO page_categories VALUES (16, 9);
INSERT INTO page_categories VALUES (17, 2);
INSERT INTO page_categories VALUES (18, 2);
INSERT INTO page_categories VALUES (19, 15);
INSERT INTO page_categories VALUES (20, 10);
INSERT INTO page_categories VALUES (21, 14);
INSERT INTO page_categories VALUES (22, 15);
INSERT INTO page_categories VALUES (23, 13);
INSERT INTO page_categories VALUES (23, 11);
INSERT INTO page_categories VALUES (24, 13);
INSERT INTO page_categories VALUES (24, 11);
INSERT INTO page_categories VALUES (25, 11);
INSERT INTO page_categories VALUES (26, 6);
INSERT INTO page_categories VALUES (27, 2);
INSERT INTO page_categories VALUES (28, 2);
INSERT INTO page_categories VALUES (28, 5);

INSERT INTO page_inlinks VALUES (1, 1028);
INSERT INTO page_inlinks VALUES (1, 1022);
INSERT INTO page_inlinks VALUES (1, 1043);
INSERT INTO page_inlinks VALUES (2, 1017);
INSERT INTO page_inlinks VALUES (4, 1014);
INSERT INTO page_inlinks VALUES (4, 1016);
INSERT INTO page_inlinks VALUES (4, 1043);
INSERT INTO page_inlinks VALUES (5, 1019);
INSERT INTO page_inlinks VALUES (5, 1042);
INSERT INTO page_inlinks VALUES (5, 1010);
INSERT INTO page_inlinks VALUES (5, 1022);
INSERT INTO page_inlinks VALUES (5, 1041);
INSERT INTO page_inlinks VALUES (6, 1028);
INSERT INTO page_inlinks VALUES (7, 1018);
INSERT INTO page_inlinks VALUES (7, 1022);
INSERT INTO page_inlinks VALUES (7, 1031);
INSERT INTO page_inlinks VALUES (8, 1019);
INSERT INTO page_inlinks VALUES (8, 1018);
INSERT INTO page_inlinks VALUES (8, 1031);
INSERT INTO page_inlinks VALUES (12, 1041);
INSERT INTO page_inlinks VALUES (15, 1016);
INSERT INTO page_inlinks VALUES (15, 1017);
INSERT INTO page_inlinks VALUES (15, 1015);
INSERT INTO page_inlinks VALUES (16, 1018);
INSERT INTO page_inlinks VALUES (16, 1031);
INSERT INTO page_inlinks VALUES (17, 1028);
INSERT INTO page_inlinks VALUES (17, 1031);
INSERT INTO page_inlinks VALUES (18, 1028);
INSERT INTO page_inlinks VALUES (20, 1028);
INSERT INTO page_inlinks VALUES (20, 1022);
INSERT INTO page_inlinks VALUES (22, 1043);
INSERT INTO page_inlinks VALUES (25, 1042);
INSERT INTO page_inlinks VALUES (25, 1021);
INSERT INTO page_inlinks VALUES (25, 1010);
INSERT INTO page_inlinks VALUES (25, 1020);
INSERT INTO page_inlinks VALUES (25, 1041);
INSERT INTO page_inlinks VALUES (26, 1010);
INSERT INTO page_inlinks VALUES (28, 1016);
INSERT INTO page_inlinks VALUES (28, 104);
INSERT INTO page_inlinks VALUES (28, 1015);
INSERT INTO page_inlinks VALUES (28, 103);

INSERT INTO page_outlinks VALUES (1, 1022);
INSERT INTO page_outlinks VALUES (2, 1017);
INSERT INTO page_outlinks VALUES (2, 1012);
INSERT INTO page_outlinks VALUES (2, 1011);
INSERT INTO page_outlinks VALUES (4, 1014);
INSERT INTO page_outlinks VALUES (4, 1016);
INSERT INTO page_outlinks VALUES (4, 1011);
INSERT INTO page_outlinks VALUES (4, 1043);
INSERT INTO page_outlinks VALUES (5, 1012);
INSERT INTO page_outlinks VALUES (6, 106);
INSERT INTO page_outlinks VALUES (7, 1018);
INSERT INTO page_outlinks VALUES (7, 1022);
INSERT INTO page_outlinks VALUES (7, 1031);
INSERT INTO page_outlinks VALUES (8, 1019);
INSERT INTO page_outlinks VALUES (8, 1018);
INSERT INTO page_outlinks VALUES (12, 1012);
INSERT INTO page_outlinks VALUES (12, 1042);
INSERT INTO page_outlinks VALUES (12, 1013);
INSERT INTO page_outlinks VALUES (12, 1011);
INSERT INTO page_outlinks VALUES (12, 1041);
INSERT INTO page_outlinks VALUES (15, 1016);
INSERT INTO page_outlinks VALUES (15, 1017);
INSERT INTO page_outlinks VALUES (15, 1015);
INSERT INTO page_outlinks VALUES (16, 1018);
INSERT INTO page_outlinks VALUES (16, 1031);
INSERT INTO page_outlinks VALUES (17, 104);
INSERT INTO page_outlinks VALUES (17, 1031);
INSERT INTO page_outlinks VALUES (18, 109);
INSERT INTO page_outlinks VALUES (20, 1014);
INSERT INTO page_outlinks VALUES (20, 1028);
INSERT INTO page_outlinks VALUES (20, 1022);
INSERT INTO page_outlinks VALUES (22, 1014);
INSERT INTO page_outlinks VALUES (22, 105);
INSERT INTO page_outlinks VALUES (22, 109);
INSERT INTO page_outlinks VALUES (22, 104);
INSERT INTO page_outlinks VALUES (22, 106);
INSERT INTO page_outlinks VALUES (22, 103);
INSERT INTO page_outlinks VALUES (22, 108);
INSERT INTO page_outlinks VALUES (22, 1043);
INSERT INTO page_outlinks VALUES (23, 1013);
INSERT INTO page_outlinks VALUES (24, 1013);
INSERT INTO page_outlinks VALUES (26, 1012);
INSERT INTO page_outlinks VALUES (26, 1010);
INSERT INTO page_outlinks VALUES (26, 1013);
INSERT INTO page_outlinks VALUES (26, 1011);
INSERT INTO page_outlinks VALUES (27, 102);
INSERT INTO page_outlinks VALUES (28, 1016);
INSERT INTO page_outlinks VALUES (28, 1017);
INSERT INTO page_outlinks VALUES (28, 104);
INSERT INTO page_outlinks VALUES (28, 1015);
INSERT INTO page_outlinks VALUES (28, 103);

INSERT INTO page_redirects VALUES (27, 'NCS');
INSERT INTO page_redirects VALUES (17, 'Rechnernetze,_Verteilte_Systeme_und_Algorithmen');
INSERT INTO page_redirects VALUES (6, 'Web_Engineering,_Web_Cooperation_and_eLearning');
INSERT INTO page_redirects VALUES (18, 'P2P');
INSERT INTO page_redirects VALUES (5, 'SIR');
INSERT INTO page_redirects VALUES (26, 'Ubiquitous_Knowledge_Processing');
