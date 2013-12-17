delimiter ;
CREATE TABLE `seq` (
  `name` varchar(20) NOT NULL,
  `val` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
insert into seq values('session',1);
CREATE TABLE `poll` (
  `sessionid` varchar(10) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `question` tinyint(4) NOT NULL,
  `answer` varchar(50) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  KEY `Poll_sessionid_idx` (`sessionid`),
  KEY `Poll_question_idx` (`question`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
DELIMITER ;;
CREATE PROCEDURE `pseq`(IN seq_name char (20), OUT nextid INTEGER)
begin
 update seq set val=last_insert_id(val+1) where name=seq_name;
 set nextid = last_insert_id();
end;;
DELIMITER ;