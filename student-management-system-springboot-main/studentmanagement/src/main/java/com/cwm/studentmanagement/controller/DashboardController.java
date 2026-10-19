package com.cwm.studentmanagement.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.cwm.studentmanagement.service.DashboardService;
import com.cwm.studentmanagement.service.EnrollmentService;

@Controller
public class DashboardController {

	// FIXED: Standardized correct logger target mapping reference to DashboardController
	private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

	private final EnrollmentService enrollmentService;
	private final DashboardService dashboardService;

	public DashboardController(EnrollmentService enrollmentService, DashboardService dashboardService) {
		this.enrollmentService = enrollmentService;
		this.dashboardService = dashboardService;
	}

	@GetMapping("/dashboard")
	public String dashboard(Model model) {
		log.info("Processing GET /dashboard - loading landing metrics view overview.");

		Object stats = dashboardService.getDashboardStats();
		// Fallback check to prevent front-end Whitelabel breaks if statistics return null state
		model.addAttribute("dashboardStats", stats != null ? stats : new Object());

		java.util.List<?> recentStudents = enrollmentService.getRecentlyEnrolledStudents();
		model.addAttribute("students", recentStudents != null ? recentStudents : new java.util.ArrayList<>());

		return "dashboard";
	}
}
