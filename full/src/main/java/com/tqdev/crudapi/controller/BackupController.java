package com.tqdev.crudapi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.tqdev.crudapi.record.RecordService;

@RestController
@RequestMapping("/backup")
public class BackupController extends BaseController {

	public static final Logger logger = LoggerFactory.getLogger(BackupController.class);

	@Autowired
	RecordService service;

	@RequestMapping(value = "/dump", method = RequestMethod.GET)
	public ResponseEntity<?> dump() {
		logger.info("Dumping records for backup");
		return success(service.getDatabaseRecords());
	}
}