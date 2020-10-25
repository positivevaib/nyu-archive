lines = LOAD 'hdfs:/user/vag273/class1/hw4/input.txt' AS (line:chararray);

hackathon_map = FOREACH lines GENERATE ((LOWER(line) matches '.*hackathon.*' ? 1: 0)) AS presence;
hackathon_reduce = FOREACH (GROUP hackathon_map ALL) GENERATE SUM(hackathon_map);
hackathon_out = FOREACH hackathon_reduce GENERATE CONCAT ('Hackathon ', (chararray)hackathon_reduce.$0);

dec_map = FOREACH lines GENERATE ((LOWER(line) matches '.*dec.*' ? 1: 0)) AS presence;
dec_reduce = FOREACH (GROUP dec_map ALL) GENERATE SUM(dec_map);
dec_out = FOREACH dec_reduce GENERATE CONCAT ('Dec ', (chararray)dec_reduce.$0);

chicago_map = FOREACH lines GENERATE ((LOWER(line) matches '.*chicago.*' ? 1: 0)) AS presence;
chicago_reduce = FOREACH (GROUP chicago_map ALL) GENERATE SUM(chicago_map);
chicago_out = FOREACH chicago_reduce GENERATE CONCAT ('Chicago ', (chararray)chicago_reduce.$0);

java_map = FOREACH lines GENERATE ((LOWER(line) matches '.*java.*' ? 1: 0)) AS presence;
java_reduce = FOREACH (GROUP java_map ALL) GENERATE SUM(java_map);
java_out = FOREACH java_reduce GENERATE CONCAT ('Java ', (chararray)java_reduce.$0);

final_out = UNION hackathon_out, dec_out, chicago_out, java_out;
DUMP final_out;
STORE final_out INTO 'hdfs:/user/vag273/class1/hw4/output';
