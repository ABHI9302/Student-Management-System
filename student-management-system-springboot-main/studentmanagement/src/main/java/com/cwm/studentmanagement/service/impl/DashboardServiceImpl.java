package com.cwm.studentmanagement.service.impl;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cwm.studentmanagement.dto.DashboardStatsDTO;
import com.cwm.studentmanagement.repository.CourseRepository;
import com.cwm.studentmanagement.repository.EnrollmentRepository;
import com.cwm.studentmanagement.repository.StudentRepository;
import com.cwm.studentmanagement.service.DashboardService;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

	private static final Logger log = LoggerFactory.getLogger(DashboardServiceImpl.class);

	private final EnrollmentRepository enrollmentRepository;
	private final StudentRepository studentRepository;
	private final CourseRepository courseRepository;

	public DashboardServiceImpl(EnrollmentRepository enrollmentRepository,
								StudentRepository studentRepository,
								CourseRepository courseRepository)
	{
		this.enrollmentRepository = enrollmentRepository;
		this.studentRepository = studentRepository;
		this.courseRepository = courseRepository;
	}

	@Override
	public DashboardStatsDTO getDashboardStats() {
		log.info("Compiling central dashboard analytics statistics.");

		long totalStudents = studentRepository.count();
		long totalCourse = courseRepository.count();
		String topPerformingCourse = getTopPerformingCourse();

		YearMonth currentMonth = YearMonth.now();
		LocalDateTime startDate = currentMonth.atDay(1).atStartOfDay();
		LocalDateTime endDate = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);

		long studentEnrolledThisMonth = enrollmentRepository.countDistinctStudentByEnrollDateBetween(startDate, endDate);

		DashboardStatsDTO dashboardStatsDTO = new DashboardStatsDTO();
		dashboardStatsDTO.setTotalStudents(totalStudents);
		dashboardStatsDTO.setTotalCourses(totalCourse);
		dashboardStatsDTO.setTopPerformingCourse(topPerformingCourse);
		dashboardStatsDTO.setStudentsEnrolledThisMonth(studentEnrolledThisMonth);

		return dashboardStatsDTO;
	}

	private String getTopPerformingCourse() {
		log.info("Querying database to resolve the highest registered course title.");

		try {
			String topCourse = enrollmentRepository.findTopPerformingCourseName();
			return (topCourse != null && !topCourse.trim().isEmpty()) ? topCourse : "N/A";
		} catch (Exception ex) { // FIXED: Changed variable name from 'e' to 'ex' here to avoid duplicate definition crash
			log.error("Aggregate custom HQL query failed or not mapped, falling back to safe local collections processing.", ex);

			// FIXED: Now lambda variable names 'enroll' can execute independently without context collisions
			return enrollmentRepository.findAll().stream()
					.filter(enroll -> enroll.getCourse() != null)
					.collect(Collectors.groupingBy(enroll -> enroll.getCourse().getCourseName(), Collectors.counting()))
					.entrySet().stream()
					.max(Map.Entry.comparingByValue())
					.map(Map.Entry::getKey)
					.orElse("N/A");
		}
	}
}
